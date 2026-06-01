package test.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import main.builder.ClassAnalysis;
import main.builder.ProjectAnalysis;

class ProjectAnalysisTest {

    @Test
    void given_defaults_when_build_should_returnDefaults() {
        final ProjectAnalysis a = ProjectAnalysis.builder().build();
        assertNull(a.getProject());
        assertEquals("<unnamed>", a.getName());
        assertNotNull(a.getAnalysisDate());
        assertTrue(a.getFiles().isEmpty());
        assertEquals(15, a.getComplexityThreshold());
    }

    @Test
    void given_allFields_when_build_should_returnConfigured() {
        final LocalDateTime d = LocalDateTime.of(2025, 5, 1, 0, 0);
        final ClassAnalysis ca = ClassAnalysis.builder().className("X.java").build();
        final ProjectAnalysis a = ProjectAnalysis.builder()
                .name("P").analysisDate(d).classes(List.of(ca)).complexityThreshold(7).build();
        assertEquals("P", a.getName());
        assertSame(d, a.getAnalysisDate());
        assertEquals(1, a.getFiles().size());
        assertEquals(7, a.getComplexityThreshold());
    }

    @Test
    void given_files_when_modify_throws_unsupportedOperation() {
        final ProjectAnalysis a = ProjectAnalysis.builder()
                .classes(List.of(ClassAnalysis.builder().build())).build();
        assertThrows(UnsupportedOperationException.class, () -> a.getFiles().clear());
    }
}
