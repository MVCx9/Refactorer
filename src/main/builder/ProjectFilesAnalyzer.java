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
import org.eclipse.jdt.core.dom.TypeDeclaration;

import main.analyzer.ComplexityAnalyzer;
import main.common.error.AnalyzeException;
import main.common.utils.Utils;

public class ProjectFilesAnalyzer {

	private final ComplexityAnalyzer analyzer;

	public ProjectFilesAnalyzer() {
		this.analyzer = new ComplexityAnalyzer();
	}

	/**
	 * Analiza un fichero .java (IFile) devolviendo un ClassAnalysis solo si el fichero
	 * contiene al menos una clase top-level. Se ignoran ficheros que solo definan
	 * enum, interface o record. Devuelve null si no hay ninguna clase.
	 */
	public ClassAnalysis analyzeFile(IFile file) throws CoreException {
		Objects.requireNonNull(file, "file");

		try {
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);
			if (icu == null) {
				throw new IllegalStateException("Refactorer: Cannot create ICompilationUnit from file: " + file.getName());
			}

			CompilationUnit cu = Utils.parserAST(icu);

			// Comprobar si existe al menos una clase (TypeDeclaration que no sea interface)
			@SuppressWarnings("unchecked")
			List<Object> topLevelTypes = cu.types();
			boolean hasClass = false;
			for (Object t : topLevelTypes) {
				if (t instanceof TypeDeclaration) { // TypeDeclaration cubre class o interface
					TypeDeclaration td = (TypeDeclaration) t;
					if (!td.isInterface()) { // es una clase
						hasClass = true;
						break;
					}
				}
				// EnumDeclaration y RecordDeclaration NO son TypeDeclaration (y por tanto se ignoran)
			}

			if (!hasClass) {
				return null; // skip enums / interfaces / records only
			}

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
					ClassAnalysis ca = analyzeFile(file);
					if (ca != null) { // ignore non-class units
						analyses.add(ca);
					}
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