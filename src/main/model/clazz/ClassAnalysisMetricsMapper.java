package main.model.clazz;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import main.builder.FileAnalysis;
import main.model.method.MethodAnalysisMetricsMapper;
import main.model.method.MethodMetrics;

public final class ClassAnalysisMetricsMapper {

	private ClassAnalysisMetricsMapper() {}

	public static ClassMetrics toClassMetrics(FileAnalysis analysis) {
		Objects.requireNonNull(analysis, "analysis must not be null");

		String className = "N/A";
		List<MethodMetrics> methodMetrics = new ArrayList<>();

		if (analysis.getClassAnalysis() != null) {
			className = analysis.getClassAnalysis().getClassName() != null ? analysis.getClassAnalysis().getClassName() : className;
			methodMetrics = MethodAnalysisMetricsMapper.toMethodMetrics(analysis.getClassAnalysis().getCurrentMethods());
					
			
		} else if (analysis.getFile() != null) {
			className = analysis.getFile().getName();
		}

		return ClassMetrics.builder()
				.name(className)
				.currentMethods(methodMetrics)
				.build();
	}
}