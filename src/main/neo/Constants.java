package main.neo;

/**
 * Define constants of the project
 */
public class Constants {
	public static final int MAX_COMPLEXITY = 15;
	public static final int ARGS = 5;
	public static final String CONTRIBUTION_TO_COMPLEXITY = "contributionToComplexity";
	public static final String ACCUMULATED_COMPLEXITY = "accumulatedComplexity";

	public static final String CONTRIBUTION_TO_COMPLEXITY_BY_NESTING = "complexityByNesting";
	public static final String ACCUMULATED_CONTRIBUTION_TO_COMPLEXITY_BY_NESTING = "accumulatedComplexityByNesting";

	public static final String ACCUMULATED_INHERENT_COMPLEXITY_COMPONENT = "accumulatedInherentComponent";
	public static final String ACCUMULATED_NESTING_COMPLEXITY_COMPONENT = "accumulatedNestingComponent";
	public static final String ACCUMULATED_NUMBER_NESTING_COMPLEXITY_CONTRIBUTORS = "numberNestingContributors";

	public static final String TEXT_RANGE = "textRange";

	public static final String COMPLEXITY_WHEN_EXTRACTING = "complexityWhenExtracting";
	public static final String OUTPUT_FOLDER = "/Users/rsain/Research/CognitiveComplexity/experiments/";
	public static final String FILE = "results.txt";
	public static final String FILE_VALIDATION = "validation-results.txt";

	public static final String EXHAUSTIVE_SEARCH_LONG_SEQUENCES_FIRST = "ES-LSF";
	public static final String EXHAUSTIVE_SEARCH_SHORT_SEQUENCES_FIRST = "ES-SSF";

	public static final String[] PROPERTIES = new String[] { 
		Constants.CONTRIBUTION_TO_COMPLEXITY,
		Constants.CONTRIBUTION_TO_COMPLEXITY_BY_NESTING,
		Constants.ACCUMULATED_CONTRIBUTION_TO_COMPLEXITY_BY_NESTING,
		Constants.ACCUMULATED_INHERENT_COMPLEXITY_COMPONENT, 
		Constants.ACCUMULATED_NESTING_COMPLEXITY_COMPONENT,
		Constants.ACCUMULATED_NUMBER_NESTING_COMPLEXITY_CONTRIBUTORS, 
		Constants.ACCUMULATED_COMPLEXITY,
		Constants.COMPLEXITY_WHEN_EXTRACTING 
	};

	public static final int MAX_EVALS = 10000;
}
