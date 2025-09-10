package main.neo.algorithms.exhaustivesearch;

import java.io.IOException;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import main.neo.Constants;
import main.neo.algorithms.Solution;
import main.neo.refactoringcache.ConsecutiveSequenceIterator.APPROACH;
import main.neo.refactoringcache.RefactoringCache;

/**
 * Enumerative search for finding solutions ({@link Solution}) to reduce methods
 * cognitive complexity
 */
public class EnumerativeSearch {
	private Solution bestSolution;
	
	/**
	 * NEO's enumerative search algorithm to find optimal solutions for reducing without all params
	 * 
	 * @param approach
	 * @param compilationUnit
	 * @param refactoringCache
	 * @param ast
	 * @param methodComplexity
	 * @return
	 * @throws IOException
	 * 
	 * Author: Miguel Valadez Cano
	 */
	public Solution run(APPROACH approach, CompilationUnit compilationUnit, RefactoringCache refactoringCache, ASTNode ast)
			throws IOException {
		bestSolution = null;

		ExhaustiveEnumerationAlgorithm eea = new ExhaustiveEnumerationAlgorithm(refactoringCache, ast, approach);
		try {
			eea.run(solution -> {
				Solution sol = new Solution(solution, compilationUnit, ast);
				sol.evaluate(refactoringCache);
				if (bestSolution == null || sol.getFitness() < bestSolution.getFitness()) {
					bestSolution = sol;
				}
			}, Constants.MAX_EVALS);
		} catch (RuntimeException e) {
			System.out.print("Optimal " + bestSolution.toString());
		}

		return bestSolution;
	}

}