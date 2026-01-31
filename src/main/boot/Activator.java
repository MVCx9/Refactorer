package main.boot;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import main.preferences.PluginPreferences;
import main.session.SessionAnalysisStore;

public class Activator extends AbstractUIPlugin implements IStartup {

    public static final String PLUGIN_ID = "Refactorer"; 

    private static Activator plugin;

    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("### Plugin Refactorer cargado");
        super.start(context);
        plugin = this;
        
        SessionAnalysisStore.getInstance().clear();
        System.out.println(">> Plugin Refactorer ACTIVADO correctamente");
    }
    
    /**
     * Se ejecuta cuando el workbench está completamente cargado.
     * Aquí cargamos CPLEX porque las preferencias ya están disponibles.
     */
    @Override
    public void earlyStartup() {
        System.out.println(">> Activator.earlyStartup() - Workbench listo");
        loadCplexWithNotification();
    }
    
    /**
     * Carga la librería CPLEX si está configurada y muestra una notificación al usuario.
     */
    private void loadCplexWithNotification() {
        String configuredPath = PluginPreferences.getIlpExecutablePath();
        
        if (configuredPath == null || configuredPath.trim().isEmpty()) {
            System.out.println(">> CPLEX: No hay ruta configurada, omitiendo carga");
            return;
        }
        
        System.out.println(">> CPLEX: Intentando cargar desde ruta almacenada: " + configuredPath);
        boolean loaded = PluginPreferences.loadCplexFromPath(configuredPath.trim());
        
        if (loaded) {
            showCplexLoadedNotification();
        } else {
            System.out.println(">> CPLEX: No se pudo cargar la librería");
        }
    }
    
    /**
     * Muestra una notificación visual al usuario indicando que CPLEX fue cargado.
     * Se ejecuta de forma asíncrona para garantizar que la UI esté disponible.
     */
    private void showCplexLoadedNotification() {
        Display display = Display.getDefault();
        if (display == null) {
            System.out.println(">> CPLEX: Display no disponible, omitiendo notificación visual");
            return;
        }
        
        display.asyncExec(() -> {
            try {
                IWorkbench workbench = PlatformUI.getWorkbench();
                if (workbench == null) {
                    return;
                }
                
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window == null) {
                    IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
                    if (windows.length > 0) {
                        window = windows[0];
                    }
                }
                
                if (window != null && window.getShell() != null) {
                    boolean isEnglish = PluginPreferences.isEnglish();
                    String title = isEnglish ? "CPLEX Loaded" : "CPLEX Cargado";
                    String message = isEnglish 
                        ? "CPLEX library loaded successfully on plugin startup. ILP algorithm is now available."
                        : "Librería CPLEX cargada correctamente al iniciar el plugin. El algoritmo ILP está disponible.";
                    
                    MessageDialog.openInformation(window.getShell(), title, message);
                }
            } catch (Exception e) {
                System.err.println(">> CPLEX: Error mostrando notificación: " + e.getMessage());
            }
        });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }
}