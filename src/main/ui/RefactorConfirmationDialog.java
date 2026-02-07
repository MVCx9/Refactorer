package main.ui;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
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

public class RefactorConfirmationDialog extends Dialog {
    
    public static final int APPLY_ALL = 1001;
    public static final int SELECT_INDIVIDUALLY = 1002;
    
    private final List<SelectedClassInfo> selectedClasses;
    private final boolean isApply;
    
    public RefactorConfirmationDialog(Shell parentShell, List<SelectedClassInfo> selectedClasses, boolean isApply) {
        super(parentShell);
        this.selectedClasses = selectedClasses;
        this.isApply = isApply;
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(isApply ? Messages.getConfirmRefactorTitle() : Messages.getConfirmUndoTitle());
        newShell.setMinimumSize(500, 400);
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        Label messageLabel = new Label(container, SWT.WRAP);
        messageLabel.setText(isApply ? Messages.getConfirmRefactorMessage() : Messages.getConfirmUndoMessage());
        GridData msgGd = new GridData(SWT.FILL, SWT.TOP, true, false);
        msgGd.widthHint = 450;
        messageLabel.setLayoutData(msgGd);
        
        Label spacer = new Label(container, SWT.NONE);
        spacer.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        Label classesHeader = new Label(container, SWT.NONE);
        classesHeader.setText(Messages.getSelectedClassesHeader());
        classesHeader.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        Table classesTable = new Table(container, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        classesTable.setHeaderVisible(true);
        classesTable.setLinesVisible(true);
        GridData tableGd = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGd.heightHint = 200;
        classesTable.setLayoutData(tableGd);
        
        TableColumn projectCol = new TableColumn(classesTable, SWT.LEFT);
        projectCol.setText(Messages.getTableColumnProject());
        projectCol.setWidth(200);
        
        TableColumn classCol = new TableColumn(classesTable, SWT.LEFT);
        classCol.setText(Messages.getTableColumnClass());
        classCol.setWidth(250);
        
        for (SelectedClassInfo info : selectedClasses) {
            TableItem item = new TableItem(classesTable, SWT.NONE);
            item.setText(new String[] { info.projectName, info.classMetrics.getName() });
        }
        
        return container;
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        String applyAllText = isApply ? Messages.getButtonApplyAll() : Messages.getButtonUndoAll();
        createButton(parent, APPLY_ALL, applyAllText, true);
        createButton(parent, SELECT_INDIVIDUALLY, Messages.getButtonSelectIndividually(), false);
        createButton(parent, CANCEL, Messages.getButtonClose(), false);
    }
    
    @Override
    protected void buttonPressed(int buttonId) {
        setReturnCode(buttonId);
        close();
    }
    
    public static class SelectedClassInfo {
        public final String projectName;
        public final ClassMetrics classMetrics;
        
        public SelectedClassInfo(String projectName, ClassMetrics classMetrics) {
            this.projectName = projectName;
            this.classMetrics = classMetrics;
        }
    }
}
