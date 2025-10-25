package main.model.project;

import java.util.List;
import java.util.Objects;

import main.builder.ProjectAnalysis;
import main.model.clazz.ClassAnalysisMetricsMapper;
import main.model.clazz.ClassMetrics;

public final class ProjectAnalysisMetricsMapper {

	private ProjectAnalysisMetricsMapper() {
	}

	public static ProjectMetrics toProjectMetrics(ProjectAnalysis analysis) {
		Objects.requireNonNull(analysis, "analysis must not be null");

		List<ClassMetrics> classMetrics = analysis.getFiles().stream()
			.map(ClassAnalysisMetricsMapper::toClassMetrics)
			.toList();

		// Obtener el umbral de complejidad de la primera clase (todas deben tener el mismo valor)
		int complexityThreshold = classMetrics.stream()
			.findFirst()
			.map(ClassMetrics::getThreshold)
			.orElse(15); // Valor por defecto si no hay clases

		return ProjectMetrics.builder()
			.name(analysis.getName())
			.analysisDate(analysis.getAnalysisDate())
			.classes(classMetrics)
			.complexityThreshold(complexityThreshold)
			.build();
	}
}