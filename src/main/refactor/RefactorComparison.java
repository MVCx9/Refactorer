package main.refactor;

import org.eclipse.ltk.core.refactoring.Change;

import main.neo.cem.CodeExtractionMetrics;
import main.neo.cem.CodeExtractionMetricsStats;

public class RefactorComparison {

	private final int refactoredCc;
	private final int refactoredLoc;
	private final CodeExtractionMetrics bestMetrics; // del método ganador
	private final CodeExtractionMetricsStats stats; // agregadas
	private final Change doPlan; // apply plan (NEO → Eclipse Change)
	private final Change undoPlan; // undo plan

	public static Builder builder() {
		return new Builder();
	}
	
	public RefactorComparison(Builder b) {
		this.refactoredCc = b.refactoredCc;
		this.refactoredLoc = b.refactoredLoc;
		this.bestMetrics = b.bestMetrics;
		this.stats = b.stats;
		this.doPlan = b.doPlan;
		this.undoPlan = b.undoPlan;
	}

	public int getRefactoredCc() {
		return refactoredCc;
	}

	public int getRefactoredLoc() {
		return refactoredLoc;
	}

	public CodeExtractionMetrics getBestMetrics() {
		return bestMetrics;
	}

	public CodeExtractionMetricsStats getStats() {
		return stats;
	}

	public Change getDoPlan() {
		return doPlan;
	}

	public Change getUndoPlan() {
		return undoPlan;
	}
	
	public static class Builder {
		private int refactoredCc;
		private int refactoredLoc;
		private CodeExtractionMetrics bestMetrics;
		private CodeExtractionMetricsStats stats;
		private Change doPlan;
		private Change undoPlan;
		
		public Builder refactoredCc(int v) {
			this.refactoredCc = v;
			return this;
		}
		
		public Builder refactoredLoc(int v) {
			this.refactoredLoc = v;
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
		
		public Builder doPlan(Change v) {
			this.doPlan = v;
			return this;
		}
		
		public Builder undoPlan(Change v) {
			this.undoPlan = v;
			return this;
		}
		
		public RefactorComparison build() {
			return new RefactorComparison(this);
		}
	}
}
