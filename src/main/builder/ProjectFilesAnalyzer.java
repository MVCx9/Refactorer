package main.builder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
			char[] sourceCode = extracted(file);

			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);
			CompilationUnit cu = parserAST(sourceCode);

			List<ClassAnalysis> classes = analyzer.analyze(cu, icu);
			return FileAnalysis.of(file, icu, classes);

		} catch (Exception e) {
			throw new CoreException(
					org.eclipse.core.runtime.Status.error("Refactorer: Error analyzing file: " + file.getName(), e));
		}
	}

	private char[] extracted(IFile file) throws CoreException {
		try (InputStream is = file.getContents();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

			StringBuilder sourceCode = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sourceCode.append(line).append("\n");
			}

			return sourceCode.toString().toCharArray();

		} catch (Exception e) {
			throw new CoreException(
					org.eclipse.core.runtime.Status.error("Refactorer: Error extracting file content ", e));
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

	private CompilationUnit parserAST(char[] source) {
		ASTParser parser = ASTParser.newParser(AST.JLS21);
        parser.setSource(source);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        return (CompilationUnit) parser.createAST(null);
	}

}
