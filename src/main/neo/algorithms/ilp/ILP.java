package main.neo.algorithms.ilp;

import java.io.IOException;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;

import ilog.concert.IloException;
import main.neo.algorithms.Solution;
import main.neo.graphs.ExtractionVertex;
import main.neo.refactoringcache.RefactoringCache;

public class ILP {
	private Solution bestSolution;

	/* NOTE ABOUT CPLEX AND THE SOLUTIONS POOL:
	A copy of the incumbent solution (that is, the best integer solution found relative to the objective function)
	is always added to the pool, as long as the pool capacity is at least one, regardless of its evaluation with
	respect to any filters and regardless of the replacement criterion governing the solution pool.
	This copy of the incumbent solution will be the first member of the solution pool, that is, the solution with
	index 0 (zero).
	
	Details: https://www.ibm.com/docs/en/icos/12.10.0?topic=solutions-incumbent-solution-pool
	*/
	
	public Solution run(CompilationUnit compilationUnit, RefactoringCache rf, ASTNode ast, int methodComplexity, SimpleGraph<ExtractionVertex, DefaultEdge> conflictsGraph,
			SimpleDirectedWeightedGraph<ExtractionVertex, DefaultWeightedEdge> graphWithoutConflicts,
			SimpleDirectedWeightedGraph<ExtractionVertex, DefaultWeightedEdge> graph, int threshold) throws IOException, IloException {
		bestSolution = null;
		int numberOfSolutions = 0, numberOfOptimalSolutions = 0;
		String modelStatus;
		Model<ExtractionVertex, DefaultWeightedEdge> m = 
				new Model<ExtractionVertex, DefaultWeightedEdge>(conflictsGraph, graphWithoutConflicts, graph, threshold);
		m.setDefaultParameters();
		//m.cplex.exportModel(Constants.OUTPUT_FOLDER + compilationUnit.getJavaElement().getJavaProject().getElementName() + "-ILP-" + classWithIssues.replace('/', '.') + "." + methodName + ".lp");
		m.cplex.populate();
		numberOfSolutions = m.cplex.getSolnPoolNsolns();
		modelStatus = m.cplex.getStatus().toString();
		System.out.println("NUMBER OF SOLUTIONS IN POOL: " + numberOfSolutions);
		System.out.println("MODEL STATUS = " + modelStatus);
		m.showSolutionPop();
		numberOfOptimalSolutions = m.showSolutionPopOptima();
		if (numberOfOptimalSolutions > 0) {
			System.out.println("NUMBER OF OPTIMAL SOLUTIONS: " + numberOfOptimalSolutions);
			bestSolution = m.getSolution(0, compilationUnit, ast);
			bestSolution.evaluate(rf);
			System.out.println(bestSolution.toStringVerbose());
		}
		else {
			System.out.println("NO SOLUTION!");
		}
		m.clearModel();

		return bestSolution;
	}

}
