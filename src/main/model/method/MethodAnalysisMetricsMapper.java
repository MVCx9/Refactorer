package main.model.method;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import main.builder.MethodAnalysis;
import main.model.change.ExtractionPlan;
import main.neo.cem.CodeExtractionMetrics;
import main.neo.cem.CodeExtractionMetricsStats;
import main.refactor.RefactorComparison;

public final class MethodAnalysisMetricsMapper {

	private MethodAnalysisMetricsMapper() {}

	public static List<MethodMetrics> toMethodMetrics(List<MethodAnalysis> ma) {
		return ma.stream().map(MethodAnalysisMetricsMapper::toMethodMetrics).toList();
	}
	
	private static MethodMetrics toMethodMetrics(MethodAnalysis ma) {
		ExtractionPlan applyPlan = ma.getDoPlan() != null ? ma.getDoPlan() : new ExtractionPlan(Collections.emptyList());
		ExtractionPlan undoPlan = ma.getUndoPlan() != null ? ma.getUndoPlan() : new ExtractionPlan(Collections.emptyList());
		
		return MethodMetrics.builder()
			.name(ma.getMethodName())
			.loc(ma.getRefactoredLoc())
			.cc(ma.getRefactoredCc())
			.doPlan(applyPlan)
			.undoPlan(undoPlan)
			.build();
	}

	public static List<MethodAnalysis> toMethodAnalysis(List<RefactorComparison> comparison) {
		
		List<MethodAnalysis> result = new LinkedList<>();
		
		List<CodeExtractionMetrics> metricsList = comparison.stream()
			.map(RefactorComparison::getExtraction)
			.filter(Objects::nonNull)
			.toList();
		CodeExtractionMetricsStats stats = null;
		
		if(!metricsList.isEmpty()) {
			stats = new CodeExtractionMetricsStats(metricsList.toArray(new CodeExtractionMetrics[0]));
		}
		
		for (RefactorComparison c : comparison) {
			MethodAnalysis m = MethodAnalysis.builder()
			.methodName(c.getName())
			.currentCc(c.getOriginalCc())
			.currentLoc(c.getOriginalLoc())
			.refactoredCc(c.getRefactoredCc())
			.refactoredLoc(c.getRefactoredLoc())
			.extraction(c.getExtraction())
			.stats(stats)
			.doPlan(c.getDoPlan())
			.undoPlan(c.getUndoPlan())
			.build();
			
			result.add(m);
		}
		
		return result;
	}

	public static MethodAnalysis toMethodAnalysis(MethodDeclaration md, int currentCc, int currentLoc) {
		return MethodAnalysis.builder()
			.methodName(md.getName().getIdentifier())
			.currentCc(currentCc)
			.currentLoc(currentLoc)
			.refactoredCc(currentCc)
			.refactoredLoc(currentLoc)
			.extraction(null)
			.stats(null)
			.doPlan(new ExtractionPlan(Collections.emptyList()))
			.undoPlan(new ExtractionPlan(Collections.emptyList()))
			.build();
	}
}