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

public class AnalyzeSingleFileHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object classSelected = selection.getFirstElement();

		if (classSelected == null) {
			System.out.println("No hay fichero seleccionado para analizar.");
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
			FileAnalysis analysis = pfa.analyzeFile(file);
			// TODO: Persistir/mostrar el resultado en tu vista. Por ahora, logeamos
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
				sb.append("    ")
					.append(ma.methodName())
					.append(" | CC ").append(ma.currentCc())
					.append(" -> ").append(ma.refactoredCc())
					.append(" | LOC ").append(ma.currentLoc())
					.append(" -> ").append(ma.refactoredLoc())
					.append(ma.extractionPlan() != null ? " | plan: YES" : " | plan: NO")
					.append('\n');
			});
		});
		System.out.println(sb.toString());
	}

}
