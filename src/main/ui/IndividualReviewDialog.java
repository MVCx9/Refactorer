package main.ui;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import main.common.languaje.Messages;
import main.common.utils.Utils;
import main.model.clazz.ClassMetrics;
import main.ui.RefactorConfirmationDialog.SelectedClassInfo;

public class IndividualReviewDialog extends TitleAreaDialog {
    
    private static final int APPLY_AND_NEXT = 1001;
    private static final int UNDO_AND_NEXT = 1002;
    private static final int NEXT = 1003;
    
    private final List<SelectedClassInfo> selectedClasses;
    private final boolean isApplyMode;
    private int currentIndex = 0;
    
    private Color delColor;
    private Color insColor;
    private Color modColor;
    private final Color black = org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
    
    private StyledText leftText;
    private StyledText rightText;
    private Composite mainContainer;
    
    public IndividualReviewDialog(Shell parentShell, List<SelectedClassInfo> selectedClasses, boolean isApplyMode) {
        super(parentShell);
        this.selectedClasses = selectedClasses;
        this.isApplyMode = isApplyMode;
        setHelpAvailable(false);
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getIndividualReviewTitle());
        newShell.setMinimumSize(1100, 700);
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        mainContainer = (Composite) super.createDialogArea(parent);
        mainContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainContainer.setLayout(new GridLayout(1, false));
        
        updateContent();
        
