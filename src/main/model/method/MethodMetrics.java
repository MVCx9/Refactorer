package main.model.method;

import java.util.Collections;
import java.util.Objects;

import main.model.change.ExtractionPlan;
import main.model.common.Identifiable;

public class MethodMetrics implements Identifiable {

	private final String name;
	private final int loc;
	private final int cc;
	private final ExtractionPlan doPlan;
	private final ExtractionPlan undoPlan;

	public MethodMetrics(MethodMetricsBuilder methodMetricsBuilder) {
		super();
		this.name = methodMetricsBuilder.name;
		this.loc = methodMetricsBuilder.loc;
		this.cc = methodMetricsBuilder.cc;
		this.doPlan = methodMetricsBuilder.doPlan;
		this.undoPlan = methodMetricsBuilder.undoPlan;
	}

	public static MethodMetricsBuilder builder() {
		return new MethodMetricsBuilder();
	}

	public ExtractionPlan getDoPlan() {
		return doPlan;
	}

	public ExtractionPlan getUndoPlan() {
		return undoPlan;
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
		private ExtractionPlan doPlan = new ExtractionPlan(Collections.emptyList());
		private ExtractionPlan undoPlan = new ExtractionPlan(Collections.emptyList());

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

		public MethodMetricsBuilder doPlan(ExtractionPlan applyPlan) {
			this.doPlan = applyPlan;
			return this;
		}

		public MethodMetricsBuilder undoPlan(ExtractionPlan undoPlan) {
			this.undoPlan = undoPlan;
			return this;
		}

		public MethodMetrics build() {
			return new MethodMetrics(this);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(cc, doPlan, loc, name, undoPlan);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodMetrics other = (MethodMetrics) obj;
		return cc == other.cc && Objects.equals(doPlan, other.doPlan) && loc == other.loc
				&& Objects.equals(name, other.name) && Objects.equals(undoPlan, other.undoPlan);
	}

	@Override
	public String toString() {
		return "MethodMetrics [name=" + name + ", loc=" + loc + ", cc=" + cc + ", doPlan=" + doPlan + ", undoPlan="
				+ undoPlan + "]";
	}
}
