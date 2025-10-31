package main.neo.algorithms.ilp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import main.neo.Constants;
import main.neo.algorithms.Sequence;
import main.neo.algorithms.Solution;
import main.neo.graphs.ExtractionVertex;
import main.neo.graphs.Utils;

public class Model<V, E> {

	public IloCplex cplex;
	public Set<String> uniqueSolutions;

	private int threshold;
	private int numExtractions;
	private IloNumVar[] decisionVariables;
	private IloNumVar[][] Zvariables;
	private IloLinearNumExpr objective;
	private SimpleGraph<ExtractionVertex, DefaultEdge> conflictGraph;
	private SimpleDirectedGraph<V, E> graphNoConflicts;
	private SimpleDirectedGraph<V, E> fullGraph;
	private List<ExtractionVertex> nodos;
	private Map<ExtractionVertex, Integer> map;

	public Model(SimpleGraph<ExtractionVertex, DefaultEdge> conflicts, SimpleDirectedGraph<V, E> NoConflicts,
			SimpleDirectedGraph<V, E> graph, int threshold) throws IloException {

		conflictGraph = conflicts;
		graphNoConflicts = NoConflicts;
		fullGraph = graph;
		nodos = Utils.getVerticesSortedByTheirLexicographicPosition((Graph<ExtractionVertex, E>) fullGraph);
		map = Utils.mapVerticesToTheirLexicographicPosition((Graph<ExtractionVertex, E>) fullGraph);
		uniqueSolutions = new HashSet<String>();
		this.threshold = threshold;

		/*
		 * System.out.println("LISTADO"); for (ExtractionVertex v:nodos) {
		 * System.out.println(v.toString()); }
		 */

		cplex = new IloCplex();

		numExtractions = graph.vertexSet().size();
		Zvariables = new IloNumVar[numExtractions][numExtractions];
		setDecisionVariables(cplex.boolVarArray(numExtractions));

		// Too many Zs...not so optimized
		for (int i = 0; i < numExtractions; i++) {
			Zvariables[i] = cplex.boolVarArray(numExtractions);
		}
		for (int i = 0; i < numExtractions; i++) {
			for (int j = 0; j < this.numExtractions; j++) {
				Zvariables[j][i].setName("Z" + j + "" + i);
			}
		}

		addAllConstraints();
		addObjective();
	}

	public void setDefaultParameters () throws IloException {
		this.cplex.setParam(IloCplex.DoubleParam.EpInt, 1E-9);
		this.cplex.setParam(IloCplex.DoubleParam.EpGap, 1E-9);
		this.cplex.setParam(IloCplex.DoubleParam.EpOpt, 1E-9);
		
		this.cplex.setParam(IloCplex.DoubleParam.TimeLimit, Constants.CPLEX_TIME_LIMIT);
	}
	
	private void addObjective() throws IloException {
		// TODO Auto-generated method stub

		// Obliga a extraer la raiz
		cplex.addEq(1, this.decisionVariables[0]);

		objective = cplex.linearNumExpr();
		for (int i = 0; i < decisionVariables.length; i++) {
			objective.addTerm(1, decisionVariables[i]);
		}
		cplex.addMinimize(objective);
	}

	private void addAllConstraints() throws IloException {
		// TODO Auto-generated method stub

		addConflictConstraints();
		addMainConstraints();
	}

	private void addMainConstraints() throws IloException {
		// Para todo i
		for (int i = 0; i < nodos.size(); i++) {
			IloLinearNumExpr expr = cplex.linearNumExpr();
			// System.out.println("NodoI"+nodos.get(i).toString());
			int comple = nodos.get(i).getAccumulatedInherentComponent() + nodos.get(i).getAccumulatedNestingComponent()
					- Constants.MAX_COMPLEXITY;
			expr.addTerm(comple, this.decisionVariables[i]);
			// expr.setConstant(comple);
			// Necesitamos los nodos J anidados en I
			List<ExtractionVertex> jotas = (List<ExtractionVertex>) Utils.previousVertices(graphNoConflicts,
					(V) nodos.get(i));
			int conti = 0;
			for (int j = 0; j < jotas.size(); j++) {
				// System.out.println("J:"+jotas.get(j));
				int indiceJ = map.get(jotas.get(j));
				int sum = jotas.get(j).getAccumulatedInherentComponent()
						+ jotas.get(j).getAccumulatedNestingComponent();
				int distance = Utils.getNestingDistanceBetweenVertices(
						(SimpleDirectedGraph<ExtractionVertex, E>) this.graphNoConflicts, nodos.get(indiceJ),
						nodos.get(i));
				int contributors = jotas.get(j).getNumberNestingContributors();
				sum += distance * contributors;

				expr.addTerm(-sum, this.Zvariables[indiceJ][i]);

				IloLinearNumExpr zExpr = addIntermediateZs(indiceJ, i);
				cplex.addLe(zExpr, 0);
				conti++;
			}

			cplex.addLe(expr, 0);
			// System.out.println(expr);
		}

	}

