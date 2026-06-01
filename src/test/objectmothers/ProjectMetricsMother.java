package test.objectmothers;

import java.util.List;

import main.model.clazz.ClassMetrics;
import main.model.project.ProjectMetrics;

public final class ProjectMetricsMother {

    private ProjectMetricsMother() {
    }

    public static ProjectMetrics empty(final String name) {
        return ProjectMetrics.builder()
                .name(name)
                .analysisDate(ClassMetricsMother.FIXED_DATE)
                .classes(List.of())
                .complexityThreshold(15)
                .build();
    }

    public static ProjectMetrics simple(final String name) {
        return ProjectMetrics.builder()
                .name(name)
                .analysisDate(ClassMetricsMother.FIXED_DATE)
                .classes(List.of(ClassMetricsMother.simple("A.java")))
                .complexityThreshold(15)
                .build();
    }

    public static ProjectMetrics withRefactors(final String name) {
        return ProjectMetrics.builder()
                .name(name)
                .analysisDate(ClassMetricsMother.FIXED_DATE)
                .classes(List.of(
                        ClassMetricsMother.withRefactors("A.java"),
                        ClassMetricsMother.simple("B.java")))
                .complexityThreshold(10)
                .build();
    }

    public static ProjectMetrics fromClasses(final String name, final List<ClassMetrics> classes) {
        return ProjectMetrics.builder()
                .name(name)
                .analysisDate(ClassMetricsMother.FIXED_DATE)
                .classes(classes)
                .complexityThreshold(15)
                .build();
    }
}
