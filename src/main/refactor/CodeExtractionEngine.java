package main.refactor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;

import main.model.method.MethodMetrics;
import main.neo.algorithms.Solution;
import main.neo.algorithms.exhaustivesearch.EnumerativeSearch;
import main.neo.algorithms.ilp.ILP;
import main.neo.graphs.ExtractionVertex;
import main.neo.refactoringcache.ConsecutiveSequenceIterator.APPROACH;
import main.neo.refactoringcache.RefactoringCache;
import main.neo.refactoringcache.RefactoringCacheFiller;

/**
 * Orquestador principal que invoca la lógica de NEO (paquete
 * <code>neo.*</code>) y traduce los resultados al modelo de dominio
 * (<code>model.*</code>).
 */
public final class CodeExtractionEngine {

	/**
	 * Analiza un método, busca posibles extracciones de código con la heurística de
	 * NEO y devuelve un {@link MethodMetrics} rellenado con:
	 * <ul>
	 * <li>Complejidad y LOC</li>
	 * <li>Compilation Unit con los cambios aplicados</li>
	 * </ul>
	 * 
	 * @param cu         unidad de compilación que contiene el método
	 * @param icuWorkingCopy 
	 * @param node       declaración del método a analizar
	 * @param cc  complejidad cognitiva actual del método
	 * @param loc líneas de código actuales del método
	 * @param loc 
	 * @return métricas completas para el método
	 * @throws CoreException propagadas desde el modelo de refactorización de
	 *                       Eclipse
	 * @throws IOException 
	 */
	public static List<RefactorComparison> analyseAndPlan(CompilationUnit cu, ICompilationUnit icuWorkingCopy, MethodDeclaration node, int loc, int threshold) throws CoreException, IOException {
		RefactoringCache cache = new RefactoringCache(cu);
		int cc = main.neo.cem.Utils.computeAndAnnotateAccumulativeCognitiveComplexity(node);

		if (node == null || cu == null || cc <= threshold) {
			return Collections.emptyList();
		}

		List<RefactorComparison> result = new LinkedList<>();
				
		// Llenar caché de refactorizaciones
		RefactoringCacheFiller.exhaustiveEnumerationAlgorithm(cache, node);
		
		List<Solution> solutions = new LinkedList<>();
		Solution solution = new Solution(cu, node, threshold);
		boolean usedILP = false;
		
		// Crear carpeta temporal para archivos .dot dentro del proyecto
		IProject project = icuWorkingCopy.getJavaProject() != null ? 
				icuWorkingCopy.getJavaProject().getProject() : null;
		Path tempDir = null;
		List<Path> tempFiles = new ArrayList<>();
		
		try {
			if (project != null) {
				// Crear carpeta temporal .refactorer-temp en el proyecto
				tempDir = Paths.get(project.getLocation().toOSString(), ".refactorer-temp");
				Files.createDirectories(tempDir);
			}
		} catch (IOException e) {
			// Si no se puede crear la carpeta temporal, no ejecutaremos ILP
			tempDir = null;
		}
		
		
		// Ejecutar algoritmo ILP con fallback a búsqueda exhaustiva
		try {
			/* Initialize graphs 
			 * -> The one including conflicts
			 * -> The one excluding conflicts
			 * -> The one just including conflicts
			 */
			ExtractionVertex root = 
					main.neo.graphs.Utils.getRootForGraphAssociatedToMethodBody(node);
			SimpleGraph<ExtractionVertex, DefaultEdge> conflictsGraph = 
					new SimpleGraph<>(DefaultEdge.class);
			SimpleDirectedWeightedGraph<ExtractionVertex, DefaultWeightedEdge> graphWithoutConflicts = 
					new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
			SimpleDirectedWeightedGraph<ExtractionVertex, DefaultWeightedEdge> graph = 
					cache.getGraphOfFeasibleRefactorings(root, cc, graphWithoutConflicts, conflictsGraph);

			// Renderizar grafos solo si se pudo crear la carpeta temporal
			if (tempDir != null) {
				String methodName = node.getName().getIdentifier();
				String timestamp = String.valueOf(System.currentTimeMillis());
				
				// Generar nombres únicos para los archivos
				String fileNameForGraph = methodName + "-graph-" + timestamp + ".dot";
				String fileNameForGraphWithoutConflicts = methodName + "-graph-no-conflicts-" + timestamp + ".dot";
				String fileNameForConflictGraph = methodName + "-conflicts-" + timestamp + ".dot";
				
				// render full graph
				Path graphFile = tempDir.resolve(fileNameForGraph);
				main.neo.graphs.Utils.renderGraphInDotFormatInFile(
					graph, 
					tempDir.toString() + File.separator, 
					fileNameForGraph
				);
				tempFiles.add(graphFile);

				// render graph without conflicts
				Path graphNoConflictsFile = tempDir.resolve(fileNameForGraphWithoutConflicts);
				main.neo.graphs.Utils.renderGraphInDotFormatInFile(
					graphWithoutConflicts, 
					tempDir.toString() + File.separator, 
					fileNameForGraphWithoutConflicts
				);
				tempFiles.add(graphNoConflictsFile);

				// render conflicts graph
				Path conflictsFile = tempDir.resolve(fileNameForConflictGraph);
				main.neo.graphs.Utils.renderConflictGraphInDotFormatInFile(
					conflictsGraph, 
					tempDir.toString() + File.separator, 
					fileNameForConflictGraph
				);
				tempFiles.add(conflictsFile);
			}

			// Ejecutar algoritmo ILP
			solution = new ILP()
				.run(
					cu, 
					cache,
					node, 
					cc, 
					conflictsGraph, 
					graphWithoutConflicts,
					graph,
					threshold
				);
			
			usedILP = true;
			
			// Limpiar grafos para reducir uso de memoria
			if (graph != null)
				main.neo.graphs.Utils.clear(graph);
			if (graphWithoutConflicts != null)
				main.neo.graphs.Utils.clear(graphWithoutConflicts);
			if (conflictsGraph != null)
				main.neo.graphs.Utils.clear(conflictsGraph);
			
		} catch (Exception | UnsatisfiedLinkError e) {
			// En caso de cualquier error durante el ILP, ejecutar algoritmo de búsqueda exhaustiva
			usedILP = false;
			solution = new EnumerativeSearch()
					.run(APPROACH.LONG_SEQUENCE_FIRST, cu, cache, node, threshold);
		} finally {
			
			// Eliminar archivos temporales
			for (Path tempFile : tempFiles) {
				try {
					Files.deleteIfExists(tempFile);
				} catch (IOException e) {
					// Ignorar errores al eliminar archivos temporales
				}
			}
			
			// Intentar eliminar la carpeta temporal si está vacía
			if (tempDir != null) {
				try {
					File tempDirFile = tempDir.toFile();
					if (tempDirFile.exists() && tempDirFile.isDirectory() && 
						tempDirFile.list().length == 0) {
						Files.deleteIfExists(tempDir);
					}
				} catch (IOException e) {
					// Ignorar errores al eliminar la carpeta temporal
				}
			}
		}
		
		if (solution != null && solution.getSequenceList() != null && solution.getSequenceList().isEmpty()) {
			return Collections.emptyList();
		}
		
		solutions.add(main.neo.algorithms.utils.Utils.indexOfInsertionToKeepListSorted(solution, solutions), solution);
		
		for(Solution sol : solutions) {
			cu = sol.applyExtractMethodsToCompilationUnit(true, cu, icuWorkingCopy);
			result.add(RefactorComparison.builder()
				.name(sol.getMethodName())
				.compilationUnitRefactored(cu)
				.reducedComplexity(sol.getReducedComplexity())
				.numberOfExtractions(sol.getSize())
				.stats(sol.getExtractionMetricsStats())
				.usedILP(usedILP)
				.build());
		}
		
		return result;
	}

}