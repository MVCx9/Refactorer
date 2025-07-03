package neo.graphs;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.clique.DegeneracyBronKerboschCliqueFinder;
import org.jgrapht.generate.ComplementGraphGenerator;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.ExportException;
import org.jgrapht.nio.dot.DOTExporter;

import neo.reducecognitivecomplexity.Constants;
import neo.reducecognitivecomplexity.algorithms.Pair;

import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

/**
 * This class provides different utilities to process graphs
 */
public class Utils {
	/**
	 * Render a graph in DOT format.
	 *
	 * @param g a graph
	 */
	public static String renderGraphInDotFormat(SimpleDirectedWeightedGraph<ExtractionVertex, DefaultWeightedEdge> g)
			throws ExportException {
		DOTExporter<ExtractionVertex, DefaultWeightedEdge> exporter = new DOTExporter<>();
		exporter.setVertexAttributeProvider(v -> {
			Map<String, Attribute> map = new LinkedHashMap<>();
			map.put("label", DefaultAttribute.createAttribute(v.toString()));

			return map;
		});
		exporter.setEdgeAttributeProvider(v -> {
			Map<String, Attribute> map = new LinkedHashMap<>();
			if (g.getEdgeWeight(v) == 0) {
				map.put("label", DefaultAttribute.createAttribute((int) (g.getEdgeWeight(v))));
				map.put("color", DefaultAttribute.createAttribute("red"));
			}

			return map;
		});
		Writer writer = new StringWriter();
		exporter.exportGraph(g, writer);
		return (writer.toString());
	}

	/**
	 * Render a graph in DOT format.
	 * 
	 * @param <V>
	 * @param <E>
	 *
	 * @param g   a graph
	 */
	public static <V, E> String renderConflictGraphInDotFormat(Graph<ExtractionVertex, DefaultEdge> g)
			throws ExportException {
		DOTExporter<ExtractionVertex, DefaultEdge> exporter = new DOTExporter<>();
		exporter.setVertexAttributeProvider(v -> {
			Map<String, Attribute> map = new LinkedHashMap<>();
			map.put("label", DefaultAttribute.createAttribute(v.toString()));

			return map;
		});
		exporter.setEdgeAttributeProvider(v -> {
			Map<String, Attribute> map = new LinkedHashMap<>();
			map.put("color", DefaultAttribute.createAttribute("red"));
			return map;
		});
		Writer writer = new StringWriter();
		exporter.exportGraph(g, writer);
		return (writer.toString());
	}

	/**
	 * Render a graph in DOT format in the given file.
	 *
	 * @param g        a graph
	 * @param path     to folder
	 * @param fileName a file name
	 * @throws IOException
	 */
	public static void renderGraphInDotFormatInFile(
			SimpleDirectedWeightedGraph<ExtractionVertex, DefaultWeightedEdge> g, String path, String fileName)
					throws IOException {
		String dotInfo = renderGraphInDotFormat(g);

		FileWriter fw = new FileWriter(path + fileName, false);
		fw.append(dotInfo);
		fw.close();
	}

	/**
	 * Render a graph in DOT format in the given file.
	 *
	 * @param g        a graph
	 * @param path     to folder
	 * @param fileName a file name
	 * @throws IOException
	 */
	public static void renderConflictGraphInDotFormatInFile(Graph<ExtractionVertex, DefaultEdge> g, String path,
			String fileName) throws IOException {
		String dotInfo = renderConflictGraphInDotFormat(g);

		FileWriter fw = new FileWriter(path + fileName, false);
		fw.append(dotInfo);
		fw.close();
	}

	/**
	 * Get maximal independent sets of given graph.
	 * 
	 * @param graph
	 * @return Maximal independent sets
	 */
	public static <V, E> Iterable<Set<V>> getMaximalIndependentSets(SimpleGraph<V, E> graph) {
		SimpleGraph<V, E> complementGraph = new SimpleGraph(DefaultEdge.class);

		// generate the complement graph
		ComplementGraphGenerator<V, E> cgg = new ComplementGraphGenerator<>(graph);
		cgg.generateGraph(complementGraph);

		// return iterable over maximal cliques
		return new DegeneracyBronKerboschCliqueFinder<>(complementGraph);
	}

