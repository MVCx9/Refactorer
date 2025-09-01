package main.model.method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import main.builder.MethodAnalysis;
import main.model.change.ExtractionPlan;
import main.refactor.RefactorComparison;

public final class MethodAnalysisMetricsMapper {

	private MethodAnalysisMetricsMapper() {}

	public static List<MethodMetrics> toMethodMetrics(List<MethodAnalysis> ma) {
		return ma.stream().map(MethodAnalysisMetricsMapper::toMethodMetrics).toList();
	}
	
	private static MethodMetrics toMethodMetrics(MethodAnalysis ma) {
		ExtractionPlan applyPlan = ma.doPlan() != null ? ma.doPlan() : new ExtractionPlan(Collections.emptyList());
		ExtractionPlan undoPlan = ma.undoPlan() != null ? ma.undoPlan() : new ExtractionPlan(Collections.emptyList());
		
		return MethodMetrics.builder()
			.name(ma.methodName())
			.loc(ma.refactoredLoc())
			.cc(ma.refactoredCc())
			.doPlan(applyPlan)
			.undoPlan(undoPlan)
			.build();
	}

	public static List<MethodAnalysis> toMethodAnalysis(List<RefactorComparison> comparison) {
		List<MethodAnalysis> result = new ArrayList<>();
		
		for (RefactorComparison c : comparison) {
			MethodAnalysis m = MethodAnalysis.builder()
			.methodName(c.getName())
			.currentCc(c.getOriginalCc())
			.currentLoc(c.getOriginalLoc())
			.refactoredCc(c.getRefactoredCc())
			.refactoredLoc(c.getRefactoredLoc())
			.extraction(c.getExtraction())
			.stats(c.getStats())
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