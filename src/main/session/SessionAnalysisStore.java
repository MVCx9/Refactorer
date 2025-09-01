package main.session;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import main.model.clazz.ClassMetrics;
import main.model.project.ProjectMetrics;
import main.model.workspace.WorkspaceMetrics;

/**
 * Almacén en memoria (vida = sesión de ejecución del plugin) de los análisis
 * realizados. Se pierde al reiniciar el workspace / recargar el plugin.
 */
public class SessionAnalysisStore {

	private static final SessionAnalysisStore INSTANCE = new SessionAnalysisStore();

	public static SessionAnalysisStore getInstance() {
		return INSTANCE;
	}

	private final List<HistoryEntry<?>> history = new CopyOnWriteArrayList<>();

	private SessionAnalysisStore() {
	}

	public void register(ActionType type, ClassMetrics metrics) {
		internalRegister(type, metrics);
	}

	public void register(ActionType type, ProjectMetrics metrics) {
		internalRegister(type, metrics);
	}

	public void register(ActionType type, WorkspaceMetrics metrics) {
		internalRegister(type, metrics);
	}

	private <T> void internalRegister(ActionType type, T metrics) {
		Objects.requireNonNull(type, "type");
		Objects.requireNonNull(metrics, "metrics");
		history.add(new HistoryEntry<>(type, LocalDateTime.now(), metrics));
	}

	public List<HistoryEntry<?>> getHistory() {
		return Collections.unmodifiableList(history);
	}

	public void clear() {
		history.clear();
	}

	public static final class HistoryEntry<T> {
		private final ActionType actionType;
		private final LocalDateTime timestamp;
		private final T metrics; // ClassMetrics | ProjectMetrics | WorkspaceMetrics

        private HistoryEntry(ActionType actionType, LocalDateTime timestamp, T metrics) {
            this.actionType = actionType;
            this.timestamp = timestamp;
            this.metrics = metrics;
        }

		public ActionType getActionType() {
			return actionType;
		}

		public LocalDateTime getTimestamp() {
			return timestamp;
		}

		public T getMetrics() {
			return metrics;
		}
	}
}