	/**
	 * Get shortest path between two nodes using the Dijkstra algorithm
	 *
	 * @param graph
	 * @param vertexA starting vertex
	 * @param vertexB ending vertex
	 * @return graph path from vertexA to vertexB or null if not path exists
	 */
	public static <V, E> GraphPath<V, E> getShortestPathBetweenVertices(SimpleDirectedGraph<V, E> graph, V vertexA,
			V vertexB) {
		DijkstraShortestPath<V, E> dijkstra = new DijkstraShortestPath<V, E>(graph);

		return dijkstra.getPath(vertexA, vertexB);
	}

	/**
	 * Get distance between two vertices in a graph
	 *
	 * @param graph
	 * @param vertexA starting vertex
	 * @param vertexB ending vertex
	 * @return Distance from vertexA (excluding it) to vertexB (including it) or -1
	 *         if not path exists
	 */
	public static <V, E> int getDistanceBetweenVertices(SimpleDirectedGraph<V, E> graph, V vertexA, V vertexB) {
		GraphPath<V, E> result = getShortestPathBetweenVertices(graph, vertexA, vertexB);

		System.out.println(result);

		if (result == null)
			return -1;
		else
			return result.getLength();
	}

	/**
	 * Get the nesting distance between two vertices in a graph. The nesting
	 * distance is the number of control flow structures increasing the nesting
	 * degree between two vertices.
	 *
	 * @param graph
	 * @param vertexA starting vertex
	 * @param vertexB ending vertex
	 * @return Nesting distance from vertexA to vertexB or -1 if not path exists
	 */
	public static <V, E> int getNestingDistanceBetweenVertices(SimpleDirectedGraph<ExtractionVertex, E> graph,
			ExtractionVertex vertexA, ExtractionVertex vertexB) {
		
		GraphPath<ExtractionVertex, E> result = getShortestPathBetweenVertices(graph, vertexA, vertexB);

		if (result == null)
			throw new RuntimeException("No connection between vertices");
		else
			return vertexA.getNesting() - vertexB.getNesting();
	}

	/**
	 * Remove all edges of given graph
	 * 
	 * @param graph
	 */
	public static <V, E> void removeAllEdges(Graph<V, E> graph) {
		LinkedList<E> copy = new LinkedList<E>();
		for (E e : graph.edgeSet()) {
			copy.add(e);
		}
		graph.removeAllEdges(copy);
	}

	/**
	 * Remove all vertices and edges of given graph
	 * 
	 * @param graph
	 */
	public static <V, E> void clear(Graph<V, E> graph) {
		removeAllEdges(graph);
		removeAllVertices(graph);
	}

	/**
	 * Remove all vertices of give graph
	 * 
	 * @param graph
	 */
	public static <V, E> void removeAllVertices(Graph<V, E> graph) {
		LinkedList<V> copy = new LinkedList<V>();
		for (V v : graph.vertexSet()) {
			copy.add(v);
		}
		graph.removeAllVertices(copy);
	}

	/**
	 * Copy first given graph into second given graph
	 * 
	 * @param Source graph to copy from
	 * @param Target graph to copy to
	 */
	public static <V, E> void copy(Graph<V, E> a, Graph<V, E> b) {
		for (E e : a.edgeSet()) {
			b.addVertex(a.getEdgeSource(e));
			b.addVertex(a.getEdgeTarget(e));
			b.addEdge(a.getEdgeSource(e), a.getEdgeTarget(e), e);
		}
	}

	/**
	 * Get leaves of given graph
	 * 
	 * @param g
	 * @return List of leaves
	 */
	public static <V, E> List<V> leaves(Graph<V, E> g) {
		return g.vertexSet().stream().filter(key -> g.incomingEdgesOf(key).size() == 0).collect(Collectors.toList());

	}

	/**
	 * Get the roots of given graph
	 * 
	 * @param g
	 * @return List of roots
	 */
	public static <V, E> List<V> roots(Graph<V, E> g) {
		return g.vertexSet().stream().filter(key -> g.outgoingEdgesOf(key).size() == 0).collect(Collectors.toList());
	}

