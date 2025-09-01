package main.handler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import main.builder.ProjectAnalysis;
import main.builder.ProjectFilesAnalyzer;
import main.builder.WorkspaceAnalysis;
import main.error.AnalyzeException;
import main.error.ResourceNotFoundException;
import main.model.workspace.WorkspaceAnalysisMetricsMapper;
import main.model.workspace.WorkspaceMetrics;
import main.session.ActionType;
import main.session.SessionAnalysisStore;
import main.ui.AnalysisMetricsDialog;

public class AnalyzeWorkspaceHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
    	IWorkspaceRoot workspaceWork = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] eclipseProjects = workspaceWork.getProjects();
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
                .name(workspaceWork.getName())
                .analysisDate(LocalDateTime.now())
                .projects(projectAnalyses)
                .build();

        WorkspaceMetrics workspaceMetrics = WorkspaceAnalysisMetricsMapper.toWorkspaceMetrics(workspaceAnalysis);
        SessionAnalysisStore.getInstance().register(ActionType.WORKSPACE, workspaceMetrics);
        new AnalysisMetricsDialog(org.eclipse.ui.handlers.HandlerUtil.getActiveShell(event), ActionType.WORKSPACE, workspaceMetrics).open();
        return null;
    }

}