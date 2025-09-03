package main.analyzer;

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
    public ClassAnalysis analyze(CompilationUnit cu, ICompilationUnit icu) throws JavaModelException {
        MethodDeclaration targetMethod = null;
        Set<MethodDeclaration> processedMethods = new LinkedHashSet<>();
        
        String currentSource = icu.getSource();
        List<MethodAnalysis> currentMethods = new ArrayList<>();
        List<MethodAnalysis> refactoredMethods = new ArrayList<>();
        try {
        	
        	// Analyze methods for current state
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
            
            // Analyze methods for refactored state (if any)
            while(true) {
	            targetMethod = findNextMethodNeedingRefactor(cu, processedMethods);
	            
	            if (targetMethod == null) {
	                break;
	            }
	            
	            // Marcar el método como procesado
	            processedMethods.add(targetMethod);
	            
	            List<MethodAnalysis> refactoredMethodAnalysis = analyzeAndPlanMethod(cu, targetMethod);
	            
	            // Insertar según nombre y firma del método usando Set<MethodDeclaration>
	            if (refactoredMethodAnalysis != null && !refactoredMethodAnalysis.isEmpty()) {
	            	
	            	// Conjunto de declaraciones ya existentes en la lista refactorizada
	            	Set<MethodDeclaration> existingDecls = refactoredMethods.stream()
	            		.map(MethodAnalysis::getMethodDeclaration)
	            		.filter(Objects::nonNull)
	            		.collect(Collectors.toCollection(LinkedHashSet::new));
	            	
	            	List<MethodAnalysis> toAdd = new LinkedList<>();
	            	for (MethodAnalysis ma : refactoredMethodAnalysis) {
	            		
	            		MethodDeclaration cand = ma.getMethodDeclaration();
	            		// si no hay declaración o nombre, no podemos comparar de forma fiable
	            		if (cand == null || cand.getName() == null) {
	            			continue; 
	            		}
	            		
	            		String candName = cand.getName().getIdentifier();
	            		boolean nameExists = existingDecls.stream().anyMatch(e -> e.getName() != null && candName.equals(e.getName().getIdentifier()));
	            		boolean sameSigExists = existingDecls.stream().anyMatch(e -> sameSignature(e, cand));
	            		
	            		// Nuevo método (nombre distinto a todos los existentes)
	            		if (!nameExists) {
	            			toAdd.add(ma);
	            			existingDecls.add(cand);
	            			continue;
	            		}
	            		
	            		// Mismo nombre pero distinta cantidad/tipos de parámetros
	            		if (!sameSigExists) {
	            			toAdd.add(ma);
	            			existingDecls.add(cand);
	            		}
	            		
	            		// Si la firma es igual, ya fue añadido; pasar al siguiente
	            	}
	            	
	            	if (!toAdd.isEmpty()) {
	            		refactoredMethods.addAll(toAdd);
	            		if(toAdd.getLast().getExtraction() != null && toAdd.getLast().getExtraction().getCompilationUnitWithChanges() != null) {
	            			cu = toAdd.getLast().getExtraction().getCompilationUnitWithChanges();
	            		}
	            	}
	            }
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
        // Defensive: avoid NPE if md is unexpectedly null
        if (md == null) return null;
        // 1) Complejidad cognitiva actual
        CognitiveComplexityVisitor ccVisitor = new CognitiveComplexityVisitor();
        md.accept(ccVisitor);
        
        // 2) LOC actuales (aprox. rango de líneas del método)
        int currentCc = ccVisitor.getComplexity();
        int startLine = cu.getLineNumber(md.getStartPosition());
        int endLine = cu.getLineNumber(md.getStartPosition() + md.getLength());
        int currentLoc = Math.max(0, endLine - startLine + 1);
        
        // 3) Mapear al modelo de método
        return MethodAnalysisMetricsMapper.toMethodAnalysis(md, currentCc, currentLoc);
    }

    private List<MethodAnalysis> analyzeAndPlanMethod(CompilationUnit cu, MethodDeclaration md) throws CoreException {
    	// Defensive: avoid NPE if md is unexpectedly null
        if (md == null) return List.of();
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
    
    private MethodDeclaration findNextMethodNeedingRefactor(CompilationUnit cu, Set<MethodDeclaration> processed) {
    	// Recorre los métodos en orden de aparición
        var types = cu.types();
        for (Object tObj : types) {
            var typeDecl = (org.eclipse.jdt.core.dom.TypeDeclaration) tObj;
            for (MethodDeclaration md : typeDecl.getMethods()) {
                boolean alreadyProcessed = processed.stream().anyMatch(p -> sameSignature(p, md));
                if (!alreadyProcessed && md != null) {
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