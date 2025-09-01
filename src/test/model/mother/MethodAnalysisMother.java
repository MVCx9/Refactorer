package test.mother;

import java.util.List;

import main.builder.MethodAnalysis;
import main.model.change.ExtractionPlan;
import main.neo.cem.CodeExtractionMetrics;
import main.neo.cem.CodeExtractionMetricsStats;

public final class MethodAnalysisMother {

    private MethodAnalysisMother() {}

    public static MethodAnalysis.Builder base() {
        return MethodAnalysis.builder()
            .methodName("m")
            .currentCc(5)
            .currentLoc(20)
            .refactoredCc(3)
            .refactoredLoc(12)
            .extraction(new CodeExtractionMetrics(List.of()))
            .stats(new CodeExtractionMetricsStats(0,0,0,0))
            .doPlan(new ExtractionPlan(List.of()))
            .undoPlan(new ExtractionPlan(List.of()));
    }

    public static MethodAnalysis with(String name, int currLoc, int currCc, int refLoc, int refCc) {
        return base()
            .methodName(name)
            .currentLoc(currLoc)
            .currentCc(currCc)
            .refactoredLoc(refLoc)
            .refactoredCc(refCc)
            .build();
    }
}
