package main.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import main.builder.ProjectFilesAnalyzer;

public class AnalyzeWorkspaceHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // Obtener el proyecto activo en Eclipse
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        
        if (projects.length == 0) {
            showMessage("No hay proyectos abiertos en el workspace.");
            return null;
        }


        System.out.println("***** Analizando WorkSpace COMPLETO *****");
        // Recorre todos los proyectos para analizar los métodos
        for(IProject project : projects) {
			System.out.println("*** Analizando projecto: " + project.getName() + " ***");
			// Ejecutar el análisis
			ProjectFilesAnalyzer.analyzeProject(project);
		}
        
        showMessage("Análisis completado. Revisa la consola.");

        return null;
    }

    private void showMessage(String message) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        MessageDialog.openInformation(shell, "Análisis de Complejidad", message);
    }
}

