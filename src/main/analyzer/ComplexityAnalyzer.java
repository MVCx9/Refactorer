package main.analyzer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import main.builder.ClassAnalysis;
import main.builder.MethodAnalysis;
import main.common.error.AnalyzeException;
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

		IProgressMonitor pm = new NullProgressMonitor();

		MethodDeclaration targetMethod = null;
		Set<String> processedMethods = new LinkedHashSet<>();

		List<MethodAnalysis> currentMethods = new ArrayList<>();
	    List<MethodAnalysis> refactoredMethods = new ArrayList<>();
		try {
			icu.becomeWorkingCopy(pm);
			while(true) {
				try {
					targetMethod = findNextMethodNeedingRefactor(cu, processedMethods);
	                if (targetMethod == null) {
	                	break;
	                }
	                
	                String key = bindingKey(targetMethod);
	                if (key != null) processedMethods.add(key);
	                
	                
					List<MethodAnalysis> currentMethodAnalysis = analyzeMethod(cu, targetMethod);
					if (currentMethodAnalysis != null && !currentMethodAnalysis.isEmpty()) {
						currentMethods.addAll(currentMethodAnalysis);
					}
					
					List<MethodAnalysis> refactoredMethodAnalysis = analyzeAndPlanMethod(cu, targetMethod);
					
					// Insertar solo MethodAnalysis con nombre no existente en refactoredMethods
					if (refactoredMethodAnalysis != null && !refactoredMethodAnalysis.isEmpty()) {
						Set<String> existingNames = refactoredMethods.stream()
							.map(MethodAnalysis::getMethodName)
							.collect(Collectors.toSet());
						List<MethodAnalysis> toAdd = refactoredMethodAnalysis.stream()
							.filter(ma -> ma.getMethodName() != null && !existingNames.contains(ma.getMethodName()))
							.toList();
						
						if (!toAdd.isEmpty()) {
							refactoredMethods.addAll(toAdd);
						}
						
						// Reiniciar la CU tras los posibles cambios
						if(refactoredMethodAnalysis != null && !refactoredMethodAnalysis.isEmpty() && refactoredMethodAnalysis.getLast().getExtraction() != null) {
							cu = refactoredMethodAnalysis.getLast().getExtraction().getCompilationUnitWithChanges();
						}
					}
				} catch (CoreException e) {
					throw new AnalyzeException("Error analyzing method " + targetMethod.getName().getIdentifier(), e);
				}
			}

	
			if (!refactoredMethods.isEmpty()) {
				ClassAnalysis ca = ClassAnalysis.builder()
					.icu(icu)
					.compilationUnit(cu)
					.className(icu.getElementName())
					.analysisDate(LocalDateTime.now())
					.currentMethods(currentMethods)
					.refactoredMethods(refactoredMethods)
					.refactoredSource(cu.toString())
					.build();

				resultHolder.add(ca);
			}
	
		} finally {
			icu.discardWorkingCopy();
		}
		
		ClassAnalysis result = resultHolder.isEmpty() ? ClassAnalysis.builder().build() : resultHolder.get(0);
		return result;
	}
	
	private List<MethodAnalysis> analyzeMethod(CompilationUnit cu, MethodDeclaration md) {
		
		// 0) Evitar analizar de extracciones ya realizadas
		if(md.getName().toString().contains("_ext_")) {
			return List.of();
		}
		
		// 1) Complejidad cognitiva actual
		CognitiveComplexityVisitor ccVisitor = new CognitiveComplexityVisitor();
		md.accept(ccVisitor);
		
		// 2) LOC actuales (aprox. rango de líneas del método)
		int currentCc = ccVisitor.getComplexity();
		int startLine = cu.getLineNumber(md.getStartPosition());
		int endLine = cu.getLineNumber(md.getStartPosition() + md.getLength());
		int currentLoc = Math.max(0, endLine - startLine + 1);
		
		// 3) Mapear al modelo de método
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

		// 3) Invocar a CodeExtractionEngine (usa NEO internamente) para evaluar posibles extracciones y obtener métricas + plan.
		List<RefactorComparison> comparison = extractionEngine.analyseAndPlan(cu, md, currentCc, currentLoc);

		// 4) Mapear al modelo de método
		return MethodAnalysisMetricsMapper.toMethodAnalysis(comparison);
	}
	
	private MethodDeclaration findNextMethodNeedingRefactor(CompilationUnit cu, Set<String> processed) {
        var types = cu.types();
        for (Object tObj : types) {
            // Recorre los métodos en orden de aparición y que no sean resultado de otras extracciones
            var typeDecl = (org.eclipse.jdt.core.dom.TypeDeclaration) tObj;
            for (MethodDeclaration md : typeDecl.getMethods()) {
                String key = bindingKey(md);
                if (processed.contains(key)) {
                	continue;
                }
                return md;
            }
        }
        return null;
    }

    private String bindingKey(MethodDeclaration md) {
        IMethodBinding b = md.resolveBinding();
        return (b != null) ? b.getKey() : (md.getName() != null ? md.getName().getIdentifier()+"@"+md.getStartPosition() : null);
    }
}