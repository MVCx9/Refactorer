package main.neo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.swt.widgets.Display;
import com.google.gson.Gson;

import main.neo.algorithms.Solution;
import main.neo.algorithms.exhaustivesearch.EnumerativeSearch;
import main.neo.cem.Utils;
import main.neo.refactoringcache.RefactoringCache;
import main.neo.refactoringcache.RefactoringCacheFiller;
import main.neo.refactoringcache.ConsecutiveSequenceIterator.APPROACH;
import main.neo.sonar.cognitivecomplexity.CognitiveComplexMethod;
import main.neo.sonar.cognitivecomplexity.ProjectIssues;


public class Application {
	private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

	public void start(IApplicationContext arg) throws Exception {

		Display.getDefault(); // This is required to work in OSX systems: the display must be created in the
		// main thread

		String sonarServer, projectNameInSonar, projectNameInWorkspace, token, uri;
		String[] args = (String[]) arg.getArguments().get("application.args");

		// Check the number of arguments given
		if (args.length != Constants.ARGS) {
			LOGGER.severe("Number of arguments must be " + Constants.ARGS + "!");
		}

		// Read app arguments
		sonarServer = args[0];
		projectNameInSonar = args[1];
		token = args[2];
		projectNameInWorkspace = args[3];
		String algorithmName = args[4];

		try {
			// creating and adding information to the results file
			BufferedWriter bf = new BufferedWriter(new FileWriter(Constants.OUTPUT_FOLDER
					+ projectNameInWorkspace.replace('/', '.') + "-" + algorithmName + "-" + Constants.FILE, false));
			bf.append("algorithm;class;method;initialComplexity;solution;extractions;fitness;"
					+ "reductionComplexity;finalComplexity;"
					+ "minExtractedLOC;maxExtractedLOC;meanExtractedLOC;totalExtractedLOC;"
					+ "minParamsExtractedMethods;maxParamsExtractedMethods;meanParamsExtractedMethods;totalParamsExtractedMethods;"
					+ "minReductionOfCC;maxReductionOfCC;meanReductionOfCC;totalReductionOfCC;" + "optimo;");
			bf.append("runTimeToFillRefactoringCache;");
			bf.append("executionTime\n");

			// By default Sonar paginates queries to 100 elements per page. We have to
			// paginate the content.
			boolean allPagesProccessed = false;
			int currentPage = 1;
			while (!allPagesProccessed) {
				String json = null;

				// Compose SONAR server URI
				uri = main.neo.sonar.Utils.composeSonarUri(sonarServer, projectNameInSonar,
						currentPage);

				// Query cognitive complexity issues in project through the Sonar Web API
				LOGGER.info("Querying complexy issues from Sonar: " + uri);
				json = main.neo.sonar.Utils.GETRequest(uri, token);

				// Parse json to get project issues
				Gson gson = new Gson();
				ProjectIssues issues = gson.fromJson(json, ProjectIssues.class);

				int totalPagesInSonar = (int) Math.ceil(issues.getTotal() / 100.0);
				LOGGER.info("Proccesing issues in page " + currentPage + " (over " + totalPagesInSonar + " pages)");

				// Read cognitive complex methods from issues reported by SONAR
				Map<String, List<CognitiveComplexMethod>> methodsWithIssues = ProjectIssues
						.getCognitiveComplexity(issues);

				// Iterate over classes containing cognitive complex methods
				int classWithIssuesCounter = 0;
				LOGGER.info("#classes:" + methodsWithIssues.keySet().size());
				for (String classWithIssues : methodsWithIssues.keySet()) {
					classWithIssuesCounter++;

					// Get the relative path of current class in project in workspace
					String relativePathForFileToProcess = projectNameInWorkspace + File.separator + classWithIssues;

					List<Solution> solutions = new ArrayList<>();

					// Read compilation unit
					CompilationUnit compilationUnit = Utils
							.createCompilationUnitFromFileInWorkspace(relativePathForFileToProcess);

					// Validate if compilation unit is accessible and valid
					if (compilationUnit.getLength() == 0) {
						LOGGER.warning("ERROR WITH COMPILATION UNIT: " + compilationUnit.getTypeRoot().toString());
						LOGGER.warning(relativePathForFileToProcess);
					} else {
						int methodsWithIssuesInClassCounter = 0;

						// Iterate over cognitive complex methods in current class
						for (CognitiveComplexMethod complexMethod : methodsWithIssues.get(classWithIssues)) {
							String methodName;
							methodsWithIssuesInClassCounter++;
							RefactoringCache refactoringCache = new RefactoringCache(compilationUnit);
							List<ASTNode> auxList = new ArrayList<ASTNode>();
							
							// Get AST of the method, including contribution to complexity reported by SONAR
							 ASTNode ast = main.neo.sonar.Utils
									.getASTForMethodAnnotatingContributionToCognitiveComplexity(compilationUnit,
											complexMethod, auxList);
							
							LOGGER.info("Processing class " + classWithIssuesCounter + " of "
									+ methodsWithIssues.keySet().size() + " [" + currentPage + " (over "
									+ totalPagesInSonar + " pages)");
							LOGGER.info("Processing class '" + classWithIssues + "' ...");

							// Get method name: this is the method name plus their signature
							// joined by dashes. We do this because could exist several methods with similar
							// names (but different signature)
							methodName = ((MethodDeclaration) ast).getName().toString();

							LOGGER.info("Processing method " + methodsWithIssuesInClassCounter + " of "
									+ methodsWithIssues.get(classWithIssues).size());
							LOGGER.info("Processing method '" + methodName + "' ...");
							String composedMethodName = new String(methodName);
							if ((((MethodDeclaration) ast).parameters() != null)
									&& ((MethodDeclaration) ast).parameters().size() > 0) {
								composedMethodName = methodName + "-"
										+ String.join("-", Utils.getTypesInSignature((MethodDeclaration) ast));
							}
							// We reduce composed method name length to avoid problems with the operative
							// system
							// replace special characters in composed method name
							composedMethodName = composedMethodName.replace('<', '-');
							composedMethodName = composedMethodName.replace('>', '-');
							composedMethodName = composedMethodName.replace('?', '-');
							composedMethodName = composedMethodName.replace(':', '-');
							composedMethodName = composedMethodName.replace('\\', '-');
							composedMethodName = composedMethodName.replace('/', '-');
							composedMethodName = composedMethodName.replace('*', '-');
							composedMethodName = composedMethodName.replace('|', '-');
							composedMethodName = composedMethodName.replace('"', '-');
							LOGGER.info("Composed method name: '" + composedMethodName + "'");

							// define path for output files
							String prefixForFileNames = projectNameInWorkspace.replace('/', '.') + "-" + algorithmName
									+ "-" + classWithIssues.replace('/', '.') + "." + methodName;
							String fileNameForRefactoringCacheInfo = new String(prefixForFileNames + ".csv");
							String fileNameForSolution = new String(
									Constants.OUTPUT_FOLDER + prefixForFileNames + ".solution.txt");

							// Compute and annotate accumulated complexity in AST nodes
							int methodComplexity = Utils
									.computeAndAnnotateAccumulativeCognitiveComplexity((MethodDeclaration) ast);

							// Report the cognitive complexity of the method
							LOGGER.info("CognitiveComplexity (" + methodName + ")=" + methodComplexity);

							// Compute refactoring cache of current method
							LOGGER.info("Computing refactoring cache ...");
							long startTime = System.currentTimeMillis();
							RefactoringCacheFiller.exhaustiveEnumerationAlgorithm(refactoringCache, ast);
							long runtime = System.currentTimeMillis() - startTime;
							LOGGER.info("Refactoring cache for method '" + methodName + "' succesfully generated in "
									+ runtime + "ms.");
							refactoringCache.writeToCSV(Constants.OUTPUT_FOLDER, fileNameForRefactoringCacheInfo);
							LOGGER.info("Refactoring cache for method '" + methodName + "' succesfully generated in '"
									+ fileNameForRefactoringCacheInfo + "'!");

							// Solve cognitive complexity reduction problem
							LOGGER.info("Solving cognitive complexity reduction problem ...");
							Solution solution = new Solution(compilationUnit, ast);
							switch (algorithmName) {
							case Constants.EXHAUSTIVE_SEARCH_LONG_SEQUENCES_FIRST:
								solution = new EnumerativeSearch().run(APPROACH.LONG_SEQUENCE_FIRST, bf,
										classWithIssues, compilationUnit, refactoringCache, runtime, auxList, ast,
										methodComplexity);
								break;
							case Constants.EXHAUSTIVE_SEARCH_SHORT_SEQUENCES_FIRST:
								solution = new EnumerativeSearch().run(APPROACH.SHORT_SEQUENCE_FIRST, bf,
										classWithIssues, compilationUnit, refactoringCache, runtime, auxList, ast,
										methodComplexity);
								break;
							default:
								LOGGER.severe("No algorithm with name " + algorithmName);
							}
							
							if (solution != null) {
								solution.writeInFile(fileNameForSolution);
								LOGGER.info(solution.toString());
								if (!solution.getSequenceList().isEmpty())
									solutions.add(main.neo.sonar.Utils
											.indexOfInsertionToKeepListSorted(solution, solutions), solution);
							}
						}
					}

					LOGGER.info("Refactoring operations to apply in class " + classWithIssues + ":\n" + solutions);
				}
				currentPage++;
				allPagesProccessed = currentPage > totalPagesInSonar;
			} // end while loop to paginating issues in Sonar

			bf.close();
		} catch (SocketTimeoutException e) {
			LOGGER.severe("Wrong communication with SONAR server '" + sonarServer + "'!");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
