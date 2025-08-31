package main.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import main.builder.FileAnalysis;
import main.builder.ProjectFilesAnalyzer;
import main.error.AnalyzeException;
import main.error.ResourceNotFoundException;
import main.model.clazz.ClassAnalysisMetricsMapper;
import main.model.clazz.ClassMetrics;

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
			FileAnalysis analysis = pfa.analyzeFile(file);
			ClassMetrics cm = ClassAnalysisMetricsMapper.toClassMetrics(analysis);
			logToConsole(cm);
		} catch (CoreException e) {
			throw new AnalyzeException("Error analyzing file", e);
		}
		return null;
	}

	private void logToConsole(ClassMetrics cm) {
		StringBuilder sb = new StringBuilder();
		sb.append("Refactorer — Métricas de clase: ").append(cm.getName()).append('\n')
		.append("  Current LOC ").append(cm.getCurrentLoc()).append(" -> Refactored LOC: ").append(cm.getRefactoredLoc()).append('\n')
		.append("  Current CC ").append(cm.getCurrentCc()).append(" -> Refactored CC: ").append(cm.getRefactoredCc()).append('\n')
		.append("  Current Methods: ").append(cm.getCurrentMethodCount())
		.append(" | Refactored Methods: ").append(cm.getRefactoredMethodCount()).append('\n')
		.append('\n');
		
		sb.append('\n');
		System.out.println(sb.toString());
	}

}