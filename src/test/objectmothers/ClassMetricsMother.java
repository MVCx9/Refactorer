package test.objectmothers;

import java.time.LocalDateTime;
import java.util.List;

import main.model.clazz.ClassMetrics;
import main.model.method.MethodMetrics;

public final class ClassMetricsMother {

    public static final LocalDateTime FIXED_DATE = LocalDateTime.of(2025, 1, 1, 12, 0);

    private ClassMetricsMother() {
    }

    public static ClassMetrics empty() {
        return ClassMetrics.builder()
                .name("Empty.java")
                .analysisDate(FIXED_DATE)
                .currentSource("")
                .refactoredSource("")
                .complexityThreshold(15)
                .build();
    }

    public static ClassMetrics simple(final String name) {
        final MethodMetrics m = MethodMetricsMother.basic("foo");
        return ClassMetrics.builder()
                .name(name)
                .analysisDate(FIXED_DATE)
                .currentMethods(List.of(m))
                .refactoredMethods(List.of(m))
                .currentSource("class A {}")
                .refactoredSource("class A {}")
                .complexityThreshold(15)
                .path("/p/src/" + name)
                .build();
    }

    public static ClassMetrics withRefactors(final String name) {
        final MethodMetrics original = MethodMetricsMother.withCc("foo", 20);
        final MethodMetrics base = MethodMetricsMother.withCc("foo", 8);
        final MethodMetrics ext1 = MethodMetricsMother.extracted("foo", 1, 4);
        final MethodMetrics ext2 = MethodMetricsMother.extracted("foo", 2, 5);
        return ClassMetrics.builder()
                .name(name)
                .analysisDate(FIXED_DATE)
                .currentMethods(List.of(original))
                .refactoredMethods(List.of(base, ext1, ext2))
                .currentSource("class A {\n  void foo(){}\n}\n")
                .refactoredSource("class A {\n  void foo(){bar();}\n  void foo_ext_1(){}\n  void foo_ext_2(){}\n}\n")
                .complexityThreshold(10)
                .path("/p/src/" + name)
                .build();
    }
}
