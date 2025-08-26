package main.model.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import main.builder.FileAnalysis;
import main.builder.ProjectAnalysis;
import main.model.clazz.ClassAnalysisMetricsMapper;
import main.model.clazz.ClassMetrics;

public final class ProjectAnalysisMetricsMapper {

	private ProjectAnalysisMetricsMapper() {
	}

	public static ProjectMetrics toProjectMetrics(ProjectAnalysis analysis) {
		Objects.requireNonNull(analysis, "analysis must not be null");

		List<ClassMetrics> classMetrics = new ArrayList<>();
		for (FileAnalysis fa : analysis.getFiles()) {
			classMetrics.add(ClassAnalysisMetricsMapper.toClassMetrics(fa));
		}

		return ProjectMetrics.builder()
			.name(analysis.getName())
			.classes(classMetrics)
			.build();
	}
}
