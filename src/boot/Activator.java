package boot;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

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
