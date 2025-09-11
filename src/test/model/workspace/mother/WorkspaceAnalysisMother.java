package test.model.workspace.mother;

import java.time.LocalDateTime;
import java.util.List;

import main.builder.WorkspaceAnalysis;
import main.builder.ProjectAnalysis;
import test.model.project.mother.ProjectAnalysisMother;

public final class WorkspaceAnalysisMother {
	private static final String DEFAULT_NAME = "SampleWorkspace";

	private WorkspaceAnalysisMother() {}

	public static WorkspaceAnalysis defaultAnalysis() {
		final ProjectAnalysis p1 = ProjectAnalysisMother.withName("ProjectOne");
		final ProjectAnalysis p2 = ProjectAnalysisMother.withName("ProjectTwo");
		final List<ProjectAnalysis> projects = List.of(p1, p2);
		return WorkspaceAnalysis.builder()
				.name(DEFAULT_NAME)
				.analysisDate(LocalDateTime.now())
				.projects(projects)
				.build();
	}

	public static WorkspaceAnalysis withName(String name) {
		final String n = name == null ? DEFAULT_NAME : name;
		final ProjectAnalysis p1 = ProjectAnalysisMother.withName("ProjectOne");
		final ProjectAnalysis p2 = ProjectAnalysisMother.withName("ProjectTwo");
		final List<ProjectAnalysis> projects = List.of(p1, p2);
		return WorkspaceAnalysis.builder()
				.name(n)
				.analysisDate(LocalDateTime.now())
				.projects(projects)
				.build();
	}

	public static WorkspaceAnalysis custom(String name, LocalDateTime analysisDate, List<ProjectAnalysis> projects) {
		final String n = name == null ? DEFAULT_NAME : name;
		final LocalDateTime date = analysisDate == null ? LocalDateTime.now() : analysisDate;
		final List<ProjectAnalysis> projs = projects == null ? List.of() : projects;
		return WorkspaceAnalysis.builder()
				.name(n)
				.analysisDate(date)
				.projects(projs)
				.build();
	}
}