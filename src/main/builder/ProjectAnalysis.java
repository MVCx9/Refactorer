package main.builder;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;

public class ProjectAnalysis {

    private final IProject project;
    private final String name;
    private final List<FileAnalysis> files;

    private ProjectAnalysis(Builder builder) {
        this.project = builder.project;
        this.name = builder.name;
        this.files = List.copyOf(builder.files);
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

    public List<FileAnalysis> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public static class Builder {
        private IProject project;
        private String name = "<unnamed>";
        private List<FileAnalysis> files = Collections.emptyList();

        public Builder project(IProject project) {
            this.project = project;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder files(List<FileAnalysis> files) {
            this.files = files;
            return this;
        }

        public ProjectAnalysis build() {
            return new ProjectAnalysis(this);
        }
    }
}