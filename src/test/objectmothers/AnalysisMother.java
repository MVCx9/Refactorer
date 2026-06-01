package test.objectmothers;

import java.time.LocalDateTime;
import java.util.List;

import main.builder.ClassAnalysis;
import main.builder.MethodAnalysis;
import main.builder.ProjectAnalysis;
import main.builder.WorkspaceAnalysis;

public final class AnalysisMother {

    public static final LocalDateTime FIXED_DATE = LocalDateTime.of(2025, 6, 1, 10, 0);

    private AnalysisMother() {
    }

    public static ClassAnalysis classAnalysis(final String name) {
        final MethodAnalysis current = MethodAnalysisMother.basic("foo");
        final MethodAnalysis refactored = MethodAnalysisMother.refactored("foo", 4, 1);
        return ClassAnalysis.builder()
                .className(name)
                .analysisDate(FIXED_DATE)
                .currentMethods(List.of(current))
                .refactoredMethods(List.of(refactored))
                .currentSource("class A {}")
                .refactoredSource("class A { void foo(){} }")
                .complexityThreshold(12)
                .path("/p/src/" + name)
                .build();
    }

    public static ClassAnalysis emptyClassAnalysis() {
        return ClassAnalysis.builder().build();
    }

    public static ProjectAnalysis projectAnalysis(final String name) {
        return ProjectAnalysis.builder()
                .name(name)
                .analysisDate(FIXED_DATE)
                .classes(List.of(classAnalysis("A.java")))
                .complexityThreshold(12)
                .build();
    }

    public static WorkspaceAnalysis workspaceAnalysis() {
        return WorkspaceAnalysis.builder()
                .name("WS")
                .analysisDate(FIXED_DATE)
                .projects(List.of(projectAnalysis("P1")))
                .build();
    }
}
