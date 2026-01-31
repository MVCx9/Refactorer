package test.model.clazz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.model.clazz.ClassMetrics;
import main.model.method.MethodMetrics;
import test.model.clazz.mother.ClassMetricsMother;
import test.model.method.mother.MethodMetricsMother;

class ClassMetricsTest {
	private final LocalDateTime fixedDate = LocalDateTime.of(2024, 6, 15, 10, 30);

	private void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("given_builderWithDefaults_when_build_should_createClassMetricsWithDefaultValues")
	void given_builderWithDefaults_when_build_should_createClassMetricsWithDefaultValues() {
		final ClassMetrics metrics = ClassMetrics.builder().build();
		assertNotNull(metrics);
		toStrictEqual("<unnamed>", metrics.getName());
		assertNotNull(metrics.getAnalysisDate());
		toStrictEqual(0, metrics.getCurrentMethodCount());
		toStrictEqual(0, metrics.getRefactoredMethodCount());
		toStrictEqual("", metrics.getCurrentSource());
		toStrictEqual("", metrics.getRefactoredSource());
		toStrictEqual(15, metrics.getComplexityThreshold());
	}

	@Test
	@DisplayName("given_builderWithAllValues_when_build_should_createClassMetricsWithAllValues")
	void given_builderWithAllValues_when_build_should_createClassMetricsWithAllValues() {
		final List<MethodMetrics> currentMethods = List.of(MethodMetricsMother.custom("m1", 10, 3));
		final List<MethodMetrics> refactoredMethods = List.of(
				MethodMetricsMother.custom("m1", 10, 3),
				MethodMetricsMother.custom("m1_ext_0", 5, 1));
		final ClassMetrics metrics = ClassMetrics.builder()
				.name("TestClass")
				.analysisDate(this.fixedDate)
				.currentMethods(currentMethods)
				.refactoredMethods(refactoredMethods)
				.currentSource("class TestClass {}")
				.refactoredSource("class TestClass { void m1_ext_0(){} }")
				.complexityThreshold(20)
				.build();
		toStrictEqual("TestClass", metrics.getName());
		toStrictEqual(this.fixedDate, metrics.getAnalysisDate());
		toStrictEqual(1, metrics.getCurrentMethodCount());
		toStrictEqual(2, metrics.getRefactoredMethodCount());
		toStrictEqual(20, metrics.getComplexityThreshold());
	}

	@Test
	@DisplayName("given_motherDefaultMetrics_when_getValues_should_returnExpectedDefaults")
	void given_motherDefaultMetrics_when_getValues_should_returnExpectedDefaults() {
		final ClassMetrics metrics = ClassMetricsMother.defaultMetrics();
		assertNotNull(metrics);
		toStrictEqual("SampleClass", metrics.getName());
		assertNotNull(metrics.getAnalysisDate());
		toStrictEqual(2, metrics.getCurrentMethodCount());
		toStrictEqual(3, metrics.getRefactoredMethodCount());
	}

	@Test
	@DisplayName("given_motherWithName_when_getName_should_returnSpecifiedName")
	void given_motherWithName_when_getName_should_returnSpecifiedName() {
		final ClassMetrics metrics = ClassMetricsMother.withName("CustomClass");
		toStrictEqual("CustomClass", metrics.getName());
	}

	@Test
	@DisplayName("given_classMetrics_when_getCurrentLoc_should_sumMethodLocs")
	void given_classMetrics_when_getCurrentLoc_should_sumMethodLocs() {
		final List<MethodMetrics> methods = List.of(
				MethodMetricsMother.custom("m1", 10, 3),
				MethodMetricsMother.custom("m2", 20, 5));
		final ClassMetrics metrics = ClassMetrics.builder()
				.currentMethods(methods)
				.build();
		toStrictEqual(30, metrics.getCurrentLoc());
	}

	@Test
	@DisplayName("given_classMetrics_when_getCurrentCc_should_sumMethodCcs")
	void given_classMetrics_when_getCurrentCc_should_sumMethodCcs() {
		final List<MethodMetrics> methods = List.of(
				MethodMetricsMother.custom("m1", 10, 3),
				MethodMetricsMother.custom("m2", 20, 5));
		final ClassMetrics metrics = ClassMetrics.builder()
				.currentMethods(methods)
				.build();
		toStrictEqual(8, metrics.getCurrentCc());
	}

	@Test
	@DisplayName("given_classMetrics_when_getRefactoredLoc_should_sumRefactoredMethodLocs")
	void given_classMetrics_when_getRefactoredLoc_should_sumRefactoredMethodLocs() {
		final List<MethodMetrics> methods = List.of(
				MethodMetricsMother.custom("m1", 15, 2),
				MethodMetricsMother.custom("m2", 25, 4));
		final ClassMetrics metrics = ClassMetrics.builder()
				.refactoredMethods(methods)
				.build();
		toStrictEqual(40, metrics.getRefactoredLoc());
	}

