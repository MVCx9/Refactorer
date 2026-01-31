package test.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.builder.MethodAnalysis;

class MethodAnalysisTest {

	private void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("given_builderWithDefaults_when_build_should_createMethodAnalysisWithDefaultValues")
	void given_builderWithDefaults_when_build_should_createMethodAnalysisWithDefaultValues() {
		final MethodAnalysis analysis = MethodAnalysis.builder().build();
		assertNotNull(analysis);
		assertNull(analysis.getMethodName());
		toStrictEqual(0, analysis.getCc());
		toStrictEqual(0, analysis.getLoc());
		toStrictEqual(0, analysis.getReducedComplexity());
		toStrictEqual(0, analysis.getNumberOfExtractions());
		assertNull(analysis.getCompilationUnitRefactored());
		assertNull(analysis.getStats());
		assertFalse(analysis.isUsedILP());
	}

	@Test
	@DisplayName("given_builderWithAllValues_when_build_should_createMethodAnalysisWithAllValues")
	void given_builderWithAllValues_when_build_should_createMethodAnalysisWithAllValues() {
		final MethodAnalysis analysis = MethodAnalysis.builder()
				.methodName("testMethod")
				.cc(10)
				.loc(25)
				.reducedComplexity(5)
				.numberOfExtractions(2)
				.usedILP(true)
				.build();
		toStrictEqual("testMethod", analysis.getMethodName());
		toStrictEqual(10, analysis.getCc());
		toStrictEqual(25, analysis.getLoc());
		toStrictEqual(5, analysis.getReducedComplexity());
		toStrictEqual(2, analysis.getNumberOfExtractions());
		assertTrue(analysis.isUsedILP());
	}

	@Test
	@DisplayName("given_nullMethodName_when_build_should_acceptNullMethodName")
	void given_nullMethodName_when_build_should_acceptNullMethodName() {
		final MethodAnalysis analysis = MethodAnalysis.builder()
				.methodName(null)
				.build();
		assertNull(analysis.getMethodName());
	}

	@Test
	@DisplayName("given_zeroValues_when_build_should_acceptZeroValues")
	void given_zeroValues_when_build_should_acceptZeroValues() {
		final MethodAnalysis analysis = MethodAnalysis.builder()
				.cc(0)
				.loc(0)
				.reducedComplexity(0)
				.numberOfExtractions(0)
				.build();
		toStrictEqual(0, analysis.getCc());
		toStrictEqual(0, analysis.getLoc());
		toStrictEqual(0, analysis.getReducedComplexity());
		toStrictEqual(0, analysis.getNumberOfExtractions());
	}

	@Test
	@DisplayName("given_negativeValues_when_build_should_acceptNegativeValues")
	void given_negativeValues_when_build_should_acceptNegativeValues() {
		final MethodAnalysis analysis = MethodAnalysis.builder()
				.cc(-5)
				.loc(-10)
				.reducedComplexity(-3)
				.numberOfExtractions(-1)
				.build();
		toStrictEqual(-5, analysis.getCc());
		toStrictEqual(-10, analysis.getLoc());
		toStrictEqual(-3, analysis.getReducedComplexity());
		toStrictEqual(-1, analysis.getNumberOfExtractions());
	}

	@Test
	@DisplayName("given_largeValues_when_build_should_acceptLargeValues")
	void given_largeValues_when_build_should_acceptLargeValues() {
		final MethodAnalysis analysis = MethodAnalysis.builder()
				.cc(1000)
				.loc(5000)
				.reducedComplexity(500)
				.numberOfExtractions(100)
				.build();
		toStrictEqual(1000, analysis.getCc());
		toStrictEqual(5000, analysis.getLoc());
		toStrictEqual(500, analysis.getReducedComplexity());
		toStrictEqual(100, analysis.getNumberOfExtractions());
	}

	@Test
	@DisplayName("given_usedILPFalse_when_isUsedILP_should_returnFalse")
	void given_usedILPFalse_when_isUsedILP_should_returnFalse() {
		final MethodAnalysis analysis = MethodAnalysis.builder()
				.usedILP(false)
				.build();
		assertFalse(analysis.isUsedILP());
	}

	@Test
	@DisplayName("given_usedILPTrue_when_isUsedILP_should_returnTrue")
	void given_usedILPTrue_when_isUsedILP_should_returnTrue() {
		final MethodAnalysis analysis = MethodAnalysis.builder()
				.usedILP(true)
				.build();
		assertTrue(analysis.isUsedILP());
	}

	@Test
	@DisplayName("given_emptyMethodName_when_getMethodName_should_returnEmptyString")
	void given_emptyMethodName_when_getMethodName_should_returnEmptyString() {
		final MethodAnalysis analysis = MethodAnalysis.builder()
				.methodName("")
				.build();
		toStrictEqual("", analysis.getMethodName());
	}
}
