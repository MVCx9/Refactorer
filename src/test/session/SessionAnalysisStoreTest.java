package test.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.model.clazz.ClassMetrics;
import main.model.project.ProjectMetrics;
import main.model.workspace.WorkspaceMetrics;
import main.session.ActionType;
import main.session.SessionAnalysisStore;
import main.session.SessionAnalysisStore.HistoryEntry;
import test.model.clazz.mother.ClassMetricsMother;
import test.model.project.mother.ProjectMetricsMother;
import test.model.workspace.mother.WorkspaceMetricsMother;

class SessionAnalysisStoreTest {
	private final SessionAnalysisStore store = SessionAnalysisStore.getInstance();

	private void toStrictEqual(Object expected, Object actual) { assertEquals(expected, actual); }

	@BeforeEach
	void clearStore() {
		this.store.clear();
	}

	@Test
	@DisplayName("given_emptyStore_when_getHistory_should_returnEmptyList")
	void given_emptyStore_when_getHistory_should_returnEmptyList() {
		final List<HistoryEntry<?>> history = this.store.getHistory();
		toStrictEqual(0, history.size());
	}

	@Test
	@DisplayName("given_classMetrics_when_register_should_storeHistoryEntryWithTypeClass")
	void given_classMetrics_when_register_should_storeHistoryEntryWithTypeClass() {
		final ClassMetrics cm = ClassMetricsMother.defaultMetrics();
		this.store.register(ActionType.CLASS, cm);
		final List<HistoryEntry<?>> history = this.store.getHistory();
		toStrictEqual(1, history.size());
		toStrictEqual(ActionType.CLASS, history.get(0).getActionType());
		assertNotNull(history.get(0).getTimestamp());
		toStrictEqual(cm, history.get(0).getMetrics());
	}

	@Test
	@DisplayName("given_projectMetrics_when_register_should_storeHistoryEntryWithTypeProject")
	void given_projectMetrics_when_register_should_storeHistoryEntryWithTypeProject() {
		final ProjectMetrics pm = ProjectMetricsMother.defaultMetrics();
		this.store.register(ActionType.PROJECT, pm);
		final HistoryEntry<?> entry = this.store.getHistory().get(0);
		toStrictEqual(ActionType.PROJECT, entry.getActionType());
		toStrictEqual(pm, entry.getMetrics());
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_register_should_storeHistoryEntryWithTypeWorkspace")
	void given_workspaceMetrics_when_register_should_storeHistoryEntryWithTypeWorkspace() {
		final WorkspaceMetrics wm = WorkspaceMetricsMother.defaultMetrics();
		this.store.register(ActionType.WORKSPACE, wm);
		final HistoryEntry<?> entry = this.store.getHistory().get(0);
		toStrictEqual(ActionType.WORKSPACE, entry.getActionType());
		toStrictEqual(wm, entry.getMetrics());
	}

	@Test
	@DisplayName("given_multipleRegisters_when_getHistory_should_preserveInsertionOrder")
	void given_multipleRegisters_when_getHistory_should_preserveInsertionOrder() throws InterruptedException {
		final ClassMetrics cm = ClassMetricsMother.withName("C1");
		final ProjectMetrics pm = ProjectMetricsMother.withName("P1");
		final WorkspaceMetrics wm = WorkspaceMetricsMother.withName("W1");
		this.store.register(ActionType.CLASS, cm);
		Thread.sleep(5);
		this.store.register(ActionType.PROJECT, pm);
		Thread.sleep(5);
		this.store.register(ActionType.WORKSPACE, wm);
		final List<HistoryEntry<?>> history = this.store.getHistory();
		toStrictEqual(3, history.size());
		toStrictEqual(ActionType.CLASS, history.get(0).getActionType());
		toStrictEqual(ActionType.PROJECT, history.get(1).getActionType());
		toStrictEqual(ActionType.WORKSPACE, history.get(2).getActionType());
		assertTrue(history.get(0).getTimestamp().isBefore(history.get(1).getTimestamp()) || history.get(0).getTimestamp().equals(history.get(1).getTimestamp()));
		assertTrue(history.get(1).getTimestamp().isBefore(history.get(2).getTimestamp()) || history.get(1).getTimestamp().equals(history.get(2).getTimestamp()));
	}

	@Test
	@DisplayName("given_storeWithEntries_when_clear_should_removeAllEntries")
	void given_storeWithEntries_when_clear_should_removeAllEntries() {
		this.store.register(ActionType.CLASS, ClassMetricsMother.defaultMetrics());
		this.store.register(ActionType.PROJECT, ProjectMetricsMother.defaultMetrics());
		toStrictEqual(2, this.store.getHistory().size());
		this.store.clear();
		toStrictEqual(0, this.store.getHistory().size());
	}

	@Test
	@DisplayName("given_history_when_attemptModify_should_throwUnsupportedOperationException")
	void given_history_when_attemptModify_should_throwUnsupportedOperationException() {
		this.store.register(ActionType.CLASS, ClassMetricsMother.defaultMetrics());
		final List<HistoryEntry<?>> history = this.store.getHistory();
		assertThrows(UnsupportedOperationException.class, () -> history.add(null));
	}

	@Test
	@DisplayName("given_nullActionType_when_registerClassMetrics_throws_NullPointerException")
	void given_nullActionType_when_registerClassMetrics_throws_NullPointerException() {
		final ClassMetrics cm = ClassMetricsMother.defaultMetrics();
		assertThrows(NullPointerException.class, () -> this.store.register(null, cm));
	}

	@Test
	@DisplayName("given_nullMetrics_when_registerClassMetrics_throws_NullPointerException")
	void given_nullMetrics_when_registerClassMetrics_throws_NullPointerException() {
		assertThrows(NullPointerException.class, () -> this.store.register(ActionType.CLASS, (ClassMetrics) null));
	}

	@Test
	@DisplayName("given_nullMetrics_when_registerProjectMetrics_throws_NullPointerException")
	void given_nullMetrics_when_registerProjectMetrics_throws_NullPointerException() {
		assertThrows(NullPointerException.class, () -> this.store.register(ActionType.PROJECT, (ProjectMetrics) null));
	}

	@Test
	@DisplayName("given_nullMetrics_when_registerWorkspaceMetrics_throws_NullPointerException")
	void given_nullMetrics_when_registerWorkspaceMetrics_throws_NullPointerException() {
		assertThrows(NullPointerException.class, () -> this.store.register(ActionType.WORKSPACE, (WorkspaceMetrics) null));
	}

	private void assertTrue(boolean condition) { org.junit.jupiter.api.Assertions.assertTrue(condition); }
}