	/**
	 * Check if given vertex is a leaf of given graph
	 * 
	 * @param g The graph
	 * @param v The vertex
	 * @return True if the vertex is a leaf in the graph
	 */
	public static <V, E> boolean isLeaf(Graph<V, E> g, V v) {
		return g.outDegreeOf(v) == 0;
	}

	/**
	 * Check if given vertex is a root of given graph
	 * 
	 * @param g The graph
	 * @param v The vertex
	 * @return True if the vertex is a root in the graph
	 */
	public static <V, E> boolean isRoot(Graph<V, E> g, V v) {
		return g.inDegreeOf(v) == 0;
	}

	/**
	 * Get the list of immediate previous vertices to the given vertex in a graph
	 * 
	 * @param g
	 * @param v
	 * @return List of immediate previous vertices to the given one
	 */
	public static <V, E> List<V> immediatePreviousVertices(Graph<V, E> g, V v) {
		List<V> result = new ArrayList<V>();

		if (g.vertexSet().contains(v)) {
			for (E e : g.incomingEdgesOf(v)) {
				result.add(g.getEdgeSource(e));
			}
		}

		return result;
	}

	/**
	 * Get the list of previous vertices to the given vertex in a graph.
	 * <p>
	 * Note that this method assumes that there is no cycles in the graph. Otherwise
	 * the algorithm produces an stack overflow due to infinite recursion calls.
	 * 
	 * @param g
	 * @param v
	 * @param exploredVertices
	 * @return List of previous vertices to the given one
	 */
	public static <V, E> List<V> previousVertices(Graph<V, E> g, V v) {
		List<V> result = new ArrayList<V>();

		if (g.vertexSet().contains(v)) {
			result = previousVerticesRecursion(g, v, new ArrayList<V>());
		}

		return result;
	}

	// Auxiliary private method for previousVertices
	private static <V, E> List<V> previousVerticesRecursion(Graph<V, E> g, V v, List<V> exploredVertices) {
		for (V tempV : immediatePreviousVertices(g, v)) {
			if (!exploredVertices.contains(tempV)) {
				exploredVertices.add(tempV);
				previousVerticesRecursion(g, tempV, exploredVertices);
			}
		}
		return exploredVertices;
	}

	/**
	 * Map each vertex in the graph to its lexicographic position when sorting by
	 * vertices offset.
	 * 
	 * The returned map uses vertices as keys and the lexicographic position in the
	 * graph as values.
	 * 
	 * @param g Graph to map vertices from
	 * @return Map containing pairs of vertices and their lexicographic position in
	 *         the graph
	 */
	@SuppressWarnings("unchecked")
	public static <E> Map<ExtractionVertex, Integer> mapVerticesToTheirLexicographicPosition(
			Graph<ExtractionVertex, E> g) {
		HashMap<ExtractionVertex, Integer> result = new HashMap<ExtractionVertex, Integer>();

		List<ExtractionVertex> vertices = getVerticesSortedByTheirLexicographicPosition(g);

		for (int i = 0; i < vertices.size(); i++) {
			result.put(vertices.get(i), i);
		}

		return result;
	}

	/**
	 * Get a list of vertices where the position on the list is the lexicographic
	 * position of the vertex when sorting by vertices offset.
	 * 
	 * Note that the root of the graph is processed independently to be always the
	 * first element of the list.
	 * 
	 * @param g Graph to list vertices from
	 * @return List containing vertices sorted by their lexicographic position in
	 *         the graph
	 */
	@SuppressWarnings("unchecked")
	public static <E> List<ExtractionVertex> getVerticesSortedByTheirLexicographicPosition(
			Graph<ExtractionVertex, E> g) {

		List<ExtractionVertex> result = new ArrayList<ExtractionVertex>();
		List<ExtractionVertex> vertices = new ArrayList<ExtractionVertex>();
		ExtractionVertex root = null;

		for (ExtractionVertex v : g.vertexSet()) {
			if (g.outDegreeOf(v) == 0) {
				if (root != null) {
					String message = "More than one root was found in the graph:\n";
					message = message + "New root found was (" + v.toString() + ").\n";
					message = message + "If it is not the method body something is wrong.\n";
					message = message + "EXITING ...";
					System.err.println(message);
				}
				root = v;
			} else {
				vertices.add(v);
			}
		}

		Collections.sort(vertices);

		result.add(root);
		for (int i = 0; i < vertices.size(); i++) {
			result.add(vertices.get(i));
		}

		return result;
	}
	
