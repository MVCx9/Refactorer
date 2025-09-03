package main.builder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ClassAnalysis {

	private final ICompilationUnit icu;
	private final CompilationUnit cu;
	private final IFile file;
	private final String className;
	private final LocalDateTime analysisDate;
	private final List<MethodAnalysis> currentMethods;
	private final List<MethodAnalysis> refactoredMethods;
	private final String currentSource;
	private final String refactoredSource;

	public static Builder builder() {
		return new Builder();
	}

	private ClassAnalysis(Builder b) {
		this.icu = b.icu;
		this.cu = b.cu;
		this.file = b.file;
		this.className = b.className;
		this.analysisDate = b.analysisDate;
		this.currentMethods = Collections.unmodifiableList(b.currentMethods);
		this.refactoredMethods = Collections.unmodifiableList(b.refactoredMethods);
		this.refactoredSource = b.refactoredSource;
		this.currentSource = b.currentSource;
	}

	public ICompilationUnit getIcu() {
		return icu;
	}

	public CompilationUnit getCompilationUnit() {
		return cu;
	}
	
	public IFile getFile() {
		return file;
	}

	public String getClassName() {
		return className;
	}
	
	public LocalDateTime getAnalysisDate() {
		return analysisDate;
	}

	public List<MethodAnalysis> getCurrentMethods() {
		return Collections.unmodifiableList(currentMethods);
	}
	
	public List<MethodAnalysis> getRefactoredMethods() {
		return Collections.unmodifiableList(refactoredMethods);
	}
	
	public String getCurrentSource() {
		return currentSource;
	}
	
	public String getRefactoredSource() {
		return refactoredSource;
	}

	public static class Builder {
		private ICompilationUnit icu;
		private CompilationUnit cu;
		private IFile file;
		private String className;
		private LocalDateTime analysisDate = LocalDateTime.now();
		private List<MethodAnalysis> currentMethods = List.of();
		private List<MethodAnalysis> refactoredMethods = List.of();
		private String currentSource;
		private String refactoredSource;

		public Builder icu(ICompilationUnit v) {
			this.icu = v;
			return this;
		}

		public Builder compilationUnit(CompilationUnit v) {
			this.cu = v;
			return this;
		}
		
		public Builder file(IFile v) {
			this.file = v;
			return this;
		}

		public Builder className(String v) {
			this.className = v;
			return this;
		}
		
		public Builder analysisDate(LocalDateTime v) {
			this.analysisDate = v;
			return this;
		}

		public Builder currentMethods(List<MethodAnalysis> v) {
			this.currentMethods = v;
			return this;
		}
		
		public Builder refactoredMethods(List<MethodAnalysis> v) {
			this.refactoredMethods = v;
			return this;
		}
		
		public Builder currentSource(String v) {
			this.currentSource = v;
			return this;
		}
		
		public Builder refactoredSource(String v) {
			this.refactoredSource = v;
			return this;
		}

		public ClassAnalysis build() {
			return new ClassAnalysis(this);
		}
	}
}