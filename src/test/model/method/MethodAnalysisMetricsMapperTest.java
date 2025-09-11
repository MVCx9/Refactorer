package test.model.method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.builder.MethodAnalysis;
import main.model.method.MethodAnalysisMetricsMapper;
import main.model.method.MethodMetrics;
import main.refactor.RefactorComparison;
import test.model.method.mother.MethodAnalysisMother;
import test.refactor.RefactorComparisonMother;

class MethodAnalysisMetricsMapperTest {
	private final String classTemplate = "public class Sample { %s }";

	private static void toStrictEqual(Object expected, Object actual) { assertEquals(expected, actual); }

	@Test
	@DisplayName("given_validMethodAnalysisList_when_toMethodMetrics_should_mapAllFields")
	void given_validMethodAnalysisList_when_toMethodMetrics_should_mapAllFields() {
		final MethodAnalysis m1 = MethodAnalysisMother.custom("mA", 5, 11, 1, 0, null, null);
		final MethodAnalysis m2 = MethodAnalysisMother.custom("mB", 7, 22, 2, 1, null, null);
		final List<MethodMetrics> metrics = MethodAnalysisMetricsMapper.toMethodMetrics(List.of(m1, m2));
		toStrictEqual(2, metrics.size());
		toStrictEqual("mA", metrics.get(0).getName());
		toStrictEqual(5, metrics.get(0).getCc());
		toStrictEqual(11, metrics.get(0).getLoc());
		toStrictEqual("mB", metrics.get(1).getName());
		toStrictEqual(7, metrics.get(1).getCc());
		toStrictEqual(22, metrics.get(1).getLoc());
	}

	@Test
	@DisplayName("given_emptyMethodAnalysisList_when_toMethodMetrics_should_returnEmptyList")
	void given_emptyMethodAnalysisList_when_toMethodMetrics_should_returnEmptyList() {
		final List<MethodMetrics> metrics = MethodAnalysisMetricsMapper.toMethodMetrics(Collections.emptyList());
		toStrictEqual(0, metrics.size());
	}

	@Test
	@DisplayName("given_nullMethodAnalysisList_when_toMethodMetrics_throws_NullPointerException")
	void given_nullMethodAnalysisList_when_toMethodMetrics_throws_NullPointerException() {
		assertThrows(NullPointerException.class, () -> MethodAnalysisMetricsMapper.toMethodMetrics(null));
	}

	@Test
	@DisplayName("given_methodAnalysisListWithNullElement_when_toMethodMetrics_throws_NullPointerException")
	void given_methodAnalysisListWithNullElement_when_toMethodMetrics_throws_NullPointerException() {
		final MethodAnalysis ok = MethodAnalysisMother.defaultAnalysis();
		assertThrows(NullPointerException.class, () -> MethodAnalysisMetricsMapper.toMethodMetrics(Arrays.asList(ok, null)));
	}

	@Test
	@DisplayName("given_validRefactorComparisonList_when_toMethodAnalysis_should_mapAllFields")
	void given_validRefactorComparisonList_when_toMethodAnalysis_should_mapAllFields() {
		final RefactorComparison c1 = RefactorComparisonMother.withName("rA");
		final RefactorComparison c2 = RefactorComparisonMother.withName("rB");
		final List<MethodAnalysis> analysis = MethodAnalysisMetricsMapper.toMethodAnalysis(List.of(c1, c2));
		toStrictEqual(2, analysis.size());
		toStrictEqual("rA", analysis.get(0).getMethodName());
		toStrictEqual(0, analysis.get(0).getCc());
		toStrictEqual(0, analysis.get(0).getLoc());
		toStrictEqual(c1.getReducedComplexity(), analysis.get(0).getReducedComplexity());
		toStrictEqual(c1.getNumberOfExtractions(), analysis.get(0).getNumberOfExtractions());
		toStrictEqual("rB", analysis.get(1).getMethodName());
	}

	@Test
	@DisplayName("given_emptyRefactorComparisonList_when_toMethodAnalysis_should_returnEmptyList")
	void given_emptyRefactorComparisonList_when_toMethodAnalysis_should_returnEmptyList() {
		final List<MethodAnalysis> analysis = MethodAnalysisMetricsMapper.toMethodAnalysis(Collections.emptyList());
		toStrictEqual(0, analysis.size());
	}

	@Test
	@DisplayName("given_nullRefactorComparisonList_when_toMethodAnalysis_throws_NullPointerException")
	void given_nullRefactorComparisonList_when_toMethodAnalysis_throws_NullPointerException() {
		assertThrows(NullPointerException.class, () -> MethodAnalysisMetricsMapper.toMethodAnalysis(null));
	}

	@Test
	@DisplayName("given_refactorComparisonListWithNullElement_when_toMethodAnalysis_throws_NullPointerException")
	void given_refactorComparisonListWithNullElement_when_toMethodAnalysis_throws_NullPointerException() {
		final RefactorComparison ok = RefactorComparisonMother.defaultComparison();
		assertThrows(NullPointerException.class, () -> MethodAnalysisMetricsMapper.toMethodAnalysis(Arrays.asList(ok, null)));
	}

	@Test
	@DisplayName("given_validMethodDeclaration_when_toMethodAnalysis_should_mapFields")
	void given_validMethodDeclaration_when_toMethodAnalysis_should_mapFields() {
		final MethodDeclaration md = firstMethodDeclaration("void z(){ int a=0; }");
		final MethodAnalysis analysis = MethodAnalysisMetricsMapper.toMethodAnalysis(md, 9, 33);
		toStrictEqual("z", analysis.getMethodName());
		toStrictEqual(9, analysis.getCc());
		toStrictEqual(33, analysis.getLoc());
		toStrictEqual(0, analysis.getReducedComplexity());
		toStrictEqual(0, analysis.getNumberOfExtractions());
		toStrictEqual(null, analysis.getCompilationUnitRefactored());
		toStrictEqual(null, analysis.getStats());
	}

	@Test
	@DisplayName("given_nullMethodDeclaration_when_toMethodAnalysis_throws_NullPointerException")
	void given_nullMethodDeclaration_when_toMethodAnalysis_throws_NullPointerException() {
		assertThrows(NullPointerException.class, () -> MethodAnalysisMetricsMapper.toMethodAnalysis(null, 1, 1));
	}

	private MethodDeclaration firstMethodDeclaration(String methodSrc) {
		final String src = String.format(this.classTemplate, methodSrc);
		final ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(src.toCharArray());
		parser.setResolveBindings(false);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		final TypeDeclaration type = (TypeDeclaration) cu.types().get(0);
		return type.getMethods()[0];
	}
}