package test.analyzer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import main.analyzer.ComplexityAnalyzer;
import main.builder.ClassAnalysis;
import main.refactor.CodeExtractionEngine;
import main.refactor.RefactorComparison;
import main.error.AnalyzeException;
import test.analyzer.mother.CodeExtractionEngineMother;
import test.analyzer.mother.CompilationUnitMother;
import test.analyzer.mother.ICompilationUnitMother;
import test.analyzer.mother.MethodDeclarationMother;
import test.analyzer.mother.RefactorComparisonMother;
import test.analyzer.mother.TypeDeclarationMother;

public class ComplexityAnalyzerTest {

	private CodeExtractionEngine engine;
	private ComplexityAnalyzer analyzer;

	@BeforeEach
	void setUp() {
		this.engine = mock(CodeExtractionEngine.class);
		this.analyzer = new ComplexityAnalyzer(this.engine);
	}

	private static void toStrictEqual(Object expected, Object actual) {
		org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
	}

	@Test
	@DisplayName("Empty result when there are no refactor suggestions")
	void given_classWithSingleMethodAndNoRefactorSuggestions_when_analyze_should_returnEmptyClassAnalysis() throws Exception {
		final String className = "SampleClass";
		final String methodName = "doWork";
		final MethodDeclaration method = MethodDeclarationMother.simple(methodName, 0, 10);
		final TypeDeclaration type = TypeDeclarationMother.withMethods(className, method);
		final CompilationUnit cu = CompilationUnitMother.withSingleType(type, method, 1, 11);
		final ICompilationUnit icu = ICompilationUnitMother.any();

		when(this.engine.analyseAndPlan(cu, method, anyInt(), anyInt())).thenReturn(List.of());

		final ClassAnalysis result = this.analyzer.analyze(cu, icu);

		assertNotNull(result);
		assertNull(result.getClassName());
		toStrictEqual(0, result.getCurrentMethods().size());
		toStrictEqual(0, result.getRefactoredMethods().size());
		assertNotNull(result.getAnalysisDate());
	}

	@Test
	@DisplayName("Class analysis filled when there are refactor suggestions")
	void given_classWithSingleMethodAndRefactorSuggestions_when_analyze_should_buildClassAnalysisWithMethods() throws Exception {
		final String className = "SampleClass";
		final String methodName = "doWork";
		final MethodDeclaration method = MethodDeclarationMother.simple(methodName, 0, 10);
		final TypeDeclaration type = TypeDeclarationMother.withMethods(className, method);
		final CompilationUnit cu = CompilationUnitMother.withSingleType(type, method, 1, 11);
		final ICompilationUnit icu = ICompilationUnitMother.any();

		final List<RefactorComparison> comparisons = RefactorComparisonMother.originalAndExtraction(methodName, 10, 11, 5, 6);
		when(this.engine.analyseAndPlan(cu, method, anyInt(), anyInt())).thenReturn(comparisons);

		final ClassAnalysis result = this.analyzer.analyze(cu, icu);

		toStrictEqual(className, result.getClassName());
		toStrictEqual(1, result.getCurrentMethods().size());
		toStrictEqual(comparisons.size(), result.getRefactoredMethods().size());
		toStrictEqual(icu, result.getIcu());
		toStrictEqual(cu, result.getCompilationUnit());
	}

	@Test
	@DisplayName("Second class returned when first has no refactors and second does")
	void given_multipleClassesFirstWithoutRefactorsSecondWithRefactors_when_analyze_should_returnSecondClassAnalysis() throws Exception {
		final MethodDeclaration firstMethod = MethodDeclarationMother.simple("m1", 0, 5);
		final MethodDeclaration secondMethod = MethodDeclarationMother.simple("m2", 10, 5);
		final TypeDeclaration firstType = TypeDeclarationMother.withMethods("First", firstMethod);
		final TypeDeclaration secondType = TypeDeclarationMother.withMethods("Second", secondMethod);
		final CompilationUnit cu = CompilationUnitMother.withTwoTypes(firstType, secondType, firstMethod, secondMethod, 1, 3, 4, 8);
		final ICompilationUnit icu = ICompilationUnitMother.any();

		when(this.engine.analyseAndPlan(cu, firstMethod, anyInt(), anyInt())).thenReturn(List.of());
		final List<RefactorComparison> secondComparisons = RefactorComparisonMother.originalAndExtraction("m2", 8, 10, 4, 5);
		when(this.engine.analyseAndPlan(cu, secondMethod, anyInt(), anyInt())).thenReturn(secondComparisons);

		final ClassAnalysis result = this.analyzer.analyze(cu, icu);

		toStrictEqual("Second", result.getClassName());
		toStrictEqual(1, result.getCurrentMethods().size());
		toStrictEqual(secondComparisons.size(), result.getRefactoredMethods().size());
	}

	@Test
	@DisplayName("First class returned even if later classes also have refactors")
	void given_multipleClassesFirstWithRefactorsSecondWithRefactors_when_analyze_should_returnFirstClassAnalysisOnly() throws Exception {
		final MethodDeclaration firstMethod = MethodDeclarationMother.simple("m1", 0, 5);
		final MethodDeclaration secondMethod = MethodDeclarationMother.simple("m2", 10, 5);
		final TypeDeclaration firstType = TypeDeclarationMother.withMethods("First", firstMethod);
		final TypeDeclaration secondType = TypeDeclarationMother.withMethods("Second", secondMethod);
		final CompilationUnit cu = CompilationUnitMother.withTwoTypes(firstType, secondType, firstMethod, secondMethod, 1, 3, 4, 8);
		final ICompilationUnit icu = ICompilationUnitMother.any();

		final List<RefactorComparison> firstComparisons = RefactorComparisonMother.originalAndExtraction("m1", 10, 12, 6, 7);
		final List<RefactorComparison> secondComparisons = RefactorComparisonMother.originalAndExtraction("m2", 8, 10, 4, 5);
		when(this.engine.analyseAndPlan(cu, firstMethod, anyInt(), anyInt())).thenReturn(firstComparisons);
		when(this.engine.analyseAndPlan(cu, secondMethod, anyInt(), anyInt())).thenReturn(secondComparisons);

		final ClassAnalysis result = this.analyzer.analyze(cu, icu);

		toStrictEqual("First", result.getClassName());
		toStrictEqual(1, result.getCurrentMethods().size());
		toStrictEqual(firstComparisons.size(), result.getRefactoredMethods().size());
	}

	@Test
	@DisplayName("AnalyzeException thrown when engine throws CoreException")
	void given_engineThrowsCoreException_when_analyze_throws_AnalyzeException() throws Exception {
		final MethodDeclaration method = MethodDeclarationMother.simple("failMethod", 0, 5);
		final TypeDeclaration type = TypeDeclarationMother.withMethods("FailClass", method);
		final CompilationUnit cu = CompilationUnitMother.withSingleType(type, method, 1, 3);
		final ICompilationUnit icu = ICompilationUnitMother.any();

		doThrow(new CoreException(null)).when(this.engine).analyseAndPlan(cu, method, anyInt(), anyInt());

		final AnalyzeException ex = assertThrows(AnalyzeException.class, () -> this.analyzer.analyze(cu, icu));
		assertTrue(ex.getMessage().contains("failMethod"));
	}

	@Test
	@DisplayName("Null dependency in constructor")
	void given_nullExtractionEngine_when_createAnalyzer_throws_NullPointerException() {
		assertThrows(NullPointerException.class, () -> new ComplexityAnalyzer(null));
	}
}