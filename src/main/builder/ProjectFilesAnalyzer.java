package main.builder;

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

import main.analyzer.ComplexityAnalyzer;
import main.refactor.CodeExtractionEngine;

public class ProjectFilesAnalyzer {

	private final ComplexityAnalyzer analyzer;

	public ProjectFilesAnalyzer() {
		// Inyecta el motor una sola vez para reutilizar caches si los usas
		CodeExtractionEngine engine = new CodeExtractionEngine();
		this.analyzer = new ComplexityAnalyzer(engine);
	}

	/**
	 * Analiza un fichero .java (IFile) devolviendo un FileAnalysis con las clases y
	 * sus métodos evaluados (actual vs refactorizado).
	 */
	public FileAnalysis analyzeFile(IFile file) throws CoreException {
		Objects.requireNonNull(file, "file");

		IJavaElement je = JavaCore.create(file);
		if (!(je instanceof ICompilationUnit)) {
			return FileAnalysis.empty(file);
		}
		
		ICompilationUnit icu = (ICompilationUnit) je;
		try {
			List<ClassAnalysis> classes = analyzer.analyze(icu);
			return FileAnalysis.of(file, icu, classes);
			
		} catch (Exception e) {
			throw new CoreException(
					org.eclipse.core.runtime.Status.error("Refactorer: Error analyzing file: " + file.getName(), e));
		}
	}

	/**
	 * Analiza un proyecto Java completo. Reutiliza analyzeFile para cada unidad.
	 */
	public List<FileAnalysis> analyzeProject(IProject project) throws CoreException {
		Objects.requireNonNull(project, "project");
		if (!project.isOpen())
			return List.of();

		IJavaProject jp = JavaCore.create(project);
		if (jp == null)
			return List.of();

		List<FileAnalysis> results = new ArrayList<>();

		for (IPackageFragmentRoot root : jp.getPackageFragmentRoots()) {
			if (root.getKind() != IPackageFragmentRoot.K_SOURCE)
				continue;
			for (IJavaElement pkgEl : root.getChildren()) {
				if (!(pkgEl instanceof IPackageFragment))
					continue;
				IPackageFragment pkg = (IPackageFragment) pkgEl;
				for (ICompilationUnit icu : pkg.getCompilationUnits()) {
					IFile file = (IFile) icu.getResource();
					if (file == null)
						continue;
					results.add(analyzeFile(file));
				}
			}
		}
		
		return results;
	}

}
