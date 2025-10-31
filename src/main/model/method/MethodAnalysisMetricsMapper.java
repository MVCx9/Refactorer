package main.model.method;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import main.builder.MethodAnalysis;
import main.refactor.RefactorComparison;

public final class MethodAnalysisMetricsMapper {

	private MethodAnalysisMetricsMapper() {}

	public static List<MethodMetrics> toMethodMetrics(List<MethodAnalysis> ma) {
		return ma.stream().map(MethodAnalysisMetricsMapper::toMethodMetrics).toList();
	}
	
	private static MethodMetrics toMethodMetrics(MethodAnalysis ma) {
		
		return MethodMetrics.builder()
			.name(ma.getMethodName())
			.cc(ma.getCc())
			.loc(ma.getLoc())
			.usedILP(ma.isUsedILP())
			.build();
	}

	public static List<MethodAnalysis> toMethodAnalysis(List<RefactorComparison> comparison) {
		
		List<MethodAnalysis> result = new LinkedList<>();
		
		
		for (RefactorComparison c : comparison) {
			MethodAnalysis m = MethodAnalysis.builder()
			.methodName(c.getName())
			.cc(0)
			.loc(0)
			.numberOfExtractions(c.getNumberOfExtractions())
			.reducedComplexity(c.getReducedComplexity())
			.compilationUnitRefactored(c.getCompilationUnitRefactored())
			.stats(c.getStats())
			.usedILP(c.isUsedILP())
			.build();
			
			result.add(m);
		}
		
		return result;
	}

	public static MethodAnalysis toMethodAnalysis(MethodDeclaration md, int cc, int loc) {
		return MethodAnalysis.builder()
			.methodName(md.getName().getIdentifier())
			.cc(cc)
			.loc(loc)
			.reducedComplexity(0)
			.numberOfExtractions(0)
			.compilationUnitRefactored(null)
			.stats(null)
			.build();
	}
}