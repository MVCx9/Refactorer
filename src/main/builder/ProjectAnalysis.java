package main.builder;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;

public class ProjectAnalysis {

    private final IProject project;
    private final String name;
    private final List<ClassAnalysis> classes;

    private ProjectAnalysis(Builder builder) {
        this.project = builder.project;
        this.name = builder.name;
        this.classes = List.copyOf(builder.classes);
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

    public List<ClassAnalysis> getFiles() {
        return Collections.unmodifiableList(classes);
    }

    public static class Builder {
        private IProject project;
        private String name = "<unnamed>";
        private List<ClassAnalysis> classes = Collections.emptyList();

        public Builder project(IProject project) {
            this.project = project;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder classes(List<ClassAnalysis> files) {
            this.classes = files;
            return this;
        }

        public ProjectAnalysis build() {
            return new ProjectAnalysis(this);
        }
    }
}