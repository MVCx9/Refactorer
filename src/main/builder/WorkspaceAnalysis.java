package main.builder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class WorkspaceAnalysis {

	private final String name;
	private final LocalDateTime analysisDate;
	private final List<ProjectAnalysis> projects;

	private WorkspaceAnalysis(Builder b) {
		this.name = b.name;
		this.analysisDate = b.analysisDate;
		this.projects = List.copyOf(b.projects);
	}

	public static Builder builder() {
		return new Builder();
	}

	public String getName() {
		return name;
	}

	public LocalDateTime getAnalysisDate() {
		return analysisDate;
	}

	public List<ProjectAnalysis> getProjects() {
		return Collections.unmodifiableList(projects);
	}

	public static class Builder {
		private String name = "<unnamed>";
		private LocalDateTime analysisDate = LocalDateTime.now();
		private List<ProjectAnalysis> projects = Collections.emptyList();

		public Builder name(String v) {
			this.name = v;
			return this;
		}

		public Builder analysisDate(LocalDateTime v) {
			this.analysisDate = v;
			return this;
		}

		public Builder projects(List<ProjectAnalysis> v) {
			this.projects = v;
			return this;
		}

		public WorkspaceAnalysis build() {
			return new WorkspaceAnalysis(this);
		}
	}
}