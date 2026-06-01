package test.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import main.builder.ClassAnalysis;
import main.builder.MethodAnalysis;
import test.objectmothers.AnalysisMother;
import test.objectmothers.MethodAnalysisMother;

class ClassAnalysisTest {

    @Test
    void given_defaults_when_build_should_haveDefaultValues() {
        final ClassAnalysis a = ClassAnalysis.builder().build();
        assertNull(a.getIcu());
        assertNull(a.getCompilationUnit());
        assertNull(a.getFile());
        assertNull(a.getClassName());
        assertNull(a.getCurrentSource());
        assertNull(a.getRefactoredSource());
        assertNull(a.getPath());
        assertEquals(15, a.getComplexityThreshold());
        assertNotNull(a.getAnalysisDate());
        assertTrue(a.getCurrentMethods().isEmpty());
        assertTrue(a.getRefactoredMethods().isEmpty());
    }

    @Test
    void given_allBuilderFields_when_build_should_returnConfiguredAnalysis() {
        final LocalDateTime date = LocalDateTime.of(2025, 1, 2, 3, 4);
        final MethodAnalysis m = MethodAnalysisMother.basic("foo");
        final ClassAnalysis a = ClassAnalysis.builder()
                .className("A.java")
                .analysisDate(date)
                .currentMethods(List.of(m))
                .refactoredMethods(List.of(m))
                .currentSource("class A {}")
                .refactoredSource("class A { void foo(){} }")
                .complexityThreshold(20)
                .path("/x/A.java")
                .build();
        assertEquals("A.java", a.getClassName());
        assertSame(date, a.getAnalysisDate());
        assertEquals(1, a.getCurrentMethods().size());
        assertEquals(1, a.getRefactoredMethods().size());
        assertEquals("class A {}", a.getCurrentSource());
        assertEquals("class A { void foo(){} }", a.getRefactoredSource());
        assertEquals(20, a.getComplexityThreshold());
        assertEquals("/x/A.java", a.getPath());
    }

    @Test
    void given_immutableMethods_when_modify_throws_unsupportedOperation() {
        final ClassAnalysis a = AnalysisMother.classAnalysis("A.java");
        assertThrows(UnsupportedOperationException.class, () -> a.getCurrentMethods().clear());
        assertThrows(UnsupportedOperationException.class, () -> a.getRefactoredMethods().clear());
        assertFalse(a.getCurrentMethods().isEmpty());
    }
}
