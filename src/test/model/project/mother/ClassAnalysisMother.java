package test.model.project.mother;

import java.time.LocalDateTime;
import java.util.List;
import main.builder.ClassAnalysis;
import main.builder.MethodAnalysis;

public final class ClassAnalysisMother {

    private ClassAnalysisMother() {
    }

    public static ClassAnalysis withName(String name) {
        return ClassAnalysis.builder()
            .className(name)
            .analysisDate(LocalDateTime.of(2025, 1, 1, 10, 0))
            .currentMethods(List.<MethodAnalysis>of())
            .refactoredMethods(List.<MethodAnalysis>of())
            .build();
    }
}
