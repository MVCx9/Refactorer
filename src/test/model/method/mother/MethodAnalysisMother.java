package test.model.method.mother;

import java.util.List;
import main.builder.MethodAnalysis;
import main.model.change.ExtractionPlan;
import main.neo.cem.CodeExtractionMetrics;
import main.neo.cem.CodeExtractionMetricsStats;

public final class MethodAnalysisMother {
    private MethodAnalysisMother() {}

    public static MethodAnalysis withValues(final String name, final int currentCc, final int currentLoc, final int refCc, final int refLoc) {
        return MethodAnalysis.builder()
            .methodName(name)
            .currentCc(currentCc)
            .currentLoc(currentLoc)
            .refactoredCc(refCc)
            .refactoredLoc(refLoc)
            .extraction((CodeExtractionMetrics) null)
            .stats((CodeExtractionMetricsStats) null)
            .doPlan(new ExtractionPlan(List.of()))
            .undoPlan(new ExtractionPlan(List.of()))
            .build();
    }

    public static MethodAnalysis withPlans(final String name, final int currentCc, final int currentLoc, final int refCc, final int refLoc, final ExtractionPlan doPlan, final ExtractionPlan undoPlan) {
        return MethodAnalysis.builder()
            .methodName(name)
            .currentCc(currentCc)
            .currentLoc(currentLoc)
            .refactoredCc(refCc)
            .refactoredLoc(refLoc)
            .extraction((CodeExtractionMetrics) null)
            .stats((CodeExtractionMetricsStats) null)
            .doPlan(doPlan)
            .undoPlan(undoPlan)
            .build();
    }
}
