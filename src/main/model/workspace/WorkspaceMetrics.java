package main.model.workspace;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import main.model.common.ComplexityStats;
import main.model.common.Identifiable;
import main.model.common.LocStats;
import main.model.project.ProjectMetrics;

public class WorkspaceMetrics implements Identifiable, ComplexityStats, LocStats{

	private final String name;
	private final LocalDateTime analysisDate;
	private final List<ProjectMetrics> projects;

	public WorkspaceMetrics(WorkspaceMetricsBuilder workSpaceMetricsBuilder) {
		super();
		this.name = workSpaceMetricsBuilder.name;
		this.analysisDate = workSpaceMetricsBuilder.analysisDate;
		this.projects = workSpaceMetricsBuilder.projects;
	}

	public static WorkspaceMetricsBuilder builder() {
		return new WorkspaceMetricsBuilder();
	}

	@Override
	public String getName() {
		return name;
	}

	public LocalDateTime getAnalysisDate() {
		return analysisDate;
	}

	public List<ProjectMetrics> getProjects() {
		return projects;
	}

	@Override
	public int getCurrentLoc() {
		return projects.stream().mapToInt(ProjectMetrics::getCurrentLoc).sum();
	}

	@Override
	public int getRefactoredLoc() {
		return projects.stream().mapToInt(ProjectMetrics::getRefactoredLoc).sum();
	}

	@Override
	public int getCurrentCc() {
		return average(ProjectMetrics::getCurrentCc);
	}

	@Override
	public int getRefactoredCc() {
		return average(ProjectMetrics::getRefactoredCc);
	}

	public int getCurrentMethodCount() {
		return projects.stream().mapToInt(ProjectMetrics::getCurrentMethodCount).sum();
	}
	
	public int getRefactoredMethodCount() {
		return projects.stream().mapToInt(ProjectMetrics::getRefactoredMethodCount).sum();
	}
	
	public int getAverageCurrentLoc() {
		return average(ProjectMetrics::getAverageCurrentLoc);
	}
	
	public int getAverageRefactoredLoc() {
		return average(ProjectMetrics::getAverageRefactoredLoc);
	}
	
	public int getAverageCurrentCc() {
		return average(ProjectMetrics::getAverageCurrentCc);
	}
	
	public int getAverageRefactoredCc() {
		return average(ProjectMetrics::getAverageRefactoredCc);
	}
	
	public int getAverageCurrentMethodCount() {
		return average(ProjectMetrics::getAverageCurrentMethodCount);
	}
	
	public int getAverageRefactoredMethodCount() {
		return average(ProjectMetrics::getAverageRefactoredMethodCount);
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

	private int average(java.util.function.ToIntFunction<ProjectMetrics> mapper) {
		return (int) Math.round(projects.stream().mapToInt(mapper).average().orElse(0.0));
	}

	public static class WorkspaceMetricsBuilder {
		private String name = "<unnamed>";
		private LocalDateTime analysisDate = LocalDateTime.now();
		private List<ProjectMetrics> projects = Collections.emptyList();

		public WorkspaceMetricsBuilder() {
		}

		public WorkspaceMetricsBuilder name(String name) {
			this.name = name;
			return this;
		}

		public WorkspaceMetricsBuilder analysisDate(LocalDateTime analysisDate) {
			this.analysisDate = analysisDate;
			return this;
		}

		public WorkspaceMetricsBuilder projects(List<ProjectMetrics> projects) {
			this.projects = projects;
			return this;
		}

		public WorkspaceMetrics build() {
			return new WorkspaceMetrics(this);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(analysisDate, name, projects);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkspaceMetrics other = (WorkspaceMetrics) obj;
		return Objects.equals(analysisDate, other.analysisDate) && Objects.equals(name, other.name)
				&& Objects.equals(projects, other.projects);
	}

	@Override
	public String toString() {
		return "WorkspaceMetrics [name=" + name + ", analysisDate=" + analysisDate + ", projects=" + projects + "]";
	}
}