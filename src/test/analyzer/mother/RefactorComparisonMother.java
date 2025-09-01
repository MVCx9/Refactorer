package test.analyzer.mother;

import java.util.List;
import main.model.change.ExtractionPlan;
import main.refactor.RefactorComparison;

public final class RefactorComparisonMother {
    private RefactorComparisonMother() {}

    public static List<RefactorComparison> originalAndExtraction(final String methodName, final int originalCc, final int refactoredCc, final int originalLoc, final int refactoredLoc) {
        final RefactorComparison original = RefactorComparison.builder()
            .name(methodName)
            .originalCc(originalCc)
            .originalLoc(originalLoc)
            .refactoredCc(refactoredCc)
            .refactoredLoc(refactoredLoc)
            .doPlan(new ExtractionPlan(List.of()))
            .undoPlan(new ExtractionPlan(List.of()))
            .build();
        final RefactorComparison extracted = RefactorComparison.builder()
            .name(methodName + "_extracted_1")
            .originalCc(originalCc / 2)
            .originalLoc(originalLoc / 2)
            .refactoredCc(refactoredCc / 2)
            .refactoredLoc(refactoredLoc / 2)
            .doPlan(new ExtractionPlan(List.of()))
            .undoPlan(new ExtractionPlan(List.of()))
            .build();
        return List.of(original, extracted);
    }
}
