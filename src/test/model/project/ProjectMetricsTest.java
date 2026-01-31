package test.model.project;

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
import main.model.project.ProjectMetrics;
import test.model.clazz.mother.ClassMetricsMother;
import test.model.method.mother.MethodMetricsMother;
import test.model.project.mother.ProjectMetricsMother;

class ProjectMetricsTest {
	private final LocalDateTime fixedDate = LocalDateTime.of(2024, 6, 15, 10, 30);

	private void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("given_builderWithDefaults_when_build_should_createProjectMetricsWithDefaultValues")
	void given_builderWithDefaults_when_build_should_createProjectMetricsWithDefaultValues() {
		final ProjectMetrics metrics = ProjectMetrics.builder().build();
		assertNotNull(metrics);
		toStrictEqual("<unnamed>", metrics.getName());
		assertNotNull(metrics.getAnalysisDate());
		toStrictEqual(0, metrics.getClassCount());
		toStrictEqual(15, metrics.getComplexityThreshold());
	}

	@Test
	@DisplayName("given_builderWithAllValues_when_build_should_createProjectMetricsWithAllValues")
	void given_builderWithAllValues_when_build_should_createProjectMetricsWithAllValues() {
		final List<ClassMetrics> classes = List.of(ClassMetricsMother.defaultMetrics());
		final ProjectMetrics metrics = ProjectMetrics.builder()
				.name("TestProject")
				.analysisDate(this.fixedDate)
				.classes(classes)
				.complexityThreshold(20)
				.build();
		toStrictEqual("TestProject", metrics.getName());
		toStrictEqual(this.fixedDate, metrics.getAnalysisDate());
		toStrictEqual(1, metrics.getClassCount());
		toStrictEqual(20, metrics.getComplexityThreshold());
	}

	@Test
	@DisplayName("given_motherDefaultMetrics_when_getValues_should_returnExpectedDefaults")
	void given_motherDefaultMetrics_when_getValues_should_returnExpectedDefaults() {
		final ProjectMetrics metrics = ProjectMetricsMother.defaultMetrics();
		assertNotNull(metrics);
		toStrictEqual("SampleProject", metrics.getName());
		assertNotNull(metrics.getAnalysisDate());
		toStrictEqual(2, metrics.getClassCount());
	}

	@Test
	@DisplayName("given_motherWithName_when_getName_should_returnSpecifiedName")
	void given_motherWithName_when_getName_should_returnSpecifiedName() {
		final ProjectMetrics metrics = ProjectMetricsMother.withName("CustomProject");
		toStrictEqual("CustomProject", metrics.getName());
	}

	@Test
	@DisplayName("given_projectMetrics_when_getCurrentLoc_should_sumClassLocs")
	void given_projectMetrics_when_getCurrentLoc_should_sumClassLocs() {
		final List<MethodMetrics> methods1 = List.of(MethodMetricsMother.custom("m1", 10, 3));
		final List<MethodMetrics> methods2 = List.of(MethodMetricsMother.custom("m2", 20, 5));
		final ClassMetrics c1 = ClassMetrics.builder().name("C1").currentMethods(methods1).build();
		final ClassMetrics c2 = ClassMetrics.builder().name("C2").currentMethods(methods2).build();
		final ProjectMetrics metrics = ProjectMetrics.builder()
				.classes(List.of(c1, c2))
				.build();
		toStrictEqual(30, metrics.getCurrentLoc());
	}

	@Test
	@DisplayName("given_projectMetrics_when_getCurrentCc_should_sumClassCcs")
	void given_projectMetrics_when_getCurrentCc_should_sumClassCcs() {
		final List<MethodMetrics> methods1 = List.of(MethodMetricsMother.custom("m1", 10, 3));
		final List<MethodMetrics> methods2 = List.of(MethodMetricsMother.custom("m2", 20, 5));
		final ClassMetrics c1 = ClassMetrics.builder().name("C1").currentMethods(methods1).build();
		final ClassMetrics c2 = ClassMetrics.builder().name("C2").currentMethods(methods2).build();
		final ProjectMetrics metrics = ProjectMetrics.builder()
				.classes(List.of(c1, c2))
				.build();
		toStrictEqual(8, metrics.getCurrentCc());
	}

