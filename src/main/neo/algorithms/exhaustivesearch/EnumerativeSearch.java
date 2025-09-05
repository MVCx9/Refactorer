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
	public Solution run(APPROACH approach, CompilationUnit compilationUnit, RefactoringCache refactoringCache, ASTNode ast, int methodComplexity)
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
	
	/*
	 public Solution run(APPROACH approach, BufferedWriter bf, String classWithIssues, CompilationUnit compilationUnit,
			RefactoringCache refactoringCache, long runtimeToFillRefactoringCache, List<ASTNode> auxList, ASTNode ast, int methodComplexity)
			throws IOException {
		bestSolution = null;
		int optimo = 0;

		long startTime = System.currentTimeMillis();
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
			optimo = 1;
		}

		long runtime = System.currentTimeMillis() - startTime;

		// write ES results into file
		bf.append(approach.name() + ";");
		bf.append(classWithIssues + ";");
		bf.append(((MethodDeclaration) ast).getName().toString() + ";");
		bf.append(methodComplexity + ";");
		bf.append(bestSolution.toStringForFileFormat() + ";");
		bf.append(bestSolution.getSize() + ";");
		bf.append(bestSolution.getFitness() + ";");
		bf.append(bestSolution.getReducedComplexity() + ";");
		bf.append(methodComplexity - bestSolution.getReducedComplexity() + ";");
		bf.append(bestSolution.getExtractionMetricsStats().getMinNumberOfExtractedLinesOfCode() + ";");
		bf.append(bestSolution.getExtractionMetricsStats().getMaxNumberOfExtractedLinesOfCode() + ";");
		bf.append(bestSolution.getExtractionMetricsStats().getMeanNumberOfExtractedLinesOfCode() + ";");
		bf.append(bestSolution.getExtractionMetricsStats().getTotalNumberOfExtractedLinesOfCode() + ";");
		bf.append(bestSolution.getExtractionMetricsStats().getMinNumberOfParametersInExtractedMethods() + ";");
		bf.append(bestSolution.getExtractionMetricsStats().getMaxNumberOfParametersInExtractedMethods() + ";");
		bf.append(bestSolution.getExtractionMetricsStats().getMeanNumberOfParametersInExtractedMethods() + ";");
		bf.append(bestSolution.getExtractionMetricsStats().getTotalNumberOfParametersInExtractedMethods() + ";");
		bf.append(bestSolution.getExtractionMetricsStats().getMinReductionOfCognitiveComplexity() + ";");
		bf.append(bestSolution.getExtractionMetricsStats().getMaxReductionOfCognitiveComplexity() + ";");
		bf.append(bestSolution.getExtractionMetricsStats().getMeanReductionOfCognitiveComplexity() + ";");
		bf.append(bestSolution.getExtractionMetricsStats().getTotalNumberOfReductionOfCognitiveComplexity() + ";");
		bf.append("" + optimo + ";");
		bf.append(Long.toString(runtimeToFillRefactoringCache) + ";");
		bf.append(Long.toString(runtime) + "\n");

		bf.flush();

		return bestSolution;
	}
	 */

}