        return mainContainer;
    }
    
    private void updateContent() {
        for (Control child : mainContainer.getChildren()) {
            child.dispose();
        }
        
        if (currentIndex >= selectedClasses.size()) {
            MessageDialog.openInformation(getShell(), Messages.getInfoTitle(), Messages.getAllClassesReviewedMessage());
            close();
            return;
        }
        
        SelectedClassInfo currentClass = selectedClasses.get(currentIndex);
        ClassMetrics cm = currentClass.classMetrics;
        
        setTitle(Messages.getIndividualReviewProgress(currentIndex + 1, selectedClasses.size()));
        setMessage(currentClass.projectName + " - " + cm.getName());
        
        SashForm codeSash = new SashForm(mainContainer, SWT.HORIZONTAL);
        codeSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        Composite codeLeft = new Composite(codeSash, SWT.NONE);
        codeLeft.setLayout(new GridLayout(1, false));
        Label codeLeftTitle = new Label(codeLeft, SWT.NONE);
        codeLeftTitle.setText(Messages.getCodeSectionCurrent());
        codeLeftTitle.setFont(bold(codeLeftTitle));
        leftText = new StyledText(codeLeft, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY | SWT.MULTI);
        leftText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        leftText.setText(cm.getCurrentSource() != null ? cm.getCurrentSource() : "");
        leftText.setFont(JFaceResources.getTextFont());
        
        Composite codeRight = new Composite(codeSash, SWT.NONE);
        codeRight.setLayout(new GridLayout(1, false));
        Label codeRightTitle = new Label(codeRight, SWT.NONE);
        codeRightTitle.setText(Messages.getCodeSectionRefactored());
        codeRightTitle.setFont(bold(codeRightTitle));
        rightText = new StyledText(codeRight, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY | SWT.MULTI);
        rightText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        rightText.setText(cm.getRefactoredSource() != null ? cm.getRefactoredSource() : "");
        rightText.setFont(JFaceResources.getTextFont());
        
        codeSash.setWeights(new int[] { 1, 1 });
        
        createDiffColors(mainContainer);
        diffAndHighlight(leftText, rightText);
        installScrollSync(leftText, rightText);
        createLegend(mainContainer);
        
        mainContainer.layout(true, true);
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, UNDO_AND_NEXT, Messages.getButtonUndoExtractions(), false);
        createButton(parent, APPLY_AND_NEXT, Messages.getButtonApplyExtractions(), false);
        createButton(parent, NEXT, Messages.getButtonNext(), true);
        createButton(parent, CANCEL, Messages.getButtonClose(), false);
    }
    
    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == APPLY_AND_NEXT) {
            applyCurrentClass();
            moveToNext();
            return;
        }
        if (buttonId == UNDO_AND_NEXT) {
            undoCurrentClass();
            moveToNext();
            return;
        }
        if (buttonId == NEXT) {
            moveToNext();
            return;
        }
        super.buttonPressed(buttonId);
    }
    
    private void moveToNext() {
        currentIndex++;
        if (currentIndex >= selectedClasses.size()) {
            MessageDialog.openInformation(getShell(), Messages.getInfoTitle(), Messages.getAllClassesReviewedMessage());
            close();
        } else {
            updateContent();
        }
    }
    
    private void applyCurrentClass() {
        if (currentIndex < selectedClasses.size()) {
            SelectedClassInfo info = selectedClasses.get(currentIndex);
            applyForClass(info.classMetrics, info.classMetrics.getRefactoredSource());
        }
    }
    
    private void undoCurrentClass() {
        if (currentIndex < selectedClasses.size()) {
            SelectedClassInfo info = selectedClasses.get(currentIndex);
            applyForClass(info.classMetrics, info.classMetrics.getCurrentSource());
        }
    }
    
    private void applyForClass(ClassMetrics cm, String source) {
        if (source == null || source.isEmpty()) return;
        String fileName = cm.getName();
        if (fileName == null || fileName.isBlank()) return;
        if (!fileName.endsWith(".java")) fileName = fileName + ".java";

        List<ICompilationUnit> units = findCompilationUnitsByFileName(fileName);
        String formatted = Utils.formatJava(source);
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
    
    private Font bold(Control c) {
        org.eclipse.swt.graphics.FontData[] fds = c.getFont().getFontData();
        for (org.eclipse.swt.graphics.FontData fd : fds) {
            fd.setStyle(SWT.BOLD);
        }
        org.eclipse.swt.graphics.Font f = new org.eclipse.swt.graphics.Font(c.getDisplay(), fds);
        c.addListener(SWT.Dispose, e -> f.dispose());
        return f;
    }
    
    private void createDiffColors(Control control) {
        if (delColor != null) return;
        delColor = new Color(control.getDisplay(), 255, 200, 200);
        insColor = new Color(control.getDisplay(), 200, 255, 200);
        modColor = new Color(control.getDisplay(), 255, 240, 170);
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
        
        addLegendEntry(legend, delColor, Messages.getLegendDeleted());
        addLegendEntry(legend, insColor, Messages.getLegendAdded());
        addLegendEntry(legend, modColor, Messages.getLegendModified());
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
        a.addListener(SWT.MouseWheel, e -> b.setTopIndex(a.getTopIndex()));
        b.addListener(SWT.MouseWheel, e -> a.setTopIndex(b.getTopIndex()));
    }
    
    private void diffAndHighlight(StyledText leftTxt, StyledText rightTxt) {
        String leftAll = leftTxt.getText();
        String rightAll = rightTxt.getText();
        if (leftAll.equals(rightAll)) return;

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

        boolean[] modifiedMarker = new boolean[ops.size()];
        for (int k = 0; k < ops.size() - 1; k++) {
            Op a = ops.get(k);
            Op b = ops.get(k + 1);
            if (a.type.equals("DELETE") && b.type.equals("INSERT")) {
                modifiedMarker[k] = true; modifiedMarker[k + 1] = true;
            }
        }

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

        if (delColor == null || insColor == null || modColor == null) {
            createDiffColors(leftTxt);
        }

        List<StyleRange> leftRanges = new ArrayList<>();
        for (int line : leftDeleted) addLineStyle(leftTxt, line, delColor, leftRanges);
        for (int line : leftModified) addLineStyle(leftTxt, line, modColor, leftRanges);
        List<StyleRange> rightRanges = new ArrayList<>();
        for (int line : rightInserted) addLineStyle(rightTxt, line, insColor, rightRanges);
        for (int line : rightModified) addLineStyle(rightTxt, line, modColor, rightRanges);
        
        leftRanges.sort((aa,bb)->Integer.compare(aa.start,bb.start));
        rightRanges.sort((aa,bb)->Integer.compare(aa.start,bb.start));
        try { leftTxt.setStyleRanges(leftRanges.toArray(new StyleRange[0])); } catch (IllegalArgumentException ex) { }
        try { rightTxt.setStyleRanges(rightRanges.toArray(new StyleRange[0])); } catch (IllegalArgumentException ex) { }
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
        sr.foreground = black;
        list.add(sr);
    }
}
