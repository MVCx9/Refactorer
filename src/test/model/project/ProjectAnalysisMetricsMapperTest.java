package test.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.builder.ClassAnalysis;
import main.builder.MethodAnalysis;
import main.builder.ProjectAnalysis;
import main.model.project.ProjectAnalysisMetricsMapper;
import main.model.project.ProjectMetrics;
import test.model.project.mother.ProjectAnalysisMother;
import test.model.method.mother.MethodAnalysisMother;

class ProjectAnalysisMetricsMapperTest {
	private static void toStrictEqual(Object expected, Object actual) { assertEquals(expected, actual); }

	@Test
	@DisplayName("given_validProjectAnalysis_when_toProjectMetrics_should_mapAllClassesAndAggregate")
	void given_validProjectAnalysis_when_toProjectMetrics_should_mapAllClassesAndAggregate() {
		final ProjectAnalysis analysis = ProjectAnalysisMother.defaultAnalysis();
		final ProjectMetrics metrics = ProjectAnalysisMetricsMapper.toProjectMetrics(analysis);
		assertNotNull(metrics);
		toStrictEqual(analysis.getName(), metrics.getName());
		toStrictEqual(analysis.getAnalysisDate(), metrics.getAnalysisDate());
		toStrictEqual(analysis.getFiles().size(), metrics.getClasses().size());
	}

	@Test
	@DisplayName("given_projectAnalysisWithEmptyClasses_when_toProjectMetrics_should_returnMetricsWithZeroClasses")
	void given_projectAnalysisWithEmptyClasses_when_toProjectMetrics_should_returnMetricsWithZeroClasses() {
		final LocalDateTime date = LocalDateTime.of(2024, 5, 10, 12, 0);
		final ProjectAnalysis analysis = ProjectAnalysis.builder()
				.name("EmptyProject")
				.analysisDate(date)
				.classes(new ArrayList<>())
				.build();
		final ProjectMetrics metrics = ProjectAnalysisMetricsMapper.toProjectMetrics(analysis);
		toStrictEqual("EmptyProject", metrics.getName());
		toStrictEqual(date, metrics.getAnalysisDate());
		toStrictEqual(0, metrics.getClassCount());
	}

	@Test
	@DisplayName("given_nullAnalysis_when_toProjectMetrics_throws_NullPointerException")
	void given_nullAnalysis_when_toProjectMetrics_throws_NullPointerException() {
		assertThrows(NullPointerException.class, () -> ProjectAnalysisMetricsMapper.toProjectMetrics(null));
	}

	@Test
	@DisplayName("given_projectAnalysisWithClassWithNullName_when_toProjectMetrics_should_setClassNameToNA")
	void given_projectAnalysisWithClassWithNullName_when_toProjectMetrics_should_setClassNameToNA() {
		final MethodAnalysis m = MethodAnalysisMother.custom("m", 5, 10, 0, 0, null, null);
		final ClassAnalysis classWithNullName = ClassAnalysis.builder()
				.className(null)
				.currentMethods(List.of(m))
				.refactoredMethods(List.of(m))
				.currentSource("class A {}").refactoredSource("class A {}").build();
		final ProjectAnalysis analysis = ProjectAnalysis.builder()
				.name("ProjectNA")
				.classes(List.of(classWithNullName))
				.build();
		final ProjectMetrics metrics = ProjectAnalysisMetricsMapper.toProjectMetrics(analysis);
		toStrictEqual(1, metrics.getClassCount());
		toStrictEqual("N/A", metrics.getClasses().get(0).getName());
	}

	@Test
	@DisplayName("given_projectMetrics_getClasses_when_modifyAttempt_throws_UnsupportedOperationException")
	void given_projectMetrics_getClasses_when_modifyAttempt_throws_UnsupportedOperationException() {
		final ProjectAnalysis analysis = ProjectAnalysisMother.defaultAnalysis();
		final ProjectMetrics metrics = ProjectAnalysisMetricsMapper.toProjectMetrics(analysis);
		assertThrows(UnsupportedOperationException.class, () -> metrics.getClasses().add(null));
	}

	@Test
	@DisplayName("given_projectAnalysisWithKnownMethodMetrics_when_toProjectMetrics_should_computeAggregateValues")
	void given_projectAnalysisWithKnownMethodMetrics_when_toProjectMetrics_should_computeAggregateValues() {
		final MethodAnalysis m1 = MethodAnalysisMother.custom("a", 3, 10, 0, 0, null, null);
		final MethodAnalysis m2 = MethodAnalysisMother.custom("b", 7, 20, 0, 0, null, null);
		final MethodAnalysis m3 = MethodAnalysisMother.custom("c", 5, 30, 0, 0, null, null);
		final ClassAnalysis c1 = ClassAnalysis.builder()
				.className("C1")
				.currentMethods(List.of(m1))
				.refactoredMethods(List.of(m1, m2))
				.currentSource("")
				.refactoredSource("")
				.build();
		final ClassAnalysis c2 = ClassAnalysis.builder()
				.className("C2")
				.currentMethods(List.of(m2, m3))
				.refactoredMethods(List.of(m2, m3))
				.currentSource("")
				.refactoredSource("")
				.build();
		final ProjectAnalysis analysis = ProjectAnalysis.builder()
				.name("AggProject")
				.classes(List.of(c1, c2))
				.build();
		final ProjectMetrics metrics = ProjectAnalysisMetricsMapper.toProjectMetrics(analysis);
		final int expectedCurrentLoc = 10 + 20 + 30; // m1 + m2 + m3 
		final int expectedCurrentCc = 3 + 7 + 5; // m1 + m2 + m3
		toStrictEqual(expectedCurrentLoc, metrics.getCurrentLoc());
		toStrictEqual(expectedCurrentCc, metrics.getCurrentCc());
	}
}