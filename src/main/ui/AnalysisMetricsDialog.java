package main.ui;

import java.time.format.DateTimeFormatter;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import main.model.clazz.ClassMetrics;
import main.model.common.ComplexityStats;
import main.model.common.Identifiable;
import main.model.common.LocStats;
import main.model.project.ProjectMetrics;
import main.model.workspace.WorkspaceMetrics;
import main.session.ActionType;

public class AnalysisMetricsDialog extends TitleAreaDialog {

    private final ActionType actionType;
    private final Object metrics; // ClassMetrics | ProjectMetrics | WorkspaceMetrics
    private final String leftSource;  // only for CLASS
    private final String rightSource; // only for CLASS

    public AnalysisMetricsDialog(Shell parentShell, ActionType actionType, Object metrics) {
        super(parentShell);
        this.actionType = actionType;
        this.metrics = metrics;
        this.leftSource = null;
        this.rightSource = null;
        setHelpAvailable(false);
    }

    public AnalysisMetricsDialog(Shell parentShell, ActionType actionType, Object metrics, String leftSource, String rightSource) {
        super(parentShell);
        this.actionType = actionType;
        this.metrics = metrics;
        this.leftSource = leftSource;
        this.rightSource = rightSource;
        setHelpAvailable(false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        String tipo;
        switch (actionType) {
            case CLASS: tipo = "de una clase"; break;
            case PROJECT: tipo = "de un proyecto"; break;
            case WORKSPACE: tipo = "del workspace"; break;
            default: tipo = "análisis"; break;
        }
        setTitle("Análisis y planificación de complejidad cognitiva " + tipo);

        String name = (metrics instanceof Identifiable) ? ((Identifiable) metrics).getName() : "<sin nombre>";
        String fecha = getFechaAnalisis(metrics);
        setMessage(fecha != null ? name + " — Analizado el " + fecha : name);

        if (actionType == ActionType.CLASS && leftSource != null && rightSource != null) {
            SashForm root = new SashForm(container, SWT.VERTICAL);
            root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            Composite compareContainer = new Composite(root, SWT.NONE);
            compareContainer.setLayout(new GridLayout(1, false));

            SashForm codeSash = new SashForm(compareContainer, SWT.HORIZONTAL);
            codeSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            Composite codeLeft = new Composite(codeSash, SWT.NONE);
            codeLeft.setLayout(new GridLayout(1, false));
            Label codeLeftTitle = new Label(codeLeft, SWT.NONE);
            codeLeftTitle.setText("Actual");
            codeLeftTitle.setFont(bold(codeLeftTitle));
            StyledText leftText = new StyledText(codeLeft, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY | SWT.MULTI);
            leftText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            leftText.setText(leftSource);
            leftText.setFont(JFaceResources.getTextFont());

            Composite codeRight = new Composite(codeSash, SWT.NONE);
            codeRight.setLayout(new GridLayout(1, false));
            Label codeRightTitle = new Label(codeRight, SWT.NONE);
            codeRightTitle.setText("Refactorizado");
            codeRightTitle.setFont(bold(codeRightTitle));
            StyledText rightText = new StyledText(codeRight, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY | SWT.MULTI);
            rightText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            rightText.setText(rightSource);
            rightText.setFont(JFaceResources.getTextFont());

            codeSash.setWeights(new int[] { 1, 1 });

            // Metrics section below
            Composite metricsContainer = new Composite(root, SWT.NONE);
            metricsContainer.setLayout(new GridLayout(1, false));
            SashForm metricsSash = new SashForm(metricsContainer, SWT.HORIZONTAL);
            metricsSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            Composite left = buildMetricsLeft(metricsSash);
            Composite right = buildMetricsRight(metricsSash);
            fillMetrics(left, right);

            metricsSash.setWeights(new int[] { 1, 1 });
            root.setWeights(new int[] { 2, 1 });
            return container;
        }

        SashForm sash = new SashForm(container, SWT.HORIZONTAL);
        sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Composite left = buildMetricsLeft(sash);
        Composite right = buildMetricsRight(sash);
        fillMetrics(left, right);
        sash.setWeights(new int[] { 1, 1 });
        return container;
    }

    private Composite buildMetricsLeft(SashForm parent) {
        Composite left = new Composite(parent, SWT.NONE);
        left.setLayout(new GridLayout(2, false));
        Label leftTitle = new Label(left, SWT.NONE);
        leftTitle.setText("Actual");
        leftTitle.setFont(bold(leftTitle));
        GridData leftTitleGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        leftTitleGD.horizontalSpan = 2;
        leftTitle.setLayoutData(leftTitleGD);
        return left;
    }

    private Composite buildMetricsRight(SashForm parent) {
        Composite right = new Composite(parent, SWT.NONE);
        right.setLayout(new GridLayout(2, false));
        Label rightTitle = new Label(right, SWT.NONE);
        rightTitle.setText("Refactorizado");
        rightTitle.setFont(bold(rightTitle));
        GridData rightTitleGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        rightTitleGD.horizontalSpan = 2;
        rightTitle.setLayoutData(rightTitleGD);
        return right;
    }

    private void fillMetrics(Composite left, Composite right) {
        // Fill common metrics (LOC / CC)
        if (metrics instanceof LocStats && metrics instanceof ComplexityStats) {
            LocStats ls = (LocStats) metrics;
            ComplexityStats cs = (ComplexityStats) metrics;
            metric(left, "Líneas de código (LOC)", String.valueOf(ls.getCurrentLoc()));
            metric(left, "Complejidad cognitiva (CC)", String.valueOf(cs.getCurrentCc()));
            metric(right, "Líneas de código (LOC)", String.valueOf(ls.getRefactoredLoc()));
            metric(right, "Complejidad cognitiva (CC)", String.valueOf(cs.getRefactoredCc()));
        }

        // Type-specific details
        if (metrics instanceof ClassMetrics) {
            ClassMetrics cm = (ClassMetrics) metrics;
            metric(left, "Métodos", String.valueOf(cm.getCurrentMethodCount()));
            metric(left, "Media LOC por método", String.valueOf(cm.getAverageCurrentLoc()));
            metric(left, "Media CC por método", String.valueOf(cm.getAverageCurrentCc()));

            metric(right, "Métodos", String.valueOf(cm.getRefactoredMethodCount()));
            metric(right, "Media LOC por método", String.valueOf(cm.getAverageRefactoredLoc()));
            metric(right, "Media CC por método", String.valueOf(cm.getAverageRefactoredCc()));

        } else if (metrics instanceof ProjectMetrics) {
            ProjectMetrics pm = (ProjectMetrics) metrics;
            metric(left, "Clases", String.valueOf(pm.getClassCount()));
            metric(left, "Métodos", String.valueOf(pm.getCurrentMethodCount()));
            metric(left, "Media métodos por clase", String.valueOf(pm.getAverageCurrentMethodCount()));
            metric(left, "Media LOC por clase", String.valueOf(pm.getAverageCurrentLoc()));
            metric(left, "Media CC por clase", String.valueOf(pm.getAverageCurrentCc()));

            metric(right, "Clases", String.valueOf(pm.getClassCount()));
            metric(right, "Métodos", String.valueOf(pm.getRefactoredMethodCount()));
            metric(right, "Media métodos por clase", String.valueOf(pm.getAverageRefactoredMethodCount()));
            metric(right, "Media LOC por clase", String.valueOf(pm.getAverageRefactoredLoc()));
            metric(right, "Media CC por clase", String.valueOf(pm.getAverageRefactoredCc()));
        } else if (metrics instanceof WorkspaceMetrics) {
            WorkspaceMetrics wm = (WorkspaceMetrics) metrics;
            metric(left, "Proyectos", String.valueOf(wm.getProjects().size()));
            metric(left, "Métodos", String.valueOf(wm.getCurrentMethodCount()));
            metric(left, "Media métodos por projecto", String.valueOf(wm.getAverageCurrentMethodCount()));
            metric(left, "Media LOC por proyecto", String.valueOf(wm.getAverageCurrentLoc()));
            metric(left, "Media CC por proyecto", String.valueOf(wm.getAverageCurrentCc()));

            metric(right, "Proyectos", String.valueOf(wm.getProjects().size()));
            metric(right, "Métodos", String.valueOf(wm.getRefactoredMethodCount()));
            metric(right, "Media métodos por projecto", String.valueOf(wm.getAverageRefactoredMethodCount()));
            metric(right, "Media LOC por proyecto", String.valueOf(wm.getAverageRefactoredLoc()));
            metric(right, "Media CC por proyecto", String.valueOf(wm.getAverageRefactoredCc()));
        }
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        String tipo;
        switch (actionType) {
            case CLASS: tipo = "clase"; break;
            case PROJECT: tipo = "proyecto"; break;
            case WORKSPACE: tipo = "workspace"; break;
            default: tipo = "análisis"; break;
        }
        newShell.setText("Análisis y planificación de complejidad cognitiva de una " + tipo);
        if (actionType == ActionType.CLASS) {
            newShell.setMinimumSize(1100, 750);
        } else {
            newShell.setMinimumSize(640, 420);
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, OK, "Cerrar", true);
    }

    private void metric(Composite parent, String label, String value) {
        Label l = new Label(parent, SWT.NONE);
        l.setText(label + ": ");
        l.setFont(bold(l));
        Label v = new Label(parent, SWT.NONE);
        v.setText(value);
        v.setFont(bold(v));
    }

    private Font bold(Control c) {
        org.eclipse.swt.graphics.FontData[] fds = c.getFont().getFontData();
        for (org.eclipse.swt.graphics.FontData fd : fds) {
            fd.setStyle(SWT.BOLD);
        }
        org.eclipse.swt.graphics.Font f = new org.eclipse.swt.graphics.Font(c.getDisplay(), fds);
        c.addListener(SWT.Dispose, e -> f.dispose());
        return f;
    }

    private String getFechaAnalisis(Object m) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (m instanceof ClassMetrics) return ((ClassMetrics) m).getAnalysisDate().format(fmt);
        if (m instanceof ProjectMetrics) return ((ProjectMetrics) m).getAnalysisDate().format(fmt);
        if (m instanceof WorkspaceMetrics) return ((WorkspaceMetrics) m).getAnalysisDate().format(fmt);
        return null;
    }
}
