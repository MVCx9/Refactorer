package test.model.workspace;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static test.util.AssertionsStrict.toStrictEqual;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import main.builder.ClassAnalysis;
import main.builder.MethodAnalysis;
import main.builder.ProjectAnalysis;
import main.builder.WorkspaceAnalysis;
import main.model.project.ProjectMetrics;
import main.model.project.ProjectAnalysisMetricsMapper;
import main.model.workspace.WorkspaceAnalysisMetricsMapper;
import main.model.workspace.WorkspaceMetrics;
import test.mother.ClassAnalysisMother;
import test.mother.MethodAnalysisMother;
import test.mother.ProjectAnalysisMother;
import test.mother.WorkspaceAnalysisMother;

class WorkspaceAnalysisMetricsMapperTest {

	@Test
	@DisplayName("given_nullAnalysis_when_toWorkspaceMetrics_throws_NullPointerException")
	void given_nullAnalysis_when_toWorkspaceMetrics_throws_NullPointerException() {
		assertThrows(NullPointerException.class, () -> WorkspaceAnalysisMetricsMapper.toWorkspaceMetrics(null));
	}

	@Test
	@DisplayName("given_workspaceWithSingleProject_when_toWorkspaceMetrics_should_mapAllFields")
	void given_workspaceWithSingleProject_when_toWorkspaceMetrics_should_mapAllFields() {
		final WorkspaceAnalysis analysis = WorkspaceAnalysisMother.base().build();
		final WorkspaceMetrics metrics = WorkspaceAnalysisMetricsMapper.toWorkspaceMetrics(analysis);
		assertAll(
			() -> assertNotNull(metrics),
			() -> toStrictEqual(analysis.getName(), metrics.getName()),
			() -> toStrictEqual(analysis.getAnalysisDate(), metrics.getAnalysisDate()),
			() -> toStrictEqual(1, metrics.getProjects().size())
		);
	}

	@Test
	@DisplayName("given_workspaceWithMultipleProjects_when_toWorkspaceMetrics_should_aggregateProjectMetrics")
	void given_workspaceWithMultipleProjects_when_toWorkspaceMetrics_should_aggregateProjectMetrics() {
		final MethodAnalysis m1 = MethodAnalysisMother.with("m1",10,2,6,1);
		final MethodAnalysis m2 = MethodAnalysisMother.with("m2",30,6,20,3);
		final ClassAnalysis c1 = ClassAnalysisMother.base().className("C1").currentMethods(List.of(m1)).refactoredMethods(List.of(m1)).build();
		final ClassAnalysis c2 = ClassAnalysisMother.base().className("C2").currentMethods(List.of(m2)).refactoredMethods(List.of(m2)).build();
		final ProjectAnalysis p1 = ProjectAnalysisMother.base().name("P1").classes(List.of(c1)).build();
		final ProjectAnalysis p2 = ProjectAnalysisMother.base().name("P2").classes(List.of(c2)).build();
		final LocalDateTime date = LocalDateTime.now();
		final WorkspaceAnalysis wa = WorkspaceAnalysisMother.withProjects(List.of(p1, p2)).analysisDate(date).name("Workspace").build();
		final WorkspaceMetrics wm = WorkspaceAnalysisMetricsMapper.toWorkspaceMetrics(wa);
		final ProjectMetrics pm1 = wm.getProjects().get(0);
		final ProjectMetrics pm2 = wm.getProjects().get(1);
		assertAll(
			() -> toStrictEqual("Workspace", wm.getName()),
			() -> toStrictEqual(date, wm.getAnalysisDate()),
			() -> toStrictEqual(2, wm.getProjects().size()),
			() -> toStrictEqual("P1", pm1.getName()),
			() -> toStrictEqual("P2", pm2.getName()),
			() -> toStrictEqual(40, wm.getCurrentLoc()),
			() -> toStrictEqual(26, wm.getRefactoredLoc()),
			() -> toStrictEqual(8, wm.getCurrentCc()),
			() -> toStrictEqual(4, wm.getRefactoredCc())
		);
	}

	@Test
	@DisplayName("given_analysis_when_toWorkspaceMetrics_should_delegateProjectMapping")
	void given_analysis_when_toWorkspaceMetrics_should_delegateProjectMapping() {
		final ProjectAnalysis project = ProjectAnalysisMother.base().build();
		final WorkspaceAnalysis wa = WorkspaceAnalysisMother.withProjects(List.of(project)).name("W1").build();
		try (var mocked = mockStatic(ProjectAnalysisMetricsMapper.class)) {
			final ProjectMetrics stubMetrics = ProjectMetrics.builder().name("PStub").classes(List.of()).build();
			when(ProjectAnalysisMetricsMapper.toProjectMetrics(project)).thenReturn(stubMetrics);
			final WorkspaceMetrics wm = WorkspaceAnalysisMetricsMapper.toWorkspaceMetrics(wa);
			assertAll(
				() -> toStrictEqual("W1", wm.getName()),
				() -> toStrictEqual(1, wm.getProjects().size()),
				() -> toStrictEqual("PStub", wm.getProjects().get(0).getName())
			);
			mocked.verify(() -> ProjectAnalysisMetricsMapper.toProjectMetrics(project), times(1));
		}
	}

	@Nested
	class EdgeCases {
		@Test
		@DisplayName("given_workspaceWithNoProjects_when_toWorkspaceMetrics_should_returnEmptyMetrics")
		void given_workspaceWithNoProjects_when_toWorkspaceMetrics_should_returnEmptyMetrics() {
			final WorkspaceAnalysis wa = WorkspaceAnalysis.builder().name("Empty").projects(List.of()).build();
			final WorkspaceMetrics wm = WorkspaceAnalysisMetricsMapper.toWorkspaceMetrics(wa);
			assertAll(
				() -> toStrictEqual("Empty", wm.getName()),
				() -> toStrictEqual(0, wm.getProjects().size()),
				() -> toStrictEqual(0, wm.getCurrentLoc()),
				() -> toStrictEqual(0, wm.getRefactoredLoc()),
				() -> toStrictEqual(0, wm.getCurrentCc()),
				() -> toStrictEqual(0, wm.getRefactoredCc())
			);
		}
	}
}

