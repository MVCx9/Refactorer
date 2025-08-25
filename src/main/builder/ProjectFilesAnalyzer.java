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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import main.analyzer.ComplexityAnalyzer;
import main.refactor.CodeExtractionEngine;

public class ProjectFilesAnalyzer {

	private final ComplexityAnalyzer analyzer;

	public ProjectFilesAnalyzer() {
		// Inyecta el motor una sola vez para reutilizar caches
		CodeExtractionEngine engine = new CodeExtractionEngine();
		this.analyzer = new ComplexityAnalyzer(engine);
	}

	/**
	 * Analiza un fichero .java (IFile) devolviendo un FileAnalysis con las clases y
	 * sus métodos evaluados (actual vs refactorizado).
	 */
	public FileAnalysis analyzeFile(IFile file) throws CoreException {
		Objects.requireNonNull(file, "file");

		try {
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);
			if (icu == null) {
				throw new IllegalStateException("Cannot create ICompilationUnit from file: " + file.getName());
			}

			CompilationUnit cu = parserAST(icu);

			ClassAnalysis classes = analyzer.analyze(cu, icu);
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

	private CompilationUnit parserAST(ICompilationUnit icu) {
		ASTParser parser = ASTParser.newParser(AST.JLS21);
		parser.setSource(icu);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		IJavaProject jp = icu.getJavaProject();
		if (jp != null) {
			parser.setProject(jp);
		}
		try {
			return (CompilationUnit) parser.createAST(null);
		} catch (IllegalStateException ex) {
			// Fallback when the project is missing the system library / boot classpath
			ASTParser fallback = ASTParser.newParser(AST.JLS21);
			fallback.setSource(icu);
			fallback.setKind(ASTParser.K_COMPILATION_UNIT);
			fallback.setResolveBindings(false);
			fallback.setBindingsRecovery(false);
			return (CompilationUnit) fallback.createAST(null);
		}
	}

}