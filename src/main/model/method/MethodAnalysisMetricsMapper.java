package main.model.method;

import java.util.Collections;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import main.builder.MethodAnalysis;
import main.model.change.ExtractionPlan;
import main.neo.cem.CodeExtractionMetricsStats;
import main.refactor.RefactorComparison;

public final class MethodAnalysisMetricsMapper {

	private MethodAnalysisMetricsMapper() {}

	public static MethodMetrics toMethodMetrics(MethodAnalysis ma) {
		CodeExtractionMetricsStats stats = ma.stats();
		int totalExtractedLoc = stats != null ? stats.getTotalNumberOfExtractedLinesOfCode() : 0;
		int totalReductionOfCc = stats != null ? stats.getTotalNumberOfReductionOfCognitiveComplexity() : 0;
		ExtractionPlan applyPlan = ma.doPlan() != null ? ma.doPlan() : new ExtractionPlan(Collections.emptyList());
		ExtractionPlan undoPlan = ma.undoPlan() != null ? ma.undoPlan() : new ExtractionPlan(Collections.emptyList());
		return MethodMetrics.builder()
			.name(ma.methodName())
			.currentLoc(ma.currentLoc())
			.refactoredLoc(ma.refactoredLoc())
			.currentCc(ma.currentCc())
			.refactoredCc(ma.refactoredCc())
			.totalExtractedLinesOfCode(totalExtractedLoc)
			.totalReductionOfCc(totalReductionOfCc)
			.doPlan(applyPlan)
			.undoPlan(undoPlan)
			.build();
	}

	public static MethodAnalysis toMethodAnalysis(MethodDeclaration md, int currentCc, int currentLoc, RefactorComparison comparison) {
		return MethodAnalysis.builder()
			.methodName(md.getName().getIdentifier())
			.declaration(md)
			.currentCc(currentCc)
			.currentLoc(currentLoc)
			.refactoredCc(comparison.getRefactoredCc())
			.refactoredLoc(comparison.getRefactoredLoc())
			.extractionCount(comparison.getExtractionCount())
			.extractions(comparison.getExtractions())
			.bestExtraction(comparison.getBestMetrics())
			.stats(comparison.getStats())
			.doPlan(comparison.getDoPlan())
			.undoPlan(comparison.getUndoPlan())
			.build();
	}

}