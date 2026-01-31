package test.model.method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.model.method.MethodMetrics;
import test.model.method.mother.MethodMetricsMother;

class MethodMetricsTest {

	private void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("given_builderWithDefaults_when_build_should_createMethodMetricsWithDefaultValues")
	void given_builderWithDefaults_when_build_should_createMethodMetricsWithDefaultValues() {
		final MethodMetrics metrics = MethodMetrics.builder().build();
		assertNotNull(metrics);
		toStrictEqual("<unnamed>", metrics.getName());
		toStrictEqual(0, metrics.getLoc());
		toStrictEqual(0, metrics.getCc());
		assertFalse(metrics.isUsedILP());
	}

	@Test
	@DisplayName("given_builderWithAllValues_when_build_should_createMethodMetricsWithAllValues")
	void given_builderWithAllValues_when_build_should_createMethodMetricsWithAllValues() {
		final MethodMetrics metrics = MethodMetrics.builder()
				.name("testMethod")
				.loc(25)
				.cc(8)
				.usedILP(true)
				.build();
		toStrictEqual("testMethod", metrics.getName());
		toStrictEqual(25, metrics.getLoc());
		toStrictEqual(8, metrics.getCc());
		assertTrue(metrics.isUsedILP());
	}

	@Test
	@DisplayName("given_motherDefaultMetrics_when_getValues_should_returnExpectedDefaults")
	void given_motherDefaultMetrics_when_getValues_should_returnExpectedDefaults() {
		final MethodMetrics metrics = MethodMetricsMother.defaultMetrics();
		assertNotNull(metrics);
		toStrictEqual("method", metrics.getName());
		toStrictEqual(10, metrics.getLoc());
		toStrictEqual(5, metrics.getCc());
	}

	@Test
	@DisplayName("given_motherWithName_when_getName_should_returnSpecifiedName")
	void given_motherWithName_when_getName_should_returnSpecifiedName() {
		final MethodMetrics metrics = MethodMetricsMother.withName("customMethod");
		toStrictEqual("customMethod", metrics.getName());
	}

	@Test
	@DisplayName("given_motherWithNullName_when_getName_should_returnDefaultName")
	void given_motherWithNullName_when_getName_should_returnDefaultName() {
		final MethodMetrics metrics = MethodMetricsMother.withName(null);
		toStrictEqual("method", metrics.getName());
	}

	@Test
	@DisplayName("given_motherCustom_when_getValues_should_returnCustomValues")
	void given_motherCustom_when_getValues_should_returnCustomValues() {
		final MethodMetrics metrics = MethodMetricsMother.custom("custom", 30, 12);
		toStrictEqual("custom", metrics.getName());
		toStrictEqual(30, metrics.getLoc());
		toStrictEqual(12, metrics.getCc());
	}

	@Test
	@DisplayName("given_methodMetrics_when_getNameUpper_should_returnUppercaseName")
	void given_methodMetrics_when_getNameUpper_should_returnUppercaseName() {
		final MethodMetrics metrics = MethodMetricsMother.withName("myMethod");
		toStrictEqual("MYMETHOD", metrics.getNameUpper());
	}

	@Test
	@DisplayName("given_methodMetricsWithNullName_when_getNameUpper_should_returnEmptyString")
	void given_methodMetricsWithNullName_when_getNameUpper_should_returnEmptyString() {
		final MethodMetrics metrics = MethodMetrics.builder().name(null).build();
		toStrictEqual("", metrics.getNameUpper());
	}

	@Test
	@DisplayName("given_zeroLoc_when_build_should_acceptZeroValue")
	void given_zeroLoc_when_build_should_acceptZeroValue() {
		final MethodMetrics metrics = MethodMetrics.builder()
				.name("test")
				.loc(0)
				.cc(0)
				.build();
		toStrictEqual(0, metrics.getLoc());
		toStrictEqual(0, metrics.getCc());
	}

	@Test
	@DisplayName("given_negativeLoc_when_build_should_acceptNegativeValue")
	void given_negativeLoc_when_build_should_acceptNegativeValue() {
		final MethodMetrics metrics = MethodMetrics.builder()
				.name("test")
				.loc(-5)
				.cc(-3)
				.build();
		toStrictEqual(-5, metrics.getLoc());
		toStrictEqual(-3, metrics.getCc());
	}

	@Test
	@DisplayName("given_usedILPFalse_when_isUsedILP_should_returnFalse")
	void given_usedILPFalse_when_isUsedILP_should_returnFalse() {
		final MethodMetrics metrics = MethodMetrics.builder()
				.usedILP(false)
				.build();
		assertFalse(metrics.isUsedILP());
	}

	@Test
	@DisplayName("given_usedILPTrue_when_isUsedILP_should_returnTrue")
	void given_usedILPTrue_when_isUsedILP_should_returnTrue() {
		final MethodMetrics metrics = MethodMetrics.builder()
				.usedILP(true)
				.build();
		assertTrue(metrics.isUsedILP());
	}

	@Test
	@DisplayName("given_builderReused_when_buildMultipleTimes_should_createIndependentInstances")
	void given_builderReused_when_buildMultipleTimes_should_createIndependentInstances() {
		final MethodMetrics.MethodMetricsBuilder builder = MethodMetrics.builder().name("shared");
		final MethodMetrics first = builder.loc(10).build();
		final MethodMetrics second = builder.loc(20).build();
		toStrictEqual(10, first.getLoc());
		toStrictEqual(20, second.getLoc());
	}

	@Test
	@DisplayName("given_emptyName_when_getName_should_returnEmptyString")
	void given_emptyName_when_getName_should_returnEmptyString() {
		final MethodMetrics metrics = MethodMetrics.builder().name("").build();
		toStrictEqual("", metrics.getName());
	}

	@Test
	@DisplayName("given_largeValues_when_build_should_acceptLargeValues")
	void given_largeValues_when_build_should_acceptLargeValues() {
		final MethodMetrics metrics = MethodMetrics.builder()
				.name("largeMethod")
				.loc(10000)
				.cc(500)
				.build();
		toStrictEqual(10000, metrics.getLoc());
		toStrictEqual(500, metrics.getCc());
	}
}
