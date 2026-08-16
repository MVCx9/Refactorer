package main.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import main.common.languaje.Messages;
import main.model.clazz.ClassMetrics;
import main.model.common.ComplexityStats;
import main.model.common.Identifiable;
import main.model.common.LocStats;
import main.model.method.MethodMetrics;
import main.model.project.ProjectMetrics;
import main.model.workspace.WorkspaceMetrics;
import main.session.ActionType;

/**
 * Dialog shown when the analysis could not propose any refactoring. When some
 * methods were detected above the cognitive complexity threshold they are
 * listed, so the user knows which ones need attention even though no extraction
 * plan could be built for them.
 */
public class AnalysisNoRefactorDialog extends TitleAreaDialog {

    private static final int DEFAULT_THRESHOLD = 15;
    private static final int TABLE_HEIGHT_HINT = 180;
    private static final int SECTION_SPACING = 10;

    private final ActionType actionType;
    private final Object metrics;
    private final IProject project;
    /** Methods above the threshold for which no refactoring plan was found. */
    private final List<DetectedMethod> detectedMethods;

    public AnalysisNoRefactorDialog(Shell parentShell, ActionType actionType, Object metrics, IProject project) {
        super(parentShell);
        this.actionType = actionType;
        this.metrics = metrics;
        this.project = project;
        this.detectedMethods = collectDetectedMethods(metrics);
        setHelpAvailable(false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getNoRefactorTitle());
        if (detectedMethods.isEmpty()) {
            newShell.setMinimumSize(560, 300);
        } else {
            newShell.setMinimumSize(820, 580);
        }
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
            gl.marginTop = SECTION_SPACING;
            gl.horizontalSpacing = 14;
            gl.verticalSpacing = 6;
            metricsComp.setLayout(gl);
            metricsComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

            addMetrics(metricsComp);
        }
        if (!detectedMethods.isEmpty()) {
            createDetectedMethodsSection(container);
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

        if (!detectedMethods.isEmpty()) {
            return switch (actionType) {
                case CLASS -> Messages.getNoPlanMessageClass(name, threshold, detectedMethods.size());
                case PROJECT -> Messages.getNoPlanMessageProject(name, threshold, detectedMethods.size());
                case WORKSPACE -> Messages.getNoPlanMessageWorkspace(detectedMethods.size());
            };
        }

        return switch (actionType) {
            case CLASS -> Messages.getNoRefactorMessageClass(name, threshold);
            case PROJECT -> Messages.getNoRefactorMessageProject(name, threshold);
            case WORKSPACE -> Messages.getNoRefactorMessageWorkspace(threshold);
        };
    }

    private int getThreshold() {
        if (metrics instanceof ClassMetrics cm) return cm.getComplexityThreshold();
        if (metrics instanceof ProjectMetrics pm) return pm.getComplexityThreshold();
        return DEFAULT_THRESHOLD;
    }

    private String buildMainExplanation() {
        if (actionType == ActionType.CLASS && metrics == null) {
            return Messages.getNoRefactorUnsupportedExplanation();
        }
        if (!detectedMethods.isEmpty()) {
            return Messages.getNoPlanExplanation();
        }
        return Messages.getNoRefactorExplanation(actionType == ActionType.CLASS);
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

    // ---------------- Methods detected above the threshold ----------------

    /**
     * Renders the list of methods that exceed the cognitive complexity threshold
     * and for which the analysis could not propose any extraction.
     */
    private void createDetectedMethodsSection(Composite parent) {
        Label title = new Label(parent, SWT.NONE);
        title.setText(Messages.getTableTitleMethodsWithoutRefactor());
        title.setFont(bold(title));
        GridData titleGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        titleGd.verticalIndent = SECTION_SPACING;
        title.setLayoutData(titleGd);

        Composite tableContainer = new Composite(parent, SWT.NONE);
        TableColumnLayout columnLayout = new TableColumnLayout();
        tableContainer.setLayout(columnLayout);
        GridData tableGd = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGd.heightHint = TABLE_HEIGHT_HINT;
        tableContainer.setLayoutData(tableGd);

        Table table = new Table(tableContainer, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        createColumns(table, columnLayout);
        fillRows(table);
    }

    private void createColumns(Table table, TableColumnLayout layout) {
        createColumn(table, layout, Messages.getTableColumnNumber(), 5, 40);
        if (actionType == ActionType.WORKSPACE) {
            createColumn(table, layout, Messages.getTableColumnProject(), 18, 110);
            createColumn(table, layout, Messages.getTableColumnThreshold(), 8, 70);
        }
        if (actionType != ActionType.CLASS) {
            createColumn(table, layout, Messages.getTableColumnClass(), 22, 120);
        }
        createColumn(table, layout, Messages.getTableColumnOriginalMethod(), 30, 140);
        createColumn(table, layout, Messages.getTableColumnOriginalCC(), 15, 130);
    }

    private void createColumn(Table table, TableColumnLayout layout, String text, int weight, int minimumWidth) {
        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(text);
        column.setResizable(true);
        layout.setColumnData(column, new ColumnWeightData(weight, minimumWidth, true));
    }

    private void fillRows(Table table) {
        int rowNumber = 1;
        for (DetectedMethod detected : detectedMethods) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(buildRow(rowNumber++, detected));
        }
    }

    private String[] buildRow(int rowNumber, DetectedMethod detected) {
        List<String> cells = new ArrayList<>();
        cells.add(Integer.toString(rowNumber));
        if (actionType == ActionType.WORKSPACE) {
            cells.add(detected.projectName());
            cells.add(Integer.toString(detected.threshold()));
        }
        if (actionType != ActionType.CLASS) {
            cells.add(detected.className());
        }
        cells.add(detected.method().getName());
        cells.add(Integer.toString(detected.method().getCc()));
        return cells.toArray(new String[0]);
    }

    /**
     * Flattens the analysed element into the methods that exceed the complexity
     * threshold and for which no refactoring plan was found.
     */
    private static List<DetectedMethod> collectDetectedMethods(Object metrics) {
        if (metrics instanceof ClassMetrics cm) {
            return methodsOf("", List.of(cm.copyWithMethods(cm.getMethodsWithoutRefactors(), List.of())));
        }
        if (metrics instanceof ProjectMetrics pm) {
            return methodsOf(pm.getName(), pm.getClassesWithoutRefactors());
        }
        if (metrics instanceof WorkspaceMetrics wm) {
            List<DetectedMethod> detected = new ArrayList<>();
            for (ProjectMetrics trimmedProject : wm.getProjectsWithoutRefactors()) {
                detected.addAll(methodsOf(trimmedProject.getName(), trimmedProject.getClasses()));
            }
            return detected;
        }
        return List.of();
    }

    private static List<DetectedMethod> methodsOf(String projectName, List<ClassMetrics> classes) {
        List<DetectedMethod> detected = new ArrayList<>();
        for (ClassMetrics clazz : classes) {
            for (MethodMetrics method : clazz.getCurrentMethods()) {
                detected.add(new DetectedMethod(projectName, clazz.getComplexityThreshold(), clazz.getName(), method));
            }
        }
        return detected;
    }

    private Font bold(Control control) {
        FontData[] fontData = control.getFont().getFontData();
        for (FontData data : fontData) {
            data.setStyle(SWT.BOLD);
        }
        Font font = new Font(control.getDisplay(), fontData);
        control.addListener(SWT.Dispose, e -> font.dispose());
        return font;
    }

    /** A method detected above the threshold, with the context needed to show it. */
    private record DetectedMethod(String projectName, int threshold, String className, MethodMetrics method) {
    }
}