	private IloLinearNumExpr addIntermediateZs(int j, int i) throws IloException {
		IloLinearNumExpr expr = cplex.linearNumExpr();
		expr.addTerm(1, this.Zvariables[j][i]);
		List<ExtractionVertex> eles = Utils.verticesBetweenTwoVertices(
				(Graph<ExtractionVertex, E>) this.graphNoConflicts, nodos.get(i), nodos.get(j));
		expr.addTerm(eles.size(), this.Zvariables[j][i]);
		// Termino independiente de la expresion???
		expr.setConstant(-eles.size());
		expr.addTerm(-1, this.decisionVariables[j]);
		for (ExtractionVertex l : eles) {
			int indiceL = map.get(l);
			expr.addTerm(1, this.decisionVariables[indiceL]);
		}

		return expr;

	}

	private void addConflictConstraints() throws IloException {
		// TODO Auto-generated method stub

		for (int i = 0; i < nodos.size() - 1; i++) {
			for (int j = i + 1; j < nodos.size(); j++) {
				if (this.conflictGraph.containsEdge(nodos.get(i), nodos.get(j))) {
					IloLinearNumExpr constraints = cplex.linearNumExpr();
					constraints.addTerm(1, decisionVariables[i]);
					constraints.addTerm(1, decisionVariables[j]);
					cplex.addLe(constraints, 1);
					// System.out.print("Conflicto:"+i+" "+j);
				}
			}
		}

	}

	public void showSolution(int index) {

		try {

			System.out.println("---------------------------");
			// System.out.println("OBJECTIVE VALUE = " + (cplex.getObjValue()-1.0));
			System.out.println("Length = " + (decisionVariables.length - 1));
			System.out.println("OBJECTIVE VALUE = " + (cplex.getObjValue(index) - 1));

			for (int i = 1; i < nodos.size(); i++) {

				if (cplex.getValue(decisionVariables[i], index) > 0.9
						&& cplex.getValue(decisionVariables[i], index) < 1.1) {
					System.out.println("Extraccion Nodo:" + i + " " + nodos.get(i));

				} else {

				}

			}

		} catch (IloException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Solution getSolution(int index, CompilationUnit compilationUnit, ASTNode methodDeclarationNode) {
		Solution solution;
		List<Sequence> listOfSequences = new ArrayList<>();
		int initialOffset, finalOffset, length, startLine, startColumn;

		try {
			for (int i = 1; i < nodos.size(); i++) {
				if (cplex.getValue(decisionVariables[i], index) > 0.9
						&& cplex.getValue(decisionVariables[i], index) < 1.1) {
					List<ASTNode> listOfNodes = new ArrayList<>();

					initialOffset = nodos.get(i).getInitialOffset();
					finalOffset = nodos.get(i).getEndOffset();
					length = finalOffset - initialOffset;
					startLine = compilationUnit.getLineNumber(initialOffset);
					startColumn = compilationUnit.getColumnNumber(initialOffset);

					listOfNodes = new main.neo.cem.Utils.NodeFinderVisitorForGivenSelection(
							compilationUnit, compilationUnit.getPosition(startLine, startColumn), length + 1)
									.getNodes();

					listOfSequences.add(new Sequence(compilationUnit, listOfNodes));
				}
			}

		} catch (IloException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		solution = new Solution(listOfSequences, compilationUnit, methodDeclarationNode, threshold);
		return solution;
	}

	public void showSolutionPop() throws IloException {
		for (int i = 0; i < cplex.getSolnPoolNsolns(); i++) {
			showSolution(i);
		}
	}

	public int showSolutionPopOptima() throws IloException {
		String sol;
		for (int x = 0; x < cplex.getSolnPoolNsolns(); x++) {
			sol = "";
			if (cplex.getObjValue() == cplex.getObjValue(x)) {
				sol += 0;
				for (int i = 1; i < nodos.size(); i++) {

					if (cplex.getValue(decisionVariables[i], x) > 0.9
							&& cplex.getValue(decisionVariables[i], x) < 1.1) {
						sol += 1;
					} else {
						sol += 0;
					}

				}

				this.uniqueSolutions.add(sol);

			}
		}
		int cont = 0;
		for (String s : this.uniqueSolutions) {
			System.out.println("Sol:" + (cont+1));
			System.out.println(s);
			for (int j = 1; j < s.length(); j++) {
				if (s.charAt(j) == '1') {
					System.out.println("Extraccion Nodo:" + j + " " + nodos.get(j));
				}
			}
			cont++;
		}
		
		return this.uniqueSolutions.size();
	}

	public void clearModel() {
		try {
			cplex.clearModel();

		} catch (IloException e) {
			throw new RuntimeException(e);
		}
	}

	public IloNumVar[] getDecisionVariables() {
		return decisionVariables;
	}

	public void setDecisionVariables(IloNumVar[] decisionVariables) {
		this.decisionVariables = decisionVariables;
		for (int i = 0; i < this.decisionVariables.length; i++) {
			this.decisionVariables[i].setName("X" + i);
		}
	}

}
