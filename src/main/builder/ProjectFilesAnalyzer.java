package main.builder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import main.analyzer.ComplexityAnalyzer;

public class ProjectFilesAnalyzer {

	public static void analyzeProject(IProject project) {

        try {
            // Recorre todos los archivos del proyecto
            project.accept(resource -> {
                if (resource instanceof IFile && resource.getName().endsWith(".java")) {
                    analyzeFile((IFile) resource);
                }
                return true;
            });
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
	
    public static void analyzeFile(IFile file) {
    	if (!Objects.requireNonNull(file.getFileExtension()).equals("java")) 
    		System.out.println("El fichero " + file.getName() + " no es una clase .java");
    	
        try (InputStream is = file.getContents();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            // Leer el contenido del archivo .java
            StringBuilder sourceCode = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sourceCode.append(line).append("\n");
            }

            // Ejecutar el análisis de complejidad
            System.out.println("---> Analizando clase: " + file.getName());
            ComplexityAnalyzer.analyze(sourceCode.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
