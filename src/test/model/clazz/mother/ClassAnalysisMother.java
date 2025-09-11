package test.model.clazz.mother;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import main.builder.ClassAnalysis;
import main.builder.MethodAnalysis;
import test.model.method.mother.MethodAnalysisMother;

public final class ClassAnalysisMother {
	private static final String DEFAULT_CLASS_NAME = "SampleClass";
	private static final String DEFAULT_CURRENT_SOURCE = "public class SampleClass { void m(){} void m2(){} }";
	private static final String DEFAULT_REFACTORED_SOURCE = "public class SampleClass { void m(){} void m2(){} void m_ext_0(){} }";

	private ClassAnalysisMother() {}

	public static ClassAnalysis defaultAnalysis() {
		final CompilationUnit cu = sampleCompilationUnit();
		final List<MethodAnalysis> current = List.of(MethodAnalysisMother.withMethodName("m"), MethodAnalysisMother.withMethodName("m2"));
		final List<MethodAnalysis> refactored = List.of(current.get(0), current.get(1), MethodAnalysisMother.withMethodName("m_ext_0"));
		return ClassAnalysis.builder()
				.compilationUnit(cu)
				.className(DEFAULT_CLASS_NAME)
				.analysisDate(LocalDateTime.now())
				.currentMethods(current)
				.refactoredMethods(refactored)
				.currentSource(DEFAULT_CURRENT_SOURCE)
				.refactoredSource(DEFAULT_REFACTORED_SOURCE)
				.build();
	}

	public static ClassAnalysis withName(String className) {
		final String n = className == null ? DEFAULT_CLASS_NAME : className;
		final CompilationUnit cu = sampleCompilationUnit();
		final List<MethodAnalysis> current = List.of(MethodAnalysisMother.withMethodName("m"), MethodAnalysisMother.withMethodName("m2"));
		final List<MethodAnalysis> refactored = List.of(current.get(0), current.get(1), MethodAnalysisMother.withMethodName("m_ext_0"));
		return ClassAnalysis.builder()
				.compilationUnit(cu)
				.className(n)
				.analysisDate(LocalDateTime.now())
				.currentMethods(current)
				.refactoredMethods(refactored)
				.currentSource(DEFAULT_CURRENT_SOURCE)
				.refactoredSource(DEFAULT_REFACTORED_SOURCE)
				.build();
	}

	public static ClassAnalysis custom(ICompilationUnit icu, CompilationUnit cu, IFile file, String className, LocalDateTime analysisDate, List<MethodAnalysis> currentMethods, List<MethodAnalysis> refactoredMethods, String currentSource, String refactoredSource) {
		final CompilationUnit compilationUnit = cu == null ? sampleCompilationUnit() : cu;
		final String n = className == null ? DEFAULT_CLASS_NAME : className;
		final LocalDateTime date = analysisDate == null ? LocalDateTime.now() : analysisDate;
		final List<MethodAnalysis> current = currentMethods == null ? List.of() : currentMethods;
		final List<MethodAnalysis> refactored = refactoredMethods == null ? List.of() : refactoredMethods;
		final String curSrc = currentSource == null ? DEFAULT_CURRENT_SOURCE : currentSource;
		final String refSrc = refactoredSource == null ? DEFAULT_REFACTORED_SOURCE : refactoredSource;
		return ClassAnalysis.builder()
				.icu(icu)
				.compilationUnit(compilationUnit)
				.file(file)
				.className(n)
				.analysisDate(date)
				.currentMethods(current)
				.refactoredMethods(refactored)
				.currentSource(curSrc)
				.refactoredSource(refSrc)
				.build();
	}

	private static CompilationUnit sampleCompilationUnit() {
		final ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		final String src = DEFAULT_CURRENT_SOURCE;
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(src.toCharArray());
		parser.setResolveBindings(false);
		return (CompilationUnit) parser.createAST(null);
	}
}