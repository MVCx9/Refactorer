package main.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
			ResourceNotFoundException error = new ResourceNotFoundException("No hay fichero seleccionado para analizar.");
			ErrorDetailsDialog.open(HandlerUtil.getActiveShell(event), error.getMessage(), error);
			return null;
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
			// Get IProject from file
			IProject project = file != null ? file.getProject() : null;
			
			ClassAnalysis analysis = pfa.analyzeFile(file);
			
			if(analysis == null) {
				new AnalysisNoRefactorDialog(
                        HandlerUtil.getActiveShell(event),
                        ActionType.CLASS,
                        null,
                        project).open();
                return null;
			}
			
			ClassMetrics cm = ClassAnalysisMetricsMapper.toClassMetrics(analysis);
			SessionAnalysisStore.getInstance().register(ActionType.CLASS, cm);
			
            if (cm.getMethodExtractionCount() == 0) {
                new AnalysisNoRefactorDialog(
                        HandlerUtil.getActiveShell(event),
                        ActionType.CLASS,
                        cm,
                        project).open();
                return null;
            }
			new AnalysisMetricsDialog(
					HandlerUtil.getActiveShell(event), 
					ActionType.CLASS, 
					cm, 
					cm.getCurrentSource(), 
					cm.getRefactoredSource() != null ? cm.getRefactoredSource() : cm.getCurrentSource(),
					project)
				.open();
			
			return null;
			
		} catch (Throwable e) {
			AnalyzeException error = new AnalyzeException("Error analyzing class", e);
			ErrorDetailsDialog.open(HandlerUtil.getActiveShell(event), error.getMessage(), error);
			return null;
		}
	}

}