package test.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import main.builder.MethodAnalysis;

class MethodAnalysisTest {

    @Test
    void given_defaults_when_build_should_returnDefaults() {
        final MethodAnalysis ma = MethodAnalysis.builder().build();
        assertNull(ma.getMethodName());
        assertEquals(0, ma.getCc());
        assertEquals(0, ma.getLoc());
        assertEquals(0, ma.getReducedComplexity());
        assertEquals(0, ma.getNumberOfExtractions());
        assertNull(ma.getCompilationUnitRefactored());
        assertNull(ma.getRefactoredSource());
        assertNull(ma.getStats());
        assertFalse(ma.isUsedILP());
    }

    @Test
    void given_allFields_when_build_should_returnConfigured() {
        final MethodAnalysis ma = MethodAnalysis.builder()
                .methodName("foo").cc(5).loc(8)
                .reducedComplexity(2).numberOfExtractions(1)
                .refactoredSource("src").usedILP(true).build();
        assertEquals("foo", ma.getMethodName());
        assertEquals(5, ma.getCc());
        assertEquals(8, ma.getLoc());
        assertEquals(2, ma.getReducedComplexity());
        assertEquals(1, ma.getNumberOfExtractions());
        assertEquals("src", ma.getRefactoredSource());
        assertTrue(ma.isUsedILP());
    }
}
