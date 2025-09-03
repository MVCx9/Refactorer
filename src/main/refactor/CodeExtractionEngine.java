package main.refactor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ASTVisitor;

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
		if (node == null || cu == null) {
			return buildNoRefactor(node, currentCc, currentLoc);
		}

		int maxCc = Constants.MAX_COMPLEXITY;
		
		if (currentCc <= maxCc) {
			return buildNoRefactor(node, currentCc, currentLoc);
		}

		List<RefactorComparison> result = new LinkedList<>();
		
		// set accumulative cognitive complexity properties in AST nodes
		main.neo.cem.Utils.computeAndAnnotateAccumulativeCognitiveComplexity(node);

		RefactoringCache cache = new RefactoringCache(cu);
		SentencesSelectorVisitor selector = new SentencesSelectorVisitor(cu);
		node.accept(selector);
		List<Sequence> allSequences = selector.getSentencesToIterate();
		if (allSequences.isEmpty()) {
			return buildNoRefactor(node, currentCc, currentLoc);
		}

		List<Candidate> candidates = new ArrayList<>();
		for (Sequence seq : allSequences) {
			CodeExtractionMetrics m = cache.getMetrics(seq);
			if (m == null) {
				continue;
			}
			if (!m.isFeasible()) {
				continue;
			}
			int reduction = Math.max(0, m.getReductionOfCognitiveComplexity());
			if (reduction <= 0) {
				continue;
			}
			Pair offsets = seq.getOffsetAsPair();
			candidates.add(new Candidate(offsets, m));
		}

		if (candidates.isEmpty()) {
			return buildNoRefactor(node, currentCc, currentLoc);
		}

		candidates.sort(new CandidateComparator());

		List<Candidate> selected = new ArrayList<>();
		Set<Pair> used = new HashSet<>();
		int remainingReduction = Math.max(0, currentCc - maxCc);
		for (Candidate candidate : candidates) {
			boolean overlaps = used.stream().anyMatch(p -> !Pair.disjoint(p, candidate.getOffsets()));
			if (overlaps)
				continue;
			selected.add(candidate);
			used.add(candidate.getOffsets());
			remainingReduction -= candidate.getMetrics().getReductionOfCognitiveComplexity();
			if (remainingReduction <= 0)
				break;
		}

		if (selected.isEmpty()) {
			return buildNoRefactor(node, currentCc, currentLoc);
		}

		List<CodeExtractionMetrics> plannedPerExtraction = new ArrayList<>();

		int finalCc = currentCc;
		int finalLoc = currentLoc;

		for (Candidate selectedCandidate : selected) {
			String extractedName = node.getName().getIdentifier() + "_ext";
			int selectionStart = selectedCandidate.getOffsets().getA();
			int selectionLength = selectedCandidate.getOffsets().getB() - selectedCandidate.getOffsets().getA();

			CodeExtractionMetrics planned = main.neo.cem.Utils.extractCode(cu, selectionStart, selectionLength, extractedName, true);
			planned.setReductionOfCognitiveComplexity(selectedCandidate.getMetrics().getReductionOfCognitiveComplexity());
			planned.setAccumulatedInherentComponent(selectedCandidate.getMetrics().getAccumulatedInherentComponent());
			planned.setAccumulatedNestingComponent(selectedCandidate.getMetrics().getAccumulatedNestingComponent());
			planned.setNumberNestingContributors(selectedCandidate.getMetrics().getNumberNestingContributors());
			planned.setNesting(selectedCandidate.getMetrics().getNesting());
			planned.setExtractedMethodName(extractedName);
			
			plannedPerExtraction.add(planned);

			finalCc = Math.max(0, finalCc - selectedCandidate.getMetrics().getReductionOfCognitiveComplexity());
			finalLoc = Math.max(0, finalLoc - planned.getNumberOfExtractedLinesOfCode());
		}
		
		if (plannedPerExtraction.isEmpty()) {
			return buildNoRefactor(node, currentCc, currentLoc);
		}
		
		// Selecciona el mejor resultado: mayor reducción, más LOC extraídos y mayor CC del nuevo método
		CodeExtractionMetrics planned = plannedPerExtraction.stream()
			.max(Comparator
					.comparingInt(CodeExtractionMetrics::getReductionOfCognitiveComplexity)
					.thenComparingInt(CodeExtractionMetrics::getNumberOfExtractedLinesOfCode)
					.thenComparingInt(CodeExtractionMetrics::getCognitiveComplexityOfNewExtractedMethod))
			.orElse(plannedPerExtraction.get(0));
		
		result.add(RefactorComparison.builder()
			.name(planned.getExtractedMethodName())
			.methodDeclaration(findMethodDeclaration(planned.getCompilationUnitWithChanges(), planned.getExtractedMethodName()))
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
		
		// Añadir también el método original, para tener la comparación completa
		result.add(0, RefactorComparison.builder()
			.name(node.getName().getIdentifier())
			.methodDeclaration(node)
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

	private static class Candidate {
		private final Pair offsets;
		private final CodeExtractionMetrics metrics;

		Candidate(Pair p, CodeExtractionMetrics m) {
			this.offsets = p;
			this.metrics = m;
		}
		
		Pair getOffsets() {
			return offsets;
		}
		
		CodeExtractionMetrics getMetrics() {
			return metrics;
		}
	}

	private static class CandidateComparator implements Comparator<Candidate> {
		@Override
		public int compare(Candidate a, Candidate b) {
			boolean aOk = a.getMetrics().getCognitiveComplexityOfNewExtractedMethod() <= Constants.MAX_COMPLEXITY;
			boolean bOk = b.getMetrics().getCognitiveComplexityOfNewExtractedMethod() <= Constants.MAX_COMPLEXITY;
			if (aOk != bOk)
				return aOk ? -1 : 1;
			int byReduction = Integer.compare(b.getMetrics().getReductionOfCognitiveComplexity(),
					a.getMetrics().getReductionOfCognitiveComplexity());
			if (byReduction != 0)
				return byReduction;
			int byParams = Integer.compare(a.getMetrics().getNumberOfParametersInExtractedMethod(),
					b.getMetrics().getNumberOfParametersInExtractedMethod());
			if (byParams != 0)
				return byParams;
			return Integer.compare(a.getMetrics().getNumberOfExtractedLinesOfCode(),
					b.getMetrics().getNumberOfExtractedLinesOfCode());
		}
	}
	
	private List<RefactorComparison> buildNoRefactor(MethodDeclaration node, int currentCc, int currentLoc) {
		List<RefactorComparison> result = new LinkedList<>();
		
		result.add(RefactorComparison.builder()
			.name(node.getName().getIdentifier())
			.methodDeclaration(node)
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
	
	private MethodDeclaration findMethodDeclaration(CompilationUnit cu, String extractedMethodName) {
		if (cu == null || extractedMethodName == null) {
			return null;
		}
		final MethodDeclaration[] found = new MethodDeclaration[1];
		cu.accept(new ASTVisitor() {
			@Override
			public boolean preVisit2(org.eclipse.jdt.core.dom.ASTNode node) {
				// Si ya hemos encontrado el método, no seguir recorriendo
				return found[0] == null;
			}
			@Override
			public boolean visit(MethodDeclaration node) {
				if (node.getName() != null && extractedMethodName.equals(node.getName().getIdentifier())) {
					found[0] = node;
					return false;
				}
				return true;
			}
		});
		return found[0];
	}
}