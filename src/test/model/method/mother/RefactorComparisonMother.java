package test.model.method.mother;

import java.util.List;
import main.model.change.ExtractionPlan;
import main.refactor.RefactorComparison;

public final class RefactorComparisonMother {
    private RefactorComparisonMother() {}

    public static RefactorComparison simple(final String name, final int originalCc, final int originalLoc, final int refCc, final int refLoc) {
        return RefactorComparison.builder()
            .name(name)
            .originalCc(originalCc)
            .originalLoc(originalLoc)
            .refactoredCc(refCc)
            .refactoredLoc(refLoc)
            .doPlan(new ExtractionPlan(List.of()))
            .undoPlan(new ExtractionPlan(List.of()))
            .build();
    }

    public static RefactorComparison withPlans(final String name, final int originalCc, final int originalLoc, final int refCc, final int refLoc, final ExtractionPlan doPlan, final ExtractionPlan undoPlan) {
        return RefactorComparison.builder()
            .name(name)
            .originalCc(originalCc)
            .originalLoc(originalLoc)
            .refactoredCc(refCc)
            .refactoredLoc(refLoc)
            .doPlan(doPlan)
            .undoPlan(undoPlan)
            .build();
    }

    public static RefactorComparison withNullPlans(final String name, final int originalCc, final int originalLoc, final int refCc, final int refLoc) {
        return RefactorComparison.builder()
            .name(name)
            .originalCc(originalCc)
            .originalLoc(originalLoc)
            .refactoredCc(refCc)
            .refactoredLoc(refLoc)
            .doPlan(null)
            .undoPlan(null)
            .build();
    }
}
