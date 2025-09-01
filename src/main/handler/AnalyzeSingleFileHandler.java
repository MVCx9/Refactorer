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

import main.builder.ClassAnalysis;
import main.builder.ProjectFilesAnalyzer;
import main.error.AnalyzeException;
import main.error.ResourceNotFoundException;
import main.model.clazz.ClassAnalysisMetricsMapper;
import main.model.clazz.ClassMetrics;
import main.model.method.MethodMetrics;

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
			ClassMetrics cm = ClassAnalysisMetricsMapper.toClassMetrics(analysis);
			logToConsole(cm);
		} catch (CoreException e) {
			throw new AnalyzeException("Error analyzing file", e);
		}
		return null;
	}

	private void logToConsole(ClassMetrics cm) {
		StringBuilder sb = new StringBuilder();
		sb.append("Refactorer — Métricas de clase: ").append(cm.getName()).append(" tomadas en ").append(cm.getAnalysisDate()).append('\n')
		.append("  Current LOC ").append(cm.getAverageCurrentLoc()).append(" -> Refactored LOC: ").append(cm.getAverageRefactoredLoc()).append('\n')
		.append("  Current CC ").append(cm.getAverageCurrentCc()).append(" -> Refactored CC: ").append(cm.getAverageRefactoredCc()).append('\n')
		.append("  Current Methods: ").append(cm.getCurrentMethodCount())
		.append(" | Refactored Methods: ").append(cm.getRefactoredMethodCount()).append('\n')
		.append('\n');
		
		for(MethodMetrics mm : cm.getCurrentMethods()) {
			sb.append("    [Current] Method: ").append(mm.getName())
			.append(" | LOC: ").append(mm.getLoc())
			.append(" | CC: ").append(mm.getCc())
			.append('\n');
		}
		
		sb.append('\n');
		
		for(MethodMetrics mm : cm.getRefactoredMethods()) {
			sb.append("    [Refactored] Method: ").append(mm.getName())
			.append(" | LOC: ").append(mm.getLoc())
			.append(" | CC: ").append(mm.getCc())
			.append('\n');
		}
		
		sb.append('\n');
		System.out.println(sb.toString());
	}

}