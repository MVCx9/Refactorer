package test.model.method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import main.builder.MethodAnalysis;
import main.model.method.MethodAnalysisMetricsMapper;
import main.model.method.MethodMetrics;
import main.refactor.RefactorComparison;
import test.objectmothers.MethodAnalysisMother;
import test.objectmothers.RefactorComparisonMother;

class MethodAnalysisMetricsMapperTest {

    @Test
    void given_analysisList_when_toMethodMetrics_should_mapEveryField() {
        final MethodAnalysis ma = MethodAnalysisMother.refactored("foo", 3, 2);
        final List<MethodMetrics> result = MethodAnalysisMetricsMapper.toMethodMetrics(List.of(ma));
        final MethodMetrics m = result.getFirst();
        assertEquals("foo", m.getName());
        assertEquals(2, m.getCc());
        assertEquals(5, m.getLoc());
        assertTrue(m.isUsedILP());
    }

    @Test
    void given_emptyAnalysisList_when_toMethodMetrics_should_returnEmpty() {
        assertTrue(MethodAnalysisMetricsMapper.toMethodMetrics(List.of()).isEmpty());
    }

    @Test
    void given_comparisons_when_toMethodAnalysis_should_mapAndZeroBaseMetrics() {
        final RefactorComparison c = RefactorComparisonMother.basic("foo");
        final List<MethodAnalysis> result = MethodAnalysisMetricsMapper.toMethodAnalysis(List.of(c));
        final MethodAnalysis ma = result.getFirst();
        assertEquals("foo", ma.getMethodName());
        assertEquals(0, ma.getCc());
        assertEquals(0, ma.getLoc());
        assertEquals(4, ma.getReducedComplexity());
        assertEquals(1, ma.getNumberOfExtractions());
        assertTrue(ma.isUsedILP());
        assertNull(ma.getCompilationUnitRefactored());
    }

    @Test
    void given_emptyComparisonList_when_toMethodAnalysis_should_returnEmpty() {
        assertTrue(MethodAnalysisMetricsMapper.toMethodAnalysis(List.<RefactorComparison>of()).isEmpty());
    }
}
