package main.ui;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
 * Dialog to display error details with a simplified initial view and detailed error information.
 */
public class ErrorDetailsDialog extends Dialog {

    private static final String DEFAULT_ERROR_MESSAGE = "An error occurred in the Refactorer plugin";
    private static final int VIEW_ERROR_ID = IDialogConstants.CLIENT_ID + 1;

    private final Throwable error;
    private final String customTitle;

    public ErrorDetailsDialog(Shell parentShell, String title, Throwable error) {
        super(parentShell);
        this.customTitle = title != null ? title : "Error";
        this.error = error;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(customTitle);
        shell.setMinimumSize(400, 200);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));

        Label messageLabel = new Label(container, SWT.WRAP);
        messageLabel.setText(DEFAULT_ERROR_MESSAGE);
        messageLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.widthHint = 350;
        messageLabel.setLayoutData(gd);

        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, VIEW_ERROR_ID, "View Error", false);
        createButton(parent, IDialogConstants.OK_ID, "Close", true);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == VIEW_ERROR_ID) {
            openDetailedErrorDialog();
        } else {
            super.buttonPressed(buttonId);
        }
    }

    private void openDetailedErrorDialog() {
        new DetailedErrorDialog(getShell(), customTitle, error).open();
    }

    public static void open(Shell shell, String title, Throwable error) {
        new ErrorDetailsDialog(shell, title, error).open();
    }

    /**
     * Inner dialog class for displaying detailed error information.
     */
    private static class DetailedErrorDialog extends TitleAreaDialog {

        private final Throwable error;
        private final String title;
        private Text stackTraceText;

        public DetailedErrorDialog(Shell parentShell, String title, Throwable error) {
            super(parentShell);
            this.title = title != null ? title : "Error Details";
            this.error = error;
            setHelpAvailable(false);
        }

        @Override
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            shell.setText(title);
            shell.setMinimumSize(800, 600);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite container = (Composite) super.createDialogArea(parent);
            setTitle(title);
            setMessage(error != null ? buildSafeMessage(error) : "An unknown error has occurred");

            Composite content = new Composite(container, SWT.NONE);
            content.setLayout(new GridLayout(1, false));
            content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            createMessageSection(content);
            createStackTraceSection(content);
            createCopyButton(content);

            return container;
        }

        private void createMessageSection(Composite parent) {
            Label lblMsgTitle = new Label(parent, SWT.NONE);
            lblMsgTitle.setText("Message:");
            lblMsgTitle.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));

            Text txtMessage = new Text(parent, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
            txtMessage.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            txtMessage.setText(error != null ? buildFullMessage(error) : "<no information>");
        }

        private void createStackTraceSection(Composite parent) {
            Label lblStack = new Label(parent, SWT.NONE);
            lblStack.setText("Stack Trace:");
            lblStack.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
            lblStack.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            stackTraceText = new Text(parent, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
            GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
            gd.heightHint = 380;
            stackTraceText.setLayoutData(gd);
            stackTraceText.setFont(JFaceResources.getTextFont());
            stackTraceText.setText(buildStackTrace(error));
        }

        private void createCopyButton(Composite parent) {
            Button copyBtn = new Button(parent, SWT.PUSH);
            copyBtn.setText("Copy to Clipboard");
            copyBtn.addListener(SWT.Selection, e -> {
                stackTraceText.selectAll();
                stackTraceText.copy();
            });
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            createButton(parent, IDialogConstants.OK_ID, "Close", true);
        }

        private String buildSafeMessage(Throwable t) {
            String message = t.getMessage();
            if (message == null || message.isBlank()) {
                return t.getClass().getSimpleName();
            }
            return t.getClass().getSimpleName() + ": " + message;
        }

        private String buildFullMessage(Throwable t) {
            StringBuilder sb = new StringBuilder();
            Throwable current = t;
            while (current != null) {
                if (sb.length() > 0) {
                    sb.append("\nCaused by: ");
                }
                sb.append(current.getClass().getName());
                if (current.getMessage() != null && !current.getMessage().isBlank()) {
                    sb.append(": ").append(current.getMessage());
                }
                current = current.getCause();
            }
            return sb.toString();
        }

        private String buildStackTrace(Throwable t) {
            if (t == null) {
                return "<no stack trace>";
            }
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.flush();
            return sw.toString();
        }
    }
}
