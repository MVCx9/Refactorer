package main.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import main.preferences.PluginPreferences;

public class ConfigurationDialog extends TitleAreaDialog {

    private Combo languageCombo;
    private Text ilpPathText;

    public ConfigurationDialog(Shell parentShell) {
        super(parentShell);
        setHelpAvailable(false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        boolean isEnglish = PluginPreferences.isEnglish();
        newShell.setText(isEnglish ? "Plugin Configuration" : "Configuración del Plugin");
        newShell.setMinimumSize(550, 350);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        boolean isEnglish = PluginPreferences.isEnglish();
        setTitle(isEnglish ? "Refactorer Configuration" : "Configuración de Refactorer");
        setMessage(isEnglish ? "Configure plugin display options" : "Configura las opciones de visualización del plugin");

        Composite content = new Composite(container, SWT.NONE);
        content.setLayout(new GridLayout(3, false));
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label langLabel = new Label(content, SWT.NONE);
        langLabel.setText(isEnglish ? "Plugin Language:" : "Idioma del Plugin:");

        languageCombo = new Combo(content, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData langComboGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        langComboGD.horizontalSpan = 2;
        languageCombo.setLayoutData(langComboGD);
        languageCombo.setItems(new String[] {"Castellano", "English"});

        String currentLanguage = PluginPreferences.getPluginLanguage();
        int langIndex = "English".equals(currentLanguage) ? 1 : 0;
        languageCombo.select(langIndex);

        Label separator1 = new Label(content, SWT.NONE);
        GridData sep1GD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        sep1GD.horizontalSpan = 3;
        sep1GD.heightHint = 10;
        separator1.setLayoutData(sep1GD);

        Label ilpLabel = new Label(content, SWT.NONE);
        ilpLabel.setText(isEnglish ? "CPLEX Library Path:" : "Ruta de la librería CPLEX:");

        ilpPathText = new Text(content, SWT.BORDER);
        ilpPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ilpPathText.setText(PluginPreferences.getIlpExecutablePath());

        Button browseButton = new Button(content, SWT.PUSH);
        browseButton.setText(isEnglish ? "Browse..." : "Explorar...");
        browseButton.addListener(SWT.Selection, e -> {
            DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
            dialog.setText(isEnglish ? "Select CPLEX Library Folder" : "Seleccionar carpeta de librería CPLEX");
            dialog.setMessage(isEnglish 
                ? "Select the folder containing the CPLEX native library" 
                : "Seleccione la carpeta que contiene la librería nativa de CPLEX");
            String currentPath = ilpPathText.getText().trim();
            if (!currentPath.isEmpty()) {
                dialog.setFilterPath(currentPath);
            }
            String selectedPath = dialog.open();
            if (selectedPath != null) {
                ilpPathText.setText(selectedPath);
            }
        });

        Label ilpHint = new Label(content, SWT.WRAP);
        String ilpHintText = isEnglish 
            ? "Example: /Applications/CPLEX_Studio/cplex/bin/x86-64_osx"
            : "Ejemplo: /Applications/CPLEX_Studio/cplex/bin/x86-64_osx";
        ilpHint.setText(ilpHintText);
        ilpHint.setForeground(content.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        GridData ilpHintGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        ilpHintGD.horizontalSpan = 3;
        ilpHint.setLayoutData(ilpHintGD);
        
        Label ilpStatus = new Label(content, SWT.NONE);
        String statusText = PluginPreferences.isCplexLoaded() 
            ? (isEnglish ? "Status: CPLEX loaded ✓" : "Estado: CPLEX cargado ✓")
            : (isEnglish ? "Status: CPLEX not loaded" : "Estado: CPLEX no cargado");
        ilpStatus.setText(statusText);
        ilpStatus.setForeground(PluginPreferences.isCplexLoaded() 
            ? content.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN)
            : content.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        GridData statusGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        statusGD.horizontalSpan = 3;
        ilpStatus.setLayoutData(statusGD);

        Label separator2 = new Label(content, SWT.NONE);
        GridData sep2GD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        sep2GD.horizontalSpan = 3;
        sep2GD.heightHint = 15;
        separator2.setLayoutData(sep2GD);

        Label reminderLabel = new Label(content, SWT.WRAP);
        String reminderText = isEnglish 
            ? "Note: To modify the cognitive complexity threshold, configure it individually in each Java project's properties (Right-click on project > Properties > Refactorer Plugin Threshold)."
            : "Nota: Para modificar el umbral de complejidad cognitiva, debe configurarse individualmente en las propiedades de cada proyecto Java (Clic derecho en proyecto > Propiedades > Refactorer Plugin Threshold).";
        reminderLabel.setText(reminderText);
        reminderLabel.setForeground(content.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        GridData reminderGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        reminderGD.horizontalSpan = 3;
        reminderGD.widthHint = 480;
        reminderLabel.setLayoutData(reminderGD);

        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        boolean isEnglish = PluginPreferences.isEnglish();
        createButton(parent, OK, isEnglish ? "Save" : "Guardar", true);
        createButton(parent, CANCEL, isEnglish ? "Cancel" : "Cancelar", false);
    }

    @Override
    protected void okPressed() {
        boolean isEnglish = PluginPreferences.isEnglish();
        
        int langIndex = languageCombo.getSelectionIndex();
        String language = langIndex == 1 ? "English" : "Castellano";
        PluginPreferences.setPluginLanguage(language);
        
        String ilpPath = ilpPathText.getText().trim();
        String previousPath = PluginPreferences.getIlpExecutablePath();
        PluginPreferences.setIlpExecutablePath(ilpPath);
        
        if (ilpPath.isEmpty()) {
            PluginPreferences.resetCplexState();
            String title = isEnglish ? "CPLEX Unconfigured" : "CPLEX Desconfigurado";
            String msg = isEnglish 
                ? "CPLEX library path has been cleared. ILP algorithm will not be available.\n\nNote: This configuration will not be available when the IDE is restarted."
                : "La ruta de la librería CPLEX ha sido eliminada. El algoritmo ILP no estará disponible.\n\nNota: Esta configuración no estará disponible cuando se reinicie el IDE.";
            MessageDialog.openInformation(getShell(), title, msg);
        } else if (!ilpPath.equals(previousPath)) {
            PluginPreferences.resetCplexState();
            boolean loaded = PluginPreferences.loadCplexFromPath(ilpPath);
            if (loaded) {
                String title = isEnglish ? "CPLEX Loaded" : "CPLEX Cargado";
                String msg = isEnglish 
                    ? "CPLEX library loaded successfully. ILP algorithm is now available."
                    : "Librería CPLEX cargada correctamente. El algoritmo ILP está disponible.";
                MessageDialog.openInformation(getShell(), title, msg);
            } else {
                String title = isEnglish ? "CPLEX Load Error" : "Error al cargar CPLEX";
                String msg = isEnglish 
                    ? "Could not load CPLEX library from the specified path. Please verify the path is correct and contains the CPLEX native library."
                    : "No se pudo cargar la librería CPLEX desde la ruta especificada. Verifique que la ruta es correcta y contiene la librería nativa de CPLEX.";
                MessageDialog.openWarning(getShell(), title, msg);
            }
        }
        
        super.okPressed();
    }
}
