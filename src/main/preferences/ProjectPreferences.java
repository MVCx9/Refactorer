package main.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import main.neo.app.Constants;
import main.boot.Activator;

public final class ProjectPreferences {
	private static final String KEY_THRESHOLD = "complexityThreshold";

	private ProjectPreferences() {
	}

	public static int getComplexityThreshold(IProject project) {
		if (project == null)
			return Constants.COGNITIVE_COMPLEXITY_THRESHOLD;
		try {
			IEclipsePreferences node = new ProjectScope(project).getNode(Activator.PLUGIN_ID);
			String v = node.get(KEY_THRESHOLD, Integer.toString(Constants.COGNITIVE_COMPLEXITY_THRESHOLD));
			return parseOrDefault(v);
		} catch (Exception e) {
			return Constants.COGNITIVE_COMPLEXITY_THRESHOLD;
		}
	}

	public static void setComplexityThreshold(IProject project, int value) {
		if (project == null)
			return;
		if (value <= 0)
			value = Constants.COGNITIVE_COMPLEXITY_THRESHOLD;
		try {
			IEclipsePreferences node = new ProjectScope(project).getNode(Activator.PLUGIN_ID);
			node.putInt(KEY_THRESHOLD, value);
			node.flush();
		} catch (Exception ignore) {
		}
	}

	private static int parseOrDefault(String v) {
		try {
			int val = Integer.parseInt(v.trim());
			if (val <= 0)
				return Constants.COGNITIVE_COMPLEXITY_THRESHOLD;
			return val;
		} catch (Exception e) {
			return Constants.COGNITIVE_COMPLEXITY_THRESHOLD;
		}
	}
}