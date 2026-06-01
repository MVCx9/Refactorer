package test.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import main.model.clazz.ClassMetrics;
import main.model.project.ProjectMetrics;
import test.objectmothers.ClassMetricsMother;
import test.objectmothers.ProjectMetricsMother;

class ProjectMetricsTest {

    @Test
    void given_defaults_when_build_should_returnDefaults() {
        final ProjectMetrics p = ProjectMetrics.builder().build();
        assertEquals("<unnamed>", p.getName());
        assertEquals(0, p.getClassCount());
        assertEquals(0, p.getCurrentLoc());
        assertEquals(0, p.getCurrentCc());
        assertEquals(0, p.getRefactoredLoc());
        assertEquals(0, p.getRefactoredCc());
        assertEquals(15, p.getComplexityThreshold());
    }

    @Test
    void given_classes_when_aggregations_should_sumAndAverage() {
        final ProjectMetrics p = ProjectMetricsMother.withRefactors("P");
        assertEquals(2, p.getClassCount());
        assertTrue(p.getCurrentMethodCount() >= 1);
        assertTrue(p.getRefactoredMethodCount() >= 1);
        assertEquals(p.getCurrentCc() - p.getRefactoredCc(), p.getReducedComplexity());
        assertEquals(p.getCurrentLoc() - p.getRefactoredLoc(), p.getReducedLoc());
        assertEquals(p.getRefactoredMethodCount() - p.getCurrentMethodCount(), p.getMethodExtractionCount());
        assertEquals(p.getAverageCurrentCc() - p.getAverageRefactoredCc(), p.getAverageReducedComplexity());
        assertEquals(p.getAverageCurrentLoc() - p.getAverageRefactoredLoc(), p.getAverageReducedLoc());
    }

    @Test
    void given_projectWithRefactors_when_getMethodsWithRefactors_should_returnOnlyTrimmedClasses() {
        final ProjectMetrics p = ProjectMetricsMother.withRefactors("P");
        final List<ClassMetrics> trimmed = p.getMethodsWithRefactors();
        assertEquals(1, trimmed.size());
        assertEquals("A.java", trimmed.getFirst().getName());
    }

    @Test
    void given_projectWithoutRefactors_when_getMethodsWithRefactors_should_returnEmpty() {
        final ProjectMetrics p = ProjectMetricsMother.simple("P");
        assertTrue(p.getMethodsWithRefactors().isEmpty());
    }

    @Test
    void given_immutableClasses_when_modify_throws_unsupportedOperation() {
        final ProjectMetrics p = ProjectMetricsMother.simple("P");
        assertThrows(UnsupportedOperationException.class, () -> p.getClasses().clear());
    }

    @Test
    void given_twoEqualProjects_when_equals_should_returnTrue() {
        final ProjectMetrics a = ProjectMetrics.builder().name("P")
                .analysisDate(ClassMetricsMother.FIXED_DATE).build();
        final ProjectMetrics b = ProjectMetrics.builder().name("P")
                .analysisDate(ClassMetricsMother.FIXED_DATE).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, null);
        assertNotEquals(a, "x");
    }

    @Test
    void given_projectMetrics_when_toString_should_containName() {
        assertTrue(ProjectMetricsMother.simple("P").toString().contains("P"));
    }
}
