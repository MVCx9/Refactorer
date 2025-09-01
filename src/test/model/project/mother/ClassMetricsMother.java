package test.model.project.mother;

import java.time.LocalDateTime;
import java.util.List;

import main.model.clazz.ClassMetrics;
import main.model.method.MethodMetrics;

public final class ClassMetricsMother {

    private ClassMetricsMother() {
    }

    public static ClassMetrics withName(String name) {
        return ClassMetrics.builder()
            .name(name)
            .analysisDate(LocalDateTime.of(2025, 1, 1, 10, 0))
            .currentMethods(List.<MethodMetrics>of())
            .refactoredMethods(List.<MethodMetrics>of())
            .build();
    }
}
