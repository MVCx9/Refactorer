package main.builder;

import java.util.List;

import main.model.change.ExtractionPlan;
import main.neo.cem.CodeExtractionMetrics;
import main.neo.cem.CodeExtractionMetricsStats;

public class MethodAnalysis {
	private final String methodName;
	private final int currentCc;
	private final int currentLoc;
	private final int refactoredCc;
	private final int refactoredLoc;
	private final CodeExtractionMetrics extraction;
	private final CodeExtractionMetricsStats stats;
	private final ExtractionPlan doPlan;
	private final ExtractionPlan undoPlan;

	public static Builder builder() {
		return new Builder();
	}

	private MethodAnalysis(Builder b) {
		this.methodName = b.methodName;
		this.currentCc = b.currentCc;
		this.currentLoc = b.currentLoc;
		this.refactoredCc = b.refactoredCc;
		this.refactoredLoc = b.refactoredLoc;
		this.extraction = b.extraction;
		this.stats = b.stats;
		this.doPlan = b.doPlan;
		this.undoPlan = b.undoPlan;
	}

	public String methodName() {
		return methodName;
	}

	public int currentCc() {
		return currentCc;
	}

	public int currentLoc() {
		return currentLoc;
	}

	public int refactoredCc() {
		return refactoredCc;
	}

	public int refactoredLoc() {
		return refactoredLoc;
	}

	public CodeExtractionMetrics extraction() {
		return extraction;
	}

	public CodeExtractionMetricsStats stats() {
		return stats;
	}

	public ExtractionPlan doPlan() {
		return doPlan;
	}

	public ExtractionPlan undoPlan() {
		return undoPlan;
	}

	public static class Builder {
		private String methodName;
		private int currentCc;
		private int currentLoc;
		private int refactoredCc;
		private int refactoredLoc;
		private CodeExtractionMetrics extraction;
		private CodeExtractionMetricsStats stats;
		private ExtractionPlan doPlan = new ExtractionPlan(List.of());
		private ExtractionPlan undoPlan = new ExtractionPlan(List.of());

		public Builder methodName(String v) {
			this.methodName = v;
			return this;
		}

		public Builder currentCc(int v) {
			this.currentCc = v;
			return this;
		}

		public Builder currentLoc(int v) {
			this.currentLoc = v;
			return this;
		}

		public Builder refactoredCc(int v) {
			this.refactoredCc = v;
			return this;
		}

		public Builder refactoredLoc(int v) {
			this.refactoredLoc = v;
			return this;
		}

		public Builder extraction(CodeExtractionMetrics v) {
			this.extraction = v;
			return this;
		}

		public Builder stats(CodeExtractionMetricsStats v) {
			this.stats = v;
			return this;
		}

		public Builder doPlan(ExtractionPlan v) {
			this.doPlan = v;
			return this;
		}

		public Builder undoPlan(ExtractionPlan v) {
			this.undoPlan = v;
			return this;
		}

		public MethodAnalysis build() {
			return new MethodAnalysis(this);
		}
	}
}