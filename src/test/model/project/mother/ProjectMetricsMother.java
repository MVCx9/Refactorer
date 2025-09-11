package test.model.project.mother;

import java.time.LocalDateTime;
import java.util.List;

import main.model.project.ProjectMetrics;
import main.model.clazz.ClassMetrics;
import test.model.clazz.mother.ClassMetricsMother;

public final class ProjectMetricsMother {
	private static final String DEFAULT_NAME = "SampleProject";

	private ProjectMetricsMother() {}

	public static ProjectMetrics defaultMetrics() {
		final ClassMetrics c1 = ClassMetricsMother.withName("SampleClass1");
		final ClassMetrics c2 = ClassMetricsMother.withName("SampleClass2");
		final List<ClassMetrics> classes = List.of(c1, c2);
		return ProjectMetrics.builder()
				.name(DEFAULT_NAME)
				.analysisDate(LocalDateTime.now())
				.classes(classes)
				.build();
	}

	public static ProjectMetrics withName(String name) {
		final String n = name == null ? DEFAULT_NAME : name;
		final ClassMetrics c1 = ClassMetricsMother.withName("SampleClass1");
		final ClassMetrics c2 = ClassMetricsMother.withName("SampleClass2");
		final List<ClassMetrics> classes = List.of(c1, c2);
		return ProjectMetrics.builder()
				.name(n)
				.analysisDate(LocalDateTime.now())
				.classes(classes)
				.build();
	}

	public static ProjectMetrics custom(String name, LocalDateTime analysisDate, List<ClassMetrics> classes) {
		final String n = name == null ? DEFAULT_NAME : name;
		final LocalDateTime date = analysisDate == null ? LocalDateTime.now() : analysisDate;
		final List<ClassMetrics> cls = classes == null ? List.of() : classes;
		return ProjectMetrics.builder()
				.name(n)
				.analysisDate(date)
				.classes(cls)
				.build();
	}
}