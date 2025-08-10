package main.model.common;

/**
 * Métricas de líneas de código (LOC) para un elemento del modelo.
 */
public interface LocStats {

	/** LOC actuales (sin refactorizar). */
	int getCurrentLoc();

	/** LOC previstas tras aplicar las extracciones. */
	int getRefactoredLoc();

	/* ------------- Métodos default de conveniencia ------------- */

	default int getDeltaLoc() {
		return getRefactoredLoc() - getCurrentLoc();
	}

	default double getImprovementPercentLoc() {
		if (getCurrentLoc() == 0)
			return 0.0;
		return (getCurrentLoc() - getRefactoredLoc()) * 100.0 / getCurrentLoc();
	}

	default boolean isImprovedLoc() {
		return getRefactoredLoc() < getCurrentLoc();
	}
}
