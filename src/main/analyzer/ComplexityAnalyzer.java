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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
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

	/** Marker that identifies methods created by a previous extraction. */
	private static final String EXTRACTED_METHOD_MARKER = "_ext_";

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
		Set<String> processedSignatures = new LinkedHashSet<>();
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
				targetMethod = findNextMethodNeedingRefactor(cu, processedSignatures);
				if (targetMethod == null)
					break;

				processedSignatures.add(methodSignature(targetMethod));

				// Single CC computation per method (also annotates the AST so the cache and
				// the solver can reuse the per-node properties downstream).
				int cc = computeCognitiveComplexity(targetMethod);

				MethodAnalysis currentMethodAnalysis = analyzeMethod(cu, targetMethod, cc);
				if (currentMethodAnalysis != null) {
					currentMethods.add(currentMethodAnalysis);
				}

				List<MethodAnalysis> planResult = analyzeAndPlanMethod(cu, icuWorkingCopy, targetMethod, cc, threshold);
				if (!planResult.isEmpty()) {
					String targetSignature = methodSignature(targetMethod);
					for (MethodAnalysis refactoredMethod : planResult) {
						refactoredMethodsMap.put(targetSignature, refactoredMethod);
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

				int cc = computeCognitiveComplexity(md);
				MethodAnalysis baseAnalysis = analyzeMethod(cu, md, cc);
				if (baseAnalysis == null) {
					continue;
				}

				MethodAnalysis refactoredInfo = refactoredMethodsMap.get(methodSignature(md));
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
				.signature(base.getSignature())
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
		return MethodAnalysisMetricsMapper.toMethodAnalysis(md, methodSignature(md), cc, loc);
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

	private MethodDeclaration findNextMethodNeedingRefactor(CompilationUnit cu, Set<String> processedSignatures) {
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
				if (isExtractedMethod(md)) {
					continue;
				}
				if (!processedSignatures.contains(methodSignature(md))) {
					return md;
				}
			}
		}
		return null;
	}

	/**
	 * Returns {@code true} when the method was produced by a previous extraction
	 * (its name contains the {@value #EXTRACTED_METHOD_MARKER} marker) and must
	 * therefore be skipped by the analysis loop.
	 */
	private boolean isExtractedMethod(MethodDeclaration md) {
		return md.getName().getIdentifier().contains(EXTRACTED_METHOD_MARKER);
	}

	/**
	 * Builds a stable, overload-aware signature for a method: its identifier
	 * followed by the ordered list of its parameter types.
	 * <p>
	 * Parameter order is significant, so two methods sharing a name but differing
	 * in the order of otherwise-identical parameter types are treated as distinct.
	 * The <em>syntactic</em> parameter types are used on purpose (instead of
	 * resolved bindings): the signature's source text is not altered when a method
	 * body is refactored, so the key stays identical after the compilation unit is
	 * re-parsed. This keeps the "already processed" check reliable and prevents a
	 * refactored method from being analysed twice.
	 * </p>
	 */
	private String methodSignature(MethodDeclaration md) {
		return md.getName().getIdentifier() + "(" + String.join(",", parameterTypeKeys(md)) + ")";
	}

	@SuppressWarnings("unchecked")
	private List<String> parameterTypeKeys(MethodDeclaration md) {
		List<SingleVariableDeclaration> params = md.parameters();
		List<String> keys = new ArrayList<>(params.size());
		for (SingleVariableDeclaration param : params) {
			String type = param.getType() != null ? param.getType().toString() : "unknown";
			if (param.isVarargs()) {
				type += "...";
			}
			keys.add(type);
		}
		return keys;
	}
}
