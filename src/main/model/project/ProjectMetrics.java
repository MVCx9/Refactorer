package main.model.project;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import main.model.clazz.ClassMetrics;
import main.model.common.ComplexityStats;
import main.model.common.Identifiable;
import main.model.common.LocStats;
import main.model.method.MethodMetrics;

public class ProjectMetrics implements Identifiable, ComplexityStats, LocStats {

	private final String name;
	private final LocalDateTime analysisDate;
	private final List<ClassMetrics> classes;

	public ProjectMetrics(ProjectMetricsBuilder projectMetricsBuilder) {
		super();
		this.name = projectMetricsBuilder.name;
		this.analysisDate = projectMetricsBuilder.analysisDate;
		this.classes = projectMetricsBuilder.classes;
	}

	public static ProjectMetricsBuilder builder() {
		return new ProjectMetricsBuilder();
	}

	@Override
	public String getName() {
		return name;
	}
	
	public LocalDateTime getAnalysisDate() {
		return analysisDate;
	}

	@Override
	public int getCurrentLoc() {
		return classes.stream().mapToInt(ClassMetrics::getCurrentLoc).sum();
	}
	
	@Override
	public int getCurrentCc() {
		return classes.stream().mapToInt(ClassMetrics::getCurrentCc).sum();
	}
	
	@Override
	public int getRefactoredLoc() {
		return classes.stream().mapToInt(ClassMetrics::getRefactoredLoc).sum();
	}
	
	@Override
	public int getRefactoredCc() {
		return classes.stream().mapToInt(ClassMetrics::getRefactoredCc).sum();
	}
	
	public int getAverageCurrentLoc() {
		return average(ClassMetrics::getAverageCurrentLoc);
	}
	
	public int getAverageCurrentCc() {
		return average(ClassMetrics::getAverageCurrentCc);
	}

	public int getAverageRefactoredLoc() {
		return average(ClassMetrics::getAverageRefactoredLoc);
	}
	
	public int getAverageRefactoredCc() {
		return average(ClassMetrics::getAverageRefactoredCc);
	}
	
	public int getClassCount() {
		return classes.size();
	}

	public List<ClassMetrics> getClasses() {
		return Collections.unmodifiableList(classes);
	}

	public int getCurrentMethodCount() {
		return classes.stream().mapToInt(ClassMetrics::getCurrentMethodCount).sum();
	}
	
	public int getRefactoredMethodCount() {
		return classes.stream().mapToInt(ClassMetrics::getRefactoredMethodCount).sum();
	}
	
	public int getAverageCurrentMethodCount() {
		return average(ClassMetrics::getCurrentMethodCount);
	}
	
	public int getAverageRefactoredMethodCount() {
		return average(ClassMetrics::getRefactoredMethodCount);
	}
	
	public int getAverageReducedComplexity() {
		return getAverageCurrentCc() - getAverageRefactoredCc();
	}

	public int getAverageReducedLoc() {
		return getAverageCurrentLoc() - getAverageRefactoredLoc();
	}
	
	public int getReducedComplexity() {
		return getCurrentCc() - getRefactoredCc();
	}

	public int getReducedLoc() {
		return getCurrentLoc() - getRefactoredLoc();
	}
	
	public int getMethodExtractionCount() {
		return getRefactoredMethodCount() - getCurrentMethodCount();
	}
	
	private int average(java.util.function.ToIntFunction<ClassMetrics> mapper) {
		return (int) Math.round(classes.stream().mapToInt(mapper).average().orElse(0.0));
	}
	
	public List<ClassMetrics> getMethodsWithRefactors() {
		return classes.stream()
			.filter(c -> c.getMethodExtractionCount() > 0)
			.map(originalClass -> {
				List<MethodMetrics> extractedMethods = originalClass.getRefactoredMethods().stream()
						.filter(m -> m.getName() != null && m.getName().contains("_ext_"))
						.collect(Collectors.toList());
				Set<String> baseNames = extractedMethods.stream()
						.map(m -> {
							String name = m.getName();
							int idx = name.indexOf("_ext_");
							return idx > -1 ? name.substring(0, idx) : name;
						})
						.collect(Collectors.toSet());
				List<MethodMetrics> originalBaseMethods = originalClass.getCurrentMethods().stream()
						.filter(m -> baseNames.contains(m.getName()))
						.collect(Collectors.toList());
				return ClassMetrics.builder()
						.name(originalClass.getName())
						.analysisDate(originalClass.getAnalysisDate())
						.currentSource(originalClass.getCurrentSource())
						.refactoredSource(originalClass.getRefactoredSource())
						.currentMethods(originalBaseMethods)
						.refactoredMethods(extractedMethods)
						.build();
			})
			.collect(Collectors.toList());
	}

	public static class ProjectMetricsBuilder {
		private String name = "<unnamed>";
		private LocalDateTime analysisDate = LocalDateTime.now();
		private List<ClassMetrics> classes = Collections.emptyList();

		public ProjectMetricsBuilder() {
		}

		public ProjectMetricsBuilder name(String name) {
			this.name = name;
			return this;
		}
		
		public ProjectMetricsBuilder analysisDate(LocalDateTime analysisDate) {
			this.analysisDate = analysisDate;
			return this;
		}

		public ProjectMetricsBuilder classes(List<ClassMetrics> classes) {
			this.classes = classes;
			return this;
		}

		public ProjectMetrics build() {
			return new ProjectMetrics(this);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(analysisDate, classes, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectMetrics other = (ProjectMetrics) obj;
		return Objects.equals(analysisDate, other.analysisDate) && Objects.equals(classes, other.classes)
				&& Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return "ProjectMetrics [name=" + name + ", analysisDate=" + analysisDate + ", classes=" + classes + "]";
	}
}