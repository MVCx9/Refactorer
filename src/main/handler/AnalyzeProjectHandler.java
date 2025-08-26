package main.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import main.builder.FileAnalysis;
import main.builder.ProjectAnalysis;
import main.builder.ProjectFilesAnalyzer;
import main.model.clazz.ClassMetrics;
import main.model.method.MethodMetrics;
import main.model.project.ProjectAnalysisMetricsMapper;
import main.model.project.ProjectMetrics;

public class AnalyzeProjectHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = null;
		Object selected = ((IStructuredSelection) HandlerUtil.getCurrentSelection(event)).getFirstElement();

		if (selected == null) {
			System.out.println("No hay proyecto para analizar");
			return null;
		}

		if (selected instanceof IAdaptable) {
			project = ((IAdaptable) selected).getAdapter(IProject.class);
		}

		if (project == null) {
			System.out.println("La selección no es un proyecto Eclipse válido.");
			return null;
		}

		ProjectFilesAnalyzer analyzer = new ProjectFilesAnalyzer();
		List<FileAnalysis> fileAnalyses = new ArrayList<>();

		try {
			IJavaProject javaProject = JavaCore.create(project);
			if (javaProject != null) {
				for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()) {
					if (root.getKind() != IPackageFragmentRoot.K_SOURCE)
						continue;
					for (IJavaElement element : root.getChildren()) {
						if (!(element instanceof IPackageFragment))
							continue;
						IPackageFragment pkg = (IPackageFragment) element;
						for (ICompilationUnit icu : pkg.getCompilationUnits()) {
							IFile file = (IFile) icu.getResource();
							if (file == null)
								continue;
							fileAnalyses.add(analyzer.analyzeFile(file));
						}
					}
				}
			}
		} catch (CoreException e) {
			throw new ExecutionException("Error analyzing project", e);
		}

		ProjectAnalysis analysis = ProjectAnalysis.builder()
				.project(project)
				.name(project.getName())
				.files(fileAnalyses).build();

		ProjectMetrics metrics = ProjectAnalysisMetricsMapper.toProjectMetrics(analysis);
		logToConsole(metrics);
		return null;
	}

	private void logToConsole(ProjectMetrics projectMetrics) {
		StringBuilder sb = new StringBuilder();
		sb.append("Refactorer — Métricas de proyecto: ").append(projectMetrics.getName()).append('\n');
		sb.append("  Clases ").append(projectMetrics.getClassCount()).append('\n');
		sb.append("  Métodos ").append(projectMetrics.getCurrentMethodCount()).append('\n');
		sb.append("  LOC ").append(projectMetrics.getCurrentLoc()).append(" -> ").append(projectMetrics.getRefactoredLoc()).append('\n');
		sb.append("  CC ").append(projectMetrics.getCurrentCc()).append(" -> ").append(projectMetrics.getRefactoredCc()).append('\n');
		for (ClassMetrics cm : projectMetrics.getClasses()) {
			sb.append("    -> Clase ").append(cm.getName()).append('\n');
			sb.append("      Métodos ").append(cm.getCurrentMethodCount()).append('\n');
			sb.append("      LOC ").append(cm.getCurrentLoc()).append(" -> ").append(cm.getRefactoredLoc()).append('\n');
			sb.append("      CC ").append(cm.getCurrentCc()).append(" -> ").append(cm.getRefactoredCc()).append('\n');
			for (MethodMetrics mm : cm.getMethods()) {
				sb.append("\n   - ")
				.append(mm.getName())
				.append(" | LOC: ").append(mm.getCurrentLoc())
				.append(" -> Refactored LOC: ").append(mm.getRefactoredLoc())
				.append(" | CC: ").append(mm.getCurrentCc())
				.append(" -> Refactored CC: ").append(mm.getRefactoredCc());
			}
			sb.append('\n');
		}
		System.out.println(sb.toString());
	}
}