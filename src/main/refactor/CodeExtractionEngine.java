package main.refactor;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import main.neo.core.Solution;
import main.neo.core.Solution.SimulationResult;
import main.neo.core.graphs.GraphBundle;
import main.neo.core.graphs.GraphService;
import main.neo.core.jdt.JavaMethodProcessor.MethodComplexityRecord;
import main.neo.core.refactoringcache.RefactoringCache;
import main.neo.core.refactoringcache.RefactoringCacheFiller;
import main.neo.core.solvers.RefactoringSolver;
import main.neo.core.solvers.SolverContext;
import main.neo.core.solvers.SolverFactory;
import main.neo.core.solvers.SolverType;

/**
 * Orchestrator that drives the {@code main.neo} refactoring pipeline (cache
 * generation, graph construction, solver execution and in-memory simulation)
 * and adapts the result to the plugin's {@link RefactorComparison} domain
 * model.
 * <p>
 * The engine never persists artefacts to disk: solver outputs, refactoring
 * caches and applied extractions are kept in memory and surfaced through the
 * UI.
 * </p>
 */
public final class CodeExtractionEngine {

	private static final Logger LOGGER = Logger.getLogger(CodeExtractionEngine.class.getName());

	private CodeExtractionEngine() {
		// utility class
	}

	/**
	 * Analyses a method and returns the best refactoring plan found, simulated in
	 * memory so the user can review it before applying it.
	 *
	 * @param cu             the compilation unit containing the method
	 * @param icuWorkingCopy working copy used for in-memory simulation; its file is
	 *                       <b>not</b> modified
	 * @param node           the method to refactor
	 * @param cc             pre-computed cognitive complexity of the method
	 * @param threshold      project-specific cognitive complexity threshold
	 * @return a singleton list with the {@link RefactorComparison}, or an empty
	 *         list if no improving refactoring exists
	 */
	public static List<RefactorComparison> analyseAndPlan(CompilationUnit cu, ICompilationUnit icuWorkingCopy,
			MethodDeclaration node, int cc, int threshold) throws CoreException {

		if (node == null || cu == null || cc <= threshold) {
			return Collections.emptyList();
		}

		// 1. Build the cache of feasible refactoring opportunities for this method.
		RefactoringCache cache = new RefactoringCache(cu, node);
		RefactoringCacheFiller.exhaustiveEnumerationAlgorithm(cache, node);

		// 2. Build the solver context (record + threshold).
		int lineNumber = cu.getLineNumber(node.getStartPosition());
		MethodComplexityRecord record = new MethodComplexityRecord(node.getName().getIdentifier(), lineNumber, cc, node);

		Solution solution;
		boolean usedILP;
		SolverContext ctx = new SolverContext(cu, record, SolverType.ILP.getKey(), threshold);
		GraphBundle graphs = GraphService.buildGraphs(cache, node);
		ctx.setPrecomputedGraphs(graphs);

		solution = runSolver(ctx, cache);
		usedILP = solution != null;
		
		if(solution == null) {
			// Fallback to enumerative search if CPLEX is unavailable or failed.
			ctx = new SolverContext(cu, record, SolverType.ES_LONG_SEQUENCE_FIRST.getKey(), threshold);
			solution = runFallback(ctx, cache);
			usedILP = false;
		}

		if (solution == null || solution.getSequenceList() == null || solution.getSequenceList().isEmpty()) {
			return Collections.emptyList();
		}

		// 3. Simulate the extractions on a working copy without touching the file.
		SimulationResult sim = solution.simulateExtractMethods(icuWorkingCopy);
		if (sim == null) {
			return Collections.emptyList();
		}

		RefactorComparison comparison = RefactorComparison.builder()
				.name(solution.getMethodName())
				.compilationUnitRefactored(sim.getCompilationUnit())
				.refactoredSource(sim.getSource())
				.reducedComplexity(solution.getReducedComplexity())
				.numberOfExtractions(solution.getSize())
				.stats(solution.getExtractionMetricsStats())
				.usedILP(usedILP)
				.build();

		return List.of(comparison);
	}

	/**
	 * Runs the configured ILP solver. Any failure (including a missing CPLEX
	 * native library) is logged and translated into a {@code null} result so the
	 * caller can fall back to the enumerative search.
	 */
	private static Solution runSolver(SolverContext ctx, RefactoringCache cache) {
		try {
			RefactoringSolver solver = SolverFactory.getSolver(SolverType.ILP);
			return solver.solve(ctx, cache);
		} catch (UnsatisfiedLinkError | Exception e) {
			LOGGER.log(Level.FINE, "ILP solver unavailable for " + ctx.record.methodName + "; using fallback", e);
			return null;
		}
	}

	/**
	 * Runs the long-sequence-first enumerative search as a deterministic fallback
	 * when the ILP solver is not available.
	 */
	private static Solution runFallback(SolverContext ctx, RefactoringCache cache) {
		try {
			RefactoringSolver solver = SolverFactory.getSolver(SolverType.ES_LONG_SEQUENCE_FIRST);
			return solver.solve(ctx, cache);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Enumerative-search fallback failed for " + ctx.record.methodName, e);
			return null;
		}
	}
}
