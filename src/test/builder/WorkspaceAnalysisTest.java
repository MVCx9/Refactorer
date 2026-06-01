package test.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import main.builder.ProjectAnalysis;
import main.builder.WorkspaceAnalysis;

class WorkspaceAnalysisTest {

    @Test
    void given_defaults_when_build_should_returnDefaults() {
        final WorkspaceAnalysis a = WorkspaceAnalysis.builder().build();
        assertEquals("<unnamed>", a.getName());
        assertNotNull(a.getAnalysisDate());
        assertTrue(a.getProjects().isEmpty());
    }

    @Test
    void given_allFields_when_build_should_returnConfigured() {
        final LocalDateTime d = LocalDateTime.of(2025, 5, 1, 0, 0);
        final ProjectAnalysis pa = ProjectAnalysis.builder().name("P").build();
        final WorkspaceAnalysis a = WorkspaceAnalysis.builder()
                .name("WS").analysisDate(d).projects(List.of(pa)).build();
        assertEquals("WS", a.getName());
        assertSame(d, a.getAnalysisDate());
        assertEquals(1, a.getProjects().size());
    }

    @Test
    void given_projects_when_modify_throws_unsupportedOperation() {
        final WorkspaceAnalysis a = WorkspaceAnalysis.builder()
                .projects(List.of(ProjectAnalysis.builder().build())).build();
        assertThrows(UnsupportedOperationException.class, () -> a.getProjects().clear());
    }
}
