package main.analyzer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
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
import main.neo.core.jdt.CognitiveComplexityVisitor;
import main.preferences.ProjectPreferences;
import main.refactor.CodeExtractionEngine;
import main.refactor.RefactorComparison;

public class ComplexityAnalyzer {

	/**
	 * Analyses an {@link ICompilationUnit} returning the resulting
	 * {@link ClassAnalysis} with the original metrics and the metrics of the
	 * proposed refactoring (when applicable).
	 * <p>
	 * The refactored source is obtained via in-memory simulation in
	 * {@link CodeExtractionEngine}; the underlying file is never modified.
	 * </p>
	 */
	public ClassAnalysis analyze(CompilationUnit cu, ICompilationUnit icu) throws JavaModelException, IOException {
		MethodDeclaration targetMethod = null;
		Set<MethodDeclaration> processedMethods = new LinkedHashSet<>();
		IProject project = icu.getJavaProject() != null ? icu.getJavaProject().getProject() : null;
		int threshold = ProjectPreferences.getComplexityThreshold(project);
		ICompilationUnit icuWorkingCopy = (ICompilationUnit) icu.getWorkingCopy(null);
		String currentSource = Utils.formatJava(icuWorkingCopy.getSource());
		String refactoredSource = currentSource;
		List<MethodAnalysis> currentMethods = new LinkedList<>();
		List<MethodAnalysis> refactoredMethods;
		String classPath = icu.getPath().toString();

		try {
			Map<String, MethodAnalysis> refactoredMethodsMap = new HashMap<>();

			while (true) {
				targetMethod = findNextMethodNeedingRefactor(cu, processedMethods);
				if (targetMethod == null)
					break;

				processedMethods.add(targetMethod);

				// Single CC computation per method (also annotates the AST so the cache and
				// the solver can reuse the per-node properties downstream).
				int cc = computeCognitiveComplexity(targetMethod);

				MethodAnalysis currentMethodAnalysis = analyzeMethod(cu, targetMethod, cc);
				if (currentMethodAnalysis != null) {
					currentMethods.add(currentMethodAnalysis);
				}

				List<MethodAnalysis> planResult = analyzeAndPlanMethod(cu, icuWorkingCopy, targetMethod, cc, threshold);
				if (!planResult.isEmpty()) {
					for (MethodAnalysis refactoredMethod : planResult) {
						refactoredMethodsMap.put(refactoredMethod.getMethodName(), refactoredMethod);
					}
					CompilationUnit lastCu = planResult.get(planResult.size() - 1).getCompilationUnitRefactored();
					if (lastCu != null) {
						cu = lastCu;
						// Use the working copy's source (which preserves comments and Javadocs)
						// instead of lastCu.toString() which strips them via NaiveASTFlattener.
						refactoredSource = Utils.formatJava(icuWorkingCopy.getSource());
					}
				}
			}

			if (!refactoredMethodsMap.isEmpty()) {
				refactoredMethods = buildRefactoredMethodsList(cu, refactoredMethodsMap);
			} else {
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
					.refactoredSource(refactoredSource)
					.complexityThreshold(threshold)
					.path(classPath)
					.build();
		} catch (CoreException e) {
			String methodName = (targetMethod != null && targetMethod.getName() != null)
					? targetMethod.getName().getIdentifier()
					: "<unknown>";
			throw new AnalyzeException("Error analyzing method " + methodName, e);
		} finally {
			if (icuWorkingCopy != null) {
				icuWorkingCopy.discardWorkingCopy();
			}
		}
	}

	private List<MethodAnalysis> buildRefactoredMethodsList(CompilationUnit cu,
			Map<String, MethodAnalysis> refactoredMethodsMap) {
		List<MethodAnalysis> result = new LinkedList<>();

		var types = cu.types();
		for (Object tObj : types) {
			if (!(tObj instanceof org.eclipse.jdt.core.dom.TypeDeclaration)) {
				continue;
			}
			var typeDecl = (org.eclipse.jdt.core.dom.TypeDeclaration) tObj;
			for (MethodDeclaration md : typeDecl.getMethods()) {
				if (md == null || md.getName() == null) {
					continue;
				}

				String methodName = md.getName().getIdentifier();
				int cc = computeCognitiveComplexity(md);
				MethodAnalysis baseAnalysis = analyzeMethod(cu, md, cc);
				if (baseAnalysis == null) {
					continue;
				}

				MethodAnalysis refactoredInfo = refactoredMethodsMap.get(methodName);
				if (refactoredInfo != null) {
					result.add(mergeMethodAnalysis(baseAnalysis, refactoredInfo));
				} else {
					result.add(baseAnalysis);
				}
			}
		}

		return result;
	}

	private MethodAnalysis mergeMethodAnalysis(MethodAnalysis base, MethodAnalysis refactored) {
		return MethodAnalysis.builder()
				.methodName(base.getMethodName())
				.cc(base.getCc())
				.loc(base.getLoc())
				.reducedComplexity(refactored.getReducedComplexity())
				.numberOfExtractions(refactored.getNumberOfExtractions())
				.compilationUnitRefactored(refactored.getCompilationUnitRefactored())
				.refactoredSource(refactored.getRefactoredSource())
				.stats(refactored.getStats())
				.usedILP(refactored.isUsedILP())
				.build();
	}

	/**
	 * Computes the cognitive complexity of the given method while annotating the
	 * AST with the per-node properties used by the cache, the solver and the
	 * {@link main.neo.core.Solution} fitness function.
	 */
	protected int computeCognitiveComplexity(MethodDeclaration md) {
		return CognitiveComplexityVisitor.methodComplexity(md).complexity;
	}

	private MethodAnalysis analyzeMethod(CompilationUnit cu, MethodDeclaration md, int cc) {
		if (md == null) {
			return null;
		}
		int loc = computeLoc(cu, md);
		return MethodAnalysisMetricsMapper.toMethodAnalysis(md, cc, loc);
	}

	private List<MethodAnalysis> analyzeAndPlanMethod(CompilationUnit cu, ICompilationUnit icuWorkingCopy,
			MethodDeclaration md, int cc, int threshold) throws CoreException, IOException {
		if (md == null) {
			return List.of();
		}
		List<RefactorComparison> comparison = CodeExtractionEngine.analyseAndPlan(cu, icuWorkingCopy, md, cc, threshold);
		return MethodAnalysisMetricsMapper.toMethodAnalysis(comparison);
	}

	private int computeLoc(CompilationUnit cu, MethodDeclaration md) {
		int startLine = cu.getLineNumber(md.getStartPosition());
		int endLine = cu.getLineNumber(md.getStartPosition() + md.getLength());
		return Math.max(0, endLine - startLine + 1);
	}

	private MethodDeclaration findNextMethodNeedingRefactor(CompilationUnit cu, Set<MethodDeclaration> processed) {
		var types = cu.types();
		for (Object tObj : types) {
			if (!(tObj instanceof org.eclipse.jdt.core.dom.TypeDeclaration)) {
				continue;
			}
			var typeDecl = (org.eclipse.jdt.core.dom.TypeDeclaration) tObj;
			for (MethodDeclaration md : typeDecl.getMethods()) {
				if (md == null) {
					continue;
				}
				if (md.getName() != null && md.getName().getIdentifier().contains("ext")) {
					continue;
				}
				boolean alreadyProcessed = processed.stream().anyMatch(p -> sameSignature(p, md));
				if (!alreadyProcessed) {
					return md;
				}
			}
		}
		return null;
	}

	private boolean sameSignature(MethodDeclaration a, MethodDeclaration b) {
		if (a == null || b == null || a.getName() == null || b.getName() == null)
			return false;
		if (!a.getName().getIdentifier().equals(b.getName().getIdentifier()))
			return false;
		List<String> aParams = parameterTypeKeys(a);
		List<String> bParams = parameterTypeKeys(b);
		if (aParams.size() != bParams.size())
			return false;
		for (int i = 0; i < aParams.size(); i++) {
			if (!aParams.get(i).equals(bParams.get(i)))
				return false;
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
				} else if (t.getErasure() != null && t.getErasure().getQualifiedName() != null
						&& !t.getErasure().getQualifiedName().isEmpty()) {
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
		return params.stream().map(p -> {
			String t = p.getType() != null ? p.getType().toString() : "unknown";
			if (p.isVarargs())
				t += "...";
			return t;
		}).collect(Collectors.toList());
	}
}
