package main.neo.core.solvers.exhaustivesearch;

import main.neo.core.Solution;
import main.neo.core.refactoringcache.RefactoringCache;
import main.neo.core.solvers.RefactoringSolver;
import main.neo.core.solvers.SolverContext;
import main.neo.core.solvers.config.EnumerativeSearchConfig;

/**
 * Enumerative search solver for finding solutions ({@link Solution}) to reduce
 * method cognitive complexity.
 * <p>
 * Exhaustively explores possible refactoring sequences using a configured
 * strategy (longest vs shortest sequence first). Reports nothing to disk; the
 * caller receives the best solution directly.
 * </p>
 */
public class EnumerativeSearchSolver implements RefactoringSolver {

	private final EnumerativeSearchConfig config;

	public EnumerativeSearchSolver(EnumerativeSearchConfig config) {
		this.config = config;
	}

	@Override
	public Solution solve(SolverContext ctx, RefactoringCache cache) throws Exception {
		final Solution[] best = new Solution[] { null };

		ExhaustiveEnumerationAlgorithm eea = new ExhaustiveEnumerationAlgorithm(cache, ctx.ast,
				this.config.getApproach());

		try {
			eea.run(sequences -> {
				Solution sol = new Solution(sequences, ctx.compilationUnit, ctx.ast).setThreshold(ctx.threshold);
				sol.evaluate(cache);

				if (best[0] == null || sol.getFitness() < best[0].getFitness()) {
					best[0] = sol;
				}
			}, this.config.getEvaluations());
		} catch (RuntimeException e) {
			// Early-termination signal raised by the exhaustive enumerator: best solution
			// found so far is already stored in {@code best[0]}.
		}

		return best[0];
	}
}