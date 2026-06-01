package test.handler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import main.handler.AnalyzeProjectHandler;
import main.handler.AnalyzeSingleFileHandler;
import main.handler.AnalyzeWorkspaceHandler;
import main.handler.ConfigurationHandler;
import main.handler.GetAnalysisHistory;

class HandlersInstantiationTest {

    @Test
    void given_analyzeProjectHandler_when_construct_should_notBeNull() {
        assertNotNull(new AnalyzeProjectHandler());
    }

    @Test
    void given_analyzeSingleFileHandler_when_construct_should_notBeNull() {
        assertNotNull(new AnalyzeSingleFileHandler());
    }

    @Test
    void given_analyzeWorkspaceHandler_when_construct_should_notBeNull() {
        assertNotNull(new AnalyzeWorkspaceHandler());
    }

    @Test
    void given_configurationHandler_when_construct_should_notBeNull() {
        assertNotNull(new ConfigurationHandler());
    }

    @Test
    void given_getAnalysisHistory_when_construct_should_notBeNull() {
        assertNotNull(new GetAnalysisHistory());
    }

    @Test
    void given_nullEvent_when_analyzeProjectHandlerExecute_throws_exception() {
        assertThrows(Throwable.class, () -> new AnalyzeProjectHandler().execute(null));
    }

    @Test
    void given_nullEvent_when_analyzeSingleFileHandlerExecute_throws_exception() {
        assertThrows(Throwable.class, () -> new AnalyzeSingleFileHandler().execute(null));
    }

    @Test
    void given_nullEvent_when_analyzeWorkspaceHandlerExecute_throws_exception() {
        assertThrows(Throwable.class, () -> new AnalyzeWorkspaceHandler().execute(null));
    }

    @Test
    void given_nullEvent_when_configurationHandlerExecute_throws_exception() {
        assertThrows(Throwable.class, () -> new ConfigurationHandler().execute(null));
    }

    @Test
    void given_nullEvent_when_getAnalysisHistoryExecute_throws_exception() {
        assertThrows(Throwable.class, () -> new GetAnalysisHistory().execute(null));
    }
}
