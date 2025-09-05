package main.refactor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import main.common.utils.Utils;
import main.model.change.ExtractionPlan;
import main.model.method.MethodMetrics;
import main.neo.Constants;
import main.neo.algorithms.Pair;
import main.neo.algorithms.Sequence;
import main.neo.algorithms.Solution;
import main.neo.algorithms.exhaustivesearch.EnumerativeSearch;
import main.neo.cem.CodeExtractionMetrics;
import main.neo.refactoringcache.ConsecutiveSequenceIterator.APPROACH;
import main.neo.refactoringcache.RefactoringCache;

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
	 * @param node       declaración del método a analizar
	 * @param currentCc  complejidad cognitiva actual del método
	 * @param currentLoc líneas de código actuales del método
	 * @return métricas completas para el método
	 * @throws CoreException propagadas desde el modelo de refactorización de
	 *                       Eclipse
	 * @throws IOException 
	 */
	public List<RefactorComparison> analyseAndPlan(CompilationUnit cu, MethodDeclaration node, int currentCc, int currentLoc) throws CoreException, IOException {
		if (node == null || cu == null || currentCc <= Constants.MAX_COMPLEXITY) {
			return buildNoRefactor(node, currentCc, currentLoc);
		}

		List<RefactorComparison> result = new LinkedList<>();
		
		// set accumulative cognitive complexity properties in AST nodes
		main.neo.cem.Utils.computeAndAnnotateAccumulativeCognitiveComplexity(node);
		RefactoringCache cache = new RefactoringCache(cu);
		
		/* Pick an algorithm to select the sequences
		 * Constants.EXHAUSTIVE_SEARCH_LONG_SEQUENCES_FIRST: APPROACH.LONG_SEQUENCE_FIRST
		 * Constants.EXHAUSTIVE_SEARCH_SHORT_SEQUENCES_FIRST: APPROACH.SHORT_SEQUENCE_FIRST
		 */
		Solution solution = new Solution(cu, node);
		solution = new EnumerativeSearch()
				.run(APPROACH.SHORT_SEQUENCE_FIRST, cu, cache, node, currentCc);
		
		
		if (solution != null && solution.getSequenceList() != null && solution.getSequenceList().isEmpty()) {
			return buildNoRefactor(node, currentCc, currentLoc);
		}
		
		List<Sequence> sequences = solution.getSequenceList();
		List<CodeExtractionMetrics> plannedPerExtraction = new ArrayList<>();

		int finalCc = currentCc;
		int finalLoc = currentLoc;
		int extractionCount = 1;
		
		for (Sequence sequence : sequences) {
			String extractedName = node.getName().getIdentifier() + "_ext_" + extractionCount++;
			
			Pair offset = sequence.getOffsetAsPair();
			int selectionStart = offset.getA();
			int selectionLength = offset.getB() - offset.getA();
			
			CodeExtractionMetrics cachedMetrics = cache.getMetrics(sequence);

			CodeExtractionMetrics planned = main.neo.cem.Utils.extractCode(cu, selectionStart, selectionLength, extractedName, true);
			planned.setExtractedMethodName(extractedName);
			planned.setReductionOfCognitiveComplexity(cachedMetrics.getReductionOfCognitiveComplexity());
			planned.setAccumulatedInherentComponent(cachedMetrics.getAccumulatedInherentComponent());
			planned.setAccumulatedNestingComponent(cachedMetrics.getAccumulatedNestingComponent());
			planned.setNumberNestingContributors(cachedMetrics.getNumberNestingContributors());
			planned.setNesting(cachedMetrics.getNesting());
			plannedPerExtraction.add(planned);

			finalCc = Math.max(0, finalCc - cachedMetrics.getReductionOfCognitiveComplexity());
			finalLoc = Math.max(0, finalLoc - planned.getNumberOfExtractedLinesOfCode());
			
			result.add(RefactorComparison.builder()
				.name(planned.getExtractedMethodName())
				.originalCc(planned.getCognitiveComplexityOfNewExtractedMethod())
				.originalLoc(planned.getNumberOfExtractedLinesOfCode())
				.refactoredCc(planned.getCognitiveComplexityOfNewExtractedMethod())
				.refactoredLoc(planned.getNumberOfExtractedLinesOfCode())
				.extraction(planned)
				.stats(null)
				.doPlan(new ExtractionPlan(Utils.asImmutable(planned.getChanges())))
				.undoPlan(new ExtractionPlan(Utils.asImmutable(planned.getUndoChanges())))
				.build()
			);
		}
		
		// Añadir también el método original, para tener la comparación completa
		result.add(0, RefactorComparison.builder()
			.name(node.getName().getIdentifier())
			.originalCc(currentCc)
			.originalLoc(currentLoc)
			.refactoredCc(finalCc)
			.refactoredLoc(finalLoc)
			.extraction(null)
			.stats(null)
			.doPlan(null)
			.undoPlan(null)
			.build()
		);
		
		return result;
	}

	private List<RefactorComparison> buildNoRefactor(MethodDeclaration node, int currentCc, int currentLoc) {
		List<RefactorComparison> result = new LinkedList<>();
		
		result.add(RefactorComparison.builder()
			.name(node.getName().getIdentifier())
			.originalCc(currentCc)
			.originalLoc(currentLoc)
			.refactoredCc(currentCc)
			.refactoredLoc(currentLoc)
			.stats(null)
			.extraction(null)
			.stats(null)
			.doPlan(new ExtractionPlan(Collections.emptyList()))
			.undoPlan(new ExtractionPlan(Collections.emptyList()))
			.build());
		
		return result;
	}
}