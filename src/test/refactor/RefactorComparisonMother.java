package test.refactor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.Change;

import main.neo.cem.CodeExtractionMetrics;
import main.neo.cem.CodeExtractionMetricsStats;
import main.refactor.RefactorComparison;

public final class RefactorComparisonMother {
	private static final String DEFAULT_NAME = "method";
	private static final int DEFAULT_REDUCED_COMPLEXITY = 4;
	private static final int DEFAULT_NUMBER_OF_EXTRACTIONS = 2;

	private RefactorComparisonMother() {}

	public static RefactorComparison defaultComparison() {
		final CompilationUnit cu = sampleCompilationUnit();
		final CodeExtractionMetricsStats stats = sampleStats();
		return RefactorComparison.builder()
				.name(DEFAULT_NAME)
				.reducedComplexity(DEFAULT_REDUCED_COMPLEXITY)
				.numberOfExtractions(DEFAULT_NUMBER_OF_EXTRACTIONS)
				.compilationUnitRefactored(cu)
				.stats(stats)
				.build();
	}

	public static RefactorComparison withName(String name) {
		final String n = name == null ? DEFAULT_NAME : name;
		final CompilationUnit cu = sampleCompilationUnit();
		final CodeExtractionMetricsStats stats = sampleStats();
		return RefactorComparison.builder()
				.name(n)
				.reducedComplexity(DEFAULT_REDUCED_COMPLEXITY)
				.numberOfExtractions(DEFAULT_NUMBER_OF_EXTRACTIONS)
				.compilationUnitRefactored(cu)
				.stats(stats)
				.build();
	}

	public static RefactorComparison custom(String name, int reducedComplexity, int numberOfExtractions, CompilationUnit cu, CodeExtractionMetricsStats stats) {
		final String n = name == null ? DEFAULT_NAME : name;
		final CompilationUnit compilationUnit = cu == null ? sampleCompilationUnit() : cu;
		final CodeExtractionMetricsStats s = stats == null ? sampleStats() : stats;
		return RefactorComparison.builder()
				.name(n)
				.reducedComplexity(reducedComplexity)
				.numberOfExtractions(numberOfExtractions)
				.compilationUnitRefactored(compilationUnit)
				.stats(s)
				.build();
	}

	private static CompilationUnit sampleCompilationUnit() {
		final ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		final String src = "public class Sample { void x(){} }";
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(src.toCharArray());
		parser.setResolveBindings(false);
		return (CompilationUnit) parser.createAST(null);
	}

	private static CodeExtractionMetricsStats sampleStats() {
		final List<Change> empty = new ArrayList<>();
		final CodeExtractionMetrics m1 = new CodeExtractionMetrics(true, "OK", true, 5, 1, empty, empty);
		final CodeExtractionMetrics m2 = new CodeExtractionMetrics(true, "OK", true, 8, 2, empty, empty);
		return new CodeExtractionMetricsStats(new CodeExtractionMetrics[] { m1, m2 });
	}
}