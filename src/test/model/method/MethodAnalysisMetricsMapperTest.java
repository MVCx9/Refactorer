package test.model.method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import main.builder.MethodAnalysis;
import main.model.change.ExtractionPlan;
import main.model.method.MethodAnalysisMetricsMapper;
import main.model.method.MethodMetrics;
import main.refactor.RefactorComparison;
import test.analyzer.mother.MethodDeclarationMother;
import test.model.method.mother.MethodAnalysisMother;
import test.model.method.mother.RefactorComparisonMother;

public class MethodAnalysisMetricsMapperTest {

	private static void toStrictEqual(final Object expected, final Object actual) {
		org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
	}

	@Test
	@DisplayName("Map single MethodAnalysis to MethodMetrics including plans")
	void given_methodAnalysisWithData_when_toMethodMetrics_should_mapAllRefactoredValues() {
	final ExtractionPlan doPlan = test.model.method.mother.ExtractionPlanMother.empty();
	final ExtractionPlan undoPlan = test.model.method.mother.ExtractionPlanMother.empty();
		final MethodAnalysis analysis = MethodAnalysisMother.withPlans("m1", 10, 20, 5, 12, doPlan, undoPlan);

		final List<MethodMetrics> result = MethodAnalysisMetricsMapper.toMethodMetrics(List.of(analysis));

		toStrictEqual(1, result.size());
		final MethodMetrics mm = result.get(0);
		toStrictEqual("m1", mm.getName());
		toStrictEqual(5, mm.getCc());
		toStrictEqual(12, mm.getLoc());
		toStrictEqual(doPlan, mm.getDoPlan());
		toStrictEqual(undoPlan, mm.getUndoPlan());
	}

	@Test
	@DisplayName("Null plans replaced with empty ExtractionPlan")
	void given_methodAnalysisWithNullPlans_when_toMethodMetrics_should_useEmptyPlans() {
		final MethodAnalysis analysis = MethodAnalysis.builder()
			.methodName("m2")
			.currentCc(8)
			.currentLoc(30)
			.refactoredCc(6)
			.refactoredLoc(28)
			.extraction(null)
			.stats(null)
			.doPlan(null)
			.undoPlan(null)
			.build();

		final List<MethodMetrics> result = MethodAnalysisMetricsMapper.toMethodMetrics(List.of(analysis));

		final MethodMetrics mm = result.get(0);
		assertNotNull(mm.getDoPlan());
		assertNotNull(mm.getUndoPlan());
		toStrictEqual(0, mm.getDoPlan().changes().size());
		toStrictEqual(0, mm.getUndoPlan().changes().size());
	}

	@Test
	@DisplayName("Map list of RefactorComparison to MethodAnalysis preserving values and plans")
	void given_refactorComparisons_when_toMethodAnalysis_should_mapAllValues() {
	final RefactorComparison rc1 = RefactorComparisonMother.withPlans("m1", 10, 50, 6, 30, test.model.method.mother.ExtractionPlanMother.empty(), test.model.method.mother.ExtractionPlanMother.empty());
		final RefactorComparison rc2 = RefactorComparisonMother.simple("m2", 12, 60, 7, 40);

		final List<MethodAnalysis> result = MethodAnalysisMetricsMapper.toMethodAnalysis(List.of(rc1, rc2));

		toStrictEqual(2, result.size());
		final MethodAnalysis a1 = result.get(0);
		toStrictEqual("m1", a1.methodName());
		toStrictEqual(10, a1.currentCc());
		toStrictEqual(50, a1.currentLoc());
		toStrictEqual(6, a1.refactoredCc());
		toStrictEqual(30, a1.refactoredLoc());
		toStrictEqual(rc1.getDoPlan(), a1.doPlan());
		toStrictEqual(rc1.getUndoPlan(), a1.undoPlan());

		final MethodAnalysis a2 = result.get(1);
		toStrictEqual("m2", a2.methodName());
		toStrictEqual(12, a2.currentCc());
		toStrictEqual(60, a2.currentLoc());
		toStrictEqual(7, a2.refactoredCc());
		toStrictEqual(40, a2.refactoredLoc());
	}

