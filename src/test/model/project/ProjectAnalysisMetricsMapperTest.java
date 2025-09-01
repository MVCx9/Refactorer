package test.model.project;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import main.builder.ClassAnalysis;
import main.builder.ProjectAnalysis;
import main.model.clazz.ClassAnalysisMetricsMapper;
import main.model.clazz.ClassMetrics;
import main.model.project.ProjectAnalysisMetricsMapper;
import main.model.project.ProjectMetrics;
import test.model.project.mother.ClassAnalysisMother;
import test.model.project.mother.ClassMetricsMother;
import test.model.project.mother.ProjectAnalysisMother;

public class ProjectAnalysisMetricsMapperTest {

	private static void toStrictEqual(Object expected, Object actual) {
		org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
	}

	@Test
	@DisplayName("Null analysis argument")
	void given_nullProjectAnalysis_when_toProjectMetrics_throws_NullPointerException() {
		assertThrows(NullPointerException.class, () -> ProjectAnalysisMetricsMapper.toProjectMetrics(null));
	}

	@Test
	@DisplayName("Empty classes mapping")
	void given_projectAnalysisWithEmptyClasses_when_toProjectMetrics_should_returnProjectMetricsWithEmptyClasses() {
		final ProjectAnalysis analysis = ProjectAnalysisMother.simple(List.of());
		final LocalDateTime expectedDate = analysis.getAnalysisDate();
		final ProjectMetrics result = ProjectAnalysisMetricsMapper.toProjectMetrics(analysis);
		toStrictEqual(analysis.getName(), result.getName());
		toStrictEqual(expectedDate, result.getAnalysisDate());
		toStrictEqual(0, result.getClasses().size());
		toStrictEqual(0, result.getClassCount());
		toStrictEqual(0, result.getCurrentLoc());
		toStrictEqual(0, result.getRefactoredLoc());
	}

	@Test
	@DisplayName("Mapping with class list")
	void given_projectAnalysisWithClasses_when_toProjectMetrics_should_returnProjectMetricsWithMappedClasses() {
		final ClassAnalysis c1 = ClassAnalysisMother.withName("A");
		final ClassAnalysis c2 = ClassAnalysisMother.withName("B");
		final ProjectAnalysis analysis = ProjectAnalysisMother.simple(List.of(c1, c2));
		final ClassMetrics cm1 = ClassMetricsMother.withName("A");
		final ClassMetrics cm2 = ClassMetricsMother.withName("B");
		try (final MockedStatic<ClassAnalysisMetricsMapper> mocked = Mockito.mockStatic(ClassAnalysisMetricsMapper.class)) {
			mocked.when(() -> ClassAnalysisMetricsMapper.toClassMetrics(c1)).thenReturn(cm1);
			mocked.when(() -> ClassAnalysisMetricsMapper.toClassMetrics(c2)).thenReturn(cm2);
			final ProjectMetrics result = ProjectAnalysisMetricsMapper.toProjectMetrics(analysis);
			toStrictEqual(analysis.getName(), result.getName());
			toStrictEqual(analysis.getAnalysisDate(), result.getAnalysisDate());
			toStrictEqual(2, result.getClasses().size());
			toStrictEqual(List.of(cm1, cm2), result.getClasses());
		}
	}

	@Test
	@DisplayName("Unmodifiable classes list")
	void given_projectMetrics_when_attemptModifyClasses_throws_UnsupportedOperationException() {
		final ClassAnalysis c1 = ClassAnalysisMother.withName("A");
		final ProjectAnalysis analysis = ProjectAnalysisMother.simple(List.of(c1));
		final ClassMetrics cm1 = ClassMetricsMother.withName("A");
		try (final MockedStatic<ClassAnalysisMetricsMapper> mocked = Mockito.mockStatic(ClassAnalysisMetricsMapper.class)) {
			mocked.when(() -> ClassAnalysisMetricsMapper.toClassMetrics(c1)).thenReturn(cm1);
			final ProjectMetrics result = ProjectAnalysisMetricsMapper.toProjectMetrics(analysis);
			assertThrows(UnsupportedOperationException.class, () -> result.getClasses().add(ClassMetricsMother.withName("X")));
		}
	}
}
