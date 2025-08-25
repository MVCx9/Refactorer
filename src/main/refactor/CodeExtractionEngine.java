package main.refactor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.ltk.core.refactoring.Change;

import main.model.change.ExtractionPlan;
import main.model.method.MethodMetrics;
import main.neo.algorithms.Sequence;
import main.neo.cem.CodeExtractionMetrics;
import main.neo.cem.CodeExtractionMetricsStats;
import main.neo.refactoringcache.RefactoringCache;
import main.neo.refactoringcache.SentencesSelectorVisitor;

/**
 * Orquestador principal que invoca la lógica de NEO (paquete
 * <code>neo.*</code>) y traduce los resultados a nuestro modelo de dominio
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
	public RefactorComparison analyseAndPlan(CompilationUnit cu, MethodDeclaration node, int currentCc, int currentLoc)
			throws CoreException {

		// 1. Preparación: cache de refactorizaciones y secuencias candidatas
		RefactoringCache cache = new RefactoringCache(cu);

		// Extrae las secuencias de sentencias (candidatas a extracción) dentro del
		// método
		SentencesSelectorVisitor selector = new SentencesSelectorVisitor(cu);
		node.accept(selector);
		List<Sequence> sequences = selector.getSentencesToIterate();

		if (sequences.isEmpty()) {
			// Método demasiado simple o vacío, no hay mejora posible
			return buildMetricsWithoutRefactor(node, currentCc, currentLoc);
		}

		// 2. Evaluación: métricas por secuencia y agregadas
		List<CodeExtractionMetrics> metricsPerSequence = new ArrayList<>();
		for (Sequence seq : sequences) {
			metricsPerSequence.add(seq.evaluate(cache));
		}

		CodeExtractionMetricsStats stats = new CodeExtractionMetricsStats(
				metricsPerSequence.toArray(new CodeExtractionMetrics[0]));

		// 3. Selección: elegimos la extracción que más reduce la CC y es factible
		CodeExtractionMetrics best = metricsPerSequence.stream().filter(CodeExtractionMetrics::isFeasible)
				.max(Comparator.comparingInt(CodeExtractionMetrics::getReductionOfCognitiveComplexity)).orElse(null);

		if (best == null || best.getReductionOfCognitiveComplexity() <= 0) {
			return buildMetricsWithoutRefactor(node, currentCc, currentLoc);
		}

		// 4. Traducción de tipos: NEO al modelo
		int refactoredCc = Math.max(0, currentCc - best.getReductionOfCognitiveComplexity());
		int refactoredLoc = Math.max(0, currentLoc - best.getNumberOfExtractedLinesOfCode());

		ExtractionPlan applyPlan = new ExtractionPlan(asImmutable(best.getChanges()));
		ExtractionPlan undoPlan = new ExtractionPlan(asImmutable(best.getUndoChanges()));

		// 5. Construcción del DTO de salida
		return RefactorComparison.builder()
				.name(node.getName().toString())
				.refactoredCc(refactoredCc)
				.refactoredLoc(refactoredLoc)
				.bestMetrics(best)
				.stats(stats)
				.doPlan(applyPlan)
				.undoPlan(undoPlan)
				.build();
	}

	private RefactorComparison buildMetricsWithoutRefactor(MethodDeclaration node, int currentCc, int currentLoc) {
		return RefactorComparison.builder()
				.name(node.getName().toString())
				.refactoredLoc(currentLoc)
				.refactoredCc(currentCc)
				.bestMetrics(null)
				.stats(null)
				.doPlan(new ExtractionPlan(Collections.emptyList()))
				.undoPlan(new ExtractionPlan(Collections.emptyList()))
				.build();
	}

	private List<Change> asImmutable(List<Change> list) {
		return list == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(list));
	}
	
	/*
	 * MethodMetrics.builder()
				.name(node.getName().toString())
				.currentLoc(currentLoc)
				.refactoredLoc(refactoredLoc)
				.currentCc(currentCc)
				.refactoredCc(refactoredCc)
				.totalExtractedLinesOfCode(stats.getTotalNumberOfExtractedLinesOfCode())
				.totalReductionOfCc(stats.getTotalNumberOfReductionOfCognitiveComplexity())
				.applyPlan(applyPlan)
				.undoPlan(undoPlan)
				.build();
	 */
}
