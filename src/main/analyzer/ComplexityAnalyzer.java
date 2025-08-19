package main.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import main.builder.ClassAnalysis;
import main.builder.MethodAnalysis;
import main.refactor.CodeExtractionEngine;
import main.refactor.RefactorComparison;

public class ComplexityAnalyzer {

	private final CodeExtractionEngine extractionEngine;

	public ComplexityAnalyzer(CodeExtractionEngine extractionEngine) {
		this.extractionEngine = Objects.requireNonNull(extractionEngine, "extractionEngine");
	}

	/**
	 * Analiza un ICompilationUnit y devuelve el análisis de la(s) clase(s) que
	 * contiene, comparando código actual vs. código refactorizado (si hay una
	 * extracción viable).
	 */
	public List<ClassAnalysis> analyze(CompilationUnit cu, ICompilationUnit icu) throws JavaModelException {
		System.out.println("******** POR FIN PASÓ EL PARSE ********");
		List<ClassAnalysis> results = new ArrayList<>();

		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(TypeDeclaration node) {
				String className = node.getName().getIdentifier();
				List<MethodAnalysis> methods = new ArrayList<>();

				for (MethodDeclaration md : node.getMethods()) {
					MethodAnalysis ma = analyzeMethod(cu, md);
					if (ma != null)
						methods.add(ma);
				}

				if (!methods.isEmpty()) {
					ClassAnalysis ca = ClassAnalysis.builder().icu(icu).compilationUnit(cu).className(className)
							.methods(methods).build();

					results.add(ca);
				}
				return false; // no descender a tipos anidados aquí; añádelo si lo necesitas
			}
		});

		System.out.println(results.toString());
		return results;
	}

	private MethodAnalysis analyzeMethod(CompilationUnit cu, MethodDeclaration md) {
		// 1) Complejidad cognitiva actual
		CognitiveComplexityVisitor ccVisitor = new CognitiveComplexityVisitor();
		md.accept(ccVisitor);
		int currentCc = ccVisitor.getComplexity();

		// 2) LOC actuales (aprox. rango de líneas del método)
		int startLine = cu.getLineNumber(md.getStartPosition());
		int endLine = cu.getLineNumber(md.getStartPosition() + md.getLength());
		int currentLoc = Math.max(0, endLine - startLine + 1);

		// 3) Invocar a CodeExtractionEngine (usa NEO internamente) para
		// evaluar posibles extracciones y obtener métricas + plan.
		RefactorComparison comparison = extractionEngine.evaluateMethod(cu, md, currentCc, currentLoc);

		// 4) Mapear al modelo de método
		return MethodAnalysis.builder().methodName(md.getName().getIdentifier()).declaration(md).currentCc(currentCc)
				.currentLoc(currentLoc).refactoredCc(comparison.getRefactoredCc())
				.refactoredLoc(comparison.getRefactoredLoc()).bestExtraction(comparison.getBestMetrics())
				.stats(comparison.getStats()).extractionPlan(comparison.getDoPlan()).undoPlan(comparison.getUndoPlan())
				.build();
	}

}
