package main.builder;

import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Centraliza el cálculo de complejidad cognitiva para desacoplar analizadores
 * del detalle de implementación en NEO.
 */
public final class CognitiveComplexityCalculator {

	private CognitiveComplexityCalculator() {
	}

	public static int compute(MethodDeclaration methodDeclaration) {
		if (methodDeclaration == null) {
			return 0;
		}
		return main.neo.cem.Utils.computeAndAnnotateAccumulativeCognitiveComplexity(methodDeclaration);
	}
}
