package test.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import main.builder.ProjectAnalysis;
import main.model.project.ProjectAnalysisMetricsMapper;
import main.model.project.ProjectMetrics;
import test.objectmothers.AnalysisMother;

class ProjectAnalysisMetricsMapperTest {

    @Test
    void given_nullAnalysis_when_toProjectMetrics_throws_nullPointerException() {
        assertThrows(NullPointerException.class, () -> ProjectAnalysisMetricsMapper.toProjectMetrics(null));
    }

    @Test
    void given_projectAnalysis_when_toProjectMetrics_should_mapAndDeriveThresholdFromFirstClass() {
        final ProjectAnalysis analysis = AnalysisMother.projectAnalysis("P");
        final ProjectMetrics metrics = ProjectAnalysisMetricsMapper.toProjectMetrics(analysis);
        assertEquals("P", metrics.getName());
        assertEquals(1, metrics.getClassCount());
        assertEquals(12, metrics.getComplexityThreshold());
    }

    @Test
    void given_emptyProject_when_toProjectMetrics_should_fallbackToDefaultThreshold() {
        final ProjectAnalysis analysis = ProjectAnalysis.builder().name("P").build();
        final ProjectMetrics metrics = ProjectAnalysisMetricsMapper.toProjectMetrics(analysis);
        assertEquals(15, metrics.getComplexityThreshold());
        assertEquals(0, metrics.getClassCount());
    }
}
