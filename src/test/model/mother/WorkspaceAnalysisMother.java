package test.mother;

import java.time.LocalDateTime;
import java.util.List;

import main.builder.ProjectAnalysis;
import main.builder.WorkspaceAnalysis;

public final class WorkspaceAnalysisMother {

    private WorkspaceAnalysisMother() {}

    public static WorkspaceAnalysis.Builder base() {
        return WorkspaceAnalysis.builder()
            .name("W")
            .analysisDate(LocalDateTime.now())
            .projects(List.of(ProjectAnalysisMother.base().name("P1").build()));
    }

    public static WorkspaceAnalysis.Builder withProjects(List<ProjectAnalysis> projects) {
        return base().projects(projects);
    }
}
