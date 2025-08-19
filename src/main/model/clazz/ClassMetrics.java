package main.model.clazz;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import main.model.common.ComplexityStats;
import main.model.common.Identifiable;
import main.model.common.LocStats;
import main.model.method.MethodMetrics;

public class ClassMetrics implements Identifiable, ComplexityStats, LocStats {

	private final String name;
	private final int currentLoc;
	private final int refactoredLoc;
	private final List<MethodMetrics> methods;

	public ClassMetrics(ClassMetricsBuilder classMetricsBuilder) {
		super();
		this.name = classMetricsBuilder.name;
		this.currentLoc = classMetricsBuilder.currentLoc;
		this.refactoredLoc = classMetricsBuilder.refactoredLoc;
		this.methods = classMetricsBuilder.methods;
	}

	public static ClassMetricsBuilder builder() {
		return new ClassMetricsBuilder();
	}

	public List<MethodMetrics> getMethods() {
		return methods;
	}

	@Override
	public int getCurrentLoc() {
		return currentLoc;
	}

	@Override
	public int getRefactoredLoc() {
		return refactoredLoc;
	}

	@Override
	public String getName() {
		return name;
	}

	public int getCurrentCc() {
		return averageCc(MethodMetrics::getCurrentCc);
	}

	public int getRefactoredCc() {
		return averageCc(MethodMetrics::getRefactoredCc);
	}

	public int getCurrentMethodCount() {
		return methods.size();
	}

	private int averageCc(java.util.function.ToIntFunction<MethodMetrics> mapper) {
		return (int) Math.round(methods.stream().mapToInt(mapper).average().orElse(0.0));
	}

	public static class ClassMetricsBuilder {
		private String name = "<unnamed>";
		private int currentLoc = 0;
		private int refactoredLoc = 0;
		private List<MethodMetrics> methods = Collections.emptyList();

		public ClassMetricsBuilder() {
		}

		public ClassMetricsBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ClassMetricsBuilder currentLoc(int currentLoc) {
			this.currentLoc = currentLoc;
			return this;
		}

		public ClassMetricsBuilder refactoredLoc(int refactoredLoc) {
			this.refactoredLoc = refactoredLoc;
			return this;
		}

		public ClassMetricsBuilder methods(List<MethodMetrics> methods) {
			this.methods = methods;
			return this;
		}

		public ClassMetrics build() {
			return new ClassMetrics(this);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(currentLoc, methods, name, refactoredLoc);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassMetrics other = (ClassMetrics) obj;
		return currentLoc == other.currentLoc && Objects.equals(methods, other.methods)
				&& Objects.equals(name, other.name) && refactoredLoc == other.refactoredLoc;
	}

	@Override
	public String toString() {
		return "ClassMetrics [name=" + name + ", currentLoc=" + currentLoc + ", refactoredLoc=" + refactoredLoc
				+ ", methods=" + methods + "]";
	}

}