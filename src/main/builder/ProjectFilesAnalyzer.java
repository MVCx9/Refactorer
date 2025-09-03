package main.builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;

import main.analyzer.ComplexityAnalyzer;
import main.common.error.AnalyzeException;
import main.common.utils.Utils;
import main.refactor.CodeExtractionEngine;

public class ProjectFilesAnalyzer {

	private final ComplexityAnalyzer analyzer;

	public ProjectFilesAnalyzer() {
		CodeExtractionEngine engine = new CodeExtractionEngine();
		this.analyzer = new ComplexityAnalyzer(engine);
	}

	/**
	 * Analiza un fichero .java (IFile) devolviendo un FileAnalysis con las clases y
	 * sus métodos evaluados (actual vs refactorizado).
	 */
	public ClassAnalysis analyzeFile(IFile file) throws CoreException {
		Objects.requireNonNull(file, "file");

		try {
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);
			if (icu == null) {
				throw new IllegalStateException("Refactorer: Cannot create ICompilationUnit from file: " + file.getName());
			}

			CompilationUnit cu = Utils.parserAST(icu);

			return analyzer.analyze(cu, icu);

		} catch (Exception e) {
			throw new AnalyzeException("Refactorer: Error analyzing file: " + file.getName(), e);
		}
	}

	/**
	 * Analiza un proyecto Java completo. Reutiliza analyzeFile para cada unidad.
	 */
	public ProjectAnalysis analyzeProject(IProject project) throws CoreException {
		Objects.requireNonNull(project, "project");
		if (!project.isOpen())
			return null;

		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null)
			return null;

		List<ClassAnalysis> analyses = new ArrayList<>();

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
					analyses.add(analyzeFile(file));
				}
			}
		}

		return ProjectAnalysis.builder()
				.project(project)
				.name(project.getName())
				.analysisDate(LocalDateTime.now())
				.classes(analyses)
				.build();
	}

}