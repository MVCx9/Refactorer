package test.model.method.mother;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.Change;

import main.builder.MethodAnalysis;
import main.neo.cem.CodeExtractionMetrics;
import main.neo.cem.CodeExtractionMetricsStats;

public final class MethodAnalysisMother {
	private static final String DEFAULT_METHOD_NAME = "method";
	private static final int DEFAULT_CC = 7;
	private static final int DEFAULT_LOC = 20;
	private static final int DEFAULT_REDUCED_COMPLEXITY = 3;
	private static final int DEFAULT_NUMBER_OF_EXTRACTIONS = 2;

	private MethodAnalysisMother() {}

	public static MethodAnalysis defaultAnalysis() {
		final CompilationUnit cu = sampleCompilationUnit();
		final CodeExtractionMetricsStats stats = sampleStats();
		return MethodAnalysis.builder()
				.methodName(DEFAULT_METHOD_NAME)
				.cc(DEFAULT_CC)
				.loc(DEFAULT_LOC)
				.reducedComplexity(DEFAULT_REDUCED_COMPLEXITY)
				.numberOfExtractions(DEFAULT_NUMBER_OF_EXTRACTIONS)
				.compilationUnitRefactored(cu)
				.stats(stats)
				.build();
	}

	public static MethodAnalysis withMethodName(String name) {
		final String n = name == null ? DEFAULT_METHOD_NAME : name;
		final CompilationUnit cu = sampleCompilationUnit();
		final CodeExtractionMetricsStats stats = sampleStats();
		return MethodAnalysis.builder()
				.methodName(n)
				.cc(DEFAULT_CC)
				.loc(DEFAULT_LOC)
				.reducedComplexity(DEFAULT_REDUCED_COMPLEXITY)
				.numberOfExtractions(DEFAULT_NUMBER_OF_EXTRACTIONS)
				.compilationUnitRefactored(cu)
				.stats(stats)
				.build();
	}

	public static MethodAnalysis custom(String methodName, int cc, int loc, int reducedComplexity, int numberOfExtractions, CompilationUnit cu, CodeExtractionMetricsStats stats) {
		final String n = methodName == null ? DEFAULT_METHOD_NAME : methodName;
		final CompilationUnit compilationUnit = cu == null ? sampleCompilationUnit() : cu;
		final CodeExtractionMetricsStats s = stats == null ? sampleStats() : stats;
		return MethodAnalysis.builder()
				.methodName(n)
				.cc(cc)
				.loc(loc)
				.reducedComplexity(reducedComplexity)
				.numberOfExtractions(numberOfExtractions)
				.compilationUnitRefactored(compilationUnit)
				.stats(s)
				.build();
	}

	private static CompilationUnit sampleCompilationUnit() {
		final ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		final String src = "public class Sample { void x() { int a=0; } }";
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(src.toCharArray());
		parser.setResolveBindings(false);
		return (CompilationUnit) parser.createAST(null);
	}

	private static CodeExtractionMetricsStats sampleStats() {
		final List<Change> empty = new ArrayList<>();
		final CodeExtractionMetrics m1 = new CodeExtractionMetrics(true, "OK", true, 5, 1, empty, empty);
		final CodeExtractionMetrics m2 = new CodeExtractionMetrics(true, "OK", true, 7, 2, empty, empty);
		return new CodeExtractionMetricsStats(new CodeExtractionMetrics[] { m1, m2 });
	}
}