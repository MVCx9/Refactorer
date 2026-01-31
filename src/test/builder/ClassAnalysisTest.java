package test.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.builder.ClassAnalysis;
import main.builder.MethodAnalysis;
import test.model.method.mother.MethodAnalysisMother;

class ClassAnalysisTest {
	private final LocalDateTime fixedDate = LocalDateTime.of(2024, 6, 15, 10, 30);
	private final String currentSource = "class Test {}";
	private final String refactoredSource = "class Test { void ext_0(){} }";

	private void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("given_builderWithDefaults_when_build_should_createClassAnalysisWithDefaultValues")
	void given_builderWithDefaults_when_build_should_createClassAnalysisWithDefaultValues() {
		final ClassAnalysis analysis = ClassAnalysis.builder().build();
		assertNotNull(analysis);
		assertNotNull(analysis.getAnalysisDate());
		assertNotNull(analysis.getCurrentMethods());
		assertNotNull(analysis.getRefactoredMethods());
		toStrictEqual(15, analysis.getComplexityThreshold());
	}

	@Test
	@DisplayName("given_builderWithAllValues_when_build_should_createClassAnalysisWithAllValues")
	void given_builderWithAllValues_when_build_should_createClassAnalysisWithAllValues() {
		final List<MethodAnalysis> methods = List.of(MethodAnalysisMother.defaultAnalysis());
		final ClassAnalysis analysis = ClassAnalysis.builder()
				.className("TestClass")
				.analysisDate(this.fixedDate)
				.currentMethods(methods)
				.refactoredMethods(methods)
				.currentSource(this.currentSource)
				.refactoredSource(this.refactoredSource)
				.complexityThreshold(20)
				.build();
		toStrictEqual("TestClass", analysis.getClassName());
		toStrictEqual(this.fixedDate, analysis.getAnalysisDate());
		toStrictEqual(1, analysis.getCurrentMethods().size());
		toStrictEqual(1, analysis.getRefactoredMethods().size());
		toStrictEqual(this.currentSource, analysis.getCurrentSource());
		toStrictEqual(this.refactoredSource, analysis.getRefactoredSource());
		toStrictEqual(20, analysis.getComplexityThreshold());
	}

	@Test
	@DisplayName("given_classAnalysis_when_getCurrentMethods_should_returnUnmodifiableList")
	void given_classAnalysis_when_getCurrentMethods_should_returnUnmodifiableList() {
		final ClassAnalysis analysis = ClassAnalysis.builder()
				.currentMethods(List.of(MethodAnalysisMother.defaultAnalysis()))
				.build();
		assertThrows(UnsupportedOperationException.class, () -> analysis.getCurrentMethods().add(null));
	}

	@Test
	@DisplayName("given_classAnalysis_when_getRefactoredMethods_should_returnUnmodifiableList")
	void given_classAnalysis_when_getRefactoredMethods_should_returnUnmodifiableList() {
		final ClassAnalysis analysis = ClassAnalysis.builder()
				.refactoredMethods(List.of(MethodAnalysisMother.defaultAnalysis()))
				.build();
		assertThrows(UnsupportedOperationException.class, () -> analysis.getRefactoredMethods().add(null));
	}

	@Test
	@DisplayName("given_nullClassName_when_build_should_acceptNullClassName")
	void given_nullClassName_when_build_should_acceptNullClassName() {
		final ClassAnalysis analysis = ClassAnalysis.builder()
				.className(null)
				.build();
		toStrictEqual(null, analysis.getClassName());
	}

	@Test
	@DisplayName("given_emptyMethods_when_build_should_createEmptyLists")
	void given_emptyMethods_when_build_should_createEmptyLists() {
		final ClassAnalysis analysis = ClassAnalysis.builder()
				.currentMethods(Collections.emptyList())
				.refactoredMethods(Collections.emptyList())
				.build();
		toStrictEqual(0, analysis.getCurrentMethods().size());
		toStrictEqual(0, analysis.getRefactoredMethods().size());
	}

	@Test
	@DisplayName("given_classAnalysis_when_getFile_should_returnNullByDefault")
	void given_classAnalysis_when_getFile_should_returnNullByDefault() {
		final ClassAnalysis analysis = ClassAnalysis.builder().build();
		toStrictEqual(null, analysis.getFile());
	}

	@Test
	@DisplayName("given_classAnalysis_when_getIcu_should_returnNullByDefault")
	void given_classAnalysis_when_getIcu_should_returnNullByDefault() {
		final ClassAnalysis analysis = ClassAnalysis.builder().build();
		toStrictEqual(null, analysis.getIcu());
	}

	@Test
	@DisplayName("given_classAnalysis_when_getCompilationUnit_should_returnNullByDefault")
	void given_classAnalysis_when_getCompilationUnit_should_returnNullByDefault() {
		final ClassAnalysis analysis = ClassAnalysis.builder().build();
		toStrictEqual(null, analysis.getCompilationUnit());
	}
}
