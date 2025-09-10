package main.handler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.handlers.HandlerUtil;

import main.builder.ProjectAnalysis;
import main.builder.ProjectFilesAnalyzer;
import main.builder.WorkspaceAnalysis;
import main.common.error.AnalyzeException;
import main.common.error.ResourceNotFoundException;
import main.model.workspace.WorkspaceAnalysisMetricsMapper;
import main.model.workspace.WorkspaceMetrics;
import main.session.ActionType;
import main.session.SessionAnalysisStore;
import main.ui.AnalysisMetricsDialog;
import main.ui.AnalysisNoRefactorDialog;
import main.ui.ErrorDetailsDialog;

public class AnalyzeWorkspaceHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // Obtener todos los proyectos y filtrar únicamente los que están abiertos
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        List<IProject> openProjects = new ArrayList<>();
        for (IProject p : allProjects) {
            if (p != null && p.isOpen()) {
                openProjects.add(p);
            }
        }
        if (openProjects.isEmpty()) {
        	ResourceNotFoundException error = new ResourceNotFoundException("No hay proyectos abiertos en el workspace.");
        	ErrorDetailsDialog.open(HandlerUtil.getActiveShell(event), error.getMessage(), error);
			return null;
        }

        ProjectFilesAnalyzer analyzer = new ProjectFilesAnalyzer();
        List<ProjectAnalysis> projectAnalyses = new ArrayList<>();

        try {
            for (IProject project : openProjects) {
                ProjectAnalysis analysis = analyzer.analyzeProject(project);
                if (analysis == null) {
                	continue;
                }
                projectAnalyses.add(analysis);
            }

            WorkspaceAnalysis workspaceAnalysis = WorkspaceAnalysis.builder()
                .name("My Workspace")
                .analysisDate(LocalDateTime.now())
                .projects(projectAnalyses)
                .build();

            WorkspaceMetrics workspaceMetrics = WorkspaceAnalysisMetricsMapper.toWorkspaceMetrics(workspaceAnalysis);
            SessionAnalysisStore.getInstance().register(ActionType.WORKSPACE, workspaceMetrics);

            if (workspaceMetrics.getMethodExtractionCount() == 0) {
                new AnalysisNoRefactorDialog(HandlerUtil.getActiveShell(event), ActionType.WORKSPACE, workspaceMetrics).open();
                return null;
            }

            new AnalysisMetricsDialog(HandlerUtil.getActiveShell(event), ActionType.WORKSPACE, workspaceMetrics).open();
            return null;

        } catch (Throwable e) {
            AnalyzeException error = new AnalyzeException("Error analyzing project", e);
            ErrorDetailsDialog.open(HandlerUtil.getActiveShell(event), error.getMessage(), error);
            return null;
        }
    }

}