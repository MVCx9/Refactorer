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

import main.builder.ClassAnalysis;
import main.builder.ProjectAnalysis;
import main.builder.ProjectFilesAnalyzer;
import main.error.AnalyzeException;
import main.error.ResourceNotFoundException;
import main.error.ValidationException;
import main.model.clazz.ClassMetrics;
import main.model.project.ProjectAnalysisMetricsMapper;
import main.model.project.ProjectMetrics;

public class AnalyzeProjectHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = null;
		Object selected = ((IStructuredSelection) HandlerUtil.getCurrentSelection(event)).getFirstElement();

		if (selected == null) {
			throw new ResourceNotFoundException("No hay proyecto para analizar");
		}

		if (selected instanceof IAdaptable) {
			project = ((IAdaptable) selected).getAdapter(IProject.class);
		}

		if (project == null) {
			throw new ValidationException("La selección no es un proyecto Eclipse válido.");
		}

		ProjectFilesAnalyzer analyzer = new ProjectFilesAnalyzer();
		List<ClassAnalysis> classesAnalyses = new ArrayList<>();

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
							classesAnalyses.add(analyzer.analyzeFile(file));
						}
					}
				}
			}
		} catch (CoreException e) {
			throw new AnalyzeException("Error analyzing project", e);
		}

		ProjectAnalysis analysis = ProjectAnalysis.builder()
				.project(project)
				.name(project.getName())
				.classes(classesAnalyses).build();

		ProjectMetrics metrics = ProjectAnalysisMetricsMapper.toProjectMetrics(analysis);
		logToConsole(metrics);
		return null;
	}

	private void logToConsole(ProjectMetrics projectMetrics) {
		StringBuilder sb = new StringBuilder();
		sb.append("Refactorer — Métricas de proyecto: ").append(projectMetrics.getName()).append('\n')
		.append("  Clases ").append(projectMetrics.getClassCount()).append('\n')
		.append("  Métodos ").append(projectMetrics.getCurrentMethodCount()).append('\n')
		.append("  Current LOC: ").append(projectMetrics.getCurrentLoc()).append(" -> Refactored LOC: ").append(projectMetrics.getRefactoredLoc()).append('\n')
		.append("  Current CC: ").append(projectMetrics.getCurrentCc()).append(" -> Refactored CC: ").append(projectMetrics.getRefactoredCc())
		.append('\n');
		for (ClassMetrics cm : projectMetrics.getClasses()) {
			sb.append("    -> Clase ").append(cm.getName()).append('\n')
			.append("      Métodos ").append(cm.getCurrentMethodCount()).append('\n')
			.append("      Current LOC: ").append(cm.getCurrentLoc()).append(" -> Refactored LOC: ").append(cm.getRefactoredLoc()).append('\n')
			.append("      Current CC: ").append(cm.getCurrentCc()).append(" -> Refactored CC: ").append(cm.getRefactoredCc()).append('\n');
		}
		sb.append('\n');
		System.out.println(sb.toString());
	}
}