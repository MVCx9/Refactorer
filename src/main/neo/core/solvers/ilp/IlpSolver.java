package main.neo.core.solvers.ilp;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.graph.DefaultWeightedEdge;

import ilog.cplex.CpxException;
import ilog.cplex.IloCplex;
import main.neo.core.Solution;
import main.neo.core.graphs.ExtractionVertex;
import main.neo.core.graphs.GraphBundle;
import main.neo.core.graphs.GraphService;
import main.neo.core.jdt.Utils;
import main.neo.core.refactoringcache.RefactoringCache;
import main.neo.core.solvers.RefactoringSolver;
import main.neo.core.solvers.SolverContext;
import main.neo.core.solvers.config.IlpConfig;
import main.preferences.PluginPreferences;

/**
 * {@link RefactoringSolver} backed by Integer Linear Programming via CPLEX.
 * <p>
 * Translates the refactoring graphs into a mathematical model and asks CPLEX
 * for the optimal subset of refactorings honouring the project-specific
 * cognitive complexity threshold from {@link SolverContext#threshold}.
 * </p>
 */
public class IlpSolver implements RefactoringSolver {

	private static final Logger LOGGER = Logger.getLogger(IlpSolver.class.getName());
	private final IlpConfig config;

	public IlpSolver(IlpConfig config) {
		this.config = config;
	}

	@Override
	public Solution solve(SolverContext ctx, RefactoringCache cache) throws Exception {
		// Guard: skip immediately if CPLEX native library is not available.
		// This avoids triggering IloCplex's static initializer which would throw UnsatisfiedLinkError.
		if (!PluginPreferences.isCplexLoaded()) {
			throw new UnsatisfiedLinkError("CPLEX native library not loaded; ILP solver unavailable");
		}

		Solution bestSolution = null;
		GraphBundle graphs = ctx.getGraphs();
		boolean localBuild = false;

		if (graphs == null) {
			graphs = GraphService.buildGraphs(cache, ctx.ast);
			localBuild = true;
		}

		Model<ExtractionVertex, DefaultWeightedEdge> m = null;

		try {
			m = new Model<>(graphs.conflicts, graphs.noConflicts, graphs.full, ctx.threshold);

			// Silence CPLEX output entirely.
			m.cplex.setOut(null);
			m.cplex.setWarning(null);

			// Numerical-stability and timeout parameters.
			m.cplex.setParam(IloCplex.DoubleParam.EpInt, 1E-9);
			m.cplex.setParam(IloCplex.DoubleParam.EpGap, 1E-9);
			m.cplex.setParam(IloCplex.DoubleParam.EpOpt, 1E-9);
			m.cplex.setParam(IloCplex.DoubleParam.TimeLimit, config.getTimeLimit());
			m.cplex.setParam(IloCplex.Param.WorkMem, config.getWorkingMemory());
			m.cplex.setParam(IloCplex.IntParam.PopulateLim, 10);

			m.cplex.populate();

			int numberOfOptimalSolutions = m.collectOptimalSolutions();
			if (numberOfOptimalSolutions > 0) {
				bestSolution = m.getSolution(0, Utils.getICompilationUnit(ctx.ast), ctx.compilationUnit, ctx.ast)
						.setThreshold(ctx.threshold);
				bestSolution.evaluate(cache);
			}

		} catch (CpxException ex) {
			LOGGER.log(Level.WARNING, "CPLEX failure for method " + ctx.record.methodName, ex);
			throw ex;
		} catch (UnsatisfiedLinkError | NoClassDefFoundError | ExceptionInInitializerError ex) {
			// Occurs when IloCplex's static initializer fails to load the native library.
			// Mark CPLEX as unavailable so subsequent calls fail fast.
			PluginPreferences.resetCplexState();
			LOGGER.log(Level.SEVERE, "CPLEX native library linkage error for method " + ctx.record.methodName
					+ ". Verify java.library.path includes the CPLEX directory "
					+ "or add '--add-opens java.base/java.lang=ALL-UNNAMED' to VM arguments.", ex);
			throw new UnsatisfiedLinkError("CPLEX native library failed to link: " + ex.getMessage());
		} catch (Exception ex) {
			LOGGER.log(Level.WARNING, "Unexpected ILP failure for method " + ctx.record.methodName, ex);
			throw ex;
		} finally {
			if (m != null && m.cplex != null) {
				m.cplex.end();
			}
			if (localBuild && graphs != null) {
				graphs.clear();
			}
		}

		return bestSolution;
	}
}