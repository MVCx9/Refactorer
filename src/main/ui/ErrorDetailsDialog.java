package main.ui;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialogo para mostrar detalles completos de un error: mensaje y pila completa.
 */
public class ErrorDetailsDialog extends TitleAreaDialog {

    private final Throwable error;
    private final String customTitle;

    private Text stackTraceText;

    public ErrorDetailsDialog(Shell parentShell, String title, Throwable error) {
        super(parentShell);
        this.customTitle = title != null ? title : "Error";
        this.error = error;
        setHelpAvailable(false);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(customTitle);
        shell.setMinimumSize(800, 600);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        setTitle(customTitle);
        setMessage(error != null ? safeMessage(error) : "Se ha producido un error desconocido");

        Composite content = new Composite(container, SWT.NONE);
        content.setLayout(new GridLayout(1, false));
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label lblMsgTitle = new Label(content, SWT.NONE);
        lblMsgTitle.setText("Mensaje:");
        lblMsgTitle.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));

        Text txtMessage = new Text(content, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
        txtMessage.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        txtMessage.setText(error != null ? buildFullMessage(error) : "<sin información>");

        Label lblStack = new Label(content, SWT.NONE);
        lblStack.setText("Pila de ejecución (stack trace):");
        lblStack.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
        lblStack.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        stackTraceText = new Text(content, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 380;
        stackTraceText.setLayoutData(gd);
        stackTraceText.setFont(JFaceResources.getTextFont());
        stackTraceText.setText(getStackTrace(error));

        Button copyBtn = new Button(content, SWT.PUSH);
        copyBtn.setText("Copiar al portapapeles");
        copyBtn.addListener(SWT.Selection, e -> {
            stackTraceText.selectAll();
            stackTraceText.copy();
        });

        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, OK, "Cerrar", true);
    }

    private String safeMessage(Throwable t) {
        String m = t.getMessage();
        if (m == null || m.isBlank()) return t.getClass().getSimpleName();
        return t.getClass().getSimpleName() + ": " + m;
    }

    private String buildFullMessage(Throwable t) {
        StringBuilder sb = new StringBuilder();
        Throwable current = t;
        while (current != null) {
            if (sb.length() > 0) sb.append("\nCausado por: ");
            sb.append(current.getClass().getName());
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                sb.append(": ").append(current.getMessage());
            }
            current = current.getCause();
        }
        return sb.toString();
    }

    private String getStackTrace(Throwable t) {
        if (t == null) return "<sin pila>";
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    public static void open(Shell shell, String title, Throwable error) {
        new ErrorDetailsDialog(shell, title, error).open();
    }
}
