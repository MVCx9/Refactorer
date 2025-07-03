package handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import builder.ProjectFilesAnalyzer;


public class AnalyzeSingleFileHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	   IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
	   Object classSelected = selection.getFirstElement();
	   
	   if (classSelected == null) {
	       System.out.println("No hay fichero seleccionado para analizar.");
	       showMessage("No se ha seleccionado un archivo.");
	       return null;
	   }
	   
	   IFile file = null;

       // Si es ICompiilationUnit, intentamos adaptarlo a IFile
       if (classSelected instanceof ICompilationUnit) {
    	   ICompilationUnit unit = (ICompilationUnit) classSelected;
           try {
               file = (IFile) unit.getCorrespondingResource();
           } catch (JavaModelException e) {
               e.printStackTrace();
           }       }
	   
       // Si es IAdaptable, intentamos adaptarlo a IFile
       if (file == null && classSelected instanceof IAdaptable) {
           file = ((IAdaptable) classSelected).getAdapter(IFile.class);
       }
       
	   if (file != null) {
	        // Asegurarse de que sea un archivo .java
	        String extension = file.getFileExtension();
	        if (extension != null && extension.equals("java")) {
	            System.out.println("***** Analizando clase INDEPENDIENTE: " + file.getName() + " *****");
	            ProjectFilesAnalyzer.analyzeFile(file);
	            showMessage("Análisis completado. Revisa la consola.");
	        } else {
	            System.out.println("El archivo seleccionado no es un archivo .java.");
	            showMessage("El archivo seleccionado no es una clase Java.");
	        }
	   } else {
	       System.out.println("La selección no es un archivo.");
	       showMessage("Solo se pueden analizar archivos Java.");
	   }

	    return null;
	}

    
    private void showMessage(String message) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        MessageDialog.openInformation(shell, "Análisis de Complejidad", message);
    }
}

