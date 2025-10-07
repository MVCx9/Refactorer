package main.ui;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import main.model.clazz.ClassMetrics;
import main.model.common.Identifiable;
import main.session.ActionType;
import main.session.SessionAnalysisStore;

public class AnalysisHistoryDialog extends TitleAreaDialog {

    private final List<SessionAnalysisStore.HistoryEntry<?>> history;

    public AnalysisHistoryDialog(Shell parentShell, List<SessionAnalysisStore.HistoryEntry<?>> history) {
        super(parentShell);
        this.history = history;
        setHelpAvailable(false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Historial de análisis de complejidad cognitiva");
        newShell.setMinimumSize(1000, 500);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setLayout(new GridLayout(1, false));

        setTitle("Historial de análisis de complejidad cognitiva");

        if (history == null || history.isEmpty()) {
            Label l = new Label(container, SWT.WRAP);
            l.setText("No se encuentran análisis de complejidad cognitiva");
            l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            return container;
        }

        Table table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        TableColumn colTipo = new TableColumn(table, SWT.LEFT);
        colTipo.setText("Tipo");
        colTipo.setWidth(180);
        colTipo.setResizable(false);

        TableColumn colFecha = new TableColumn(table, SWT.LEFT);
        colFecha.setText("Fecha");
        colFecha.setWidth(220);
        colFecha.setResizable(false);

        TableColumn colNombre = new TableColumn(table, SWT.LEFT);
        colNombre.setText("Nombre");
        colNombre.setWidth(400);
        colNombre.setResizable(false);

        TableColumn colAcciones = new TableColumn(table, SWT.CENTER);
        colAcciones.setText("Acciones");
        colAcciones.setWidth(160);
        colAcciones.setResizable(false);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (SessionAnalysisStore.HistoryEntry<?> e : history) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, toTipo(e.getActionType()));
            item.setText(1, e.getTimestamp() != null ? e.getTimestamp().format(fmt) : "");

            Object metrics = e.getMetrics();
            String name = metrics instanceof Identifiable ? ((Identifiable) metrics).getName() : "<sin nombre>";
            item.setText(2, name);

            TableEditor editor = new TableEditor(table);
            editor.horizontalAlignment = SWT.CENTER;
            editor.verticalAlignment = SWT.CENTER;
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            Button btn = new Button(table, SWT.PUSH);
            btn.setText("Ver análisis");
            btn.addListener(SWT.Selection, ev -> {
            	if(e.getActionType() == ActionType.CLASS) {
            		ClassMetrics metrics1 = (ClassMetrics) e.getMetrics();
            		AnalysisMetricsDialog d = new AnalysisMetricsDialog(
            				getShell(), 
            				e.getActionType(), 
            				metrics, 
            				metrics1.getCurrentSource(), 
            				metrics1.getRefactoredSource() != null ? metrics1.getRefactoredSource() : metrics1.getCurrentSource())
            				.setReadOnly(true);
            		d.open();
            	} else {
            		Object metrics1 = e.getMetrics();
            		AnalysisMetricsDialog d = new AnalysisMetricsDialog(getShell(), e.getActionType(), metrics1)
            				.setReadOnly(true);
            		d.open();
            	}
            });
            btn.pack();
            editor.minimumWidth = Math.max(140, btn.getSize().x + 8);
            editor.minimumHeight = Math.max(28, btn.getSize().y);
            editor.setEditor(btn, item, 3);
        }

        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, OK, "Cerrar", true);
    }

    private String toTipo(ActionType type) {
        if (type == null) return "";
        switch (type) {
            case CLASS: return "Clase";
            case PROJECT: return "Proyecto";
            case WORKSPACE: return "Workspace";
            default: return type.name();
        }
    }
}