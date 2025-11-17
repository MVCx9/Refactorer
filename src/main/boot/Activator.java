package main.boot;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import main.session.SessionAnalysisStore;

import java.io.File;
import java.lang.reflect.Field;

public class Activator extends AbstractUIPlugin {

    // El ID del plugin, debe coincidir con el declarado en MANIFEST.MF
    public static final String PLUGIN_ID = "Refactorer"; 

    // Instancia compartida
    private static Activator plugin;

    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
    	System.out.println("### Plugin Refactorer cargado");
        super.start(context);
        plugin = this;
        
        // Configurar CPLEX library path
        configureCplexLibraryPath();
        
        SessionAnalysisStore.getInstance().clear();
        System.out.println(">> Plugin Refactorer ACTIVADO correctamente");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }
    
    /**
     * Configura el java.library.path para CPLEX de forma dinámica.
     * Intenta obtener el path desde:
     * 1. Variable de entorno CPLEX_LIBRARY_PATH
     * 2. Propiedad del sistema cplex.library.path
     * 3. Ubicaciones por defecto en macOS
     */
    private void configureCplexLibraryPath() {
        String cplexPath = null;
        
        // 1. Intentar obtener desde variable de entorno
        cplexPath = System.getenv("CPLEX_LIBRARY_PATH");
        
        // 2. Intentar obtener desde propiedad del sistema
        if (cplexPath == null || cplexPath.isEmpty()) {
            cplexPath = System.getProperty("cplex.library.path");
        }
        
        // 3. Intentar ubicaciones por defecto en macOS
        if (cplexPath == null || cplexPath.isEmpty()) {
            String[] defaultPaths = {
                "/Applications/CPLEX_Studio_Community2212/cplex/bin/x86-64_osx",
                System.getProperty("user.home") + "/Applications/CPLEX_Studio_Community2212/cplex/bin/x86-64_osx",
                "/opt/ibm/ILOG/CPLEX_Studio2212/cplex/bin/x86-64_osx"
            };
            
            for (String path : defaultPaths) {
                if (new File(path).exists()) {
                    cplexPath = path;
                    System.out.println(">> CPLEX encontrado en ubicación por defecto: " + path);
                    break;
                }
            }
        }
        
        if (cplexPath != null && !cplexPath.isEmpty()) {
            try {
                // Añadir al java.library.path existente
                String currentLibraryPath = System.getProperty("java.library.path", "");
                String newLibraryPath = cplexPath + File.pathSeparator + currentLibraryPath;
                System.setProperty("java.library.path", newLibraryPath);
                
                // Forzar recarga del java.library.path (necesario en algunos casos)
                Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
                fieldSysPath.setAccessible(true);
                fieldSysPath.set(null, null);
                
                System.out.println(">> CPLEX library path configurado: " + cplexPath);
            } catch (Exception e) {
                System.err.println(">> ERROR al configurar CPLEX library path: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println(">> ADVERTENCIA: No se encontró CPLEX. Configura la variable de entorno CPLEX_LIBRARY_PATH");
            System.out.println(">> Ejemplo: export CPLEX_LIBRARY_PATH=\"/ruta/a/cplex/bin/x86-64_osx\"");
        }
    }
}