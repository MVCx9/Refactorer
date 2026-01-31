package test.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.builder.ClassAnalysis;
import main.builder.ProjectAnalysis;
import test.model.clazz.mother.ClassAnalysisMother;

class ProjectAnalysisTest {
	private final LocalDateTime fixedDate = LocalDateTime.of(2024, 6, 15, 10, 30);

	private void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("given_builderWithDefaults_when_build_should_createProjectAnalysisWithDefaultValues")
	void given_builderWithDefaults_when_build_should_createProjectAnalysisWithDefaultValues() {
		final ProjectAnalysis analysis = ProjectAnalysis.builder().build();
		assertNotNull(analysis);
		toStrictEqual("<unnamed>", analysis.getName());
		assertNotNull(analysis.getAnalysisDate());
		assertNotNull(analysis.getFiles());
		toStrictEqual(0, analysis.getFiles().size());
		toStrictEqual(15, analysis.getComplexityThreshold());
	}

	@Test
	@DisplayName("given_builderWithAllValues_when_build_should_createProjectAnalysisWithAllValues")
	void given_builderWithAllValues_when_build_should_createProjectAnalysisWithAllValues() {
		final List<ClassAnalysis> classes = List.of(ClassAnalysisMother.defaultAnalysis());
		final ProjectAnalysis analysis = ProjectAnalysis.builder()
				.name("TestProject")
				.analysisDate(this.fixedDate)
				.classes(classes)
				.complexityThreshold(20)
				.build();
		toStrictEqual("TestProject", analysis.getName());
		toStrictEqual(this.fixedDate, analysis.getAnalysisDate());
		toStrictEqual(1, analysis.getFiles().size());
		toStrictEqual(20, analysis.getComplexityThreshold());
	}

	@Test
	@DisplayName("given_projectAnalysis_when_getFiles_should_returnUnmodifiableList")
	void given_projectAnalysis_when_getFiles_should_returnUnmodifiableList() {
		final ProjectAnalysis analysis = ProjectAnalysis.builder()
				.classes(List.of(ClassAnalysisMother.defaultAnalysis()))
				.build();
		assertThrows(UnsupportedOperationException.class, () -> analysis.getFiles().add(null));
	}

	@Test
	@DisplayName("given_emptyClasses_when_build_should_createEmptyList")
	void given_emptyClasses_when_build_should_createEmptyList() {
		final ProjectAnalysis analysis = ProjectAnalysis.builder()
				.classes(Collections.emptyList())
				.build();
		toStrictEqual(0, analysis.getFiles().size());
	}

	@Test
	@DisplayName("given_projectAnalysis_when_getProject_should_returnNullByDefault")
	void given_projectAnalysis_when_getProject_should_returnNullByDefault() {
		final ProjectAnalysis analysis = ProjectAnalysis.builder().build();
		assertNull(analysis.getProject());
	}

	@Test
	@DisplayName("given_customName_when_getName_should_returnCustomName")
	void given_customName_when_getName_should_returnCustomName() {
		final ProjectAnalysis analysis = ProjectAnalysis.builder()
				.name("CustomProject")
				.build();
		toStrictEqual("CustomProject", analysis.getName());
	}

	@Test
	@DisplayName("given_customThreshold_when_getComplexityThreshold_should_returnCustomThreshold")
	void given_customThreshold_when_getComplexityThreshold_should_returnCustomThreshold() {
		final ProjectAnalysis analysis = ProjectAnalysis.builder()
				.complexityThreshold(25)
				.build();
		toStrictEqual(25, analysis.getComplexityThreshold());
	}

	@Test
	@DisplayName("given_multipleClasses_when_getFiles_should_returnAllClasses")
	void given_multipleClasses_when_getFiles_should_returnAllClasses() {
		final List<ClassAnalysis> classes = List.of(
				ClassAnalysisMother.withName("Class1"),
				ClassAnalysisMother.withName("Class2"),
				ClassAnalysisMother.withName("Class3"));
		final ProjectAnalysis analysis = ProjectAnalysis.builder()
				.classes(classes)
				.build();
		toStrictEqual(3, analysis.getFiles().size());
	}
}
