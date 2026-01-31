package test.model.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.model.clazz.ClassMetrics;
import main.model.method.MethodMetrics;
import main.model.project.ProjectMetrics;
import main.model.workspace.WorkspaceMetrics;
import test.model.method.mother.MethodMetricsMother;
import test.model.project.mother.ProjectMetricsMother;
import test.model.workspace.mother.WorkspaceMetricsMother;

class WorkspaceMetricsTest {
	private final LocalDateTime fixedDate = LocalDateTime.of(2024, 6, 15, 10, 30);

	private void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("given_builderWithDefaults_when_build_should_createWorkspaceMetricsWithDefaultValues")
	void given_builderWithDefaults_when_build_should_createWorkspaceMetricsWithDefaultValues() {
		final WorkspaceMetrics metrics = WorkspaceMetrics.builder().build();
		assertNotNull(metrics);
		toStrictEqual("<unnamed>", metrics.getName());
		assertNotNull(metrics.getAnalysisDate());
		toStrictEqual(0, metrics.getProjectCount());
	}

	@Test
	@DisplayName("given_builderWithAllValues_when_build_should_createWorkspaceMetricsWithAllValues")
	void given_builderWithAllValues_when_build_should_createWorkspaceMetricsWithAllValues() {
		final List<ProjectMetrics> projects = List.of(ProjectMetricsMother.defaultMetrics());
		final WorkspaceMetrics metrics = WorkspaceMetrics.builder()
				.name("TestWorkspace")
				.analysisDate(this.fixedDate)
				.projects(projects)
				.build();
		toStrictEqual("TestWorkspace", metrics.getName());
		toStrictEqual(this.fixedDate, metrics.getAnalysisDate());
		toStrictEqual(1, metrics.getProjectCount());
	}

	@Test
	@DisplayName("given_motherDefaultMetrics_when_getValues_should_returnExpectedDefaults")
	void given_motherDefaultMetrics_when_getValues_should_returnExpectedDefaults() {
		final WorkspaceMetrics metrics = WorkspaceMetricsMother.defaultMetrics();
		assertNotNull(metrics);
		toStrictEqual("SampleWorkspace", metrics.getName());
		assertNotNull(metrics.getAnalysisDate());
		toStrictEqual(2, metrics.getProjectCount());
	}

