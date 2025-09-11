package test.model.workspace.mother;

import java.time.LocalDateTime;
import java.util.List;

import main.model.workspace.WorkspaceMetrics;
import main.model.project.ProjectMetrics;
import test.model.project.mother.ProjectMetricsMother;

public final class WorkspaceMetricsMother {
	private static final String DEFAULT_NAME = "SampleWorkspace";

	private WorkspaceMetricsMother() {}

	public static WorkspaceMetrics defaultMetrics() {
		final ProjectMetrics p1 = ProjectMetricsMother.withName("ProjectOne");
		final ProjectMetrics p2 = ProjectMetricsMother.withName("ProjectTwo");
		final List<ProjectMetrics> projects = List.of(p1, p2);
		return WorkspaceMetrics.builder()
				.name(DEFAULT_NAME)
				.analysisDate(LocalDateTime.now())
				.projects(projects)
				.build();
	}

	public static WorkspaceMetrics withName(String name) {
		final String n = name == null ? DEFAULT_NAME : name;
		final ProjectMetrics p1 = ProjectMetricsMother.withName("ProjectOne");
		final ProjectMetrics p2 = ProjectMetricsMother.withName("ProjectTwo");
		final List<ProjectMetrics> projects = List.of(p1, p2);
		return WorkspaceMetrics.builder()
				.name(n)
				.analysisDate(LocalDateTime.now())
				.projects(projects)
				.build();
	}

	public static WorkspaceMetrics custom(String name, LocalDateTime analysisDate, List<ProjectMetrics> projects) {
		final String n = name == null ? DEFAULT_NAME : name;
		final LocalDateTime date = analysisDate == null ? LocalDateTime.now() : analysisDate;
		final List<ProjectMetrics> projs = projects == null ? List.of() : projects;
		return WorkspaceMetrics.builder()
				.name(n)
				.analysisDate(date)
				.projects(projs)
				.build();
	}
}