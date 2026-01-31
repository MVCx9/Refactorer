package test.refactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.refactor.RefactorComparison;

class RefactorComparisonTest {

	private void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("given_builderWithDefaults_when_build_should_createRefactorComparisonWithDefaultValues")
	void given_builderWithDefaults_when_build_should_createRefactorComparisonWithDefaultValues() {
		final RefactorComparison comparison = RefactorComparison.builder().build();
		assertNotNull(comparison);
		assertNull(comparison.getName());
		toStrictEqual(0, comparison.getReducedComplexity());
		toStrictEqual(0, comparison.getNumberOfExtractions());
		assertNull(comparison.getCompilationUnitRefactored());
		assertNull(comparison.getStats());
		assertFalse(comparison.isUsedILP());
	}

	@Test
	@DisplayName("given_builderWithAllValues_when_build_should_createRefactorComparisonWithAllValues")
	void given_builderWithAllValues_when_build_should_createRefactorComparisonWithAllValues() {
		final RefactorComparison comparison = RefactorComparison.builder()
				.name("testMethod")
				.reducedComplexity(5)
				.numberOfExtractions(2)
				.usedILP(true)
				.build();
		toStrictEqual("testMethod", comparison.getName());
		toStrictEqual(5, comparison.getReducedComplexity());
		toStrictEqual(2, comparison.getNumberOfExtractions());
		assertTrue(comparison.isUsedILP());
	}

	@Test
	@DisplayName("given_motherDefaultComparison_when_getValues_should_returnExpectedDefaults")
	void given_motherDefaultComparison_when_getValues_should_returnExpectedDefaults() {
		final RefactorComparison comparison = RefactorComparisonMother.defaultComparison();
		assertNotNull(comparison);
		toStrictEqual("method", comparison.getName());
		toStrictEqual(4, comparison.getReducedComplexity());
		toStrictEqual(2, comparison.getNumberOfExtractions());
		assertNotNull(comparison.getCompilationUnitRefactored());
		assertNotNull(comparison.getStats());
	}

	@Test
	@DisplayName("given_motherWithName_when_getName_should_returnSpecifiedName")
	void given_motherWithName_when_getName_should_returnSpecifiedName() {
		final RefactorComparison comparison = RefactorComparisonMother.withName("customMethod");
		toStrictEqual("customMethod", comparison.getName());
	}

	@Test
	@DisplayName("given_motherWithNullName_when_getName_should_returnDefaultName")
	void given_motherWithNullName_when_getName_should_returnDefaultName() {
		final RefactorComparison comparison = RefactorComparisonMother.withName(null);
		toStrictEqual("method", comparison.getName());
	}

	@Test
	@DisplayName("given_zeroValues_when_build_should_acceptZeroValues")
	void given_zeroValues_when_build_should_acceptZeroValues() {
		final RefactorComparison comparison = RefactorComparison.builder()
				.reducedComplexity(0)
				.numberOfExtractions(0)
				.build();
		toStrictEqual(0, comparison.getReducedComplexity());
		toStrictEqual(0, comparison.getNumberOfExtractions());
	}

	@Test
	@DisplayName("given_negativeValues_when_build_should_acceptNegativeValues")
	void given_negativeValues_when_build_should_acceptNegativeValues() {
		final RefactorComparison comparison = RefactorComparison.builder()
				.reducedComplexity(-5)
				.numberOfExtractions(-1)
				.build();
		toStrictEqual(-5, comparison.getReducedComplexity());
		toStrictEqual(-1, comparison.getNumberOfExtractions());
	}

	@Test
	@DisplayName("given_largeValues_when_build_should_acceptLargeValues")
	void given_largeValues_when_build_should_acceptLargeValues() {
		final RefactorComparison comparison = RefactorComparison.builder()
				.reducedComplexity(1000)
				.numberOfExtractions(100)
				.build();
		toStrictEqual(1000, comparison.getReducedComplexity());
		toStrictEqual(100, comparison.getNumberOfExtractions());
	}

	@Test
	@DisplayName("given_usedILPFalse_when_isUsedILP_should_returnFalse")
	void given_usedILPFalse_when_isUsedILP_should_returnFalse() {
		final RefactorComparison comparison = RefactorComparison.builder()
				.usedILP(false)
				.build();
		assertFalse(comparison.isUsedILP());
	}

	@Test
	@DisplayName("given_usedILPTrue_when_isUsedILP_should_returnTrue")
	void given_usedILPTrue_when_isUsedILP_should_returnTrue() {
		final RefactorComparison comparison = RefactorComparison.builder()
				.usedILP(true)
				.build();
		assertTrue(comparison.isUsedILP());
	}

	@Test
	@DisplayName("given_emptyName_when_getName_should_returnEmptyString")
	void given_emptyName_when_getName_should_returnEmptyString() {
		final RefactorComparison comparison = RefactorComparison.builder()
				.name("")
				.build();
		toStrictEqual("", comparison.getName());
	}

	@Test
	@DisplayName("given_nullName_when_getName_should_returnNull")
	void given_nullName_when_getName_should_returnNull() {
		final RefactorComparison comparison = RefactorComparison.builder()
				.name(null)
				.build();
		assertNull(comparison.getName());
	}
}
