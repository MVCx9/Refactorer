package main.model.workspace;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import main.builder.WorkspaceAnalysis;
import main.model.project.ProjectAnalysisMetricsMapper;
import main.model.project.ProjectMetrics;

public final class WorkspaceAnalysisMetricsMapper {

    private WorkspaceAnalysisMetricsMapper() {}

    public static WorkspaceMetrics toWorkspaceMetrics(WorkspaceAnalysis analysis) {
        Objects.requireNonNull(analysis, "analysis must not be null");

        List<ProjectMetrics> projectMetrics = analysis.getProjects().stream()
                .map(ProjectAnalysisMetricsMapper::toProjectMetrics)
                .collect(Collectors.toList());

        return WorkspaceMetrics.builder()
            .name(analysis.getName())
            .analysisDate(analysis.getAnalysisDate())
            .projects(projectMetrics)
            .build();
    }
}
