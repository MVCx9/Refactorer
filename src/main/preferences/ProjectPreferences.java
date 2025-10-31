package main.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import main.neo.Constants;
import main.boot.Activator;

public final class ProjectPreferences {
	private static final String KEY_THRESHOLD = "complexityThreshold";
	private static final String KEY_LANGUAGE = "pluginLanguage";
	private static final String DEFAULT_LANGUAGE = "Castellano";

	private ProjectPreferences() {
	}

	public static int getComplexityThreshold(IProject project) {
		if (project == null)
			return Constants.MAX_COMPLEXITY;
		try {
			IEclipsePreferences node = new ProjectScope(project).getNode(Activator.PLUGIN_ID);
			String v = node.get(KEY_THRESHOLD, Integer.toString(Constants.MAX_COMPLEXITY));
			return parseOrDefault(v);
		} catch (Exception e) {
			return Constants.MAX_COMPLEXITY;
		}
	}

	public static void setComplexityThreshold(IProject project, int value) {
		if (project == null)
			return;
		if (value <= 0)
			value = Constants.MAX_COMPLEXITY;
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
				return Constants.MAX_COMPLEXITY;
			return val;
		} catch (Exception e) {
			return Constants.MAX_COMPLEXITY;
		}
	}

	public static String getPluginLanguage(IProject project) {
		if (project == null)
			return DEFAULT_LANGUAGE;
		try {
			IEclipsePreferences node = new ProjectScope(project).getNode(Activator.PLUGIN_ID);
			String language = node.get(KEY_LANGUAGE, DEFAULT_LANGUAGE);
			// Validar que sea un valor válido
			if (language.equals("Castellano") || language.equals("English")) {
				return language;
			}
			return DEFAULT_LANGUAGE;
		} catch (Exception e) {
			return DEFAULT_LANGUAGE;
		}
	}

	public static void setPluginLanguage(IProject project, String language) {
		if (project == null)
			return;
		// Validar que sea un valor válido
		if (!language.equals("Castellano") && !language.equals("English")) {
			language = DEFAULT_LANGUAGE;
		}
		try {
			IEclipsePreferences node = new ProjectScope(project).getNode(Activator.PLUGIN_ID);
			node.put(KEY_LANGUAGE, language);
			node.flush();
		} catch (Exception ignore) {
		}
	}
}