	/**
	 * Gets the {@link ExtractionVertex} node associated to the method body of the
	 * method declaration given as parameter
	 * 
	 * @param ast Method declaration
	 * @return {@link ExtractionVertex} node associated to the entire body of the
	 *         method
	 */
	public static ExtractionVertex getRootForGraphAssociatedToMethodBody(ASTNode ast) {
		// root extraction (information about the extraction of the entire body of the
		// method)
		Block methodBody = ((MethodDeclaration) ast).getBody();
		int methodComplexity = (int) ast.getProperty(Constants.ACCUMULATED_INHERENT_COMPLEXITY_COMPONENT)
				+ (int) ast.getProperty(Constants.ACCUMULATED_NESTING_COMPLEXITY_COMPONENT);
		int numberStatementsInMethodBody = methodBody.statements().size();
		ExtractionVertex root = new ExtractionVertex(((Statement) methodBody.statements().get(0)).getStartPosition(),
				((Statement) methodBody.statements().get(numberStatementsInMethodBody - 1)).getLength()
						+ ((Statement) methodBody.statements().get(numberStatementsInMethodBody - 1)).getStartPosition(),
				methodComplexity, (int) ast.getProperty(Constants.ACCUMULATED_INHERENT_COMPLEXITY_COMPONENT),
				(int) ast.getProperty(Constants.ACCUMULATED_NESTING_COMPLEXITY_COMPONENT),
				(int) ast.getProperty(Constants.ACCUMULATED_NUMBER_NESTING_COMPLEXITY_CONTRIBUTORS), 0);

		return root;
	}

	/**
	 * Vertices between two given vertices in a graph.
	 * 
	 * Second vertex (its offset) must be contained in the first one.
	 * <p>
	 * Note that this method assumes that there is no cycles in the graph. Otherwise
	 * the algorithm produces an stack overflow due to infinite recursion calls.
	 * 
	 * @param g
	 * @param v1
	 * @param v2
	 * @return List of vertices between the given ones (excluded)
	 */
	public static <V, E> List<ExtractionVertex> verticesBetweenTwoVertices(Graph<ExtractionVertex, E> g,
			ExtractionVertex v1, ExtractionVertex v2) {
		List<ExtractionVertex> result = new ArrayList<ExtractionVertex>();

		Pair p1 = new Pair(v1.getInitialOffset(), v1.getEndOffset());
		Pair p2 = new Pair(v2.getInitialOffset(), v2.getEndOffset());

		if (g.vertexSet().contains(v1) && g.vertexSet().contains(v2) && Pair.isContained(p2, p1)) {
			result = verticesBetweenTwoVerticesRecursion(g, v1, v2, new ArrayList<ExtractionVertex>());
		}

		return result;
	}

	// Auxiliary private method for verticesBetweenTwoVertices
	private static <E> List<ExtractionVertex> verticesBetweenTwoVerticesRecursion(Graph<ExtractionVertex, E> g,
			ExtractionVertex v1, ExtractionVertex v2, List<ExtractionVertex> exploredVertices) {
		for (ExtractionVertex tempV : immediateNextVertices(g, v2)) {
			if (!tempV.equals(v1)) {
				if (!exploredVertices.contains(tempV)) {
					exploredVertices.add(tempV);
					verticesBetweenTwoVerticesRecursion(g, v1, tempV, exploredVertices);
				}
			} else
				break;
		}
		return exploredVertices;
	}

	/**
	 * Get the list of immediate next vertices to the given vertex in a graph
	 * 
	 * @param g
	 * @param v
	 * @return List of immediate next vertices to the given one
	 */
	public static <V, E> List<V> immediateNextVertices(Graph<V, E> g, V v) {
		List<V> result = new ArrayList<V>();

		if (g.vertexSet().contains(v)) {
			for (E e : g.outgoingEdgesOf(v)) {
				result.add(g.getEdgeTarget(e));
			}
		}

		return result;
	}

