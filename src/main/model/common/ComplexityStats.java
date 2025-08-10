package main.model.common;

/**
 * Métricas de complejidad cognitiva para un elemento del modelo (método, clase,
 * proyecto…).
 */
public interface ComplexityStats {

	/** Complejidad cognitiva actual (antes de refactorizar). */
	int getCurrentCc();

	/** Complejidad cognitiva tras aplicar las extracciones propuestas. */
	int getRefactoredCc();

	/* ------------- Métodos default de conveniencia ------------- */

	/** Diferencia absoluta (negativo = mejora). */
	default int getDeltaCc() {
		return getRefactoredCc() - getCurrentCc();
	}

	/** Porcentaje de mejora (> 0 → ha mejorado, < 0 → ha empeorado). */
	default double getImprovementPercentCc() {
		if (getCurrentCc() == 0)
			return 0.0;
		return (getCurrentCc() - getRefactoredCc()) * 100.0 / getCurrentCc();
	}

	/** Se ha reducido la complejidad */
	default boolean isImprovedCc() {
		return getRefactoredCc() < getCurrentCc();
	}
}
