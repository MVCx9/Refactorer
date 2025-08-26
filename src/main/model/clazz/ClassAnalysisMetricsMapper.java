package main.model.clazz;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import main.builder.FileAnalysis;
import main.builder.MethodAnalysis;
import main.model.method.MethodAnalysisMetricsMapper;
import main.model.method.MethodMetrics;

public final class ClassAnalysisMetricsMapper {

	private ClassAnalysisMetricsMapper() {}

	public static ClassMetrics toClassMetrics(FileAnalysis analysis) {
		Objects.requireNonNull(analysis, "analysis must not be null");

		String className = "N/A";
		List<MethodMetrics> methodMetrics = new ArrayList<>();
		int totalCurrentLoc = 0;
		int totalRefactoredLoc = 0;

		if (analysis.getClassAnalysis() != null) {
			className = analysis.getClassAnalysis().getClassName() != null ? analysis.getClassAnalysis().getClassName() : className;
			for (MethodAnalysis ma : analysis.getClassAnalysis().getMethods()) {
				MethodMetrics mm = MethodAnalysisMetricsMapper.toMethodMetrics(ma);
				methodMetrics.add(mm);
				totalCurrentLoc += mm.getCurrentLoc();
				totalRefactoredLoc += mm.getRefactoredLoc();
			}
		} else if (analysis.getFile() != null) {
			className = analysis.getFile().getName();
		}

		return ClassMetrics.builder()
				.name(className)
				.currentLoc(totalCurrentLoc)
				.refactoredLoc(totalRefactoredLoc)
				.methods(methodMetrics)
				.build();
	}
}