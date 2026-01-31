package test.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.builder.ProjectAnalysis;
import main.builder.WorkspaceAnalysis;
import test.model.project.mother.ProjectAnalysisMother;

class WorkspaceAnalysisTest {
	private final LocalDateTime fixedDate = LocalDateTime.of(2024, 6, 15, 10, 30);

	private void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("given_builderWithDefaults_when_build_should_createWorkspaceAnalysisWithDefaultValues")
	void given_builderWithDefaults_when_build_should_createWorkspaceAnalysisWithDefaultValues() {
		final WorkspaceAnalysis analysis = WorkspaceAnalysis.builder().build();
		assertNotNull(analysis);
		toStrictEqual("<unnamed>", analysis.getName());
		assertNotNull(analysis.getAnalysisDate());
		assertNotNull(analysis.getProjects());
		toStrictEqual(0, analysis.getProjects().size());
	}

	@Test
	@DisplayName("given_builderWithAllValues_when_build_should_createWorkspaceAnalysisWithAllValues")
	void given_builderWithAllValues_when_build_should_createWorkspaceAnalysisWithAllValues() {
		final List<ProjectAnalysis> projects = List.of(ProjectAnalysisMother.defaultAnalysis());
		final WorkspaceAnalysis analysis = WorkspaceAnalysis.builder()
				.name("TestWorkspace")
				.analysisDate(this.fixedDate)
				.projects(projects)
				.build();
		toStrictEqual("TestWorkspace", analysis.getName());
		toStrictEqual(this.fixedDate, analysis.getAnalysisDate());
		toStrictEqual(1, analysis.getProjects().size());
	}

	@Test
	@DisplayName("given_workspaceAnalysis_when_getProjects_should_returnUnmodifiableList")
	void given_workspaceAnalysis_when_getProjects_should_returnUnmodifiableList() {
		final WorkspaceAnalysis analysis = WorkspaceAnalysis.builder()
				.projects(List.of(ProjectAnalysisMother.defaultAnalysis()))
				.build();
		assertThrows(UnsupportedOperationException.class, () -> analysis.getProjects().add(null));
	}

	@Test
	@DisplayName("given_emptyProjects_when_build_should_createEmptyList")
	void given_emptyProjects_when_build_should_createEmptyList() {
		final WorkspaceAnalysis analysis = WorkspaceAnalysis.builder()
				.projects(Collections.emptyList())
				.build();
		toStrictEqual(0, analysis.getProjects().size());
	}

	@Test
	@DisplayName("given_customName_when_getName_should_returnCustomName")
	void given_customName_when_getName_should_returnCustomName() {
		final WorkspaceAnalysis analysis = WorkspaceAnalysis.builder()
				.name("CustomWorkspace")
				.build();
		toStrictEqual("CustomWorkspace", analysis.getName());
	}

	@Test
	@DisplayName("given_multipleProjects_when_getProjects_should_returnAllProjects")
	void given_multipleProjects_when_getProjects_should_returnAllProjects() {
		final List<ProjectAnalysis> projects = List.of(
				ProjectAnalysisMother.withName("Project1"),
				ProjectAnalysisMother.withName("Project2"));
		final WorkspaceAnalysis analysis = WorkspaceAnalysis.builder()
				.projects(projects)
				.build();
		toStrictEqual(2, analysis.getProjects().size());
	}

	@Test
	@DisplayName("given_workspaceAnalysis_when_getAnalysisDate_should_returnSetDate")
	void given_workspaceAnalysis_when_getAnalysisDate_should_returnSetDate() {
		final WorkspaceAnalysis analysis = WorkspaceAnalysis.builder()
				.analysisDate(this.fixedDate)
				.build();
		toStrictEqual(this.fixedDate, analysis.getAnalysisDate());
	}
}