	@Test
	@DisplayName("given_classMetrics_when_getReducedComplexity_should_returnDifference")
	void given_classMetrics_when_getReducedComplexity_should_returnDifference() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m1", 10, 8));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m1", 10, 5));
		final ClassMetrics metrics = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		toStrictEqual(3, metrics.getReducedComplexity());
	}

	@Test
	@DisplayName("given_classMetrics_when_getMethodExtractionCount_should_returnDifference")
	void given_classMetrics_when_getMethodExtractionCount_should_returnDifference() {
		final ClassMetrics metrics = ClassMetricsMother.defaultMetrics();
		toStrictEqual(1, metrics.getMethodExtractionCount());
	}

	@Test
	@DisplayName("given_classMetrics_when_getAverageCurrentLoc_should_computeAverage")
	void given_classMetrics_when_getAverageCurrentLoc_should_computeAverage() {
		final List<MethodMetrics> methods = List.of(
				MethodMetricsMother.custom("m1", 10, 2),
				MethodMetricsMother.custom("m2", 20, 4));
		final ClassMetrics metrics = ClassMetrics.builder()
				.currentMethods(methods)
				.build();
		toStrictEqual(15, metrics.getAverageCurrentLoc());
	}

	@Test
	@DisplayName("given_emptyMethods_when_getAverageCurrentLoc_should_returnZero")
	void given_emptyMethods_when_getAverageCurrentLoc_should_returnZero() {
		final ClassMetrics metrics = ClassMetrics.builder()
				.currentMethods(Collections.emptyList())
				.build();
		toStrictEqual(0, metrics.getAverageCurrentLoc());
	}

	@Test
	@DisplayName("given_classMetrics_when_modifyCurrentMethods_throws_UnsupportedOperationException")
	void given_classMetrics_when_modifyCurrentMethods_throws_UnsupportedOperationException() {
		final ClassMetrics metrics = ClassMetricsMother.defaultMetrics();
		assertThrows(UnsupportedOperationException.class, () -> metrics.getCurrentMethods().add(null));
	}

	@Test
	@DisplayName("given_classMetrics_when_modifyRefactoredMethods_throws_UnsupportedOperationException")
	void given_classMetrics_when_modifyRefactoredMethods_throws_UnsupportedOperationException() {
		final ClassMetrics metrics = ClassMetricsMother.defaultMetrics();
		assertThrows(UnsupportedOperationException.class, () -> metrics.getRefactoredMethods().add(null));
	}

	@Test
	@DisplayName("given_classMetrics_when_getNameUpper_should_returnUppercaseName")
	void given_classMetrics_when_getNameUpper_should_returnUppercaseName() {
		final ClassMetrics metrics = ClassMetricsMother.withName("myClass");
		toStrictEqual("MYCLASS", metrics.getNameUpper());
	}

	@Test
	@DisplayName("given_classMetricsWithNullName_when_getNameUpper_should_returnEmptyString")
	void given_classMetricsWithNullName_when_getNameUpper_should_returnEmptyString() {
		final ClassMetrics metrics = ClassMetrics.builder().name(null).build();
		toStrictEqual("", metrics.getNameUpper());
	}

	@Test
	@DisplayName("given_classMetrics_when_getImprovementCc_should_returnDifference")
	void given_classMetrics_when_getImprovementCc_should_returnDifference() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m1", 10, 10));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m1", 10, 6));
		final ClassMetrics metrics = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		toStrictEqual(4, metrics.getImprovementCc());
	}

	@Test
	@DisplayName("given_classMetrics_when_isImprovedCc_should_returnTrueWhenRefactoredLess")
	void given_classMetrics_when_isImprovedCc_should_returnTrueWhenRefactoredLess() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m1", 10, 10));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m1", 10, 6));
		final ClassMetrics metrics = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		assertTrue(metrics.isImprovedCc());
	}

	@Test
	@DisplayName("given_classMetrics_when_isImprovedCc_should_returnFalseWhenRefactoredMore")
	void given_classMetrics_when_isImprovedCc_should_returnFalseWhenRefactoredMore() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m1", 10, 5));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m1", 10, 10));
		final ClassMetrics metrics = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		assertFalse(metrics.isImprovedCc());
	}

	@Test
	@DisplayName("given_classMetrics_when_getImprovementPercentCc_should_computePercentage")
	void given_classMetrics_when_getImprovementPercentCc_should_computePercentage() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m1", 10, 100));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m1", 10, 75));
		final ClassMetrics metrics = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		toStrictEqual(25.0, metrics.getImprovementPercentCc());
	}

	@Test
	@DisplayName("given_zeroCurrentCc_when_getImprovementPercentCc_should_returnZero")
	void given_zeroCurrentCc_when_getImprovementPercentCc_should_returnZero() {
		final ClassMetrics metrics = ClassMetrics.builder()
				.currentMethods(Collections.emptyList())
				.refactoredMethods(Collections.emptyList())
				.build();
		toStrictEqual(0.0, metrics.getImprovementPercentCc());
	}

	@Test
	@DisplayName("given_classMetrics_when_getImprovementLoc_should_returnDifference")
	void given_classMetrics_when_getImprovementLoc_should_returnDifference() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m1", 50, 5));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m1", 40, 5));
		final ClassMetrics metrics = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		toStrictEqual(10, metrics.getImprovementLoc());
	}

	@Test
	@DisplayName("given_classMetrics_when_isImprovedLoc_should_returnTrueWhenRefactoredLess")
	void given_classMetrics_when_isImprovedLoc_should_returnTrueWhenRefactoredLess() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m1", 50, 5));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m1", 40, 5));
		final ClassMetrics metrics = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		assertTrue(metrics.isImprovedLoc());
	}

	@Test
	@DisplayName("given_classMetricsWithExtractedMethods_when_getMethodsWithRefactors_should_returnNonEmpty")
	void given_classMetricsWithExtractedMethods_when_getMethodsWithRefactors_should_returnNonEmpty() {
		final ClassMetrics metrics = ClassMetricsMother.defaultMetrics();
		final List<ClassMetrics> result = metrics.getMethodsWithRefactors();
		assertNotNull(result);
	}

	@Test
	@DisplayName("given_classMetricsWithNoExtractions_when_getMethodsWithRefactors_should_returnEmptyList")
	void given_classMetricsWithNoExtractions_when_getMethodsWithRefactors_should_returnEmptyList() {
		final List<MethodMetrics> methods = List.of(MethodMetricsMother.defaultMetrics());
		final ClassMetrics metrics = ClassMetrics.builder()
				.currentMethods(methods)
				.refactoredMethods(methods)
				.build();
		final List<ClassMetrics> result = metrics.getMethodsWithRefactors();
		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("given_sameClassMetrics_when_equals_should_returnTrue")
	void given_sameClassMetrics_when_equals_should_returnTrue() {
		final ClassMetrics m1 = ClassMetrics.builder()
				.name("Test")
				.analysisDate(this.fixedDate)
				.complexityThreshold(15)
				.build();
		final ClassMetrics m2 = ClassMetrics.builder()
				.name("Test")
				.analysisDate(this.fixedDate)
				.complexityThreshold(15)
				.build();
		toStrictEqual(m1, m2);
	}

	@Test
	@DisplayName("given_differentClassMetrics_when_equals_should_returnFalse")
	void given_differentClassMetrics_when_equals_should_returnFalse() {
		final ClassMetrics m1 = ClassMetrics.builder().name("Test1").build();
		final ClassMetrics m2 = ClassMetrics.builder().name("Test2").build();
		assertFalse(m1.equals(m2));
	}

	@Test
	@DisplayName("given_sameClassMetrics_when_hashCode_should_returnSameValue")
	void given_sameClassMetrics_when_hashCode_should_returnSameValue() {
		final ClassMetrics m1 = ClassMetrics.builder()
				.name("Test")
				.analysisDate(this.fixedDate)
				.build();
		final ClassMetrics m2 = ClassMetrics.builder()
				.name("Test")
				.analysisDate(this.fixedDate)
				.build();
		toStrictEqual(m1.hashCode(), m2.hashCode());
	}

	@Test
	@DisplayName("given_classMetrics_when_toString_should_containClassName")
	void given_classMetrics_when_toString_should_containClassName() {
		final ClassMetrics metrics = ClassMetricsMother.withName("MyTestClass");
		assertTrue(metrics.toString().contains("MyTestClass"));
	}

	@Test
	@DisplayName("given_nullMethods_when_build_should_createEmptyList")
	void given_nullMethods_when_build_should_createEmptyList() {
		final ClassMetrics metrics = ClassMetrics.builder()
				.currentMethods(null)
				.refactoredMethods(null)
				.build();
		assertNotNull(metrics.getCurrentMethods());
		assertNotNull(metrics.getRefactoredMethods());
		toStrictEqual(0, metrics.getCurrentMethodCount());
		toStrictEqual(0, metrics.getRefactoredMethodCount());
	}
}
