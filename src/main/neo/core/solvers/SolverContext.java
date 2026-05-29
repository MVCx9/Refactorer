package main.neo.core.solvers;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import main.neo.app.Constants;
import main.neo.core.graphs.GraphBundle;
import main.neo.core.jdt.JavaMethodProcessor.MethodComplexityRecord;
import main.neo.core.jdt.Utils;

/**
 * Encapsulates the environment and state required by a {@link RefactoringSolver}.
 * <p>
 * Holds references to the AST, the selected solver type and the cognitive
 * complexity threshold (per project). It implements {@link AutoCloseable} to
 * guarantee that heavy resources (like JGraphT graphs) are cleaned up after the
 * solver finishes.
 * </p>
 */
public class SolverContext implements AutoCloseable {

	// AST & metadata
	public final MethodDeclaration ast;
	public final CompilationUnit compilationUnit;
	public final MethodComplexityRecord record;

	/** Solver identifier (e.g. {@link SolverType#getKey()}). */
	public final String algorithm;

	/** Cognitive complexity threshold to enforce (per project). */
	public final int threshold;

	// Heavy resources (managed)
	private GraphBundle graphs;

	/**
	 * Creates a new solver context.
	 *
	 * @param unit       the compilation unit containing the method
	 * @param record     the complexity record identifying the method
	 * @param solverKey  identifier of the solver (or {@code null})
	 * @param threshold  cognitive complexity threshold to enforce
	 */
	public SolverContext(CompilationUnit unit, MethodComplexityRecord record, String solverKey, int threshold) {
		this.ast = record.methodDeclaration;
		this.compilationUnit = (unit != null) ? unit : Utils.getCompilationUnit(ast);
		this.record = record;
		this.algorithm = solverKey;
		this.threshold = threshold;
	}

	/**
	 * Convenience overload using {@link Constants#COGNITIVE_COMPLEXITY_THRESHOLD}.
	 */
	public SolverContext(CompilationUnit unit, MethodComplexityRecord record, String solverKey) {
		this(unit, record, solverKey, Constants.COGNITIVE_COMPLEXITY_THRESHOLD);
	}

	/** Stores pre-computed graphs to avoid rebuilding them inside the solver. */
	public void setPrecomputedGraphs(GraphBundle graphs) {
		this.graphs = graphs;
	}

	/** @return the pre-computed graphs, or {@code null} if not available. */
	public GraphBundle getGraphs() {
		return graphs;
	}

	@Override
	public void close() {
		if (graphs != null) {
			graphs.clear();
			graphs = null;
		}
	}
}