package main.handler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import main.builder.ProjectAnalysis;
import main.builder.ProjectFilesAnalyzer;
import main.builder.WorkspaceAnalysis;
import main.error.AnalyzeException;
import main.error.ResourceNotFoundException;
import main.model.project.ProjectMetrics;
import main.model.workspace.WorkspaceAnalysisMetricsMapper;
import main.model.workspace.WorkspaceMetrics;

public class AnalyzeWorkspaceHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IProject[] eclipseProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        if (eclipseProjects.length == 0) {
            throw new ResourceNotFoundException("No hay proyectos abiertos en el workspace.");
        }

        ProjectFilesAnalyzer analyzer = new ProjectFilesAnalyzer();
        List<ProjectAnalysis> projectAnalyses = new ArrayList<>();

        for (IProject project : eclipseProjects) {
            if (project == null || !project.isOpen()) continue;
            try {
                ProjectAnalysis analysis = analyzer.analyzeProject(project);
                if (analysis != null) {
                    projectAnalyses.add(analysis);
                }
            } catch (CoreException e) {
                throw new AnalyzeException("Error analyzing project: " + project.getName(), e);
            }
        }

        WorkspaceAnalysis workspaceAnalysis = WorkspaceAnalysis.builder()
                .name("Eclipse Workspace")
                .analysisDate(LocalDate.now())
                .projects(projectAnalyses)
                .build();

        WorkspaceMetrics workspaceMetrics = WorkspaceAnalysisMetricsMapper.toWorkspaceMetrics(workspaceAnalysis);
        logToConsole(workspaceMetrics);
        return null;
    }

    private void logToConsole(WorkspaceMetrics workspaceMetrics) {
        StringBuilder sb = new StringBuilder();
        sb.append("Refactorer — Métricas del workspace: ").append(workspaceMetrics.getName()).append('\n');
        sb.append("  Proyectos ").append(workspaceMetrics.getProjects().size()).append('\n');
        sb.append("  Métodos ").append(workspaceMetrics.getCurrentMethodCount()).append('\n');
        sb.append("  LOC ").append(workspaceMetrics.getCurrentLoc()).append(" -> ").append(workspaceMetrics.getRefactoredLoc()).append('\n');
        sb.append("  CC ").append(workspaceMetrics.getCurrentCc()).append(" -> ").append(workspaceMetrics.getRefactoredCc()).append('\n');
        for (ProjectMetrics pm : workspaceMetrics.getProjects()) {
            sb.append("    -> Proyecto ").append(pm.getName()).append('\n');
            sb.append("       Clases ").append(pm.getClassCount()).append('\n');
            sb.append("       Métodos ").append(pm.getCurrentMethodCount()).append('\n');
            sb.append("       LOC ").append(pm.getCurrentLoc()).append(" -> ").append(pm.getRefactoredLoc()).append('\n');
            sb.append("       CC ").append(pm.getCurrentCc()).append(" -> ").append(pm.getRefactoredCc()).append('\n');
        }
        System.out.println(sb.toString());
    }
}