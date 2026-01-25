package main.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import main.common.languaje.Messages;
import main.model.clazz.ClassMetrics;
import main.model.common.ComplexityStats;
import main.model.common.Identifiable;
import main.model.common.LocStats;
import main.model.project.ProjectMetrics;
import main.model.workspace.WorkspaceMetrics;
import main.session.ActionType;

public class AnalysisNoRefactorDialog extends TitleAreaDialog {

    private final ActionType actionType;
    private final Object metrics;
    private final IProject project;

    public AnalysisNoRefactorDialog(Shell parentShell, ActionType actionType, Object metrics, IProject project) {
        super(parentShell);
        this.actionType = actionType;
        this.metrics = metrics;
        this.project = project;
        setHelpAvailable(false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getNoRefactorTitle());
        newShell.setMinimumSize(560, 300);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        setTitle(Messages.getNoRefactorTitle());
        setMessage(buildMessageHeader());

        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setLayout(new GridLayout(1, false));

        Label explanation = new Label(container, SWT.WRAP);
        explanation.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        explanation.setText(buildMainExplanation());

        if (metrics != null) {
            Composite metricsComp = new Composite(container, SWT.NONE);
            GridLayout gl = new GridLayout(2, false);
            gl.marginTop = 10;
            gl.horizontalSpacing = 14;
            gl.verticalSpacing = 6;
            metricsComp.setLayout(gl);
            metricsComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

            addMetrics(metricsComp);
        }
        return area;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, OK, Messages.getButtonClose(), true);
    }

    private String buildMessageHeader() {
        if (actionType == ActionType.CLASS && metrics == null) {
            return Messages.getNoRefactorUnsupportedElement();
        }
        
        String name = metrics instanceof Identifiable ? ((Identifiable) metrics).getName() : "";
        int threshold = getThreshold();
        
        return switch (actionType) {
            case CLASS -> Messages.getNoRefactorMessageClass(name, threshold);
            case PROJECT -> Messages.getNoRefactorMessageProject(name, threshold);
            case WORKSPACE -> Messages.getNoRefactorMessageWorkspace(threshold);
        };
    }

    private int getThreshold() {
        if (metrics instanceof ClassMetrics cm) return cm.getComplexityThreshold();
        if (metrics instanceof ProjectMetrics pm) return pm.getComplexityThreshold();
        return 15;
    }

    private String buildMainExplanation() {
        if (actionType == ActionType.CLASS && metrics == null) {
            return Messages.getNoRefactorUnsupportedExplanation();
        }
        
        boolean isClassAction = actionType == ActionType.CLASS;
        return Messages.getNoRefactorExplanation(isClassAction);
    }


    private void addMetrics(Composite parent) {
        if (metrics instanceof ClassMetrics cm) {
            metric(parent, Messages.getMetricTotalMethods(), cm.getCurrentMethodCount());
            metric(parent, Messages.getMetricTotalLOC(), cm.getCurrentLoc());
            metric(parent, Messages.getMetricTotalCC(), cm.getCurrentCc());
            metric(parent, Messages.getMetricAverageLOCMethod(), cm.getAverageCurrentLoc());
            metric(parent, Messages.getMetricAverageCCMethod(), cm.getAverageCurrentCc());
        } else if (metrics instanceof ProjectMetrics pm) {
            metric(parent, Messages.getMetricClasses(), pm.getClassCount());
            metric(parent, Messages.getMetricTotalMethods(), pm.getCurrentMethodCount());
            metric(parent, Messages.getMetricTotalLOC(), pm.getCurrentLoc());
            metric(parent, Messages.getMetricTotalCC(), pm.getCurrentCc());
            metric(parent, Messages.getMetricAverageLOCClass(), pm.getAverageCurrentLoc());
            metric(parent, Messages.getMetricAverageCCClass(), pm.getAverageCurrentCc());
            metric(parent, Messages.getMetricAverageMethodsClass(), pm.getAverageCurrentMethodCount());
        } else if (metrics instanceof WorkspaceMetrics wm) {
            metric(parent, Messages.getMetricProjects(), wm.getProjects().size());
            metric(parent, Messages.getMetricTotalMethods(), wm.getCurrentMethodCount());
            metric(parent, Messages.getMetricTotalLOC(), wm.getCurrentLoc());
            metric(parent, Messages.getMetricCCAvgProjects(), wm.getCurrentCc());
            metric(parent, Messages.getMetricAverageLOCProject(), wm.getAverageCurrentLoc());
            metric(parent, Messages.getMetricAverageCCProject(), wm.getAverageCurrentCc());
            metric(parent, Messages.getMetricAverageMethodsProject(), wm.getAverageCurrentMethodCount());
        } else {
            if (metrics instanceof LocStats ls) metric(parent, "LOC", ls.getCurrentLoc());
            if (metrics instanceof ComplexityStats cs) metric(parent, "CC", cs.getCurrentCc());
        }
    }

    private void metric(Composite parent, String label, int value) {
        Label l = new Label(parent, SWT.NONE);
        l.setText(label + ":");
        Label v = new Label(parent, SWT.NONE);
        v.setText(String.valueOf(value));
    }
}