package main.neo.core.solvers;

import main.neo.core.Solution;
import main.neo.core.refactoringcache.RefactoringCache;

/**
 * Common interface for all refactoring solvers.
 * <p>
 * Solvers are responsible for finding the optimal (or near-optimal) set of
 * refactorings to reduce cognitive complexity within the constraints provided
 * by the context.
 * </p>
 */
public interface RefactoringSolver {

	/**
	 * Executes the specific solver to find a refactoring solution.
	 *
	 * @param ctx   the context containing method info, constraints and configuration
	 * @param cache the cache of feasible refactorings (containing metrics and
	 *              validity checks)
	 * @return the best {@link Solution} found, or {@code null} if no valid
	 *         solution exists
	 * @throws Exception if the solving process encounters a critical error
	 */
	Solution solve(SolverContext ctx, RefactoringCache cache) throws Exception;
}