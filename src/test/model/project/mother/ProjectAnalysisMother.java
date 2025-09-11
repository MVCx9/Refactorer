package test.model.project.mother;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.core.resources.IProject;

import main.builder.ProjectAnalysis;
import main.builder.ClassAnalysis;
import test.model.clazz.mother.ClassAnalysisMother;

public final class ProjectAnalysisMother {
	private static final String DEFAULT_NAME = "SampleProject";

	private ProjectAnalysisMother() {}

	public static ProjectAnalysis defaultAnalysis() {
		final ClassAnalysis c1 = ClassAnalysisMother.withName("ClassOne");
		final ClassAnalysis c2 = ClassAnalysisMother.withName("ClassTwo");
		final List<ClassAnalysis> classes = List.of(c1, c2);
		return ProjectAnalysis.builder()
				.project(null)
				.name(DEFAULT_NAME)
				.analysisDate(LocalDateTime.now())
				.classes(classes)
				.build();
	}

	public static ProjectAnalysis withName(String name) {
		final String n = name == null ? DEFAULT_NAME : name;
		final ClassAnalysis c1 = ClassAnalysisMother.withName("ClassOne");
		final ClassAnalysis c2 = ClassAnalysisMother.withName("ClassTwo");
		final List<ClassAnalysis> classes = List.of(c1, c2);
		return ProjectAnalysis.builder()
				.project(null)
				.name(n)
				.analysisDate(LocalDateTime.now())
				.classes(classes)
				.build();
	}

	public static ProjectAnalysis custom(IProject project, String name, LocalDateTime analysisDate, List<ClassAnalysis> classes) {
		final String n = name == null ? DEFAULT_NAME : name;
		final LocalDateTime date = analysisDate == null ? LocalDateTime.now() : analysisDate;
		final List<ClassAnalysis> cls = classes == null ? List.of() : classes;
		return ProjectAnalysis.builder()
				.project(project)
				.name(n)
				.analysisDate(date)
				.classes(cls)
				.build();
	}
}