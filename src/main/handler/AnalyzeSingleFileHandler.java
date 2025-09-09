package main.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import main.builder.ClassAnalysis;
import main.builder.ProjectFilesAnalyzer;
import main.common.error.AnalyzeException;
import main.common.error.ResourceNotFoundException;
import main.model.clazz.ClassAnalysisMetricsMapper;
import main.model.clazz.ClassMetrics;
import main.session.ActionType;
import main.session.SessionAnalysisStore;
import main.ui.AnalysisMetricsDialog;
import main.ui.AnalysisNoRefactorDialog;
import main.ui.ErrorDetailsDialog;

public class AnalyzeSingleFileHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object classSelected = selection.getFirstElement();

		if (classSelected == null) {
			throw new ResourceNotFoundException("No hay fichero seleccionado para analizar.");
		}

		IFile file = null;
		if (classSelected instanceof ICompilationUnit) {
			ICompilationUnit unit = (ICompilationUnit) classSelected;
			try {
				file = (IFile) unit.getCorrespondingResource();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}

		if (file == null && classSelected instanceof IAdaptable) {
			file = ((IAdaptable) classSelected).getAdapter(IFile.class);
		}

		ProjectFilesAnalyzer pfa = new ProjectFilesAnalyzer();
		try {
			ClassAnalysis analysis = pfa.analyzeFile(file);
			
			if(analysis == null) {
				new AnalysisNoRefactorDialog(
                        HandlerUtil.getActiveShell(event),
                        ActionType.CLASS,
                        null).open();
                return null;
			}
			
			ClassMetrics cm = ClassAnalysisMetricsMapper.toClassMetrics(analysis);
			SessionAnalysisStore.getInstance().register(ActionType.CLASS, cm);

            if (cm.getMethodExtractionCount() == 0) {
                new AnalysisNoRefactorDialog(
                        HandlerUtil.getActiveShell(event),
                        ActionType.CLASS,
                        cm).open();
                return null;
            }

			new AnalysisMetricsDialog(
					HandlerUtil.getActiveShell(event), 
					ActionType.CLASS, 
					cm, 
					cm.getCurrentSource(), 
					cm.getRefactoredSource() != null ? cm.getRefactoredSource() : cm.getCurrentSource())
				.open();
			
			return null;
			
		} catch (Throwable e) {
			AnalyzeException error = new AnalyzeException("Error analyzing class", e);
			ErrorDetailsDialog.open(HandlerUtil.getActiveShell(event), error.getMessage(), error);
			return null;
		}
	}

}