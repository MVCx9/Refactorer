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

/**
 * Dialog shown when there are no feasible method extraction refactorings.
 */
public class AnalysisNoRefactorDialog extends TitleAreaDialog {

    private final ActionType actionType;
    private final Object metrics; // ClassMetrics | ProjectMetrics | WorkspaceMetrics
    private final IProject project; // For i18n, null for WORKSPACE

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
        String title = actionType == ActionType.WORKSPACE ? "Complexity analysis (no refactorings)" : 
                      Messages.getNoRefactorTitle(project);
        newShell.setText(title);
        newShell.setMinimumSize(560, 300);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        String title = actionType == ActionType.WORKSPACE ? "No refactorings found" : 
                      Messages.getNoRefactorTitle(project);
        setTitle(title);
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
        String closeLabel = actionType == ActionType.WORKSPACE ? "Close" : Messages.getButtonClose(project);
        createButton(parent, OK, closeLabel, true);
    }

    private String buildMessageHeader() {
        if (actionType == ActionType.CLASS && metrics == null) {
            return project != null && "English".equals(main.preferences.ProjectPreferences.getPluginLanguage(project)) 
                ? "Unsupported element" : "Elemento no soportado";
        }
        
        String name = metrics instanceof Identifiable ? ((Identifiable) metrics).getName() : "";
        int threshold = getThreshold();
        
        return switch (actionType) {
            case CLASS -> Messages.getNoRefactorMessageClass(project, name, threshold);
            case PROJECT -> Messages.getNoRefactorMessageProject(project, name, threshold);
            case WORKSPACE -> Messages.getNoRefactorMessageWorkspace(threshold);
        };
    }

    private int getThreshold() {
        if (metrics instanceof ClassMetrics cm) return cm.getThreshold();
        if (metrics instanceof ProjectMetrics pm) return pm.getComplexityThreshold();
        return 15; // default
    }

    private String buildMainExplanation() {
        if (actionType == ActionType.CLASS && metrics == null) {
            boolean isEnglish = project != null && "English".equals(main.preferences.ProjectPreferences.getPluginLanguage(project));
            return isEnglish 
                ? "The selected element is not a Java class processable by the plugin. It may be a configuration file, enum, interface or record, which are ignored for extraction suggestions."
                : "El elemento seleccionado no es una clase Java procesable por el plugin. Puede tratarse de un fichero de configuración, un enum, una interface o un record, los cuales se ignoran para sugerencias de extracción.";
        }
        
        boolean isEnglish = actionType == ActionType.WORKSPACE || 
                           (project != null && "English".equals(main.preferences.ProjectPreferences.getPluginLanguage(project)));
        
        if (isEnglish) {
            String base = "The analysis did not identify method extraction opportunities that would reduce cognitive complexity or lines of code without introducing duplication or loss of readability.";
            String reasons = "\n\nPossible basic reasons:" +
                    "\n • Existing methods are already sufficiently small." +
                    "\n • Cognitive complexity is distributed and there are no concentrated blocks that would benefit from separation." +
                    "\n • Possible extractions would create trivial methods (e.g. 1-2 lines) that do not provide clarity." +
                    "\n • The code mainly contains getters/setters or simple linear operations." +
                    "\n • Extraction would generate duplication or break semantic cohesion." +
                    (actionType != ActionType.CLASS ? "\n • Analyzed classes are already at an acceptable complexity threshold." : "");
            return base + reasons;
        } else {
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
    }


    private void addMetrics(Composite parent) {
        boolean isEnglish = actionType == ActionType.WORKSPACE || 
                           (project != null && "English".equals(main.preferences.ProjectPreferences.getPluginLanguage(project)));
        
        if (metrics instanceof ClassMetrics cm) {
            metric(parent, isEnglish ? "Methods" : "Métodos", cm.getCurrentMethodCount());
            metric(parent, isEnglish ? "Total LOC" : "LOC totales", cm.getCurrentLoc());
            metric(parent, isEnglish ? "Total CC" : "CC total", cm.getCurrentCc());
            metric(parent, isEnglish ? "Average LOC / method" : "Media LOC / método", cm.getAverageCurrentLoc());
            metric(parent, isEnglish ? "Average CC / method" : "Media CC / método", cm.getAverageCurrentCc());
        } else if (metrics instanceof ProjectMetrics pm) {
            metric(parent, isEnglish ? "Classes" : "Clases", pm.getClassCount());
            metric(parent, isEnglish ? "Methods" : "Métodos", pm.getCurrentMethodCount());
            metric(parent, isEnglish ? "Total LOC" : "LOC totales", pm.getCurrentLoc());
            metric(parent, isEnglish ? "Total CC" : "CC total", pm.getCurrentCc());
            metric(parent, isEnglish ? "Average LOC / class" : "Media LOC / clase", pm.getAverageCurrentLoc());
            metric(parent, isEnglish ? "Average CC / class" : "Media CC / clase", pm.getAverageCurrentCc());
            metric(parent, isEnglish ? "Average methods / class" : "Media métodos / clase", pm.getAverageCurrentMethodCount());
        } else if (metrics instanceof WorkspaceMetrics wm) {
            metric(parent, "Projects", wm.getProjects().size());
            metric(parent, "Methods", wm.getCurrentMethodCount());
            metric(parent, "Total LOC", wm.getCurrentLoc());
            metric(parent, "CC (avg projects)", wm.getCurrentCc());
            metric(parent, "Average LOC / project", wm.getAverageCurrentLoc());
            metric(parent, "Average CC / project", wm.getAverageCurrentCc());
            metric(parent, "Average methods / project", wm.getAverageCurrentMethodCount());
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