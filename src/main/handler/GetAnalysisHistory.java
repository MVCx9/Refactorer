package main.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import main.session.SessionAnalysisStore;
import main.ui.AnalysisHistoryDialog;

public class GetAnalysisHistory  extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        AnalysisHistoryDialog dialog = new AnalysisHistoryDialog(shell, SessionAnalysisStore.getInstance().getHistory());
        dialog.open();
        return null;
    }

}