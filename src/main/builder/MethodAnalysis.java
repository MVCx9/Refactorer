package main.builder;

import org.eclipse.jdt.core.dom.CompilationUnit;

import main.neo.cem.CodeExtractionMetricsStats;

public class MethodAnalysis {
	private final String methodName;
	private final int cc;
	private final int loc;
	private final int reducedComplexity;
	private final int numberOfExtractions;
	private final CompilationUnit compilationUnitRefactored;
	private final CodeExtractionMetricsStats stats;

	public static Builder builder() {
		return new Builder();
	}

	private MethodAnalysis(Builder b) {
		this.methodName = b.methodName;
		this.cc = b.cc;
		this.loc = b.loc;
		this.reducedComplexity = b.reducedComplexity;
		this.numberOfExtractions = b.numberOfExtractions;
		this.compilationUnitRefactored = b.compilationUnitRefactored;
		this.stats = b.stats;
	}

	public String getMethodName() {
		return methodName;
	}

	public int getCc() {
		return cc;
	}

	public int getLoc() {
		return loc;
	}

	public int getReducedComplexity() {
		return reducedComplexity;
	}

	public int getNumberOfExtractions() {
		return numberOfExtractions;
	}

	public CompilationUnit getCompilationUnitRefactored() {
		return compilationUnitRefactored;
	}

	public CodeExtractionMetricsStats getStats() {
		return stats;
	}

	public static class Builder {
		private String methodName;
		private int cc;
		private int loc;
		private int reducedComplexity;
		private int numberOfExtractions;
		private CompilationUnit compilationUnitRefactored;
		private CodeExtractionMetricsStats stats = null;

		public Builder methodName(String v) {
			this.methodName = v;
			return this;
		}

		public Builder cc(int v) {
			this.cc = v;
			return this;
		}

		public Builder loc(int v) {
			this.loc = v;
			return this;
		}

		public Builder reducedComplexity(int v) {
			this.reducedComplexity = v;
			return this;
		}

		public Builder numberOfExtractions(int v) {
			this.numberOfExtractions = v;
			return this;
		}

		public Builder compilationUnitRefactored(CompilationUnit v) {
			this.compilationUnitRefactored = v;
			return this;
		}

		public Builder stats(CodeExtractionMetricsStats v) {
			this.stats = v;
			return this;
		}

		public MethodAnalysis build() {
			return new MethodAnalysis(this);
		}
	}
}