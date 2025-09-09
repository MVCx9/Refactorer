package main.ui;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import main.model.clazz.ClassMetrics;
import main.model.common.ComplexityStats;
import main.model.common.LocStats;
import main.model.project.ProjectMetrics;
import main.model.workspace.WorkspaceMetrics;
import main.session.ActionType;

/**
 * Dialog shown when there are no feasible method extraction refactorings.
 */
public class AnalysisNoRefactorDialog extends TitleAreaDialog {

    private final ActionType actionType;
    private final Object metrics; // ClassMetrics | ProjectMetrics | WorkspaceMetrics

    public AnalysisNoRefactorDialog(Shell parentShell, ActionType actionType, Object metrics) {
        super(parentShell);
        this.actionType = actionType;
        this.metrics = metrics;
        setHelpAvailable(false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Análisis de complejidad (sin refactorizaciones)");
        newShell.setMinimumSize(560, 300);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        setTitle("No hay extracciones de método sugeridas");
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
        createButton(parent, OK, "Cerrar", true);
    }

    private String buildMessageHeader() {
        if (actionType == ActionType.CLASS && metrics == null) {
            return "Elemento no soportado";
        }
        return switch (actionType) {
            case CLASS -> "Clase sin mejoras de extracción aplicables";
            case PROJECT -> "Proyecto sin mejoras de extracción aplicables";
            case WORKSPACE -> "Workspace sin mejoras de extracción aplicables";
        };
    }

    private String buildMainExplanation() {
        if (actionType == ActionType.CLASS && metrics == null) {
            return "El elemento seleccionado no es una clase Java procesable por el plugin. Puede tratarse de un fichero de configuración, un enum, una interface o un record, los cuales se ignoran para sugerencias de extracción.";
        }
        String base = "El análisis no ha identificado oportunidades de extracción de métodos que reduzcan la complejidad cognitiva o las líneas de código sin introducir duplicidad o pérdida de legibilidad.";
        String reasons = "\n\nPosibles razones básicas:" +
                "\n • Los métodos existentes ya son suficientemente pequeños." +
                "\n • La complejidad cognitiva está distribuida y no hay bloques concentrados que se beneficien de separar." +
                "\n • Las posibles extracciones crearían métodos triviales (p.ej. 1-2 líneas) que no aportan claridad." +
                "\n • El código contiene sobre todo getters/setters u operaciones simples lineales." +
                "\n • La extracción generaría duplicación o rompería la cohesión semántica." +
                (actionType != ActionType.CLASS ? "\n • Las clases analizadas ya están en un umbral aceptable de complejidad." : "");
        return base + reasons;
    }


    private void addMetrics(Composite parent) {
        if (metrics instanceof ClassMetrics cm) {
            metric(parent, "Métodos", cm.getCurrentMethodCount());
            metric(parent, "LOC totales", cm.getCurrentLoc());
            metric(parent, "CC total", cm.getCurrentCc());
            metric(parent, "Media LOC / método", cm.getAverageCurrentLoc());
            metric(parent, "Media CC / método", cm.getAverageCurrentCc());
        } else if (metrics instanceof ProjectMetrics pm) {
            metric(parent, "Clases", pm.getClassCount());
            metric(parent, "Métodos", pm.getCurrentMethodCount());
            metric(parent, "LOC totales", pm.getCurrentLoc());
            metric(parent, "CC total", pm.getCurrentCc());
            metric(parent, "Media LOC / clase", pm.getAverageCurrentLoc());
            metric(parent, "Media CC / clase", pm.getAverageCurrentCc());
            metric(parent, "Media métodos / clase", pm.getAverageCurrentMethodCount());
        } else if (metrics instanceof WorkspaceMetrics wm) {
            metric(parent, "Proyectos", wm.getProjects().size());
            metric(parent, "Métodos", wm.getCurrentMethodCount());
            metric(parent, "LOC totales", wm.getCurrentLoc());
            metric(parent, "CC (media proyectos)", wm.getCurrentCc());
            metric(parent, "Media LOC / proyecto", wm.getAverageCurrentLoc());
            metric(parent, "Media CC / proyecto", wm.getAverageCurrentCc());
            metric(parent, "Media métodos / proyecto", wm.getAverageCurrentMethodCount());
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