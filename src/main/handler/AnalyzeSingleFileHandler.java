package main.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import main.builder.FileAnalysis;
import main.builder.ProjectFilesAnalyzer;

public class AnalyzeSingleFileHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		Object first = ((ICompilationUnit) ((IStructuredSelection) selection).getFirstElement()).getResource();
		if (!(first instanceof IFile)) {
			return null;
		}
			
		IFile file = (IFile) first;
		ProjectFilesAnalyzer pfa = new ProjectFilesAnalyzer();
		try {
			FileAnalysis analysis = pfa.analyzeFile(file);
			// TODO: Persistir/mostrar el resultado en tu vista o reporter.
			// Por ahora, dejamos un log simple al Console view.
			logToConsole(analysis);
		} catch (CoreException e) {
			throw new ExecutionException("Error analyzing file", e);
		}
		return null;
	}

	private void logToConsole(FileAnalysis analysis) {
		StringBuilder sb = new StringBuilder();
		sb.append("Refactorer — Análisis de fichero: ").append(analysis.file().getName()).append('\n');
		analysis.classes().forEach(ca -> {
			sb.append("  Clase ").append(ca.className()).append('\n');
			ca.methods().forEach(ma -> {
				sb.append("    ").append(ma.methodName()).append(" | CC ").append(ma.currentCc()).append(" -> ")
						.append(ma.refactoredCc()).append(" | LOC ").append(ma.currentLoc()).append(" -> ")
						.append(ma.refactoredLoc()).append(ma.extractionPlan() != null ? " | plan: YES" : " | plan: NO")
						.append('\n');
			});
		});
		System.out.println(sb.toString());
	}

}
