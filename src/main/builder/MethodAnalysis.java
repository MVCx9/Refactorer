package main.builder;

import org.eclipse.jdt.core.dom.CompilationUnit;

import main.neo.core.jdt.CodeExtractionMetricsStats;


public class MethodAnalysis {
	private final String methodName;
	private final int cc;
	private final int loc;
	private final int reducedComplexity;
	private final int numberOfExtractions;
	private final CompilationUnit compilationUnitRefactored;
	/** Full source text (with comments/Javadocs preserved) after simulation. */
	private final String refactoredSource;
	private final CodeExtractionMetricsStats stats;
	private final boolean usedILP;

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
		this.refactoredSource = b.refactoredSource;
		this.stats = b.stats;
		this.usedILP = b.usedILP;
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

	/**
	 * Returns the full source text of the refactored compilation unit,
	 * preserving all comments and Javadocs.
	 */
	public String getRefactoredSource() {
		return refactoredSource;
	}

	public CodeExtractionMetricsStats getStats() {
		return stats;
	}

	public boolean isUsedILP() {
		return usedILP;
	}

	public static class Builder {
		private String methodName;
		private int cc;
		private int loc;
		private int reducedComplexity;
		private int numberOfExtractions;
		private CompilationUnit compilationUnitRefactored;
		private String refactoredSource;
		private CodeExtractionMetricsStats stats = null;
		private boolean usedILP = false;

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

		/**
		 * Sets the full source text (with comments/Javadocs) of the refactored unit.
		 */
		public Builder refactoredSource(String v) {
			this.refactoredSource = v;
			return this;
		}

		public Builder stats(CodeExtractionMetricsStats v) {
			this.stats = v;
			return this;
		}

		public Builder usedILP(boolean v) {
			this.usedILP = v;
			return this;
		}

		public MethodAnalysis build() {
			return new MethodAnalysis(this);
		}
	}
}