package test.model.clazz;

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
import main.model.clazz.ClassAnalysisMetricsMapper;
import main.model.clazz.ClassMetrics;
import test.model.clazz.mother.ClassAnalysisMother;
import test.model.method.mother.MethodAnalysisMother;

class ClassAnalysisMetricsMapperTest {
	private final LocalDateTime fixedDate = LocalDateTime.of(2024, 1, 1, 12, 0);
	private final String currentSource = "class A { void a(){} void b(){} }";
	private final String refactoredSource = "class A { void a(){} void b(){} void a_ext_0(){} }";

	private static void toStrictEqual(Object expected, Object actual) { assertEquals(expected, actual); }

	@Test
	@DisplayName("given_validClassAnalysis_when_toClassMetrics_should_mapAllFields")
	void given_validClassAnalysis_when_toClassMetrics_should_mapAllFields() {
		final MethodAnalysis m1 = MethodAnalysisMother.custom("m1", 3, 10, 0, 0, null, null);
		final MethodAnalysis m2 = MethodAnalysisMother.custom("m2", 5, 20, 0, 0, null, null);
		final MethodAnalysis m3 = MethodAnalysisMother.custom("m3", 7, 15, 0, 0, null, null);
		final ClassAnalysis analysis = ClassAnalysisMother.custom(null, null, null, "MyClass", this.fixedDate, List.of(m1, m2), List.of(m1, m2, m3), this.currentSource, this.refactoredSource);
		final ClassMetrics metrics = ClassAnalysisMetricsMapper.toClassMetrics(analysis);
		assertNotNull(metrics);
		toStrictEqual("MyClass", metrics.getName());
		toStrictEqual(this.fixedDate, metrics.getAnalysisDate());
		toStrictEqual(this.currentSource, metrics.getCurrentSource());
		toStrictEqual(this.refactoredSource, metrics.getRefactoredSource());
		toStrictEqual(2, metrics.getCurrentMethodCount());
		toStrictEqual(3, metrics.getRefactoredMethodCount());
		toStrictEqual(10 + 20, metrics.getCurrentLoc());
		toStrictEqual(3 + 5, metrics.getCurrentCc());
		toStrictEqual(10 + 20 + 15, metrics.getRefactoredLoc());
		toStrictEqual(3 + 5 + 7, metrics.getRefactoredCc());
		toStrictEqual((3 + 5) - (3 + 5 + 7), metrics.getReducedComplexity());
		toStrictEqual((10 + 20) - (10 + 20 + 15), metrics.getReducedLoc());
	}

	@Test
	@DisplayName("given_classAnalysisWithEmptyMethods_when_toClassMetrics_should_returnZeroMetrics")
	void given_classAnalysisWithEmptyMethods_when_toClassMetrics_should_returnZeroMetrics() {
		final ClassAnalysis analysis = ClassAnalysisMother.custom(null, null, null, "EmptyClass", this.fixedDate, List.of(), List.of(), this.currentSource, this.refactoredSource);
		final ClassMetrics metrics = ClassAnalysisMetricsMapper.toClassMetrics(analysis);
		toStrictEqual(0, metrics.getCurrentMethodCount());
		toStrictEqual(0, metrics.getRefactoredMethodCount());
		toStrictEqual(0, metrics.getCurrentLoc());
		toStrictEqual(0, metrics.getCurrentCc());
		toStrictEqual(0, metrics.getRefactoredLoc());
		toStrictEqual(0, metrics.getRefactoredCc());
	}

	@Test
	@DisplayName("given_nullAnalysis_when_toClassMetrics_throws_NullPointerException")
	void given_nullAnalysis_when_toClassMetrics_throws_NullPointerException() {
		assertThrows(NullPointerException.class, () -> ClassAnalysisMetricsMapper.toClassMetrics(null));
	}

	@Test
	@DisplayName("given_classMetrics_when_modifyMethodLists_throws_UnsupportedOperationException")
	void given_classMetrics_when_modifyMethodLists_throws_UnsupportedOperationException() {
		final MethodAnalysis m = MethodAnalysisMother.custom("m", 2, 5, 0, 0, null, null);
		final ClassAnalysis analysis = ClassAnalysisMother.custom(null, null, null, "ImmutableClass", this.fixedDate, List.of(m), List.of(m), this.currentSource, this.refactoredSource);
		final ClassMetrics metrics = ClassAnalysisMetricsMapper.toClassMetrics(analysis);
		assertThrows(UnsupportedOperationException.class, () -> metrics.getCurrentMethods().add(null));
		assertThrows(UnsupportedOperationException.class, () -> metrics.getRefactoredMethods().add(null));
	}

	@Test
	@DisplayName("given_classAnalysisFromMother_when_toClassMetrics_should_mapConsistently")
	void given_classAnalysisFromMother_when_toClassMetrics_should_mapConsistently() {
		final ClassAnalysis analysis = ClassAnalysisMother.defaultAnalysis();
		final ClassMetrics metrics = ClassAnalysisMetricsMapper.toClassMetrics(analysis);
		assertNotNull(metrics);
		toStrictEqual(analysis.getClassName(), metrics.getName());
		toStrictEqual(analysis.getCurrentMethods().size(), metrics.getCurrentMethodCount());
		toStrictEqual(analysis.getRefactoredMethods().size(), metrics.getRefactoredMethodCount());
	}
}