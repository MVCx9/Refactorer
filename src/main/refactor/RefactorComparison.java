package main.refactor;

import java.util.Collections;
import java.util.List;

import main.model.change.ExtractionPlan;
import main.neo.cem.CodeExtractionMetrics;
import main.neo.cem.CodeExtractionMetricsStats;

public class RefactorComparison {

	private final String name;
	private final int originalCc;
	private final int originalLoc;
	private final int refactoredCc;
	private final int refactoredLoc;
	private final int extractionCount;
	private final List<CodeExtractionMetrics> extractions;
	private final CodeExtractionMetrics bestMetrics;
	private final CodeExtractionMetricsStats stats;
	private final ExtractionPlan doPlan;
	private final ExtractionPlan undoPlan;

	public static Builder builder() {
		return new Builder();
	}

	public RefactorComparison(Builder b) {
		this.name = b.name;
		this.originalCc = b.originalCc;
		this.originalLoc = b.originalLoc;
		this.refactoredCc = b.refactoredCc;
		this.refactoredLoc = b.refactoredLoc;
		this.extractionCount = b.extractionCount;
		this.extractions = b.extractions == null ? Collections.emptyList() : Collections.unmodifiableList(b.extractions);
		this.bestMetrics = b.bestMetrics;
		this.stats = b.stats;
		this.doPlan = b.doPlan;
		this.undoPlan = b.undoPlan;
	}

	public String getName() {
		return name;
	}

	public int getOriginalCc() {
		return originalCc;
	}

	public int getOriginalLoc() {
		return originalLoc;
	}

	public int getRefactoredCc() {
		return refactoredCc;
	}

	public int getRefactoredLoc() {
		return refactoredLoc;
	}

	public int getExtractionCount() {
		return extractionCount;
	}

	public List<CodeExtractionMetrics> getExtractions() {
		return extractions;
	}

	public CodeExtractionMetrics getBestMetrics() {
		return bestMetrics;
	}

	public CodeExtractionMetricsStats getStats() {
		return stats;
	}

	public ExtractionPlan getDoPlan() {
		return doPlan;
	}

	public ExtractionPlan getUndoPlan() {
		return undoPlan;
	}

	public static class Builder {
		private String name;
		private int originalCc;
		private int originalLoc;
		private int refactoredCc;
		private int refactoredLoc;
		private int extractionCount;
		private List<CodeExtractionMetrics> extractions;
		private CodeExtractionMetrics bestMetrics;
		private CodeExtractionMetricsStats stats;
		private ExtractionPlan doPlan;
		private ExtractionPlan undoPlan;

		public Builder name(String v) {
			this.name = v;
			return this;
		}

		public Builder originalCc(int v) {
			this.originalCc = v;
			return this;
		}

		public Builder originalLoc(int v) {
			this.originalLoc = v;
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

		public Builder extractionCount(int v) {
			this.extractionCount = v;
			return this;
		}

		public Builder extractions(List<CodeExtractionMetrics> v) {
			this.extractions = v;
			return this;
		}

		public Builder bestMetrics(CodeExtractionMetrics v) {
			this.bestMetrics = v;
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

		public RefactorComparison build() {
			return new RefactorComparison(this);
		}
	}
}