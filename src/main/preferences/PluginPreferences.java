package main.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import main.boot.Activator;

public final class PluginPreferences {
    
    private static final String KEY_LANGUAGE = "pluginLanguage";
    private static final String KEY_ILP_PATH = "ilpExecutablePath";
    private static final String DEFAULT_LANGUAGE = "English";
    private static final String DEFAULT_ILP_PATH = "";
    
    private static boolean cplexLoaded = false;

    private PluginPreferences() {}

    public static String getPluginLanguage() {
        try {
            IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
            String language = prefs.get(KEY_LANGUAGE, DEFAULT_LANGUAGE);
            if ("Castellano".equals(language) || "English".equals(language)) {
                return language;
            }
            return DEFAULT_LANGUAGE;
        } catch (Exception e) {
            return DEFAULT_LANGUAGE;
        }
    }

    public static void setPluginLanguage(String language) {
        if (!"Castellano".equals(language) && !"English".equals(language)) {
            language = DEFAULT_LANGUAGE;
        }
        try {
            IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
            prefs.put(KEY_LANGUAGE, language);
            prefs.flush();
        } catch (Exception e) {
        }
    }

    public static boolean isEnglish() {
        return "English".equals(getPluginLanguage());
    }

    public static String getIlpExecutablePath() {
        try {
            IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
            return prefs.get(KEY_ILP_PATH, DEFAULT_ILP_PATH);
        } catch (Exception e) {
        	cplexLoaded = false;
            return DEFAULT_ILP_PATH;
        }
    }

    public static void setIlpExecutablePath(String path) {
        try {
            IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
            prefs.put(KEY_ILP_PATH, path != null ? path : DEFAULT_ILP_PATH);
            prefs.flush();
        } catch (Exception e) {
        }
    }
    
    public static boolean isCplexLoaded() {
        return cplexLoaded;
    }
    
    public static void resetCplexState() {
        cplexLoaded = false;
        System.out.println(">> CPLEX: Estado reiniciado, librería desconfigurada");
    }
    
    public static void loadCplexLibrary() {
        String path = getIlpExecutablePath();
        if (path == null || path.trim().isEmpty()) {
            System.out.println(">> CPLEX: No se ha configurado la ruta de la librería");
            cplexLoaded = false;
            return;
        }
        
        loadCplexFromPath(path.trim());
    }
    
    public static boolean loadCplexFromPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            System.out.println(">> CPLEX: Ruta vacía, no se puede cargar la librería");
            cplexLoaded = false;
            return false;
        }
        
        if (cplexLoaded) {
            System.out.println(">> CPLEX: Librería ya cargada previamente");
            return true;
        }
        
        path = path.trim();
        
        try {
        	System.load(path + "/libcplex2212.dylib");
            
            cplexLoaded = true;
            System.out.println(">> CPLEX: Librería cargada correctamente desde: " + path);
            return true;
        } catch (UnsatisfiedLinkError e) {
            String message = e.getMessage();
            // Si el error indica que la librería ya está cargada, consideramos éxito
            if (message != null && (message.contains("already loaded") || message.contains("Native Library"))) {
                cplexLoaded = true;
                System.out.println(">> CPLEX: Librería ya estaba cargada en la JVM: " + path);
                return true;
            }
        	cplexLoaded = false;
            System.err.println(">> CPLEX: Error al cargar la librería: " + e.getMessage());
            return false;
        } catch (Exception e) {
        	cplexLoaded = false;
            System.err.println(">> CPLEX: Error inesperado: " + e.getMessage());
            return false;
        }
    }
}
