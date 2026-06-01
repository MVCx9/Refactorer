package test.builder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import main.builder.ProjectFilesAnalyzer;

class ProjectFilesAnalyzerTest {

    @Test
    void given_newInstance_when_construct_should_notBeNull() {
        assertNotNull(new ProjectFilesAnalyzer());
    }

    @Test
    void given_nullFile_when_analyzeFile_throws_nullPointerException() {
        assertThrows(NullPointerException.class, () -> new ProjectFilesAnalyzer().analyzeFile(null));
    }

    @Test
    void given_nullProject_when_analyzeProject_throws_nullPointerException() {
        assertThrows(NullPointerException.class, () -> new ProjectFilesAnalyzer().analyzeProject(null));
    }
}
