package main.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ComplexityThresholdPropertyPage extends PropertyPage {

	private Text thresholdText;
	private Combo languageCombo;

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));

		// Campo de Umbral de Complejidad
		Label label = new Label(container, SWT.NONE);
		label.setText("Umbral de Complejidad Cognitiva:");

		thresholdText = new Text(container, SWT.BORDER);
		thresholdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label recommended = new Label(container, SWT.NONE);
		recommended.setText("Valor recomendado: 15");
		GridData recGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		recGD.horizontalSpan = 2;
		recommended.setLayoutData(recGD);

		// Espacio visual entre secciones
		Label separator = new Label(container, SWT.NONE);
		GridData sepGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		sepGD.horizontalSpan = 2;
		sepGD.heightHint = 10;
		separator.setLayoutData(sepGD);

		// Campo de Idioma del Plugin
		Label langLabel = new Label(container, SWT.NONE);
		langLabel.setText("Idioma del Plugin:");

		languageCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		languageCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		languageCombo.setItems(new String[] {"Castellano", "English"});

		// Cargar valores actuales del proyecto
		IProject project = getProjectFromElement();
		int current = ProjectPreferences.getComplexityThreshold(project);
		thresholdText.setText(Integer.toString(current));

		String currentLanguage = ProjectPreferences.getPluginLanguage(project);
		int langIndex = currentLanguage.equals("English") ? 1 : 0;
		languageCombo.select(langIndex);

		return container;
	}

	private IProject getProjectFromElement() {
		IAdaptable element = getElement();
		if (element == null) {
			return null;
		}
		if (element instanceof IProject) {
			return (IProject) element;
		}
		return element.getAdapter(IProject.class);
	}

	@Override
	public boolean performOk() {
		IProject project = getProjectFromElement();
		if (project != null) {
			// Guardar umbral de complejidad
			int value = parseThreshold(thresholdText.getText());
			ProjectPreferences.setComplexityThreshold(project, value);
			
			// Guardar idioma seleccionado
			int langIndex = languageCombo.getSelectionIndex();
			String language = langIndex == 1 ? "English" : "Castellano";
			ProjectPreferences.setPluginLanguage(project, language);
		}
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		thresholdText.setText("15");
		languageCombo.select(0); // Castellano por defecto
		super.performDefaults();
	}

	private int parseThreshold(String text) {
		try {
			int value = Integer.parseInt(text.trim());
			return value > 0 ? value : 15;
		} catch (NumberFormatException e) {
			return 15;
		}
	}
}