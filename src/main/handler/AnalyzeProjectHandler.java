package main.handler;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import main.builder.ProjectFilesAnalyzer;

public class AnalyzeProjectHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
        //seleccionamos el 1º porque  sólo pulsamos en un proyecto
        Object projectSelected = selection.getFirstElement(); 
        
        if(projectSelected == null) {
        	System.out.println("No hay proyecto para analizar");
        }
        
        IProject project = null;

        // Si es IAdaptable (como JavaProject), intentamos adaptarlo a IProject
        if (projectSelected instanceof IAdaptable) {
            project = ((IAdaptable) projectSelected).getAdapter(IProject.class);
        }

        // Verificamos que obtuvimos el proyecto correctamente
        if (project != null) {
            System.out.println("***** Analizando proyecto INDEPENDIENTE: " + project.getName() + " *****");
            ProjectFilesAnalyzer.analyzeProject(project);
            showMessage("Análisis completado. Revisa la consola.");
        } else {
            System.out.println("La selección no es un proyecto Eclipse válido.");
            showMessage("Error: No se pudo analizar el proyecto seleccionado.");
        }
        
        return null;
    }
    
    private void showMessage(String message) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        MessageDialog.openInformation(shell, "Análisis de Complejidad", message);
    }
}
