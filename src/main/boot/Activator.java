package main.boot;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import main.preferences.PluginPreferences;
import main.session.SessionAnalysisStore;

public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "Refactorer"; 

    private static Activator plugin;

    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("### Plugin Refactorer cargado");
        super.start(context);
        plugin = this;
        
        PluginPreferences.loadCplexLibrary();
        
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
}