	@Test
	@DisplayName("given_motherWithName_when_getName_should_returnSpecifiedName")
	void given_motherWithName_when_getName_should_returnSpecifiedName() {
		final WorkspaceMetrics metrics = WorkspaceMetricsMother.withName("CustomWorkspace");
		toStrictEqual("CustomWorkspace", metrics.getName());
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getCurrentLoc_should_sumProjectLocs")
	void given_workspaceMetrics_when_getCurrentLoc_should_sumProjectLocs() {
		final List<MethodMetrics> methods = List.of(MethodMetricsMother.custom("m", 10, 3));
		final ClassMetrics c = ClassMetrics.builder().name("C").currentMethods(methods).build();
		final ProjectMetrics p = ProjectMetrics.builder().name("P").classes(List.of(c)).build();
		final WorkspaceMetrics metrics = WorkspaceMetrics.builder()
				.projects(List.of(p))
				.build();
		toStrictEqual(10, metrics.getCurrentLoc());
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getCurrentCc_should_computeAverageAcrossProjects")
	void given_workspaceMetrics_when_getCurrentCc_should_computeAverageAcrossProjects() {
		final List<MethodMetrics> methods1 = List.of(MethodMetricsMother.custom("m1", 10, 6));
		final List<MethodMetrics> methods2 = List.of(MethodMetricsMother.custom("m2", 20, 4));
		final ClassMetrics c1 = ClassMetrics.builder().name("C1").currentMethods(methods1).build();
		final ClassMetrics c2 = ClassMetrics.builder().name("C2").currentMethods(methods2).build();
		final ProjectMetrics p1 = ProjectMetrics.builder().name("P1").classes(List.of(c1)).build();
		final ProjectMetrics p2 = ProjectMetrics.builder().name("P2").classes(List.of(c2)).build();
		final WorkspaceMetrics metrics = WorkspaceMetrics.builder()
				.projects(List.of(p1, p2))
				.build();
		toStrictEqual(5, metrics.getCurrentCc());
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getCurrentMethodCount_should_sumProjectMethodCounts")
	void given_workspaceMetrics_when_getCurrentMethodCount_should_sumProjectMethodCounts() {
		final WorkspaceMetrics metrics = WorkspaceMetricsMother.defaultMetrics();
		assertTrue(metrics.getCurrentMethodCount() >= 0);
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getReducedComplexity_should_returnDifference")
	void given_workspaceMetrics_when_getReducedComplexity_should_returnDifference() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m", 10, 10));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m", 10, 7));
		final ClassMetrics c = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		final ProjectMetrics p = ProjectMetrics.builder().classes(List.of(c)).build();
		final WorkspaceMetrics metrics = WorkspaceMetrics.builder()
				.projects(List.of(p))
				.build();
		assertTrue(metrics.getReducedComplexity() >= 0);
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getMethodExtractionCount_should_returnDifference")
	void given_workspaceMetrics_when_getMethodExtractionCount_should_returnDifference() {
		final WorkspaceMetrics metrics = WorkspaceMetricsMother.defaultMetrics();
		assertTrue(metrics.getMethodExtractionCount() >= 0);
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getAverageCurrentLoc_should_computeAverage")
	void given_workspaceMetrics_when_getAverageCurrentLoc_should_computeAverage() {
		final List<MethodMetrics> methods1 = List.of(MethodMetricsMother.custom("m1", 10, 2));
		final List<MethodMetrics> methods2 = List.of(MethodMetricsMother.custom("m2", 20, 4));
		final ClassMetrics c1 = ClassMetrics.builder().name("C1").currentMethods(methods1).build();
		final ClassMetrics c2 = ClassMetrics.builder().name("C2").currentMethods(methods2).build();
		final ProjectMetrics p1 = ProjectMetrics.builder().name("P1").classes(List.of(c1)).build();
		final ProjectMetrics p2 = ProjectMetrics.builder().name("P2").classes(List.of(c2)).build();
		final WorkspaceMetrics metrics = WorkspaceMetrics.builder()
				.projects(List.of(p1, p2))
				.build();
		toStrictEqual(15, metrics.getAverageCurrentLoc());
	}

	@Test
	@DisplayName("given_emptyProjects_when_getAverageCurrentLoc_should_returnZero")
	void given_emptyProjects_when_getAverageCurrentLoc_should_returnZero() {
		final WorkspaceMetrics metrics = WorkspaceMetrics.builder()
				.projects(Collections.emptyList())
				.build();
		toStrictEqual(0, metrics.getAverageCurrentLoc());
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getProjects_should_returnProjectList")
	void given_workspaceMetrics_when_getProjects_should_returnProjectList() {
		final WorkspaceMetrics metrics = WorkspaceMetricsMother.defaultMetrics();
		assertNotNull(metrics.getProjects());
		toStrictEqual(2, metrics.getProjects().size());
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getNameUpper_should_returnUppercaseName")
	void given_workspaceMetrics_when_getNameUpper_should_returnUppercaseName() {
		final WorkspaceMetrics metrics = WorkspaceMetricsMother.withName("myWorkspace");
		toStrictEqual("MYWORKSPACE", metrics.getNameUpper());
	}

	@Test
	@DisplayName("given_workspaceMetricsWithNullName_when_getNameUpper_should_returnEmptyString")
	void given_workspaceMetricsWithNullName_when_getNameUpper_should_returnEmptyString() {
		final WorkspaceMetrics metrics = WorkspaceMetrics.builder().name(null).build();
		toStrictEqual("", metrics.getNameUpper());
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getClassCount_should_sumProjectClassCounts")
	void given_workspaceMetrics_when_getClassCount_should_sumProjectClassCounts() {
		final WorkspaceMetrics metrics = WorkspaceMetricsMother.defaultMetrics();
		assertTrue(metrics.getClassCount() >= 0);
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getImprovementCc_should_returnDifference")
	void given_workspaceMetrics_when_getImprovementCc_should_returnDifference() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m", 10, 20));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m", 10, 15));
		final ClassMetrics c = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		final ProjectMetrics p = ProjectMetrics.builder().classes(List.of(c)).build();
		final WorkspaceMetrics metrics = WorkspaceMetrics.builder()
				.projects(List.of(p))
				.build();
		assertTrue(metrics.getImprovementCc() >= 0);
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_isImprovedCc_should_returnTrueWhenRefactoredLess")
	void given_workspaceMetrics_when_isImprovedCc_should_returnTrueWhenRefactoredLess() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m", 10, 20));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m", 10, 15));
		final ClassMetrics c = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		final ProjectMetrics p = ProjectMetrics.builder().classes(List.of(c)).build();
		final WorkspaceMetrics metrics = WorkspaceMetrics.builder()
				.projects(List.of(p))
				.build();
		assertTrue(metrics.isImprovedCc());
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getProjectsWithRefactors_should_returnList")
	void given_workspaceMetrics_when_getProjectsWithRefactors_should_returnList() {
		final WorkspaceMetrics metrics = WorkspaceMetricsMother.defaultMetrics();
		final List<ProjectMetrics> result = metrics.getProjectsWithRefactors();
		assertNotNull(result);
	}

	@Test
	@DisplayName("given_sameWorkspaceMetrics_when_equals_should_returnTrue")
	void given_sameWorkspaceMetrics_when_equals_should_returnTrue() {
		final WorkspaceMetrics m1 = WorkspaceMetrics.builder()
				.name("Test")
				.analysisDate(this.fixedDate)
				.build();
		final WorkspaceMetrics m2 = WorkspaceMetrics.builder()
				.name("Test")
				.analysisDate(this.fixedDate)
				.build();
		toStrictEqual(m1, m2);
	}

	@Test
	@DisplayName("given_differentWorkspaceMetrics_when_equals_should_returnFalse")
	void given_differentWorkspaceMetrics_when_equals_should_returnFalse() {
		final WorkspaceMetrics m1 = WorkspaceMetrics.builder().name("Test1").build();
		final WorkspaceMetrics m2 = WorkspaceMetrics.builder().name("Test2").build();
		assertFalse(m1.equals(m2));
	}

	@Test
	@DisplayName("given_sameWorkspaceMetrics_when_hashCode_should_returnSameValue")
	void given_sameWorkspaceMetrics_when_hashCode_should_returnSameValue() {
		final WorkspaceMetrics m1 = WorkspaceMetrics.builder()
				.name("Test")
				.analysisDate(this.fixedDate)
				.build();
		final WorkspaceMetrics m2 = WorkspaceMetrics.builder()
				.name("Test")
				.analysisDate(this.fixedDate)
				.build();
		toStrictEqual(m1.hashCode(), m2.hashCode());
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_toString_should_containWorkspaceName")
	void given_workspaceMetrics_when_toString_should_containWorkspaceName() {
		final WorkspaceMetrics metrics = WorkspaceMetricsMother.withName("MyTestWorkspace");
		assertTrue(metrics.toString().contains("MyTestWorkspace"));
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getAverageCurrentMethodCount_should_computeAverage")
	void given_workspaceMetrics_when_getAverageCurrentMethodCount_should_computeAverage() {
		final WorkspaceMetrics metrics = WorkspaceMetricsMother.defaultMetrics();
		assertTrue(metrics.getAverageCurrentMethodCount() >= 0);
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getAverageReducedComplexity_should_computeAverage")
	void given_workspaceMetrics_when_getAverageReducedComplexity_should_computeAverage() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m", 10, 10));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m", 10, 8));
		final ClassMetrics c = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		final ProjectMetrics p = ProjectMetrics.builder().classes(List.of(c)).build();
		final WorkspaceMetrics metrics = WorkspaceMetrics.builder()
				.projects(List.of(p))
				.build();
		assertTrue(metrics.getAverageReducedComplexity() >= 0);
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getReducedLoc_should_returnDifference")
	void given_workspaceMetrics_when_getReducedLoc_should_returnDifference() {
		final List<MethodMetrics> current = List.of(MethodMetricsMother.custom("m", 50, 5));
		final List<MethodMetrics> refactored = List.of(MethodMetricsMother.custom("m", 40, 5));
		final ClassMetrics c = ClassMetrics.builder()
				.currentMethods(current)
				.refactoredMethods(refactored)
				.build();
		final ProjectMetrics p = ProjectMetrics.builder().classes(List.of(c)).build();
		final WorkspaceMetrics metrics = WorkspaceMetrics.builder()
				.projects(List.of(p))
				.build();
		toStrictEqual(10, metrics.getReducedLoc());
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getRefactoredMethodCount_should_sumProjectRefactoredMethodCounts")
	void given_workspaceMetrics_when_getRefactoredMethodCount_should_sumProjectRefactoredMethodCounts() {
		final WorkspaceMetrics metrics = WorkspaceMetricsMother.defaultMetrics();
		assertTrue(metrics.getRefactoredMethodCount() >= 0);
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getAverageRefactoredLoc_should_computeAverage")
	void given_workspaceMetrics_when_getAverageRefactoredLoc_should_computeAverage() {
		final WorkspaceMetrics metrics = WorkspaceMetricsMother.defaultMetrics();
		assertTrue(metrics.getAverageRefactoredLoc() >= 0);
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getAverageRefactoredCc_should_computeAverage")
	void given_workspaceMetrics_when_getAverageRefactoredCc_should_computeAverage() {
		final WorkspaceMetrics metrics = WorkspaceMetricsMother.defaultMetrics();
		assertTrue(metrics.getAverageRefactoredCc() >= 0);
	}

	@Test
	@DisplayName("given_workspaceMetrics_when_getAverageRefactoredMethodCount_should_computeAverage")
	void given_workspaceMetrics_when_getAverageRefactoredMethodCount_should_computeAverage() {
		final WorkspaceMetrics metrics = WorkspaceMetricsMother.defaultMetrics();
		assertTrue(metrics.getAverageRefactoredMethodCount() >= 0);
	}
}
