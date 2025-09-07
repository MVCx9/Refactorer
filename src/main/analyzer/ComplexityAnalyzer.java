package main.analyzer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import main.builder.ClassAnalysis;
import main.builder.MethodAnalysis;
import main.common.error.AnalyzeException;
import main.common.utils.Utils;
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
    public ClassAnalysis analyze(CompilationUnit cu, ICompilationUnit icu) throws JavaModelException, IOException {
        MethodDeclaration targetMethod = null;
        Set<MethodDeclaration> processedMethods = new LinkedHashSet<>();
        
        String currentSource = icu.getSource();
        ICompilationUnit icuWorkingCopy = (ICompilationUnit) icu.getWorkingCopy(null);
        List<MethodAnalysis> currentMethods = new LinkedList<>();
        List<MethodAnalysis> refactoredMethodAnalysis = new LinkedList<>();
        List<MethodAnalysis> refactoredMethods = new LinkedList<>();
        try {
        	// Analizamos todos los métodos inicialmente
	    	var types = cu.types();
	        for (Object tObj : types) {
	        	var typeDecl = (org.eclipse.jdt.core.dom.TypeDeclaration) tObj;
	            for (MethodDeclaration md : typeDecl.getMethods()) {
	            	if (md == null) {
	            		continue;
	            	}
	            	MethodAnalysis ma = analyzeMethod(cu, md);
	            	if(ma != null) {
	            		currentMethods.add(ma);
            		}
            	}
	        }
            
            // Planificamos extracciones y aplicamos los cambios iterativamente al CompilationUnit
            while(true) {
	            targetMethod = findNextMethodNeedingRefactor(cu, processedMethods);
	            
	            if (targetMethod == null) {
	                break;
	            }
	            
	            // Marcar el método como procesado
	            processedMethods.add(targetMethod);
	            
	            refactoredMethodAnalysis.addAll(analyzeAndPlanMethod(cu, icuWorkingCopy, targetMethod));
	            
	            if(refactoredMethodAnalysis.isEmpty()) {
	            	// No se han encontrado extracciones para este método, continuar con el siguiente
	            	continue;
	            }
	            
	            cu = refactoredMethodAnalysis.getLast().getCompilationUnitRefactored();
            }
            
            // Analizamos de nuevo el CompilationUnit con los cambios aplicados (si los hay)
            if (refactoredMethodAnalysis != null && !refactoredMethodAnalysis.isEmpty()) {
            	types = cu.types();
    	        for (Object tObj : types) {
    	        	var typeDecl = (org.eclipse.jdt.core.dom.TypeDeclaration) tObj;
    	            for (MethodDeclaration md : typeDecl.getMethods()) {
    	            	if (md == null) {
    	            		continue;
    	            	}
    	            	MethodAnalysis ma = analyzeMethod(cu, md);
    	            	if(ma != null) {
    	            		refactoredMethods.add(ma);
                		}
                	}
    	        }
    	        
			} else {
				// No se hicieron extracciones, el estado refactorizado es igual al actual
				refactoredMethods = currentMethods;
			}	
    
            return ClassAnalysis.builder()
                    .icu(icu)
                    .compilationUnit(cu)
                    .className(icu.getElementName())
                    .analysisDate(LocalDateTime.now())
                    .currentMethods(currentMethods)
                    .refactoredMethods(refactoredMethods)
                    .currentSource(currentSource)
                    .refactoredSource(Utils.formatJava(cu.toString()))
                    .build();
    
        } catch (CoreException e) {
            String methodName = (targetMethod != null && targetMethod.getName() != null)
                    ? targetMethod.getName().getIdentifier()
                    : "<unknown>";
            throw new AnalyzeException("Error analyzing method " + methodName, e);
        }finally {
            icu.discardWorkingCopy();
        }
    }
    
    private MethodAnalysis analyzeMethod(CompilationUnit cu, MethodDeclaration md) {
    	if (md == null) {
        	return null;
        }
        
        // 1) Analizamos la complejidad cognitiva
        CognitiveComplexityVisitor ccVisitor = new CognitiveComplexityVisitor();
        md.accept(ccVisitor);
        
        // 2) Analizamos las LOC (aprox. rango de líneas del método)
        int currentCc = ccVisitor.getComplexity();
        int startLine = cu.getLineNumber(md.getStartPosition());
        int endLine = cu.getLineNumber(md.getStartPosition() + md.getLength());
        int currentLoc = Math.max(0, endLine - startLine + 1);
        
        // 3) Mapear al modelo de método
        return MethodAnalysisMetricsMapper.toMethodAnalysis(md, currentCc, currentLoc);
    }

    private List<MethodAnalysis> analyzeAndPlanMethod(CompilationUnit cu, ICompilationUnit icuWorkingCopy, MethodDeclaration md) throws CoreException, IOException {
        if (md == null) {
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

        // 3) Invocar a CodeExtractionEngine (usa NEO internamente) para evaluar posibles extracciones y obtener métricas + plan.
        List<RefactorComparison> comparison = extractionEngine.analyseAndPlan(cu, icuWorkingCopy, md, currentCc, currentLoc);

        // 4) Mapear al modelo de método
        return MethodAnalysisMetricsMapper.toMethodAnalysis(comparison);
    }
    
    private MethodDeclaration findNextMethodNeedingRefactor(CompilationUnit cu, Set<MethodDeclaration> processed) {
    	// Recorre los métodos en orden de aparición
        var types = cu.types();
        for (Object tObj : types) {
            var typeDecl = (org.eclipse.jdt.core.dom.TypeDeclaration) tObj;
            for (MethodDeclaration md : typeDecl.getMethods()) {
                if (md == null) {
                	continue;
                }
                // Omitir métodos generados por extracción
                if (md.getName() != null && md.getName().getIdentifier().contains("_ext_")) continue;
                boolean alreadyProcessed = processed.stream().anyMatch(p -> sameSignature(p, md));
                if (!alreadyProcessed) {
                    return md;
                }
            }
        }
        return null;
    }

    private boolean sameSignature(MethodDeclaration a, MethodDeclaration b) {
        if (a == null || b == null || a.getName() == null || b.getName() == null) return false;
        if (!a.getName().getIdentifier().equals(b.getName().getIdentifier())) return false;
        List<String> aParams = parameterTypeKeys(a);
        List<String> bParams = parameterTypeKeys(b);
        if (aParams.size() != bParams.size()) return false;
        for (int i = 0; i < aParams.size(); i++) {
            if (!aParams.get(i).equals(bParams.get(i))) return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private List<String> parameterTypeKeys(MethodDeclaration md) {
        IMethodBinding mb = md.resolveBinding();
        List<SingleVariableDeclaration> params = md.parameters();
        if (mb != null) {
            ITypeBinding[] types = mb.getParameterTypes();
            List<String> res = new ArrayList<>(types.length);
            for (int i = 0; i < types.length; i++) {
                ITypeBinding t = types[i];
                String name;
                if (t == null) {
                    name = "unknown";
                } else if (t.getErasure() != null && t.getErasure().getQualifiedName() != null && !t.getErasure().getQualifiedName().isEmpty()) {
                    name = t.getErasure().getQualifiedName();
                } else if (t.getQualifiedName() != null && !t.getQualifiedName().isEmpty()) {
                    name = t.getQualifiedName();
                } else {
                    name = t.getName();
                }
                if (i < params.size() && params.get(i).isVarargs()) {
                    name += "...";
                }
                res.add(name);
            }
            return res;
        }
        return params.stream()
            .map(p -> {
                String t = p.getType() != null ? p.getType().toString() : "unknown";
                if (p.isVarargs()) t += "...";
                return t;
            })
            .collect(Collectors.toList());
    }
}
