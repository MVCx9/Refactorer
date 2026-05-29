package main.refactor;

import org.eclipse.jdt.core.dom.CompilationUnit;

import main.neo.core.jdt.CodeExtractionMetricsStats;

public class RefactorComparison {
	private final String name;
	private final CompilationUnit compilationUnitRefactored;
	/** Full source text (with comments/Javadocs preserved) after simulation. */
	private final String refactoredSource;
	private final int reducedComplexity;
	private final int numberOfExtractions;
	private final CodeExtractionMetricsStats stats;
	private final boolean usedILP;

	public static Builder builder() {
		return new Builder();
	}

	public RefactorComparison(Builder b) {
		this.name = b.name;
		this.compilationUnitRefactored = b.compilationUnitRefactored;
		this.refactoredSource = b.refactoredSource;
		this.reducedComplexity = b.reducedComplexity;
		this.numberOfExtractions = b.numberOfExtractions;
		this.stats = b.stats;
		this.usedILP = b.usedILP;
	}

	public String getName() {
		return name;
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
	
	public int getReducedComplexity() {
		return reducedComplexity;
	}

	public int getNumberOfExtractions() {
		return numberOfExtractions;
	}

	public CodeExtractionMetricsStats getStats() {
		return stats;
	}

	public boolean isUsedILP() {
		return usedILP;
	}

	public static class Builder {
		private String name;
		private int reducedComplexity;
		private int numberOfExtractions;
		private CompilationUnit compilationUnitRefactored;
		private String refactoredSource;
		private CodeExtractionMetricsStats stats;
		private boolean usedILP = false;

		public Builder name(String v) {
			this.name = v;
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

		public RefactorComparison build() {
			return new RefactorComparison(this);
		}
	}
}