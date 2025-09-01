package test.mother.builder;

import java.time.LocalDateTime;
import java.util.List;
import main.builder.ClassAnalysis;
import main.builder.MethodAnalysis;

public final class ClassAnalysisMother {

    private ClassAnalysisMother() {}

    public static ClassAnalysis with(String className, LocalDateTime date, List<MethodAnalysis> current, List<MethodAnalysis> refactored) {
        return ClassAnalysis.builder()
            .className(className)
            .analysisDate(date)
            .currentMethods(current)
            .refactoredMethods(refactored)
            .build();
    }
}
