package main.model.clazz;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import main.builder.ClassAnalysis;
import main.model.method.MethodAnalysisMetricsMapper;
import main.model.method.MethodMetrics;

public final class ClassAnalysisMetricsMapper {

	private ClassAnalysisMetricsMapper() {}

	public static ClassMetrics toClassMetrics(ClassAnalysis analysis) {
		Objects.requireNonNull(analysis, "analysis must not be null");

		List<MethodMetrics> currentMethodMetrics = new ArrayList<>();
		List<MethodMetrics> refactoredMethodMetrics = new ArrayList<>();

		String className = analysis.getClassName() != null ? analysis.getClassName() : "N/A";
		currentMethodMetrics = MethodAnalysisMetricsMapper.toMethodMetrics(analysis.getCurrentMethods());
		refactoredMethodMetrics = MethodAnalysisMetricsMapper.toMethodMetrics(analysis.getRefactoredMethods());
			
		return ClassMetrics.builder()
				.name(className)
				.currentMethods(currentMethodMetrics)
				.refactoredMethods(refactoredMethodMetrics)
				.build();
	}
}