package main.neo.core.solvers;

import main.neo.app.Constants;
import main.neo.core.solvers.config.EnumerativeSearchConfig;
import main.neo.core.solvers.config.IlpConfig;
import main.neo.core.solvers.exhaustivesearch.EnumerativeSearchSolver;
import main.neo.core.solvers.exhaustivesearch.ExhaustiveEnumerationAlgorithm.Approach;
import main.neo.core.solvers.ilp.IlpSolver;

/**
 * Factory class for creating instances of Refactoring Solvers.
 * <p>
 * This class abstracts the instantiation logic for different solver algorithms
 * (ILP, Exhaustive Search, etc.) and their default configurations.
 * </p>
 */
public class SolverFactory {

    /**
     * Creates and returns a solver instance based on the provided type.
     *
     * @param type The type of solver to instantiate.
     * @return A configured instance of {@link RefactoringSolver}.
     * @throws UnsupportedOperationException if the solver type is not implemented.
     */
    public static RefactoringSolver getSolver(SolverType type) {
        switch (type) {
            case ILP:
                return new IlpSolver(new IlpConfig(Constants.TIME_LIMIT, Constants.WORKING_MEMORY));
                
            case ES_LONG_SEQUENCE_FIRST:
                return new EnumerativeSearchSolver(new EnumerativeSearchConfig(
                        10000, 
                        Approach.LONG_SEQUENCE_FIRST));
                
            case ES_SHORT_SEQUENCE_FIRST:
                return new EnumerativeSearchSolver(new EnumerativeSearchConfig(
                        10000, 
                        Approach.SHORT_SEQUENCE_FIRST));
                
            default:
                throw new UnsupportedOperationException("Solver not implemented yet: " + type);
        }
    }
}