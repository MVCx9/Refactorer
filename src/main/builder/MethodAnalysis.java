package main.builder;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import main.model.change.ExtractionPlan;
import main.neo.cem.CodeExtractionMetrics;
import main.neo.cem.CodeExtractionMetricsStats;

public class MethodAnalysis {

	private final String methodName;
	private final MethodDeclaration declaration;
	private final int currentCc;
	private final int currentLoc;
	private final int refactoredCc;
	private final int refactoredLoc;
	private final CodeExtractionMetrics bestExtraction; // métricas del mejor candidato
	private final CodeExtractionMetricsStats stats; // métricas agregadas
	private final ExtractionPlan doPlan;
	private final ExtractionPlan undoPlan;

	public static Builder builder() {
		return new Builder();
	}

	private MethodAnalysis(Builder b) {
		this.methodName = b.methodName;
		this.declaration = b.declaration;
		this.currentCc = b.currentCc;
		this.currentLoc = b.currentLoc;
		this.refactoredCc = b.refactoredCc;
		this.refactoredLoc = b.refactoredLoc;
		this.bestExtraction = b.bestExtraction;
		this.stats = b.stats;
		this.doPlan = b.doPlan;
		this.undoPlan = b.undoPlan;
	}

	public String methodName() {
		return methodName;
	}

	public MethodDeclaration declaration() {
		return declaration;
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

	public CodeExtractionMetrics bestExtraction() {
		return bestExtraction;
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
		private MethodDeclaration declaration;
		private int currentCc;
		private int currentLoc;
		private int refactoredCc;
		private int refactoredLoc;
		private CodeExtractionMetrics bestExtraction;
		private CodeExtractionMetricsStats stats;
		private ExtractionPlan doPlan;
		private ExtractionPlan undoPlan;

		public Builder methodName(String v) {
			this.methodName = v;
			return this;
		}

		public Builder declaration(MethodDeclaration v) {
			this.declaration = v;
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

		public Builder bestExtraction(CodeExtractionMetrics v) {
			this.bestExtraction = v;
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
