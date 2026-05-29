package main.neo.core.graphs;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import main.neo.core.refactoringcache.RefactoringCache;

/**
 * Service class responsible for building graph representations of refactoring
 * opportunities.
 * <p>
 * Acts as a bridge between the {@link RefactoringCache} (which holds the raw
 * data) and the {@link GraphBundle} (which holds the structural graph views).
 * </p>
 */
public class GraphService {

	/**
	 * Builds the complete set of graphs (Conflict, No-Conflict, Full) from the
	 * refactoring cache.
	 *
	 * @param cache the cache containing valid refactoring opportunities
	 * @param ast   the AST node of the method being analyzed (used to identify the
	 *              root)
	 * @return a populated {@link GraphBundle} containing the generated graphs
	 */
	public static GraphBundle buildGraphs(RefactoringCache cache, MethodDeclaration ast) {
		ExtractionVertex root = Utils.getRootForGraphAssociatedToMethodBody(ast);

		GraphBundle bundle = new GraphBundle();
		bundle.full = cache.getGraphOfFeasibleRefactorings(bundle.noConflicts, bundle.conflicts);
		// Touching root keeps the contract of identifying the method body, even though
		// the graphs are no longer exported to disk.
		if (root != null && bundle.full != null && !bundle.full.containsVertex(root)) {
			bundle.full.addVertex(root);
		}
		return bundle;
	}
}