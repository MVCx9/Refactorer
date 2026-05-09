package main.builder;

import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Centraliza el cálculo de complejidad cognitiva para desacoplar analizadores
 * del detalle de implementación en NEO. Se usa en análisis y planificación de
 * extracciones para obtener el valor de complejidad de un método Java.
 */
public final class CognitiveComplexityCalculator {

	private CognitiveComplexityCalculator() {
	}

	/**
	 * Calcula la complejidad cognitiva de un método y anota las propiedades de
	 * complejidad acumulada en su AST.
	 * 
	 * @param methodDeclaration método a evaluar; si es {@code null} devuelve 0.
	 * @return complejidad cognitiva total del método.
	 */
	public static int compute(MethodDeclaration methodDeclaration) {
		if (methodDeclaration == null) {
			return 0;
		}
		return main.neo.cem.Utils.computeAndAnnotateAccumulativeCognitiveComplexity(methodDeclaration);
	}
}
