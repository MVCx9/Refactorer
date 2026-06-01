package test.model.clazz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import main.model.clazz.ClassMetrics;
import main.model.method.MethodMetrics;
import test.objectmothers.ClassMetricsMother;
import test.objectmothers.MethodMetricsMother;

class ClassMetricsTest {

    @Test
    void given_defaults_when_build_should_returnDefaults() {
        final ClassMetrics c = ClassMetrics.builder().build();
        assertEquals("<unnamed>", c.getName());
        assertEquals(0, c.getCurrentMethodCount());
        assertEquals(0, c.getRefactoredMethodCount());
        assertEquals(15, c.getComplexityThreshold());
        assertEquals(0, c.getCurrentLoc());
        assertEquals(0, c.getCurrentCc());
        assertEquals(0, c.getRefactoredLoc());
        assertEquals(0, c.getRefactoredCc());
        assertEquals(0, c.getAverageCurrentLoc());
        assertEquals(0, c.getAverageCurrentCc());
        assertEquals(0, c.getAverageRefactoredLoc());
        assertEquals(0, c.getAverageRefactoredCc());
    }

    @Test
    void given_methods_when_sumsAndAverages_should_aggregateCorrectly() {
        final MethodMetrics m1 = MethodMetrics.builder().name("a").cc(4).loc(10).build();
        final MethodMetrics m2 = MethodMetrics.builder().name("b").cc(6).loc(20).build();
        final ClassMetrics c = ClassMetrics.builder()
                .currentMethods(List.of(m1, m2))
                .refactoredMethods(List.of(m1))
                .build();
        assertEquals(30, c.getCurrentLoc());
        assertEquals(10, c.getCurrentCc());
        assertEquals(10, c.getRefactoredLoc());
        assertEquals(4, c.getRefactoredCc());
        assertEquals(15, c.getAverageCurrentLoc());
        assertEquals(5, c.getAverageCurrentCc());
        assertEquals(10, c.getAverageRefactoredLoc());
        assertEquals(4, c.getAverageRefactoredCc());
        assertEquals(6, c.getReducedComplexity());
        assertEquals(20, c.getReducedLoc());
        assertEquals(1, c.getAverageReducedComplexity());
        assertEquals(5, c.getAverageReducedLoc());
    }

    @Test
    void given_classWithoutExtractions_when_getMethodsWithRefactors_should_returnEmpty() {
        final ClassMetrics c = ClassMetricsMother.simple("A.java");
        assertTrue(c.getMethodsWithRefactors().isEmpty());
    }

    @Test
    void given_classWithExtractedMethods_when_getMethodsWithRefactors_should_returnTrimmedClass() {
        final ClassMetrics c = ClassMetricsMother.withRefactors("A.java");
        final List<ClassMetrics> result = c.getMethodsWithRefactors();
        assertEquals(1, result.size());
        final ClassMetrics trimmed = result.getFirst();
        assertEquals(1, trimmed.getCurrentMethods().size());
        assertEquals(2, trimmed.getRefactoredMethods().size());
        assertEquals("foo", trimmed.getCurrentMethods().getFirst().getName());
        assertTrue(trimmed.getRefactoredMethods().getFirst().getName().startsWith("foo_ext_"));
    }

    @Test
    void given_extractionCountGreaterButNoExtMethods_when_getMethodsWithRefactors_should_returnEmpty() {
        final ClassMetrics c = ClassMetrics.builder()
                .currentMethods(List.of(MethodMetricsMother.basic("a")))
                .refactoredMethods(List.of(MethodMetricsMother.basic("a"), MethodMetricsMother.basic("b")))
                .build();
        assertTrue(c.getMethodsWithRefactors().isEmpty());
    }

    @Test
    void given_classWithPath_when_getPath_should_returnPath() {
        final ClassMetrics c = ClassMetrics.builder().path("/x/y/A.java").build();
        assertEquals("/x/y/A.java", c.getPath());
    }

    @Test
    void given_equalClasses_when_equals_should_returnTrue() {
        final ClassMetrics a = ClassMetrics.builder().name("A.java")
                .analysisDate(ClassMetricsMother.FIXED_DATE).currentSource("class A {}")
                .refactoredSource("class A {}").complexityThreshold(15).path("/p/src/A.java").build();
        final ClassMetrics b = ClassMetrics.builder().name("A.java")
                .analysisDate(ClassMetricsMother.FIXED_DATE).currentSource("class A {}")
                .refactoredSource("class A {}").complexityThreshold(15).path("/p/src/A.java").build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertSame(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");
    }

    @Test
    void given_classMetrics_when_toString_should_containName() {
        final ClassMetrics c = ClassMetricsMother.simple("A.java");
        assertTrue(c.toString().contains("A.java"));
    }

    @Test
    void given_methodExtractionCount_when_computed_should_returnDifference() {
        final ClassMetrics c = ClassMetricsMother.withRefactors("A.java");
        assertEquals(2, c.getMethodExtractionCount());
    }

    @Test
    void given_immutableMethods_when_modifyReturned_throws_unsupportedOperation() {
        final ClassMetrics c = ClassMetricsMother.simple("A.java");
        assertThrows(UnsupportedOperationException.class, () -> c.getCurrentMethods().clear());
        assertThrows(UnsupportedOperationException.class, () -> c.getRefactoredMethods().clear());
    }
}
