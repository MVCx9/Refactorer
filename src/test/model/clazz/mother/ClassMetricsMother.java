package test.model.clazz.mother;

import java.time.LocalDateTime;
import java.util.List;

import main.model.clazz.ClassMetrics;
import main.model.method.MethodMetrics;
import test.model.method.mother.MethodMetricsMother;

public final class ClassMetricsMother {
	private static final String DEFAULT_NAME = "SampleClass";
	private static final String DEFAULT_CURRENT_SOURCE = "public class SampleClass { void method(){} void method2(){} }";
	private static final String DEFAULT_REFACTORED_SOURCE = "public class SampleClass { void method(){} void method2(){} void method_ext_0(){} }";

	private ClassMetricsMother() {}

	public static ClassMetrics defaultMetrics() {
		final MethodMetrics m1 = MethodMetricsMother.custom("method", 10, 5);
		final MethodMetrics m2 = MethodMetricsMother.custom("method2", 12, 6);
		final MethodMetrics ext = MethodMetricsMother.custom("method_ext_0", 4, 2);
		final List<MethodMetrics> current = List.of(m1, m2);
		final List<MethodMetrics> refactored = List.of(m1, m2, ext);
		return ClassMetrics.builder()
				.name(DEFAULT_NAME)
				.analysisDate(LocalDateTime.now())
				.currentMethods(current)
				.refactoredMethods(refactored)
				.currentSource(DEFAULT_CURRENT_SOURCE)
				.refactoredSource(DEFAULT_REFACTORED_SOURCE)
				.build();
	}

	public static ClassMetrics withName(String name) {
		final String n = name == null ? DEFAULT_NAME : name;
		final MethodMetrics m1 = MethodMetricsMother.custom("method", 10, 5);
		final MethodMetrics m2 = MethodMetricsMother.custom("method2", 12, 6);
		final MethodMetrics ext = MethodMetricsMother.custom("method_ext_0", 4, 2);
		final List<MethodMetrics> current = List.of(m1, m2);
		final List<MethodMetrics> refactored = List.of(m1, m2, ext);
		return ClassMetrics.builder()
				.name(n)
				.analysisDate(LocalDateTime.now())
				.currentMethods(current)
				.refactoredMethods(refactored)
				.currentSource(DEFAULT_CURRENT_SOURCE)
				.refactoredSource(DEFAULT_REFACTORED_SOURCE)
				.build();
	}

	public static ClassMetrics custom(String name, LocalDateTime analysisDate, List<MethodMetrics> currentMethods, List<MethodMetrics> refactoredMethods, String currentSource, String refactoredSource) {
		final String n = name == null ? DEFAULT_NAME : name;
		final LocalDateTime date = analysisDate == null ? LocalDateTime.now() : analysisDate;
		final List<MethodMetrics> current = currentMethods == null ? List.of() : currentMethods;
		final List<MethodMetrics> refactored = refactoredMethods == null ? List.of() : refactoredMethods;
		final String curSrc = currentSource == null ? DEFAULT_CURRENT_SOURCE : currentSource;
		final String refSrc = refactoredSource == null ? DEFAULT_REFACTORED_SOURCE : refactoredSource;
		return ClassMetrics.builder()
				.name(n)
				.analysisDate(date)
				.currentMethods(current)
				.refactoredMethods(refactored)
				.currentSource(curSrc)
				.refactoredSource(refSrc)
				.build();
	}
}