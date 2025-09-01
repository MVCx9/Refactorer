package main.model.clazz;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static test.util.StrictAssertions.toStrictEqual;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName; 
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import main.builder.ClassAnalysis;
import main.builder.MethodAnalysis;
import main.model.method.MethodAnalysisMetricsMapper;
import main.model.method.MethodMetrics;
import test.mother.builder.ClassAnalysisMother;

@DisplayName("Tests de ClassAnalysisMetricsMapper")
class ClassAnalysisMetricsMapperTest {

	@Test
	void given_nullAnalysis_when_toClassMetrics_throws_NullPointerException() {
		final NullPointerException ex = assertThrows(NullPointerException.class, () -> ClassAnalysisMetricsMapper.toClassMetrics(null));
		toStrictEqual("analysis must not be null", ex.getMessage());
	}

	@Test
	void given_nonNullClassName_when_toClassMetrics_should_mapFields() {
		final String className = "MyClass";
		final LocalDateTime date = LocalDateTime.now();
		final MethodAnalysis m1 = mock(MethodAnalysis.class);
		final MethodAnalysis m2 = mock(MethodAnalysis.class);
		final List<MethodAnalysis> current = List.of(m1);
		final List<MethodAnalysis> refactored = List.of(m2);
		final ClassAnalysis analysis = ClassAnalysisMother.with(className, date, current, refactored);

		final MethodMetrics mmCurrent = mock(MethodMetrics.class);
		final MethodMetrics mmRef = mock(MethodMetrics.class);

		try (final MockedStatic<MethodAnalysisMetricsMapper> mocked = mockStatic(MethodAnalysisMetricsMapper.class)) {
			mocked.when(() -> MethodAnalysisMetricsMapper.toMethodMetrics(current)).thenReturn(List.of(mmCurrent));
			mocked.when(() -> MethodAnalysisMetricsMapper.toMethodMetrics(refactored)).thenReturn(List.of(mmRef));

			final ClassMetrics metrics = ClassAnalysisMetricsMapper.toClassMetrics(analysis);

			toStrictEqual(className, metrics.getName());
			toStrictEqual(date, metrics.getAnalysisDate());
			toStrictEqual(1, metrics.getCurrentMethods().size());
			toStrictEqual(1, metrics.getRefactoredMethods().size());
			toStrictEqual(mmCurrent, metrics.getCurrentMethods().get(0));
			toStrictEqual(mmRef, metrics.getRefactoredMethods().get(0));
			mocked.verify(() -> MethodAnalysisMetricsMapper.toMethodMetrics(current));
			mocked.verify(() -> MethodAnalysisMetricsMapper.toMethodMetrics(refactored));
		}
	}

	@Test
	void given_nullClassName_when_toClassMetrics_should_setDefaultNameNA() {
		final LocalDateTime date = LocalDateTime.now();
		final List<MethodAnalysis> current = List.of();
		final List<MethodAnalysis> refactored = List.of();
		final ClassAnalysis analysis = ClassAnalysisMother.with(null, date, current, refactored);

		try (final MockedStatic<MethodAnalysisMetricsMapper> mocked = mockStatic(MethodAnalysisMetricsMapper.class)) {
			mocked.when(() -> MethodAnalysisMetricsMapper.toMethodMetrics(current)).thenReturn(List.of());
			mocked.when(() -> MethodAnalysisMetricsMapper.toMethodMetrics(refactored)).thenReturn(List.of());

			final ClassMetrics metrics = ClassAnalysisMetricsMapper.toClassMetrics(analysis);

			toStrictEqual("N/A", metrics.getName());
			toStrictEqual(0, metrics.getCurrentMethods().size());
			toStrictEqual(0, metrics.getRefactoredMethods().size());
		}
	}

	@Test
	void given_emptyMethods_when_toClassMetrics_should_returnEmptyMethodMetrics() {
		final String className = "EmptyMethodsClass";
		final LocalDateTime date = LocalDateTime.now();
		final List<MethodAnalysis> current = List.of();
		final List<MethodAnalysis> refactored = List.of();
		final ClassAnalysis analysis = ClassAnalysisMother.with(className, date, current, refactored);

		try (final MockedStatic<MethodAnalysisMetricsMapper> mocked = mockStatic(MethodAnalysisMetricsMapper.class)) {
			mocked.when(() -> MethodAnalysisMetricsMapper.toMethodMetrics(current)).thenReturn(List.of());
			mocked.when(() -> MethodAnalysisMetricsMapper.toMethodMetrics(refactored)).thenReturn(List.of());

			final ClassMetrics metrics = ClassAnalysisMetricsMapper.toClassMetrics(analysis);

			toStrictEqual(className, metrics.getName());
			toStrictEqual(0, metrics.getCurrentMethods().size());
			toStrictEqual(0, metrics.getRefactoredMethods().size());
		}

		@Test
		void given_classNameEmpty_when_toClassMetrics_should_keepEmptyName() {
			final String className = "";
			final LocalDateTime date = LocalDateTime.now();
			final List<MethodAnalysis> current = List.of();
			final List<MethodAnalysis> refactored = List.of();
			final ClassAnalysis analysis = ClassAnalysisMother.with(className, date, current, refactored);
			try (final MockedStatic<MethodAnalysisMetricsMapper> mocked = mockStatic(MethodAnalysisMetricsMapper.class)) {
				mocked.when(() -> MethodAnalysisMetricsMapper.toMethodMetrics(current)).thenReturn(List.of());
				mocked.when(() -> MethodAnalysisMetricsMapper.toMethodMetrics(refactored)).thenReturn(List.of());
				final ClassMetrics metrics = ClassAnalysisMetricsMapper.toClassMetrics(analysis);
				toStrictEqual(className, metrics.getName());
			}
		}

		@Test
		void given_mappedLists_when_tryModify_should_throwUnsupportedOperationException() {
			final String className = "ImmutableListsClass";
			final LocalDateTime date = LocalDateTime.now();
			final MethodAnalysis m1 = mock(MethodAnalysis.class);
			final MethodAnalysis m2 = mock(MethodAnalysis.class);
			final List<MethodAnalysis> current = List.of(m1);
			final List<MethodAnalysis> refactored = List.of(m2);
			final ClassAnalysis analysis = ClassAnalysisMother.with(className, date, current, refactored);
			final MethodMetrics mmCurrent = mock(MethodMetrics.class);
			final MethodMetrics mmRef = mock(MethodMetrics.class);
			try (final MockedStatic<MethodAnalysisMetricsMapper> mocked = mockStatic(MethodAnalysisMetricsMapper.class)) {
				mocked.when(() -> MethodAnalysisMetricsMapper.toMethodMetrics(current)).thenReturn(List.of(mmCurrent));
				mocked.when(() -> MethodAnalysisMetricsMapper.toMethodMetrics(refactored)).thenReturn(List.of(mmRef));
				final ClassMetrics metrics = ClassAnalysisMetricsMapper.toClassMetrics(analysis);
				assertThrows(UnsupportedOperationException.class, () -> metrics.getCurrentMethods().add(mmRef));
				assertThrows(UnsupportedOperationException.class, () -> metrics.getRefactoredMethods().add(mmCurrent));
			}
		}
	}
}

