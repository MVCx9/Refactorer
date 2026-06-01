package test.refactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import main.refactor.RefactorComparison;

class RefactorComparisonTest {

    @Test
    void given_defaults_when_build_should_returnDefaults() {
        final RefactorComparison r = RefactorComparison.builder().build();
        assertNull(r.getName());
        assertNull(r.getCompilationUnitRefactored());
        assertNull(r.getRefactoredSource());
        assertEquals(0, r.getReducedComplexity());
        assertEquals(0, r.getNumberOfExtractions());
        assertNull(r.getStats());
        assertFalse(r.isUsedILP());
    }

    @Test
    void given_allFields_when_build_should_returnConfigured() {
        final RefactorComparison r = RefactorComparison.builder()
                .name("foo")
                .reducedComplexity(4)
                .numberOfExtractions(2)
                .refactoredSource("src")
                .usedILP(true)
                .build();
        assertEquals("foo", r.getName());
        assertEquals(4, r.getReducedComplexity());
        assertEquals(2, r.getNumberOfExtractions());
        assertEquals("src", r.getRefactoredSource());
        assertTrue(r.isUsedILP());
    }
}
