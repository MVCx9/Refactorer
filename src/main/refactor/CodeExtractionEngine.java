package main.refactor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import main.common.utils.Utils;
import main.model.change.ExtractionPlan;
import main.model.method.MethodMetrics;
import main.neo.Constants;
import main.neo.algorithms.Pair;
import main.neo.algorithms.Sequence;
import main.neo.cem.CodeExtractionMetrics;
import main.neo.refactoringcache.RefactoringCache;
import main.neo.refactoringcache.SentencesSelectorVisitor;

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
	 */
	public List<RefactorComparison> analyseAndPlan(CompilationUnit cu, MethodDeclaration node, int currentCc, int currentLoc) throws CoreException {
		if (node == null || cu == null)
			return List.of();

		int maxCc = Constants.MAX_COMPLEXITY;
		if (currentCc <= maxCc) {
			return List.of(buildNoRefactor(node, currentCc, currentLoc));
		}

		// set accumulative cognitive complexity properties in AST nodes
		main.neo.cem.Utils.computeAndAnnotateAccumulativeCognitiveComplexity(node);

		RefactoringCache cache = new RefactoringCache(cu);
		SentencesSelectorVisitor selector = new SentencesSelectorVisitor(cu);
		node.accept(selector);
		List<Sequence> allSequences = selector.getSentencesToIterate();
		if (allSequences.isEmpty()) {
			return List.of(buildNoRefactor(node, currentCc, currentLoc));
		}

		List<Candidate> candidates = new ArrayList<>();
		for (Sequence seq : allSequences) {
			CodeExtractionMetrics m = cache.getMetrics(seq);
			if (m == null)
				continue;
			if (!m.isFeasible())
				continue;
			int reduction = Math.max(0, m.getReductionOfCognitiveComplexity());
			if (reduction <= 0)
				continue;
			Pair offsets = seq.getOffsetAsPair();
			candidates.add(new Candidate(seq, offsets, m));
		}

		if (candidates.isEmpty()) {
			return List.of(buildNoRefactor(node, currentCc, currentLoc));
		}

		candidates.sort(new CandidateComparator());

		List<Candidate> selected = new ArrayList<>();
		Set<Pair> used = new HashSet<>();
		int remainingReduction = Math.max(0, currentCc - maxCc);
		for (Candidate candidate : candidates) {
			boolean overlaps = used.stream().anyMatch(p -> !Pair.disjoint(p, candidate.offsets));
			if (overlaps)
				continue;
			selected.add(candidate);
			used.add(candidate.offsets);
			remainingReduction -= candidate.metrics.getReductionOfCognitiveComplexity();
			if (remainingReduction <= 0)
				break;
		}

		if (selected.isEmpty()) {
			return List.of(buildNoRefactor(node, currentCc, currentLoc));
		}

		List<CodeExtractionMetrics> plannedPerExtraction = new ArrayList<>();
		List<RefactorComparison> results = new ArrayList<>();

		int finalCc = currentCc;
		int finalLoc = currentLoc;

		int extractionIndex = 1;
		for (Candidate selectedCandidate : selected) {
			String extractedName = node.getName().getIdentifier() + "_ext_" + extractionIndex++;
			int selectionStart = selectedCandidate.offsets.getA();
			int selectionLength = selectedCandidate.offsets.getB() - selectedCandidate.offsets.getA();

			CodeExtractionMetrics planned = main.neo.cem.Utils.extractCode(cu, selectionStart, selectionLength, extractedName, true);
			planned.setReductionOfCognitiveComplexity(selectedCandidate.metrics.getReductionOfCognitiveComplexity());
			planned.setAccumulatedInherentComponent(selectedCandidate.metrics.getAccumulatedInherentComponent());
			planned.setAccumulatedNestingComponent(selectedCandidate.metrics.getAccumulatedNestingComponent());
			planned.setNumberNestingContributors(selectedCandidate.metrics.getNumberNestingContributors());
			planned.setNesting(selectedCandidate.metrics.getNesting());
			planned.setExtractedMethodName(extractedName);
			
			plannedPerExtraction.add(planned);

			finalCc = Math.max(0, finalCc - selectedCandidate.metrics.getReductionOfCognitiveComplexity());
			finalLoc = Math.max(0, finalLoc - planned.getNumberOfExtractedLinesOfCode());
		}
		
		// Selecciona el mejor resultado: mayor reducción, más LOC extraídos y mayor CC del nuevo método
		CodeExtractionMetrics planned = plannedPerExtraction.stream()
			.max(Comparator
					.comparingInt(CodeExtractionMetrics::getReductionOfCognitiveComplexity)
					.thenComparingInt(CodeExtractionMetrics::getNumberOfExtractedLinesOfCode)
					.thenComparingInt(CodeExtractionMetrics::getCognitiveComplexityOfNewExtractedMethod))
			.orElse(plannedPerExtraction.get(0));

		results.add(RefactorComparison.builder()
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
		
		// place original method first in the list
		results.add(0, RefactorComparison.builder()
			.name(node.getName().getIdentifier())
			.originalCc(currentCc)
			.originalLoc(currentLoc)
			.refactoredCc(finalCc)
			.refactoredLoc(finalLoc)
			.extraction(null)
			.stats(null)
			.doPlan(null)
			.undoPlan(null)
			.build());

		return Collections.unmodifiableList(results);
	}

	private RefactorComparison buildNoRefactor(MethodDeclaration node, int currentCc, int currentLoc) {
		return RefactorComparison.builder()
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
			.build();
	}

	private static class Candidate {
		final Sequence sequence;
		final Pair offsets;
		final CodeExtractionMetrics metrics;

		Candidate(Sequence s, Pair p, CodeExtractionMetrics m) {
			this.sequence = s;
			this.offsets = p;
			this.metrics = m;
		}
	}

	private static class CandidateComparator implements Comparator<Candidate> {
		@Override
		public int compare(Candidate a, Candidate b) {
			boolean aOk = a.metrics.getCognitiveComplexityOfNewExtractedMethod() <= Constants.MAX_COMPLEXITY;
			boolean bOk = b.metrics.getCognitiveComplexityOfNewExtractedMethod() <= Constants.MAX_COMPLEXITY;
			if (aOk != bOk)
				return aOk ? -1 : 1;
			int byReduction = Integer.compare(b.metrics.getReductionOfCognitiveComplexity(),
					a.metrics.getReductionOfCognitiveComplexity());
			if (byReduction != 0)
				return byReduction;
			int byParams = Integer.compare(a.metrics.getNumberOfParametersInExtractedMethod(),
					b.metrics.getNumberOfParametersInExtractedMethod());
			if (byParams != 0)
				return byParams;
			return Integer.compare(a.metrics.getNumberOfExtractedLinesOfCode(),
					b.metrics.getNumberOfExtractedLinesOfCode());
		}
	}
}