	@Test
	@DisplayName("Map single MethodDeclaration to MethodAnalysis with mirrored metrics and empty plans")
	void given_methodDeclaration_when_toMethodAnalysis_should_mirrorCurrentMetrics() {
		final MethodDeclaration md = MethodDeclarationMother.simple("doWork", 0, 10);
		final MethodAnalysis analysis = MethodAnalysisMetricsMapper.toMethodAnalysis(md, 14, 100);
		toStrictEqual("doWork", analysis.methodName());
		toStrictEqual(14, analysis.currentCc());
		toStrictEqual(100, analysis.currentLoc());
		toStrictEqual(14, analysis.refactoredCc());
		toStrictEqual(100, analysis.refactoredLoc());
		toStrictEqual(0, analysis.doPlan().changes().size());
		toStrictEqual(0, analysis.undoPlan().changes().size());
	}

	@Test
	@DisplayName("Empty input mapping results in empty outputs")
	void given_emptyInputs_when_mapping_should_returnEmptyLists() {
		final List<MethodMetrics> metrics = MethodAnalysisMetricsMapper.toMethodMetrics(List.of());
		final List<MethodAnalysis> analyses = MethodAnalysisMetricsMapper.toMethodAnalysis(List.of());
		toStrictEqual(0, metrics.size());
		toStrictEqual(0, analyses.size());
	}

	@Test
	@DisplayName("Multiple MethodAnalysis mapped preserving order")
	void given_multipleMethodAnalysis_when_toMethodMetrics_should_preserveOrder() {
		final MethodAnalysis a1 = MethodAnalysisMother.withValues("a1", 5, 10, 3, 8);
		final MethodAnalysis a2 = MethodAnalysisMother.withValues("a2", 6, 11, 4, 9);
		final MethodAnalysis a3 = MethodAnalysisMother.withValues("a3", 7, 12, 5, 10);

		final List<MethodMetrics> metrics = MethodAnalysisMetricsMapper.toMethodMetrics(List.of(a1, a2, a3));
		toStrictEqual(List.of("a1", "a2", "a3"), metrics.stream().map(MethodMetrics::getName).toList());
	}

	@Test
	@DisplayName("Plans default to empty when null in list mapping")
	void given_multipleMethodAnalysisWithMixedNullPlans_when_toMethodMetrics_should_defaultNullsToEmpty() {
		final MethodAnalysis withNullPlans = MethodAnalysis.builder()
			.methodName("n1")
			.currentCc(1)
			.currentLoc(2)
			.refactoredCc(1)
			.refactoredLoc(2)
			.extraction(null)
			.stats(null)
			.doPlan(null)
			.undoPlan(null)
			.build();
	final MethodAnalysis withPlans = MethodAnalysisMother.withPlans("n2", 2, 4, 1, 3, test.model.method.mother.ExtractionPlanMother.empty(), test.model.method.mother.ExtractionPlanMother.empty());

		final List<MethodMetrics> metrics = MethodAnalysisMetricsMapper.toMethodMetrics(List.of(withNullPlans, withPlans));

		final MethodMetrics first = metrics.get(0);
		final MethodMetrics second = metrics.get(1);
		assertNotNull(first.getDoPlan());
		assertNotNull(first.getUndoPlan());
		toStrictEqual(0, first.getDoPlan().changes().size());
		toStrictEqual(0, first.getUndoPlan().changes().size());
		toStrictEqual(0, second.getDoPlan().changes().size());
		toStrictEqual(0, second.getUndoPlan().changes().size());
	}

	@Test
	@DisplayName("Ensure returned lists are unmodifiable (defensive copy test)")
	void given_mappedMetrics_when_tryModify_throws_UnsupportedOperationException() {
		final MethodAnalysis a1 = MethodAnalysisMother.withValues("a", 1, 2, 1, 2);
		final List<MethodMetrics> metrics = MethodAnalysisMetricsMapper.toMethodMetrics(List.of(a1));
	final UnsupportedOperationException ex = org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class, () -> metrics.add(Mockito.mock(MethodMetrics.class)));
	assertNotNull(ex);
	}
}
