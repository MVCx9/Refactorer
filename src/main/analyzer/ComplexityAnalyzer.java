package main.analyzer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import main.builder.ClassAnalysis;
import main.builder.MethodAnalysis;
import main.error.AnalyzeException;
import main.model.method.MethodAnalysisMetricsMapper;
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
	public ClassAnalysis analyze(CompilationUnit cu, ICompilationUnit icu) throws JavaModelException {
		final List<ClassAnalysis> resultHolder = new ArrayList<>(1);

		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(TypeDeclaration node) {
				String className = node.getName().getIdentifier();
				
				List<MethodAnalysis> currentMethods = new ArrayList<>();
				List<MethodAnalysis> refactoredMethods = new ArrayList<>();

				for (MethodDeclaration method : node.getMethods()) {
					try {
						List<MethodAnalysis> currentMethodAnalysis = analyzeMethod(cu, method);
						if (currentMethodAnalysis != null && !currentMethodAnalysis.isEmpty()) {
							currentMethods.addAll(currentMethodAnalysis);
						}
						
						List<MethodAnalysis> refactoredMethodAnalysis = analyzeAndPlanMethod(cu, method);
						if (refactoredMethodAnalysis != null) {
							refactoredMethods.addAll(refactoredMethodAnalysis);
						}
					} catch (CoreException e) {
						throw new AnalyzeException("Error analyzing method " + method.getName().getIdentifier(), e);
					}
				}

				if (!refactoredMethods.isEmpty()) {
					ClassAnalysis ca = ClassAnalysis.builder()
						.icu(icu)
						.compilationUnit(cu)
						.className(className)
						.analysisDate(LocalDateTime.now())
						.currentMethods(currentMethods)
						.refactoredMethods(refactoredMethods)
						.build();

					resultHolder.add(ca);
				}
				return false; // no descender a tipos anidados
			}
		});

		ClassAnalysis result = resultHolder.isEmpty() ? ClassAnalysis.builder().build() : resultHolder.get(0);
		System.out.println(result.toString());
		return result;
	}
	
	private List<MethodAnalysis> analyzeMethod(CompilationUnit cu, MethodDeclaration md) {
		CognitiveComplexityVisitor ccVisitor = new CognitiveComplexityVisitor();
		md.accept(ccVisitor);
		int currentCc = ccVisitor.getComplexity();
		int startLine = cu.getLineNumber(md.getStartPosition());
		int endLine = cu.getLineNumber(md.getStartPosition() + md.getLength());
		int currentLoc = Math.max(0, endLine - startLine + 1);
		
		MethodAnalysis m = MethodAnalysisMetricsMapper.toMethodAnalysis(md, currentCc, currentLoc);
		return List.of(m);
	}

	private List<MethodAnalysis> analyzeAndPlanMethod(CompilationUnit cu, MethodDeclaration md) throws CoreException {
		// 1) Complejidad cognitiva actual
		CognitiveComplexityVisitor ccVisitor = new CognitiveComplexityVisitor();
		md.accept(ccVisitor);

		// 2) LOC actuales (aprox. rango de líneas del método)
		int currentCc = ccVisitor.getComplexity();
		int startLine = cu.getLineNumber(md.getStartPosition());
		int endLine = cu.getLineNumber(md.getStartPosition() + md.getLength());
		int currentLoc = Math.max(0, endLine - startLine + 1);

		// 3) Invocar a CodeExtractionEngine (usa NEO internamente) para
		// evaluar posibles extracciones y obtener métricas + plan.
		List<RefactorComparison> comparison = extractionEngine.analyseAndPlan(cu, md, currentCc, currentLoc);

		// 4) Mapear al modelo de método
		return MethodAnalysisMetricsMapper.toMethodAnalysis(comparison);
	}
}