package test.session;

import java.util.List;

import main.model.clazz.ClassMetrics;
import main.model.project.ProjectMetrics;
import main.model.workspace.WorkspaceMetrics;
import main.session.ActionType;
import main.session.SessionAnalysisStore;
import test.model.clazz.mother.ClassMetricsMother;
import test.model.project.mother.ProjectMetricsMother;
import test.model.workspace.mother.WorkspaceMetricsMother;

public final class SessionAnalysisStoreMother {
	private SessionAnalysisStoreMother() {}

	public static SessionAnalysisStore defaultStore() {
		final SessionAnalysisStore store = SessionAnalysisStore.getInstance();
		store.clear();
		final ClassMetrics cm = ClassMetricsMother.defaultMetrics();
		final ProjectMetrics pm = ProjectMetricsMother.defaultMetrics();
		final WorkspaceMetrics wm = WorkspaceMetricsMother.defaultMetrics();
		store.register(ActionType.CLASS, cm);
		store.register(ActionType.PROJECT, pm);
		store.register(ActionType.WORKSPACE, wm);
		return store;
	}

	public static SessionAnalysisStore withData(List<ClassMetrics> classMetrics, List<ProjectMetrics> projectMetrics, List<WorkspaceMetrics> workspaceMetrics) {
		final SessionAnalysisStore store = SessionAnalysisStore.getInstance();
		store.clear();
		if (classMetrics != null) {
			for (final ClassMetrics m : classMetrics) {
				if (m != null) store.register(ActionType.CLASS, m);
			}
		}
		if (projectMetrics != null) {
			for (final ProjectMetrics m : projectMetrics) {
				if (m != null) store.register(ActionType.PROJECT, m);
			}
		}
		if (workspaceMetrics != null) {
			for (final WorkspaceMetrics m : workspaceMetrics) {
				if (m != null) store.register(ActionType.WORKSPACE, m);
			}
		}
		return store;
	}
}