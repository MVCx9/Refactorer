package main.model.common;

/**
 * Métricas de líneas de código (LOC) para un elemento del modelo.
 */
public interface LocStats {

	/** LOC actuales (sin refactorizar). */
	int getCurrentLoc();

	/** LOC previstas tras aplicar las extracciones. */
	int getRefactoredLoc();

	default int getImprovementLoc() {
		return getCurrentLoc() - getRefactoredLoc();
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
