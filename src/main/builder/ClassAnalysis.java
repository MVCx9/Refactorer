package main.builder;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ClassAnalysis {

	private final ICompilationUnit icu;
	private final CompilationUnit cu;
	private final String className;
	private final List<MethodAnalysis> methods;

	public static Builder builder() {
		return new Builder();
	}

	private ClassAnalysis(Builder b) {
		this.icu = b.icu;
		this.cu = b.cu;
		this.className = b.className;
		this.methods = List.copyOf(b.methods);
	}

	public ICompilationUnit getIcu() {
		return icu;
	}

	public CompilationUnit getCompilationUnit() {
		return cu;
	}

	public String getClassName() {
		return className;
	}

	public List<MethodAnalysis> getMethods() {
		return Collections.unmodifiableList(methods);
	}

	public static class Builder {
		private ICompilationUnit icu;
		private CompilationUnit cu;
		private String className;
		private List<MethodAnalysis> methods = List.of();

		public Builder icu(ICompilationUnit v) {
			this.icu = v;
			return this;
		}

		public Builder compilationUnit(CompilationUnit v) {
			this.cu = v;
			return this;
		}

		public Builder className(String v) {
			this.className = v;
			return this;
		}

		public Builder methods(List<MethodAnalysis> v) {
			this.methods = v;
			return this;
		}

		public ClassAnalysis build() {
			return new ClassAnalysis(this);
		}
	}
}
