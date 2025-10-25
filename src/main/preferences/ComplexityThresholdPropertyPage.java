package main.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ComplexityThresholdPropertyPage extends PropertyPage {

    private Text thresholdText;

    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));
        
        Label label = new Label(container, SWT.NONE);
        label.setText("Umbral de Complejidad Cognitiva:");
        
        thresholdText = new Text(container, SWT.BORDER);
        thresholdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Label recommended = new Label(container, SWT.NONE);
        recommended.setText("Valor recomendado: 15");
        GridData recGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        recGD.horizontalSpan = 2;
        recommended.setLayoutData(recGD);
        
        IProject project = getProjectFromElement();
        int current = ProjectPreferences.getComplexityThreshold(project);
        thresholdText.setText(Integer.toString(current));
        
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
            int value = parseThreshold(thresholdText.getText());
            ProjectPreferences.setComplexityThreshold(project, value);
        }
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        thresholdText.setText("15");
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