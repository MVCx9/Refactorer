package test.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.model.clazz.ClassMetrics;
import main.model.project.ProjectMetrics;
import main.model.workspace.WorkspaceMetrics;
import main.session.ActionType;
import main.session.SessionAnalysisStore;
import main.session.SessionAnalysisStore.HistoryEntry;
import test.objectmothers.ClassMetricsMother;
import test.objectmothers.ProjectMetricsMother;
import test.objectmothers.WorkspaceMetricsMother;

class SessionAnalysisStoreTest {

    private SessionAnalysisStore store;

    @BeforeEach
    void setUp() {
        this.store = SessionAnalysisStore.getInstance();
        this.store.clear();
    }

    @AfterEach
    void tearDown() {
        this.store.clear();
    }

    @Test
    void given_singleton_when_getInstance_should_returnSameReference() {
        assertSame(SessionAnalysisStore.getInstance(), SessionAnalysisStore.getInstance());
    }

    @Test
    void given_classMetrics_when_register_should_appendHistoryEntry() {
        final ClassMetrics metrics = ClassMetricsMother.simple("A.java");
        this.store.register(ActionType.CLASS, metrics);
        final List<HistoryEntry<?>> history = this.store.getHistory();
        assertEquals(1, history.size());
        final HistoryEntry<?> entry = history.getFirst();
        assertEquals(ActionType.CLASS, entry.getActionType());
        assertSame(metrics, entry.getMetrics());
        assertNotNull(entry.getTimestamp());
    }

    @Test
    void given_projectMetrics_when_register_should_appendHistoryEntry() {
        final ProjectMetrics metrics = ProjectMetricsMother.simple("P");
        this.store.register(ActionType.PROJECT, metrics);
        assertEquals(ActionType.PROJECT, this.store.getHistory().getFirst().getActionType());
    }

    @Test
    void given_workspaceMetrics_when_register_should_appendHistoryEntry() {
        final WorkspaceMetrics metrics = WorkspaceMetricsMother.simple();
        this.store.register(ActionType.WORKSPACE, metrics);
        assertEquals(ActionType.WORKSPACE, this.store.getHistory().getFirst().getActionType());
    }

    @Test
    void given_nullType_when_register_throws_nullPointerException() {
        final ClassMetrics metrics = ClassMetricsMother.simple("A.java");
        assertThrows(NullPointerException.class, () -> this.store.register((ActionType) null, metrics));
    }

    @Test
    void given_nullMetrics_when_register_throws_nullPointerException() {
        assertThrows(NullPointerException.class, () -> this.store.register(ActionType.CLASS, (ClassMetrics) null));
    }

    @Test
    void given_history_when_getHistory_should_returnUnmodifiable() {
        this.store.register(ActionType.CLASS, ClassMetricsMother.simple("A.java"));
        final List<HistoryEntry<?>> history = this.store.getHistory();
        assertThrows(UnsupportedOperationException.class, () -> history.remove(0));
    }

    @Test
    void given_populatedStore_when_clear_should_emptyHistory() {
        this.store.register(ActionType.CLASS, ClassMetricsMother.simple("A.java"));
        this.store.clear();
        assertTrue(this.store.getHistory().isEmpty());
    }
}
