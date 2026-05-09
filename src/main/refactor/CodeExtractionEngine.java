package main.refactor;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;

import main.builder.CognitiveComplexityCalculator;
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
		int cc = CognitiveComplexityCalculator.compute(node);

		if (node == null || cu == null || cc <= threshold) {
			return Collections.emptyList();
		}

		List<RefactorComparison> result = new LinkedList<>();
				
		// Llenar caché de refactorizaciones
		RefactoringCacheFiller.exhaustiveEnumerationAlgorithm(cache, node);
		
		List<Solution> solutions = new LinkedList<>();
		Solution solution = new Solution(cu, node, threshold);
		boolean usedILP = false;
		
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
		}
		
		if (null == solution || null == solution.getSequenceList() || solution.getSequenceList().isEmpty()) {
			return Collections.emptyList();
		}
		
		solutions.add(main.neo.algorithms.utils.Utils.indexOfInsertionToKeepListSorted(solution, solutions), solution);
		
		// Aplicar la mejor solución
		final Solution sol = getBestSolution(solutions);
		cu = sol.applyExtractMethodsToCompilationUnit(true, cu, icuWorkingCopy);
		result.add(RefactorComparison.builder()
			.name(sol.getMethodName())
			.compilationUnitRefactored(cu)
			.reducedComplexity(sol.getReducedComplexity())
			.numberOfExtractions(sol.getSize())
			.stats(sol.getExtractionMetricsStats())
			.usedILP(usedILP)
			.build());
		
		return result;
	}

	private static Solution getBestSolution(List<Solution> solutions) {
		if (solutions.size() == 1) {
			return solutions.getFirst();
		}
		
		return solutions.stream()
			.min((s1, s2) -> Double.compare(s1.getFitness(), s2.getFitness()))
			.orElse(null);
	}

}
