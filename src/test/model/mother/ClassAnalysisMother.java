package test.mother;

import java.time.LocalDateTime;
import java.util.List;

import main.builder.ClassAnalysis;
import main.builder.MethodAnalysis;

public final class ClassAnalysisMother {

    private ClassAnalysisMother() {}

    public static ClassAnalysis.Builder base() {
        return ClassAnalysis.builder()
            .className("C")
            .analysisDate(LocalDateTime.now())
            .currentMethods(List.of(MethodAnalysisMother.with("m1", 10, 2, 6, 1)))
            .refactoredMethods(List.of(MethodAnalysisMother.with("m1r", 10, 2, 6, 1)));
    }
    
    public static ClassAnalysis.Builder withMethods(List<MethodAnalysis> current, List<MethodAnalysis> refactored) {
        return base().currentMethods(current).refactoredMethods(refactored);
    }
}
