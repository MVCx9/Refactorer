package main.model.workspace;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import main.model.project.ProjectMetrics;

public class WorkspaceMetrics {

	private final String name;
	private final LocalDate analysisDate;
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

	public String getName() {
		return name;
	}

	public LocalDate getTimestamp() {
		return analysisDate;
	}

	public List<ProjectMetrics> getProjects() {
		return projects;
	}

	/**
	 * Suma de la complejidad cognitiva actual de todos los proyectos
	 */
	public int getCurrentLoc() {
		return projects.stream().mapToInt(ProjectMetrics::getCurrentLoc).sum();
	}

	/**
	 * Suma de la complejidad cognitiva tras refactorizar de todos los proyectos
	 */
	public int getRefactoredLoc() {
		return projects.stream().mapToInt(ProjectMetrics::getRefactoredLoc).sum();
	}

	/**
	 * Media de la suma de la complejidad cognitiva actual de todos los proyectos
	 */
	public int getCurrentCc() {
		return averageCc(ProjectMetrics::getCurrentCc);
	}

	/**
	 * Media de la suma de la complejidad cognitiva tras refactorizar de todos los
	 * proyectos
	 */
	public int getRefactoredCc() {
		return averageCc(ProjectMetrics::getRefactoredCc);
	}

	/**
	 * Cantidad de métodos actuales de todos los proyectos
	 */
	public int getCurrentMethodCount() {
		return projects.stream().mapToInt(ProjectMetrics::getCurrentMethodCount).sum();
	}

	/**
	 * Cantidad de métodos tras refactorizar de todos los proyectos
	 */
	public int getRefactoredMethodCount() {
		return projects.stream().mapToInt(ProjectMetrics::getRefactoredMethodCount).sum();
	}

	// Función para calcular la media dado un mapper
	private int averageCc(java.util.function.ToIntFunction<ProjectMetrics> mapper) {
		return (int) Math.round(projects.stream().mapToInt(mapper).average().orElse(0.0));
	}

	public static class WorkspaceMetricsBuilder {
		private String name = "<unnamed>";
		private LocalDate analysisDate = LocalDate.EPOCH;
		private List<ProjectMetrics> projects = Collections.emptyList();

		public WorkspaceMetricsBuilder() {
		}

		public WorkspaceMetricsBuilder name(String name) {
			this.name = name;
			return this;
		}

		public WorkspaceMetricsBuilder analysisDate(LocalDate analysisDate) {
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
