package main.model.method;

import main.model.common.Identifiable;

public class MethodMetrics implements Identifiable {

	/** Marker present in the name of methods produced by an extraction. */
	private static final String EXTRACTED_METHOD_MARKER = "_ext_";

	private final String name;
	private final String signature;
	private final int loc;
	private final int cc;
	private final int numberOfExtractions;
	private final boolean usedILP;

	public MethodMetrics(MethodMetricsBuilder methodMetricsBuilder) {
		super();
		this.name = methodMetricsBuilder.name;
		this.signature = methodMetricsBuilder.signature;
		this.loc = methodMetricsBuilder.loc;
		this.cc = methodMetricsBuilder.cc;
		this.numberOfExtractions = methodMetricsBuilder.numberOfExtractions;
		this.usedILP = methodMetricsBuilder.usedILP;
	}

	public static MethodMetricsBuilder builder() {
		return new MethodMetricsBuilder();
	}

	@Override
	public String getName() {
		return name;
	}

	public int getCc() {
		return cc;
	}

	public int getLoc() {
		return loc;
	}

	/**
	 * Returns the overload-aware signature ({@code name(paramType1,paramType2)})
	 * that uniquely identifies this method within its class. Overloaded methods
	 * share a simple name but have distinct signatures.
	 */
	public String getSignature() {
		return signature;
	}

	public int getNumberOfExtractions() {
		return numberOfExtractions;
	}

	/** Returns {@code true} when this method was produced by an extraction. */
	public boolean isExtracted() {
		return name != null && name.contains(EXTRACTED_METHOD_MARKER);
	}

	public boolean isUsedILP() {
		return usedILP;
	}

	public static class MethodMetricsBuilder {
		private String name = "<unnamed>";
		private String signature = "";
		private int loc = 0;
		private int cc = 0;
		private int numberOfExtractions = 0;
		private boolean usedILP = false;

		public MethodMetricsBuilder() {
		}

		public MethodMetricsBuilder name(String name) {
			this.name = name;
			return this;
		}

		public MethodMetricsBuilder signature(String signature) {
			this.signature = signature;
			return this;
		}

		public MethodMetricsBuilder loc(int loc) {
			this.loc = loc;
			return this;
		}


		public MethodMetricsBuilder cc(int cc) {
			this.cc = cc;
			return this;
		}

		public MethodMetricsBuilder numberOfExtractions(int numberOfExtractions) {
			this.numberOfExtractions = numberOfExtractions;
			return this;
		}

		public MethodMetricsBuilder usedILP(boolean usedILP) {
			this.usedILP = usedILP;
			return this;
		}

		public MethodMetrics build() {
			return new MethodMetrics(this);
		}
	}
}
