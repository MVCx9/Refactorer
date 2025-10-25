package main.builder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;

public class ProjectAnalysis {

	private final IProject project;
	private final String name;
	private final LocalDateTime analysisDate;
	private final List<ClassAnalysis> classes;
	private final int complexityThreshold;

	private ProjectAnalysis(Builder builder) {
		this.project = builder.project;
		this.name = builder.name;
		this.analysisDate = builder.analysisDate;
		this.classes = builder.classes;
		this.complexityThreshold = builder.complexityThreshold;
	}

	public static Builder builder() {
		return new Builder();
	}

	public IProject getProject() {
		return project;
	}

	public String getName() {
		return name;
	}

	public LocalDateTime getAnalysisDate() {
		return analysisDate;
	}

	public List<ClassAnalysis> getFiles() {
		return Collections.unmodifiableList(classes);
	}

	public int getComplexityThreshold() {
		return complexityThreshold;
	}

	public static class Builder {
		private IProject project;
		private String name = "<unnamed>";
		private LocalDateTime analysisDate = LocalDateTime.now();
		private List<ClassAnalysis> classes = Collections.emptyList();
		private int complexityThreshold = 15;

		public Builder project(IProject project) {
			this.project = project;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder analysisDate(LocalDateTime analysisDate) {
			this.analysisDate = analysisDate;
			return this;
		}

		public Builder classes(List<ClassAnalysis> files) {
			this.classes = files;
			return this;
		}

		public Builder complexityThreshold(int v) {
			this.complexityThreshold = v;
			return this;
		}

		public ProjectAnalysis build() {
			return new ProjectAnalysis(this);
		}
	}
}