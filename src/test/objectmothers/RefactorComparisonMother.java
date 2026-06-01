package test.objectmothers;

import main.refactor.RefactorComparison;

public final class RefactorComparisonMother {

    private RefactorComparisonMother() {
    }

    public static RefactorComparison basic(final String name) {
        return RefactorComparison.builder()
                .name(name)
                .reducedComplexity(4)
                .numberOfExtractions(1)
                .refactoredSource("void " + name + "(){}\n")
                .usedILP(true)
                .build();
    }
}
