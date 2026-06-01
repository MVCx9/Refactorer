package test.objectmothers;

import main.builder.MethodAnalysis;

public final class MethodAnalysisMother {

    private MethodAnalysisMother() {
    }

    public static MethodAnalysis basic(final String name) {
        return MethodAnalysis.builder()
                .methodName(name)
                .cc(5)
                .loc(10)
                .reducedComplexity(0)
                .numberOfExtractions(0)
                .build();
    }

    public static MethodAnalysis refactored(final String name, final int reduced, final int extractions) {
        return MethodAnalysis.builder()
                .methodName(name)
                .cc(2)
                .loc(5)
                .reducedComplexity(reduced)
                .numberOfExtractions(extractions)
                .usedILP(true)
                .refactoredSource("void " + name + "(){}\n")
                .build();
    }
}
