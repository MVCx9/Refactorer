package test.objectmothers;

import java.util.List;

import main.model.workspace.WorkspaceMetrics;

public final class WorkspaceMetricsMother {

    private WorkspaceMetricsMother() {
    }

    public static WorkspaceMetrics empty() {
        return WorkspaceMetrics.builder()
                .name("WS")
                .analysisDate(ClassMetricsMother.FIXED_DATE)
                .projects(List.of())
                .build();
    }

    public static WorkspaceMetrics simple() {
        return WorkspaceMetrics.builder()
                .name("WS")
                .analysisDate(ClassMetricsMother.FIXED_DATE)
                .projects(List.of(ProjectMetricsMother.simple("P1")))
                .build();
    }

    public static WorkspaceMetrics withRefactors() {
        return WorkspaceMetrics.builder()
                .name("WS")
                .analysisDate(ClassMetricsMother.FIXED_DATE)
                .projects(List.of(
                        ProjectMetricsMother.withRefactors("P1"),
                        ProjectMetricsMother.simple("P2")))
                .build();
    }
}
