package test.model.project.mother;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.mockito.Mockito;

import main.builder.ClassAnalysis;
import main.builder.ProjectAnalysis;

public final class ProjectAnalysisMother {

    private ProjectAnalysisMother() {
    }

    public static ProjectAnalysis with(String name, LocalDateTime date, List<ClassAnalysis> classes) {
        final IProject project = Mockito.mock(IProject.class);
        return ProjectAnalysis.builder()
            .project(project)
            .name(name)
            .analysisDate(date)
            .classes(classes)
            .build();
    }

    public static ProjectAnalysis simple(List<ClassAnalysis> classes) {
        final LocalDateTime date = LocalDateTime.of(2025, 1, 1, 10, 0);
        return with("SampleProject", date, classes);
    }
}
