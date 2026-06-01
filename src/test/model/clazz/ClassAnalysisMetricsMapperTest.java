package test.model.clazz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import main.builder.ClassAnalysis;
import main.model.clazz.ClassAnalysisMetricsMapper;
import main.model.clazz.ClassMetrics;
import test.objectmothers.AnalysisMother;

class ClassAnalysisMetricsMapperTest {

    @Test
    void given_nullAnalysis_when_toClassMetrics_throws_nullPointerException() {
        assertThrows(NullPointerException.class, () -> ClassAnalysisMetricsMapper.toClassMetrics(null));
    }

    @Test
    void given_analysis_when_toClassMetrics_should_mapAllFields() {
        final ClassAnalysis analysis = AnalysisMother.classAnalysis("A.java");
        final ClassMetrics metrics = ClassAnalysisMetricsMapper.toClassMetrics(analysis);
        assertEquals("A.java", metrics.getName());
        assertEquals(12, metrics.getComplexityThreshold());
        assertEquals("/p/src/A.java", metrics.getPath());
        assertEquals(1, metrics.getCurrentMethods().size());
        assertEquals(1, metrics.getRefactoredMethods().size());
        assertEquals("class A {}", metrics.getCurrentSource());
        assertNotNull(metrics.getAnalysisDate());
    }

    @Test
    void given_analysisWithNullClassName_when_toClassMetrics_should_fallbackToNA() {
        final ClassAnalysis analysis = ClassAnalysis.builder().build();
        final ClassMetrics metrics = ClassAnalysisMetricsMapper.toClassMetrics(analysis);
        assertEquals("N/A", metrics.getName());
    }
}
