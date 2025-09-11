package test.model.method.mother;

import main.model.method.MethodMetrics;

public final class MethodMetricsMother {
	private static final String DEFAULT_NAME = "method";
	private static final int DEFAULT_LOC = 10;
	private static final int DEFAULT_CC = 5;

	private MethodMetricsMother() {}

	public static MethodMetrics defaultMetrics() {
		return MethodMetrics.builder()
				.name(DEFAULT_NAME)
				.loc(DEFAULT_LOC)
				.cc(DEFAULT_CC)
				.build();
	}

	public static MethodMetrics withName(String name) {
		final String n = name == null ? DEFAULT_NAME : name;
		return MethodMetrics.builder()
				.name(n)
				.loc(DEFAULT_LOC)
				.cc(DEFAULT_CC)
				.build();
	}

	public static MethodMetrics custom(String name, int loc, int cc) {
		final String n = name == null ? DEFAULT_NAME : name;
		return MethodMetrics.builder()
				.name(n)
				.loc(loc)
				.cc(cc)
				.build();
	}
}