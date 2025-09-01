package test.mother;

import java.time.LocalDateTime;
import java.util.List;

import main.builder.ProjectAnalysis;
import main.builder.ClassAnalysis;

public final class ProjectAnalysisMother {

    private ProjectAnalysisMother() {}

    public static ProjectAnalysis.Builder base() {
        return ProjectAnalysis.builder()
            .name("P")
            .analysisDate(LocalDateTime.now())
            .classes(List.of(ClassAnalysisMother.base().className("C1").build()));
    }

    public static ProjectAnalysis.Builder withClasses(List<ClassAnalysis> classes) {
        return base().classes(classes);
    }
}
