package test.model.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import main.builder.WorkspaceAnalysis;
import main.model.workspace.WorkspaceAnalysisMetricsMapper;
import main.model.workspace.WorkspaceMetrics;
import test.objectmothers.AnalysisMother;

class WorkspaceAnalysisMetricsMapperTest {

    @Test
    void given_nullAnalysis_when_toWorkspaceMetrics_throws_nullPointerException() {
        assertThrows(NullPointerException.class, () -> WorkspaceAnalysisMetricsMapper.toWorkspaceMetrics(null));
    }

    @Test
    void given_workspaceAnalysis_when_toWorkspaceMetrics_should_mapAllFields() {
        final WorkspaceAnalysis analysis = AnalysisMother.workspaceAnalysis();
        final WorkspaceMetrics metrics = WorkspaceAnalysisMetricsMapper.toWorkspaceMetrics(analysis);
        assertEquals("WS", metrics.getName());
        assertEquals(1, metrics.getProjectCount());
        assertEquals("P1", metrics.getProjects().getFirst().getName());
    }
}
