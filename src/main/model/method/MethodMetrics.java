package main.model.method;

import main.model.common.Identifiable;

public class MethodMetrics implements Identifiable {

	private final String name;
	private final int loc;
	private final int cc;

	public MethodMetrics(MethodMetricsBuilder methodMetricsBuilder) {
		super();
		this.name = methodMetricsBuilder.name;
		this.loc = methodMetricsBuilder.loc;
		this.cc = methodMetricsBuilder.cc;
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

	public static class MethodMetricsBuilder {
		private String name = "<unnamed>";
		private int loc = 0;
		private int cc = 0;

		public MethodMetricsBuilder() {
		}

		public MethodMetricsBuilder name(String name) {
			this.name = name;
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

		public MethodMetrics build() {
			return new MethodMetrics(this);
		}
	}
}
