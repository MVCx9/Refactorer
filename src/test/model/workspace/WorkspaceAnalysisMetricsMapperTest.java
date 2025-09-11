package test.model.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.builder.ProjectAnalysis;
import main.builder.WorkspaceAnalysis;
import main.model.workspace.WorkspaceAnalysisMetricsMapper;
import main.model.workspace.WorkspaceMetrics;
import test.model.project.mother.ProjectAnalysisMother;
import test.model.workspace.mother.WorkspaceAnalysisMother;

class WorkspaceAnalysisMetricsMapperTest {
	private static void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("given_validWorkspaceAnalysis_when_toWorkspaceMetrics_should_mapAllProjects")
	void given_validWorkspaceAnalysis_when_toWorkspaceMetrics_should_mapAllProjects() {
		final WorkspaceAnalysis analysis = WorkspaceAnalysisMother.defaultAnalysis();
		final WorkspaceMetrics metrics = WorkspaceAnalysisMetricsMapper.toWorkspaceMetrics(analysis);
		assertNotNull(metrics);
		toStrictEqual(analysis.getName(), metrics.getName());
		toStrictEqual(analysis.getAnalysisDate(), metrics.getAnalysisDate());
		toStrictEqual(analysis.getProjects().size(), metrics.getProjects().size());
		for (int i = 0; i < analysis.getProjects().size(); i++) {
			toStrictEqual(analysis.getProjects().get(i).getName(), metrics.getProjects().get(i).getName());
		}
	}

	@Test
	@DisplayName("given_workspaceAnalysisWithEmptyProjects_when_toWorkspaceMetrics_should_returnWorkspaceMetricsWithZeroProjects")
	void given_workspaceAnalysisWithEmptyProjects_when_toWorkspaceMetrics_should_returnWorkspaceMetricsWithZeroProjects() {
		final LocalDateTime date = LocalDateTime.of(2024, 1, 1, 10, 0);
		final WorkspaceAnalysis analysis = WorkspaceAnalysis.builder()
				.name("EmptyWS")
				.analysisDate(date)
				.projects(new ArrayList<>())
				.build();
		final WorkspaceMetrics metrics = WorkspaceAnalysisMetricsMapper.toWorkspaceMetrics(analysis);
		toStrictEqual("EmptyWS", metrics.getName());
		toStrictEqual(date, metrics.getAnalysisDate());
		toStrictEqual(0, metrics.getProjects().size());
	}

	@Test
	@DisplayName("given_nullAnalysis_when_toWorkspaceMetrics_throws_NullPointerException")
	void given_nullAnalysis_when_toWorkspaceMetrics_throws_NullPointerException() {
		assertThrows(NullPointerException.class, () -> WorkspaceAnalysisMetricsMapper.toWorkspaceMetrics(null));
	}
}