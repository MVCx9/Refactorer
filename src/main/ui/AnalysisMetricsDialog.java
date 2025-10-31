package main.ui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import main.common.error.ModifyFilesException;
import main.common.utils.Utils;
import main.model.clazz.ClassMetrics;
import main.model.common.ComplexityStats;
import main.model.common.Identifiable;
import main.model.common.LocStats;
import main.model.method.MethodMetrics;
import main.model.project.ProjectMetrics;
import main.model.workspace.WorkspaceMetrics;
import main.session.ActionType;

public class AnalysisMetricsDialog extends TitleAreaDialog {

    private static final int APPLY_EXTRACT_ID = 1001;
    private static final int BREAK_EXTRACT_ID = 1002;

    private final ActionType actionType;
    private final Object metrics; // ClassMetrics | ProjectMetrics | WorkspaceMetrics
    private final String leftSource;  // only for CLASS
    private final String rightSource; // only for CLASS

    // Diff highlight colors (initialized only for CLASS view)
    private Color delColor;    // deleted
    private Color insColor;    // inserted
    private Color modColor;    // modified
    private final Color black = org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);

    private boolean readOnly = false; // when true (history), no code modification buttons

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

    /** Mark dialog as read-only (used when opened from history). */
    public AnalysisMetricsDialog setReadOnly(boolean value) {
        this.readOnly = value;
        return this;
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

            // Initialize more vivid colors for diff
            createDiffColors(container);

            // Apply diff highlighting between current and refactored sources
            diffAndHighlight(leftText, rightText);

            // Sync scroll between both code viewers
            installScrollSync(leftText, rightText);

            // Legend for diff colors
            createLegend(compareContainer);

            Composite metricsContainer = new Composite(root, SWT.NONE);
            metricsContainer.setLayout(new GridLayout(1, false));
            SashForm metricsSash = new SashForm(metricsContainer, SWT.HORIZONTAL);
            metricsSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            Composite left = buildMetricsLeft(metricsSash);
            Composite right = buildMetricsRight(metricsSash);
            fillMetrics(left, right);

            metricsSash.setWeights(new int[] { 1, 1 });
            root.setWeights(new int[] { 2, 1 });

        }

        SashForm sash = new SashForm(container, SWT.HORIZONTAL);
        sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Composite left = buildMetricsLeft(sash);
        Composite right = buildMetricsRight(sash);
        fillMetrics(left, right);
        sash.setWeights(new int[] { 1, 1 });

        // Seccion tabla de refactors para PROJECT y WORKSPACE... en CLASS no tiene sentido
        if (actionType == ActionType.PROJECT || actionType == ActionType.WORKSPACE || actionType == ActionType.CLASS) { // allow also CLASS if requested
            createRefactorsTableSection(container);
        }

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
        if (metrics instanceof ClassMetrics cm) {
            metric(left, "Métodos", String.valueOf(cm.getCurrentMethodCount()));
            metric(left, "Media LOC por método", String.valueOf(cm.getAverageCurrentLoc()));
            metric(left, "Media CC por método", String.valueOf(cm.getAverageCurrentCc()));
            metric(right, "Métodos", String.valueOf(cm.getRefactoredMethodCount()));
            metric(right, "Media LOC por método", String.valueOf(cm.getAverageRefactoredLoc()));
            metric(right, "Media CC por método", String.valueOf(cm.getAverageRefactoredCc()));
            metric(right, "Métodos extraídos", String.valueOf(cm.getRefactoredMethodCount() - cm.getCurrentMethodCount()));
            metric(right, "Umbral de Complejidad", String.valueOf(cm.getThreshold()));
        } else if (metrics instanceof ProjectMetrics pm) {
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
            metric(right, "Métodos extraídos", String.valueOf(pm.getRefactoredMethodCount() - pm.getCurrentMethodCount()));
            metric(right, "Umbral de Complejidad", String.valueOf(pm.getComplexityThreshold()));
        } else if (metrics instanceof WorkspaceMetrics wm) {
            metric(left, "Proyectos", String.valueOf(wm.getProjectCount()));
            metric(left, "Clases", String.valueOf(wm.getClassCount()));
            metric(left, "Métodos", String.valueOf(wm.getCurrentMethodCount()));
            metric(left, "Media métodos por projecto", String.valueOf(wm.getAverageCurrentMethodCount()));
            metric(left, "Media LOC por proyecto", String.valueOf(wm.getAverageCurrentLoc()));
            metric(left, "Media CC por proyecto", String.valueOf(wm.getAverageCurrentCc()));
            metric(right, "Proyectos", String.valueOf(wm.getProjectCount()));
            metric(right, "Clases", String.valueOf(wm.getClassCount()));
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
        if (!readOnly) { // only allow modifying code when not in history mode
            createButton(parent, BREAK_EXTRACT_ID, "Deshacer extracciones de código", false);
            createButton(parent, APPLY_EXTRACT_ID, "Aplicar extracciones de código", false);
        }
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (readOnly) { // ignore modification buttons if in read-only (safety)
            if (buttonId == OK) {
                super.buttonPressed(buttonId);
            }
            return;
        }
        if (buttonId == APPLY_EXTRACT_ID) {
            applyCodeExtractions();
            return; // keep dialog open after applying
        }
        if(buttonId == BREAK_EXTRACT_ID) {
            breackCodeExtractions();
            return;
        }
        super.buttonPressed(buttonId);
    }

    private void breackCodeExtractions() {
    	try {
            if (metrics instanceof ClassMetrics cm) {
                applyForClass(cm, cm.getCurrentSource());
            } else if (metrics instanceof ProjectMetrics pm) {
                for (ClassMetrics cm : pm.getClasses()) {
                    applyForClass(cm, cm.getCurrentSource());
                }
            } else if (metrics instanceof WorkspaceMetrics wm) {
                for (ProjectMetrics pm : wm.getProjects()) {
                    for (ClassMetrics cm : pm.getClasses()) {
                        applyForClass(cm, cm.getCurrentSource());
                    }
                }
            }
        } catch (Exception e) {
            throw new ModifyFilesException("Error reverting code extractions", e);
        }
	}

	private void applyCodeExtractions() {
        try {
            if (metrics instanceof ClassMetrics cm) {
                applyForClass(cm, cm.getRefactoredSource());
            } else if (metrics instanceof ProjectMetrics pm) {
                for (ClassMetrics cm : pm.getClasses()) {
                    applyForClass(cm, cm.getRefactoredSource());
                }
            } else if (metrics instanceof WorkspaceMetrics wm) {
                for (ProjectMetrics pm : wm.getProjects()) {
                    for (ClassMetrics cm : pm.getClasses()) {
                        applyForClass(cm, cm.getRefactoredSource());
                    }
                }
            }
        } catch (Exception e) {
            throw new ModifyFilesException("Error applying code extractions", e);
        }
    }

    private void applyForClass(ClassMetrics cm, String ref) {
        if (ref == null || ref.isEmpty()) return;
        String fileName = cm.getName();
        if (fileName == null || fileName.isBlank()) return;
        if (!fileName.endsWith(".java")) fileName = fileName + ".java";

        List<ICompilationUnit> units = findCompilationUnitsByFileName(fileName);
        String formatted = Utils.formatJava(ref);
        for (ICompilationUnit icu : units) {
            try {
                icu.becomeWorkingCopy(null);
                icu.getBuffer().setContents(formatted);
                icu.commitWorkingCopy(true, null);
            } catch (Exception ignore) {
            } finally {
                try { icu.discardWorkingCopy(); } catch (Exception ex) { }
            }
        }
    }

    /**
     * Computes a simple line-based diff (LCS) between the original and refactored code and highlights:
     *  - Deleted/only-in-left lines in red background (left side)
     *  - Inserted/only-in-right lines in green background (right side)
     *  - Modified (delete immediately followed by insert) lines in light yellow on both sides
     */
    private void diffAndHighlight(StyledText leftText, StyledText rightText) {
        String leftAll = leftText.getText();
        String rightAll = rightText.getText();
        if (leftAll.equals(rightAll)) return; // nothing to highlight

        String[] leftLines = leftAll.split("\r?\n", -1);
        String[] rightLines = rightAll.split("\r?\n", -1);
        int m = leftLines.length;
        int n = rightLines.length;
        int[][] lcs = new int[m + 1][n + 1];
        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                if (leftLines[i].equals(rightLines[j])) {
                    lcs[i][j] = 1 + lcs[i + 1][j + 1];
                } else {
                    lcs[i][j] = Math.max(lcs[i + 1][j], lcs[i][j + 1]);
                }
            }
        }
        // Backtrack to produce operations
        class Op { String type; int i; int j; Op(String t,int i,int j){this.type=t;this.i=i;this.j=j;} }
        List<Op> ops = new ArrayList<>();
        int i = 0, j = 0;
        while (i < m && j < n) {
            if (leftLines[i].equals(rightLines[j])) { ops.add(new Op("EQUAL", i++, j++)); }
            else if (lcs[i + 1][j] >= lcs[i][j + 1]) { ops.add(new Op("DELETE", i++, -1)); }
            else { ops.add(new Op("INSERT", -1, j++)); }
        }
        while (i < m) ops.add(new Op("DELETE", i++, -1));
        while (j < n) ops.add(new Op("INSERT", -1, j++));

        // Determine modified pairs (DELETE followed immediately by INSERT)
        boolean[] modifiedMarker = new boolean[ops.size()];
        for (int k = 0; k < ops.size() - 1; k++) {
            Op a = ops.get(k);
            Op b = ops.get(k + 1);
            if (a.type.equals("DELETE") && b.type.equals("INSERT")) {
                modifiedMarker[k] = true; modifiedMarker[k + 1] = true; // mark both
            }
        }

        // Collect line indices
        List<Integer> leftDeleted = new ArrayList<>();
        List<Integer> rightInserted = new ArrayList<>();
        List<Integer> leftModified = new ArrayList<>();
        List<Integer> rightModified = new ArrayList<>();
        for (int k = 0; k < ops.size(); k++) {
            Op op = ops.get(k);
            if (op.type.equals("DELETE")) {
                if (modifiedMarker[k]) leftModified.add(op.i); else leftDeleted.add(op.i);
            } else if (op.type.equals("INSERT")) {
                if (modifiedMarker[k]) rightModified.add(op.j); else rightInserted.add(op.j);
            }
        }

        // Prepare colors (already created externally)
        if (delColor == null || insColor == null || modColor == null) {
            createDiffColors(leftText);
        }

        // Build style ranges
        List<StyleRange> leftRanges = new ArrayList<>();
        for (int line : leftDeleted) addLineStyle(leftText, line, delColor, leftRanges);
        for (int line : leftModified) addLineStyle(leftText, line, modColor, leftRanges);
        List<StyleRange> rightRanges = new ArrayList<>();
        for (int line : rightInserted) addLineStyle(rightText, line, insColor, rightRanges);
        for (int line : rightModified) addLineStyle(rightText, line, modColor, rightRanges);
        // Ensure ranges are sorted & valid (defensive)
        leftRanges.sort((a,b)->Integer.compare(a.start,b.start));
        rightRanges.sort((a,b)->Integer.compare(a.start,b.start));
        try { leftText.setStyleRanges(leftRanges.toArray(new StyleRange[0])); } catch (IllegalArgumentException ex) { }
        try { rightText.setStyleRanges(rightRanges.toArray(new StyleRange[0])); } catch (IllegalArgumentException ex) { }
    }

    private void addLineStyle(StyledText styled, int line, Color bg, List<StyleRange> list) {
        if (line < 0 || line >= styled.getLineCount()) return;
        int lineOffset;
        try { lineOffset = styled.getOffsetAtLine(line); } catch (IllegalArgumentException ex) { return; }
        String lineText = styled.getLine(line);
        int length = lineText.length();
        if (length <= 0) return;
        if (lineOffset + length > styled.getCharCount()) {
            length = styled.getCharCount() - lineOffset;
            if (length <= 0) return;
        }
        StyleRange sr = new StyleRange();
        sr.start = lineOffset;
        sr.length = length;
        sr.background = bg;
        sr.foreground = black; // ensure text is black in highlighted areas
        list.add(sr);
    }

    private List<ICompilationUnit> findCompilationUnitsByFileName(String fileName) {
        List<ICompilationUnit> result = new ArrayList<>();
        try {
            ResourcesPlugin.getWorkspace().getRoot().accept((IResourceVisitor) res -> {
                if (res.getType() == IResource.FILE && fileName.equals(res.getName())) {
                    IFile file = (IFile) res;
                    var el = JavaCore.create(file);
                    if (el instanceof ICompilationUnit icu) {
                        result.add(icu);
                    }
                }
                return true;
            });
        } catch (Exception e) {
            // ignore
        }
        return result;
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

    private void createDiffColors(Control control) {
        if (delColor != null) return;
        // More vivid but still soft enough for readability
        delColor = new Color(control.getDisplay(), 255, 200, 200); // stronger red
        insColor = new Color(control.getDisplay(), 200, 255, 200); // stronger green
        modColor = new Color(control.getDisplay(), 255, 240, 170); // stronger yellow
        control.addListener(SWT.Dispose, e -> {
            if (delColor != null && !delColor.isDisposed()) delColor.dispose();
            if (insColor != null && !insColor.isDisposed()) insColor.dispose();
            if (modColor != null && !modColor.isDisposed()) modColor.dispose();
        });
    }

    private void createLegend(Composite parent) {
        Composite legend = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(6, false);
        gl.marginWidth = 0; gl.marginHeight = 4; gl.horizontalSpacing = 10; gl.verticalSpacing = 2;
        legend.setLayout(gl);
        legend.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        addLegendEntry(legend, delColor, "Eliminado");
        addLegendEntry(legend, insColor, "Añadido");
        addLegendEntry(legend, modColor, "Modificado");
    }

    private void addLegendEntry(Composite parent, Color color, String text) {
        Label box = new Label(parent, SWT.BORDER);
        GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.widthHint = 22; gd.heightHint = 12;
        box.setLayoutData(gd);
        box.setBackground(color);
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
    }

    private void installScrollSync(StyledText a, StyledText b) {
        final boolean[] syncing = { false };
        if (a.getVerticalBar() != null) {
            a.getVerticalBar().addListener(SWT.Selection, e -> {
                if (syncing[0]) return; syncing[0] = true;
                b.setTopIndex(a.getTopIndex());
                syncing[0] = false;
            });
        }
        if (b.getVerticalBar() != null) {
            b.getVerticalBar().addListener(SWT.Selection, e -> {
                if (syncing[0]) return; syncing[0] = true;
                a.setTopIndex(b.getTopIndex());
                syncing[0] = false;
            });
        }
        if (a.getHorizontalBar() != null) {
            a.getHorizontalBar().addListener(SWT.Selection, e -> {
                if (syncing[0]) return; syncing[0] = true;
                if (b.getHorizontalBar() != null) b.getHorizontalBar().setSelection(a.getHorizontalBar().getSelection());
                syncing[0] = false;
            });
        }
        if (b.getHorizontalBar() != null) {
            b.getHorizontalBar().addListener(SWT.Selection, e -> {
                if (syncing[0]) return; syncing[0] = true;
                if (a.getHorizontalBar() != null) a.getHorizontalBar().setSelection(b.getHorizontalBar().getSelection());
                syncing[0] = false;
            });
        }
        // Mouse wheel sync fallback
        a.addListener(SWT.MouseWheel, e -> b.setTopIndex(a.getTopIndex()));
        b.addListener(SWT.MouseWheel, e -> a.setTopIndex(b.getTopIndex()));
    }

    private void createRefactorsTableSection(Composite parent) {
        Label sepTitle = new Label(parent, SWT.NONE);
        sepTitle.setText("Detalle de métodos refactorizados");
        sepTitle.setFont(bold(sepTitle));
        sepTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        String summaryText = "";
        if (actionType == ActionType.WORKSPACE && metrics instanceof WorkspaceMetrics wm) {
            List<ProjectMetrics> trimmedProjects = wm.getProjectsWithRefactors();
            int classesAffected = trimmedProjects.stream().mapToInt(p -> p.getClasses().size()).sum();
            int methodsAffected = trimmedProjects.stream().flatMap(p -> p.getClasses().stream())
                    .mapToInt(c -> c.getCurrentMethods().size()).sum();
            summaryText = "Clases afectadas: " + classesAffected + "    Métodos originales con refactor: " + methodsAffected;
        }else if (actionType == ActionType.PROJECT && metrics instanceof ProjectMetrics pm) {
            List<ClassMetrics> trimmed = pm.getMethodsWithRefactors();
            int classesAffected = trimmed.size();
            int methodsAffected = trimmed.stream().mapToInt(c -> c.getCurrentMethods().size()).sum();
            summaryText = "Clases afectadas: " + classesAffected + "    Métodos originales con refactor: " + methodsAffected;
        } else if (actionType == ActionType.CLASS && metrics instanceof ClassMetrics cm){
            List<ClassMetrics> trimmed = cm.getMethodsWithRefactors();
            int methodsAffected = trimmed.stream().mapToInt(c -> c.getCurrentMethods().size()).sum();
            summaryText = "Métodos originales con refactor: " + methodsAffected;
        }
        if (!summaryText.isEmpty()) {
            Label summary = new Label(parent, SWT.NONE);
            summary.setText(summaryText);
            summary.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        }
        Composite tableContainer = new Composite(parent, SWT.NONE);
        tableContainer.setLayout(new GridLayout(1, false));
        GridData tcGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        tcGD.heightHint = actionType == ActionType.CLASS ? 140 : 180;
        tableContainer.setLayoutData(tcGD);
        int style = SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL;
        Table table = new Table(tableContainer, style);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        if (actionType == ActionType.WORKSPACE) {
            createColumn(table, "Nº", 50);
            createColumn(table, "Proyecto", 130);
            createColumn(table, "Umbral", 80);
            createColumn(table, "Clase", 150);
            createColumn(table, "Método original", 150);
            createColumn(table, "CC método original", 120);
            createColumn(table, "Método refactorizado", 240);
            createColumn(table, "CC método refactorizado", 150);
            createColumn(table, "Algoritmo", 80);
            populateWorkspaceTable(table, (WorkspaceMetrics) metrics);
        }else if (actionType == ActionType.PROJECT) {
            createColumn(table, "Nº", 50);
            createColumn(table, "Clase", 150);
            createColumn(table, "Método original", 150);
            createColumn(table, "CC método original", 120);
            createColumn(table, "Método refactorizado", 240);
            createColumn(table, "CC método refactorizado", 150);
            createColumn(table, "Algoritmo", 80);
            populateProjectTable(table, (ProjectMetrics) metrics);
        } else if (actionType == ActionType.CLASS){
            createColumn(table, "Nº", 50);
            createColumn(table, "Método original", 160);
            createColumn(table, "CC método original", 130);
            createColumn(table, "Método refactorizado", 240);
            createColumn(table, "CC método refactorizado", 150);
            createColumn(table, "Algoritmo", 80);
            populateClassTable(table, (ClassMetrics) metrics);
        }
        Button exportBtn = new Button(tableContainer, SWT.PUSH);
        exportBtn.setText("Exportar a archivo CSV");
        GridData btnGD = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        exportBtn.setLayoutData(btnGD);
        exportBtn.addListener(SWT.Selection, e -> exportTableToCsv(table));
    }

	private void createColumn(Table table, String text, int width) {
        TableColumn col = new TableColumn(table, SWT.LEFT);
        col.setText(text);
        col.setWidth(width);
        col.setMoveable(true);
        col.setResizable(true);
    }

    private void populateProjectTable(Table table, ProjectMetrics pm) {
        java.util.Map<String, ClassMetrics> fullByName = pm.getClasses().stream()
                .collect(java.util.stream.Collectors.toMap(ClassMetrics::getName, c -> c, (a,b)->a));
        int[] rowNum = {1};
        List<ClassMetrics> trimmedList = pm.getMethodsWithRefactors();
        for (int classIdx = 0; classIdx < trimmedList.size(); classIdx++) { // fixed loop condition
            ClassMetrics trimmed = trimmedList.get(classIdx);
            ClassMetrics full = fullByName.get(trimmed.getName());
            if (full == null) continue;
            for (int origIdx=0; origIdx<trimmed.getCurrentMethods().size(); origIdx++) {
                MethodMetrics original = trimmed.getCurrentMethods().get(origIdx);
                String baseName = original.getName();
                if (baseName == null) continue;
                List<MethodMetrics> refactoredAll = full.getRefactoredMethods().stream()
                        .filter(m -> m.getName() != null && (m.getName().equals(baseName) || m.getName().startsWith(baseName + "_ext_")))
                        .sorted((m1,m2)->{
                            boolean b1 = m1.getName().equals(baseName);
                            boolean b2 = m2.getName().equals(baseName);
                            if (b1 && !b2) return -1;
                            if (!b1 && b2) return 1;
                            return m1.getName().compareTo(m2.getName());
                        })
                        .collect(java.util.stream.Collectors.toList());
                if (refactoredAll.isEmpty()) continue;
                for (int idx=0; idx<refactoredAll.size(); idx++) {
                    MethodMetrics ref = refactoredAll.get(idx);
                    TableItem item = new TableItem(table, SWT.NONE);
                    String algorithm = original.isUsedILP() ? "ILP" : "ESH";
                    if (idx == 0) {
                        item.setText(new String[] {
                                Integer.toString(rowNum[0]++),
                                trimmed.getName(),
                                baseName,
                                Integer.toString(original.getCc()),
                                ref.getName(),
                                Integer.toString(ref.getCc()),
                                algorithm
                        });
                    } else {
                        item.setText(new String[] {
                                "",
                                "",
                                "",
                                "",
                                ref.getName(),
                                Integer.toString(ref.getCc()),
                                ""
                        });
                    }
                }
                // separator after each original method group
                addSeparatorRow(table);
            }
            // thicker separator between classes (optional) already handled by method separators
        }
        removeLastSeparatorIfPresent(table);
    }

    private void populateWorkspaceTable(Table table, WorkspaceMetrics wm) {
        java.util.Map<String, ProjectMetrics> fullProjects = wm.getProjects().stream()
                .collect(java.util.stream.Collectors.toMap(ProjectMetrics::getName, p -> p, (a,b)->a));
        int[] rowNum = {1};
        List<ProjectMetrics> trimmedProjects = wm.getProjectsWithRefactors();
        for (int pIdx=0; pIdx<trimmedProjects.size(); pIdx++) {
            ProjectMetrics trimmedProject = trimmedProjects.get(pIdx);
            ProjectMetrics fullProject = fullProjects.get(trimmedProject.getName());
            if (fullProject == null) continue;
            java.util.Map<String, ClassMetrics> fullClasses = fullProject.getClasses().stream()
                    .collect(java.util.stream.Collectors.toMap(ClassMetrics::getName, c -> c, (a,b)->a));
            for (ClassMetrics trimmed : trimmedProject.getClasses()) {
                ClassMetrics full = fullClasses.get(trimmed.getName());
                if (full == null) continue;
                for (MethodMetrics original : trimmed.getCurrentMethods()) {
                    String baseName = original.getName();
                    if (baseName == null) continue;
                    List<MethodMetrics> refactoredAll = full.getRefactoredMethods().stream()
                            .filter(m -> m.getName() != null && (m.getName().equals(baseName) || m.getName().startsWith(baseName + "_ext_")))
                            .sorted((m1,m2)->{ boolean b1 = m1.getName().equals(baseName); boolean b2 = m2.getName().equals(baseName); if (b1 && !b2) return -1; if (!b1 && b2) return 1; return m1.getName().compareTo(m2.getName()); })
                            .collect(java.util.stream.Collectors.toList());
                    if (refactoredAll.isEmpty()) continue;
                    for (int idx=0; idx<refactoredAll.size(); idx++) {
                        MethodMetrics ref = refactoredAll.get(idx);
                        TableItem item = new TableItem(table, SWT.NONE);
                        String algorithm = original.isUsedILP() ? "ILP" : "ESH";
                        if (idx == 0) {
                            item.setText(new String[] {
                                    Integer.toString(rowNum[0]++),
                                    trimmedProject.getName(),
                                    Integer.toString(fullProject.getComplexityThreshold()),
                                    trimmed.getName(),
                                    baseName,
                                    Integer.toString(original.getCc()),
                                    ref.getName(),
                                    Integer.toString(ref.getCc()),
                                    algorithm
                            });
                        } else {
                            item.setText(new String[] { "", "", "", "", "", "", ref.getName(), Integer.toString(ref.getCc()), "" });
                        }
                    }
                    addSeparatorRow(table);
                }
            }
        }
        removeLastSeparatorIfPresent(table);
    }

    private void populateClassTable(Table table, ClassMetrics full) {
        ClassMetrics trimmed = full.getMethodsWithRefactors().getFirst();
    	int[] rowNum = {1};
        for (MethodMetrics original : trimmed.getCurrentMethods()) {
            String baseName = original.getName();
            if (baseName == null) continue;
            List<MethodMetrics> refactoredAll = full.getRefactoredMethods().stream()
                    .filter(m -> m.getName() != null && (m.getName().equals(baseName) || m.getName().startsWith(baseName + "_ext_")))
                    .sorted((m1,m2)->{
                        boolean b1 = m1.getName().equals(baseName);
                        boolean b2 = m2.getName().equals(baseName);
                        if (b1 && !b2) return -1;
                        if (!b1 && b2) return 1;
                        return m1.getName().compareTo(m2.getName());
                    })
                    .collect(java.util.stream.Collectors.toList());
            if (refactoredAll.isEmpty()) continue;
            for (int idx=0; idx<refactoredAll.size(); idx++) {
                MethodMetrics ref = refactoredAll.get(idx);
                TableItem item = new TableItem(table, SWT.NONE);
                String algorithm = original.isUsedILP() ? "ILP" : "ESH";
                if (idx == 0) {
                    item.setText(new String[] {
                        Integer.toString(rowNum[0]++),
                        baseName,
                        Integer.toString(original.getCc()),
                        ref.getName(),
                        Integer.toString(ref.getCc()),
                        algorithm
                    });
                } else {
                    item.setText(new String[] {
                        "",
                        "",
                        "",
                        ref.getName(),
                        Integer.toString(ref.getCc()),
                        ""
                    });
                }
            }
            addSeparatorRow(table);
        }
        removeLastSeparatorIfPresent(table);
    }

    private void addSeparatorRow(Table table) {
        int cols = table.getColumnCount();
        if (cols == 0) return;
        TableItem sep = new TableItem(table, SWT.NONE);
        String line = "";
        // build a horizontal rule style line for last two columns; keep others empty
        for (int i=0;i<cols;i++) {
            if (i == cols-2) line = "────────"; // simple visual separator
            sep.setText(i, i == cols-2 ? line : "");
        }
        sep.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    }

    private void removeLastSeparatorIfPresent(Table table) {
        int count = table.getItemCount();
        if (count == 0) return;
        TableItem last = table.getItem(count-1);
        boolean isSep = true;
        for (int c=0;c<table.getColumnCount();c++) {
            String txt = last.getText(c);
            if (!txt.isBlank()) {
                // allow only separator pattern
                if (!(txt.equals("────────"))) { isSep = false; break; }
            }
        }
        if (isSep) last.dispose();
    }

    // --- CSV Export Helper Methods ---
    private void exportTableToCsv(Table table) {
        if (table.isDisposed()) return;
        FileDialog dialog = new FileDialog(table.getShell(), SWT.SAVE);
        dialog.setText("Exportar tabla a CSV");
        dialog.setFilterExtensions(new String[] {"*.csv"});
        dialog.setFilterNames(new String[] {"CSV (separado por comas)"});
        dialog.setFileName(buildDefaultCsvFileName());
        String pathStr = dialog.open();
        if (pathStr == null) return; // usuario canceló
        if (!pathStr.toLowerCase().endsWith(".csv")) {
            pathStr += ".csv";
        }
        Path path = Paths.get(pathStr);

        int colCount = table.getColumnCount();
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // Encabezados
            for (int c=0;c<colCount;c++) {
                bw.write(csvEscape(table.getColumn(c).getText()));
                if (c < colCount - 1) bw.write(',');
            }
            bw.write('\n');
            // Filas
            for (TableItem item : table.getItems()) {
                if (isSeparator(item, colCount)) continue; // saltar separadores
                boolean allBlank = true;
                for (int c=0;c<colCount;c++) {
                    String cell = item.getText(c);
                    if (!cell.isBlank()) allBlank = false;
                    bw.write(csvEscape(cell));
                    if (c < colCount - 1) bw.write(',');
                }
                if (!allBlank) bw.write('\n');
            }
        } catch (IOException ex) {
            MessageDialog.openError(table.getShell(), "Error", "No se pudo exportar el archivo: " + ex.getMessage());
            return;
        }
        MessageDialog.openInformation(table.getShell(), "Exportación completada", "Archivo CSV exportado en: " + path.toString());
    }

    private String buildDefaultCsvFileName() {
        String tipo = analysisTypeToken();
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm").format(LocalDateTime.now());
        return "Refactorer_" + tipo + "_" + timestamp + ".csv";
    }

    private String analysisTypeToken() {
        switch (actionType) {
            case CLASS: return "clase";
            case PROJECT: return "proyecto";
            case WORKSPACE: return "workspace";
            default: return "analisis";
        }
    }

    private String csvEscape(String cell) {
        if (cell == null) return "";
        boolean needsQuotes = cell.contains(",") || cell.contains("\n") || cell.contains("\r") || cell.contains("\"") || cell.startsWith(" ") || cell.endsWith(" ");
        String v = cell.replace("\"", "\"\"");
        if (needsQuotes) return '"' + v + '"';
        return v;
    }

    // --- Existing helper used by CSV (separator detection) ---
    private boolean isSeparator(TableItem item, int colCount) {
        int nonBlank = 0;
        for (int c=0;c<colCount;c++) {
            String t = item.getText(c);
            if (!t.isBlank()) {
                nonBlank++;
                if (!t.equals("────────")) return false; // some other text -> not separator
            }
        }
        return nonBlank == 1; // exactly one non-blank cell with the dashes
    }

    // Retained for potential future use (now unused) - previously for clipboard export
    private String escapeCell(String cell) {
        if (cell == null) return "";
        // For TSV minimal escaping: replace newlines/tabs
        return cell.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
    }
}
