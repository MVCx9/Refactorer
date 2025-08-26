package main.model.method;

import java.util.Collections;
import java.util.Objects;

import main.model.change.ExtractionPlan;
import main.model.common.ComplexityStats;
import main.model.common.Identifiable;
import main.model.common.LocStats;

public class MethodMetrics implements Identifiable, ComplexityStats, LocStats {

	private final String name;
	private final int currentLoc;
	private final int refactoredLoc;
	private final int currentCc;
	private final int refactoredCc;
	private final int totalExtractedLinesOfCode;
	private final int totalReductionOfCc;
	private final ExtractionPlan doPlan;
	private final ExtractionPlan undoPlan;

	public MethodMetrics(MethodMetricsBuilder methodMetricsBuilder) {
		super();
		this.name = methodMetricsBuilder.name;
		this.currentLoc = methodMetricsBuilder.currentLoc;
		this.refactoredLoc = methodMetricsBuilder.refactoredLoc;
		this.currentCc = methodMetricsBuilder.currentCc;
		this.refactoredCc = methodMetricsBuilder.refactoredCc;
		this.totalExtractedLinesOfCode = methodMetricsBuilder.totalExtractedLinesOfCode;
		this.totalReductionOfCc = methodMetricsBuilder.totalReductionOfCc;
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

	@Override
	public int getCurrentCc() {
		return currentCc;
	}

	@Override
	public int getRefactoredCc() {
		return refactoredCc;
	}

	@Override
	public int getCurrentLoc() {
		return currentLoc;
	}

	@Override
	public int getRefactoredLoc() {
		return refactoredLoc;
	}

	public boolean isImproved() {
		return refactoredCc < currentCc;
	}

	public static class MethodMetricsBuilder {
		private String name = "<unnamed>";
		private int currentLoc = 0;
		private int refactoredLoc = 0;
		private int currentCc = 0;
		private int refactoredCc = 0;
		private int totalExtractedLinesOfCode;
		private int totalReductionOfCc;
		private ExtractionPlan doPlan = new ExtractionPlan(Collections.emptyList());
		private ExtractionPlan undoPlan = new ExtractionPlan(Collections.emptyList());

		public MethodMetricsBuilder() {
		}

		public MethodMetricsBuilder name(String name) {
			this.name = name;
			return this;
		}

		public MethodMetricsBuilder currentLoc(int currentLoc) {
			this.currentLoc = currentLoc;
			return this;
		}

		public MethodMetricsBuilder refactoredLoc(int refactoredLoc) {
			this.refactoredLoc = refactoredLoc;
			return this;
		}

		public MethodMetricsBuilder currentCc(int currentCc) {
			this.currentCc = currentCc;
			return this;
		}

		public MethodMetricsBuilder refactoredCc(int refactoredCc) {
			this.refactoredCc = refactoredCc;
			return this;
		}
		
		public MethodMetricsBuilder totalExtractedLinesOfCode(int totalExtractedLinesOfCode) {
			this.totalExtractedLinesOfCode = totalExtractedLinesOfCode;
			return this;
		}
		
		public MethodMetricsBuilder totalReductionOfCc(int totalReductionOfCc) {
			this.totalReductionOfCc = totalReductionOfCc;
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
		return Objects.hash(doPlan, currentCc, currentLoc, name, refactoredCc, refactoredLoc,
				totalExtractedLinesOfCode, totalReductionOfCc, undoPlan);
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
		return Objects.equals(doPlan, other.doPlan) && currentCc == other.currentCc
				&& currentLoc == other.currentLoc && Objects.equals(name, other.name)
				&& refactoredCc == other.refactoredCc && refactoredLoc == other.refactoredLoc
				&& totalExtractedLinesOfCode == other.totalExtractedLinesOfCode
				&& totalReductionOfCc == other.totalReductionOfCc && Objects.equals(undoPlan, other.undoPlan);
	}

	@Override
	public String toString() {
		return "MethodMetrics [name=" + name + ", currentLoc=" + currentLoc + ", refactoredLoc=" + refactoredLoc
				+ ", currentCc=" + currentCc + ", refactoredCc=" + refactoredCc + ", totalLinesOfRefactoredCode="
				+ totalExtractedLinesOfCode + ", totalRefactoredCc=" + totalReductionOfCc + ", applyPlan=" + doPlan
				+ ", undoPlan=" + undoPlan + "]";
	}

}