	/**
	 * Remove a vertex in a graph
	 * 
	 * @param g
	 * @param v
	 * @return True if the node is removed
	 */
	public static <V, E> boolean remove(Graph<V, E> g, V v) {
		List<V> immediateNextVertices = immediateNextVertices(g, v);
		List<V> immediatePreviousVertices = immediatePreviousVertices(g, v);

		for (V immediatePreviousVertex : immediatePreviousVertices) {
			for (V immediateNextVertex : immediateNextVertices) {
				g.addEdge(immediatePreviousVertex, immediateNextVertex);
			}
		}

		// remove the node
		return g.removeVertex(v);
	}

	/**
	 * Check if a graph is a tree
	 * 
	 * @param graph
	 * @return True if the given graph is a tree
	 */
	public static <V, E> boolean checkTree(Graph<V, E> graph) {
		boolean rootFound = false;
		for (V vertex : graph.vertexSet()) {
			if (graph.outDegreeOf(vertex) == 0) {
				if (rootFound) {
					return false;
				}
				rootFound = true;
			} else if (graph.outDegreeOf(vertex) > 1) {
				return false;
			}
		}

		return GraphTests.isTree(new AsUndirectedGraph<>(graph));
	}

	/**
	 * Get a leaf from a graph (which is assumed to be a tree)
	 * 
	 * @param tree
	 * @return A vertex which is a leaf in the given graph
	 */
	public static <V, E> V anyLeaf(Graph<V, E> tree) {
		Set<V> vertices = tree.vertexSet();
		Optional<V> anyVertex = vertices.stream().findAny();
		if (anyVertex.isEmpty()) {
			throw new NoSuchElementException();
		}
		V vertex = anyVertex.get();
		while (tree.inDegreeOf(vertex) > 0) {
			E edge = tree.incomingEdgesOf(vertex).stream().findAny().get();
			vertex = tree.getEdgeSource(edge);
		}
		return vertex;
	}

	/**
	 * Remove a subtree defined by the given vertex in a graph (which is assumed to
	 * be a tree)
	 * 
	 * @param tree
	 * @param vertex
	 */
	public static <V, E> void removeSubTree(Graph<V, E> tree, V vertex) {
		Queue<V> toExplore = new LinkedList<>();
		Queue<V> explored = new LinkedList<>();
		toExplore.add(vertex);

		while (!toExplore.isEmpty()) {
			vertex = toExplore.poll();
			toExplore.addAll(immediatePreviousVertices(tree, vertex));
			explored.add(vertex);
		}
		tree.removeAllVertices(explored);
	}

	/**
	 * Check if two vertices are in conflict
	 * 
	 * Two nodes are in conflict if there is an edge between them with weight equal
	 * to 0
	 * 
	 * @param g
	 * @param v1
	 * @param v2
	 * @return true if v1 and v2 are in conflict, false otherwise
	 */
	public static <V> boolean conflict(SimpleDirectedWeightedGraph<V, DefaultWeightedEdge> g, V v1, V v2) {
		boolean result = false;

		if (g.containsVertex(v1) && g.containsVertex(v2)) {
			DefaultWeightedEdge edge = g.getEdge(v1, v2);
			if (edge != null) {
				result = g.getEdgeWeight(edge) == 0;
			}
		}

		return result;
	}

	/**
	 * Get parent f a given vertex in a graph (which is assumed to be a tree)
	 * 
	 * @param tree
	 * @param vertex
	 * @return Parent of given vertex in the graph
	 */
	public static ExtractionVertex getParent(SimpleDirectedWeightedGraph<ExtractionVertex, DefaultWeightedEdge> tree,
			ExtractionVertex vertex) {
		if (tree.outDegreeOf(vertex) == 0) {
			return null;
		}
		DefaultWeightedEdge edge = tree.outgoingEdgesOf(vertex).stream().findAny().get();
		ExtractionVertex parent = tree.getEdgeTarget(edge);
		return parent;
	}
}
