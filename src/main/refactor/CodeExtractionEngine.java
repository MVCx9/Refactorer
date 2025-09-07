package main.refactor;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import main.model.method.MethodMetrics;
import main.neo.Constants;
import main.neo.algorithms.Solution;
import main.neo.algorithms.exhaustivesearch.EnumerativeSearch;
import main.neo.refactoringcache.ConsecutiveSequenceIterator.APPROACH;
import main.neo.refactoringcache.RefactoringCache;
import main.neo.refactoringcache.RefactoringCacheFiller;

/**
 * Orquestador principal que invoca la lógica de NEO (paquete
 * <code>neo.*</code>) y traduce los resultados al modelo de dominio
 * (<code>model.*</code>).
 */
public final class CodeExtractionEngine {

	/**
	 * Analiza un método, busca posibles extracciones de código con la heurística de
	 * NEO y devuelve un {@link MethodMetrics} rellenado con:
	 * <ul>
	 * <li>Complejidad y LOC actuales vs. tras refactorización</li>
	 * <li>Número de métodos extraídos</li>
	 * <li>Planes de cambio para aplicar / deshacer</li>
	 * </ul>
	 * 
	 * @param cu         unidad de compilación que contiene el método
	 * @param icuWorkingCopy 
	 * @param node       declaración del método a analizar
	 * @param currentCc  complejidad cognitiva actual del método
	 * @param currentLoc líneas de código actuales del método
	 * @return métricas completas para el método
	 * @throws CoreException propagadas desde el modelo de refactorización de
	 *                       Eclipse
	 * @throws IOException 
	 */
	public List<RefactorComparison> analyseAndPlan(CompilationUnit cu, ICompilationUnit icuWorkingCopy, MethodDeclaration node, int currentCc, int currentLoc) throws CoreException, IOException {
		if (node == null || cu == null || currentCc <= Constants.MAX_COMPLEXITY) {
			return Collections.emptyList();
		}

		List<RefactorComparison> result = new LinkedList<>();
		
		// set accumulative cognitive complexity properties in AST nodes
		RefactoringCache cache = new RefactoringCache(cu);
		main.neo.cem.Utils.computeAndAnnotateAccumulativeCognitiveComplexity(node);
		RefactoringCacheFiller.exhaustiveEnumerationAlgorithm(cache, node);
		
		/* Pick an algorithm to select the sequences
		 * Constants.EXHAUSTIVE_SEARCH_LONG_SEQUENCES_FIRST: APPROACH.LONG_SEQUENCE_FIRST
		 * Constants.EXHAUSTIVE_SEARCH_SHORT_SEQUENCES_FIRST: APPROACH.SHORT_SEQUENCE_FIRST
		 */
		List<Solution> solutions = new LinkedList<>();
		Solution solution = new Solution(cu, node);
		solution = new EnumerativeSearch()
				.run(APPROACH.LONG_SEQUENCE_FIRST, cu, cache, node, currentCc);
		
		if (solution != null && solution.getSequenceList() != null && solution.getSequenceList().isEmpty()) {
			return Collections.emptyList();
		}
		
		solutions.add(main.neo.algorithms.utils.Utils.indexOfInsertionToKeepListSorted(solution, solutions), solution);
		
		for(Solution sol : solutions) {
			cu = sol.applyExtractMethodsToCompilationUnit(true, cu, icuWorkingCopy);
			result.add(RefactorComparison.builder()
				.name(sol.getMethodName())
				.compilationUnitRefactored(cu)
				.reducedComplexity(currentCc)
				.numberOfExtractions(currentLoc)
				.stats(sol.getExtractionMetricsStats())
				.build());
		}
		
		return result;
	}

}