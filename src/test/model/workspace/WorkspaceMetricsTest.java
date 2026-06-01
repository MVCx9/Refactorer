package test.model.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import main.model.project.ProjectMetrics;
import main.model.workspace.WorkspaceMetrics;
import test.objectmothers.ClassMetricsMother;
import test.objectmothers.WorkspaceMetricsMother;

class WorkspaceMetricsTest {

    @Test
    void given_defaults_when_build_should_returnDefaults() {
        final WorkspaceMetrics w = WorkspaceMetrics.builder().build();
        assertEquals("<unnamed>", w.getName());
        assertEquals(0, w.getProjectCount());
        assertEquals(0, w.getClassCount());
        assertEquals(0, w.getCurrentLoc());
        assertEquals(0, w.getRefactoredLoc());
        assertEquals(0, w.getCurrentCc());
    }

    @Test
    void given_workspaceWithProjects_when_aggregations_should_returnExpectedValues() {
        final WorkspaceMetrics w = WorkspaceMetricsMother.withRefactors();
        assertEquals(2, w.getProjectCount());
        assertTrue(w.getClassCount() > 0);
        assertEquals(w.getCurrentCc() - w.getRefactoredCc(), w.getReducedComplexity());
        assertEquals(w.getCurrentLoc() - w.getRefactoredLoc(), w.getReducedLoc());
        assertEquals(w.getRefactoredMethodCount() - w.getCurrentMethodCount(), w.getMethodExtractionCount());
        assertEquals(w.getAverageCurrentCc() - w.getAverageRefactoredCc(), w.getAverageReducedComplexity());
        assertEquals(w.getAverageCurrentLoc() - w.getAverageRefactoredLoc(), w.getAverageReducedLoc());
    }

    @Test
    void given_workspaceWithRefactors_when_getProjectsWithRefactors_should_filterProjects() {
        final WorkspaceMetrics w = WorkspaceMetricsMother.withRefactors();
        final List<ProjectMetrics> trimmed = w.getProjectsWithRefactors();
        assertEquals(1, trimmed.size());
        assertEquals("P1", trimmed.getFirst().getName());
    }

    @Test
    void given_workspaceWithoutRefactors_when_getProjectsWithRefactors_should_returnEmpty() {
        assertTrue(WorkspaceMetricsMother.simple().getProjectsWithRefactors().isEmpty());
    }

    @Test
    void given_equalWorkspaces_when_equals_should_returnTrue() {
        final WorkspaceMetrics a = WorkspaceMetrics.builder()
                .name("WS").analysisDate(ClassMetricsMother.FIXED_DATE).build();
        final WorkspaceMetrics b = WorkspaceMetrics.builder()
                .name("WS").analysisDate(ClassMetricsMother.FIXED_DATE).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, null);
        assertNotEquals(a, "x");
    }

    @Test
    void given_workspaceMetrics_when_toString_should_containName() {
        assertTrue(WorkspaceMetricsMother.simple().toString().contains("WS"));
    }
}