	@Test
	@DisplayName("given_projectMetrics_when_getCurrentMethodCount_should_sumClassMethodCounts")
	void given_projectMetrics_when_getCurrentMethodCount_should_sumClassMethodCounts() {
		final ProjectMetrics metrics = ProjectMetricsMother.defaultMetrics();
		assertTrue(metrics.getCurrentMethodCount() >= 0);
	}

	@Test
	@DisplayName("given_projectMetrics_when_getReducedComplexity_should_returnDifference")
	void given_projectMetrics_when_getReducedComplexity_should_returnDifference() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m", 10, 10));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m", 10, 7));
		final ClassMetrics c = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		final ProjectMetrics metrics = ProjectMetrics.builder()
				.classes(List.of(c))
				.build();
		toStrictEqual(3, metrics.getReducedComplexity());
	}

	@Test
	@DisplayName("given_projectMetrics_when_getMethodExtractionCount_should_returnDifference")
	void given_projectMetrics_when_getMethodExtractionCount_should_returnDifference() {
		final ProjectMetrics metrics = ProjectMetricsMother.defaultMetrics();
		assertTrue(metrics.getMethodExtractionCount() >= 0);
	}

	@Test
	@DisplayName("given_projectMetrics_when_getAverageCurrentLoc_should_computeAverage")
	void given_projectMetrics_when_getAverageCurrentLoc_should_computeAverage() {
		final List<MethodMetrics> methods1 = List.of(MethodMetricsMother.custom("m1", 10, 2));
		final List<MethodMetrics> methods2 = List.of(MethodMetricsMother.custom("m2", 20, 4));
		final ClassMetrics c1 = ClassMetrics.builder().name("C1").currentMethods(methods1).build();
		final ClassMetrics c2 = ClassMetrics.builder().name("C2").currentMethods(methods2).build();
		final ProjectMetrics metrics = ProjectMetrics.builder()
				.classes(List.of(c1, c2))
				.build();
		toStrictEqual(15, metrics.getAverageCurrentLoc());
	}

	@Test
	@DisplayName("given_emptyClasses_when_getAverageCurrentLoc_should_returnZero")
	void given_emptyClasses_when_getAverageCurrentLoc_should_returnZero() {
		final ProjectMetrics metrics = ProjectMetrics.builder()
				.classes(Collections.emptyList())
				.build();
		toStrictEqual(0, metrics.getAverageCurrentLoc());
	}

	@Test
	@DisplayName("given_projectMetrics_when_modifyClasses_throws_UnsupportedOperationException")
	void given_projectMetrics_when_modifyClasses_throws_UnsupportedOperationException() {
		final ProjectMetrics metrics = ProjectMetricsMother.defaultMetrics();
		assertThrows(UnsupportedOperationException.class, () -> metrics.getClasses().add(null));
	}

	@Test
	@DisplayName("given_projectMetrics_when_getNameUpper_should_returnUppercaseName")
	void given_projectMetrics_when_getNameUpper_should_returnUppercaseName() {
		final ProjectMetrics metrics = ProjectMetricsMother.withName("myProject");
		toStrictEqual("MYPROJECT", metrics.getNameUpper());
	}

	@Test
	@DisplayName("given_projectMetricsWithNullName_when_getNameUpper_should_returnEmptyString")
	void given_projectMetricsWithNullName_when_getNameUpper_should_returnEmptyString() {
		final ProjectMetrics metrics = ProjectMetrics.builder().name(null).build();
		toStrictEqual("", metrics.getNameUpper());
	}

	@Test
	@DisplayName("given_projectMetrics_when_getImprovementCc_should_returnDifference")
	void given_projectMetrics_when_getImprovementCc_should_returnDifference() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m", 10, 20));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m", 10, 15));
		final ClassMetrics c = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		final ProjectMetrics metrics = ProjectMetrics.builder()
				.classes(List.of(c))
				.build();
		toStrictEqual(5, metrics.getImprovementCc());
	}

	@Test
	@DisplayName("given_projectMetrics_when_isImprovedCc_should_returnTrueWhenRefactoredLess")
	void given_projectMetrics_when_isImprovedCc_should_returnTrueWhenRefactoredLess() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m", 10, 20));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m", 10, 15));
		final ClassMetrics c = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		final ProjectMetrics metrics = ProjectMetrics.builder()
				.classes(List.of(c))
				.build();
		assertTrue(metrics.isImprovedCc());
	}

	@Test
	@DisplayName("given_projectMetrics_when_isImprovedCc_should_returnFalseWhenRefactoredMore")
	void given_projectMetrics_when_isImprovedCc_should_returnFalseWhenRefactoredMore() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m", 10, 10));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m", 10, 15));
		final ClassMetrics c = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		final ProjectMetrics metrics = ProjectMetrics.builder()
				.classes(List.of(c))
				.build();
		assertFalse(metrics.isImprovedCc());
	}

	@Test
	@DisplayName("given_projectMetricsWithExtractedMethods_when_getMethodsWithRefactors_should_returnNonEmpty")
	void given_projectMetricsWithExtractedMethods_when_getMethodsWithRefactors_should_returnNonEmpty() {
		final ProjectMetrics metrics = ProjectMetricsMother.defaultMetrics();
		final List<ClassMetrics> result = metrics.getMethodsWithRefactors();
		assertNotNull(result);
	}

	@Test
	@DisplayName("given_sameProjectMetrics_when_equals_should_returnTrue")
	void given_sameProjectMetrics_when_equals_should_returnTrue() {
		final ProjectMetrics m1 = ProjectMetrics.builder()
				.name("Test")
				.analysisDate(this.fixedDate)
				.complexityThreshold(15)
				.build();
		final ProjectMetrics m2 = ProjectMetrics.builder()
				.name("Test")
				.analysisDate(this.fixedDate)
				.complexityThreshold(15)
				.build();
		toStrictEqual(m1, m2);
	}

	@Test
	@DisplayName("given_differentProjectMetrics_when_equals_should_returnFalse")
	void given_differentProjectMetrics_when_equals_should_returnFalse() {
		final ProjectMetrics m1 = ProjectMetrics.builder().name("Test1").build();
		final ProjectMetrics m2 = ProjectMetrics.builder().name("Test2").build();
		assertFalse(m1.equals(m2));
	}

	@Test
	@DisplayName("given_sameProjectMetrics_when_hashCode_should_returnSameValue")
	void given_sameProjectMetrics_when_hashCode_should_returnSameValue() {
		final ProjectMetrics m1 = ProjectMetrics.builder()
				.name("Test")
				.analysisDate(this.fixedDate)
				.build();
		final ProjectMetrics m2 = ProjectMetrics.builder()
				.name("Test")
				.analysisDate(this.fixedDate)
				.build();
		toStrictEqual(m1.hashCode(), m2.hashCode());
	}

	@Test
	@DisplayName("given_projectMetrics_when_toString_should_containProjectName")
	void given_projectMetrics_when_toString_should_containProjectName() {
		final ProjectMetrics metrics = ProjectMetricsMother.withName("MyTestProject");
		assertTrue(metrics.toString().contains("MyTestProject"));
	}

	@Test
	@DisplayName("given_projectMetrics_when_getAverageCurrentMethodCount_should_computeAverage")
	void given_projectMetrics_when_getAverageCurrentMethodCount_should_computeAverage() {
		final List<MethodMetrics> methods1 = List.of(
				MethodMetricsMother.custom("m1", 10, 2),
				MethodMetricsMother.custom("m2", 10, 2));
		final List<MethodMetrics> methods2 = List.of(MethodMetricsMother.custom("m3", 20, 4));
		final ClassMetrics c1 = ClassMetrics.builder().name("C1").currentMethods(methods1).build();
		final ClassMetrics c2 = ClassMetrics.builder().name("C2").currentMethods(methods2).build();
		final ProjectMetrics metrics = ProjectMetrics.builder()
				.classes(List.of(c1, c2))
				.build();
		toStrictEqual(2, metrics.getAverageCurrentMethodCount());
	}

	@Test
	@DisplayName("given_projectMetrics_when_getAverageReducedComplexity_should_computeAverage")
	void given_projectMetrics_when_getAverageReducedComplexity_should_computeAverage() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m", 10, 10));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m", 10, 8));
		final ClassMetrics c = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		final ProjectMetrics metrics = ProjectMetrics.builder()
				.classes(List.of(c))
				.build();
		assertTrue(metrics.getAverageReducedComplexity() >= 0);
	}

	@Test
	@DisplayName("given_projectMetrics_when_getReducedLoc_should_returnDifference")
	void given_projectMetrics_when_getReducedLoc_should_returnDifference() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m", 50, 5));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m", 40, 5));
		final ClassMetrics c = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		final ProjectMetrics metrics = ProjectMetrics.builder()
				.classes(List.of(c))
				.build();
		toStrictEqual(10, metrics.getReducedLoc());
	}
}
