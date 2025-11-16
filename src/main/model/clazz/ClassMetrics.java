package main.model.clazz;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import main.model.common.ComplexityStats;
import main.model.common.Identifiable;
import main.model.common.LocStats;
import main.model.method.MethodMetrics;

public class ClassMetrics implements Identifiable, ComplexityStats, LocStats {

	private final String name;
	private final LocalDateTime analysisDate;
	private final List<MethodMetrics> currentMethods;
	private final List<MethodMetrics> refactoredMethods;
	private final String currentSource;
	private final String refactoredSource;
	private final int complexityThreshold;

	public ClassMetrics(ClassMetricsBuilder classMetricsBuilder) {
		super();
		this.name = classMetricsBuilder.name;
		this.analysisDate = classMetricsBuilder.analysisDate;
		this.currentMethods = classMetricsBuilder.currentMethods == null ? Collections.emptyList()
				: Collections.unmodifiableList(classMetricsBuilder.currentMethods);
		this.refactoredMethods = classMetricsBuilder.refactoredMethods == null ? Collections.emptyList()
				: Collections.unmodifiableList(classMetricsBuilder.refactoredMethods);
		this.refactoredSource = classMetricsBuilder.refactoredSource;
		this.currentSource = classMetricsBuilder.currentSource;
		this.complexityThreshold = classMetricsBuilder.complexityThreshold;
	}

	public static ClassMetricsBuilder builder() {
		return new ClassMetricsBuilder();
	}

	public List<MethodMetrics> getCurrentMethods() {
		return currentMethods;
	}

	public List<MethodMetrics> getRefactoredMethods() {
		return refactoredMethods;
	}

	@Override
	public String getName() {
		return name;
	}

	public LocalDateTime getAnalysisDate() {
		return analysisDate;
	}

	@Override
	public int getCurrentLoc() {
		return currentMethods.stream().mapToInt(MethodMetrics::getLoc).sum();
	}

	@Override
	public int getCurrentCc() {
		return currentMethods.stream().mapToInt(MethodMetrics::getCc).sum();
	}

	@Override
	public int getRefactoredCc() {
		return refactoredMethods.stream().mapToInt(MethodMetrics::getCc).sum();
	}

	@Override
	public int getRefactoredLoc() {
		return refactoredMethods.stream().mapToInt(MethodMetrics::getLoc).sum();
	}

	public int getReducedComplexity() {
		return getCurrentCc() - getRefactoredCc();
	}

	public int getReducedLoc() {
		return getCurrentLoc() - getRefactoredLoc();
	}

	public int getAverageCurrentLoc() {
		return averageCurrent(MethodMetrics::getLoc);
	}

	public int getAverageCurrentCc() {
		return averageCurrent(MethodMetrics::getCc);
	}

	public int getAverageRefactoredCc() {
		return averageRefactored(MethodMetrics::getCc);
	}

	public int getAverageRefactoredLoc() {
		return averageRefactored(MethodMetrics::getLoc);
	}

	public int getAverageReducedComplexity() {
		return getAverageCurrentCc() - getAverageRefactoredCc();
	}

	public int getAverageReducedLoc() {
		return getAverageCurrentLoc() - getAverageRefactoredLoc();
	}

	public int getCurrentMethodCount() {
		return currentMethods.size();
	}

	public int getRefactoredMethodCount() {
		return refactoredMethods.size();
	}

	public String getRefactoredSource() {
		return refactoredSource;
	}

	public String getCurrentSource() {
		return currentSource;
	}

	public int getMethodExtractionCount() {
		return getRefactoredMethodCount() - getCurrentMethodCount();
	}

	public int getComplexityThreshold() {
		return complexityThreshold;
	}

	public List<ClassMetrics> getMethodsWithRefactors() {
		if (getMethodExtractionCount() <= 0) {
			return Collections.emptyList();
		}
		List<MethodMetrics> extracted = refactoredMethods.stream()
				.filter(m -> m.getName() != null && m.getName().contains("_ext_")).collect(Collectors.toList());
		if (extracted.isEmpty()) {
			return Collections.emptyList();
		}
		Set<String> baseNames = extracted.stream().map(m -> {
			String n = m.getName();
			int idx = n.indexOf("_ext_");
			return idx > -1 ? n.substring(0, idx) : n;
		}).collect(Collectors.toSet());
		List<MethodMetrics> originalBase = currentMethods.stream().filter(m -> baseNames.contains(m.getName()))
				.collect(Collectors.toList());
		ClassMetrics trimmed = ClassMetrics.builder().name(this.name).analysisDate(this.analysisDate)
				.currentSource(this.currentSource).refactoredSource(this.refactoredSource).currentMethods(originalBase)
				.refactoredMethods(extracted).complexityThreshold(this.complexityThreshold).build();
		return Collections.singletonList(trimmed);
	}

	private int averageCurrent(java.util.function.ToIntFunction<MethodMetrics> mapper) {
		return (int) Math.round(currentMethods.stream().mapToInt(mapper).average().orElse(0.0));
	}

	private int averageRefactored(java.util.function.ToIntFunction<MethodMetrics> mapper) {
		return (int) Math.round(refactoredMethods.stream().mapToInt(mapper).average().orElse(0.0));
	}

	public static class ClassMetricsBuilder {
		private String name = "<unnamed>";
		private LocalDateTime analysisDate = LocalDateTime.now();
		private List<MethodMetrics> currentMethods = Collections.emptyList();
		private List<MethodMetrics> refactoredMethods = Collections.emptyList();
		private String currentSource = "";
		private String refactoredSource = "";
		private int complexityThreshold = 15;

		public ClassMetricsBuilder() {
		}

		public ClassMetricsBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ClassMetricsBuilder analysisDate(LocalDateTime analysisDate) {
			this.analysisDate = analysisDate;
			return this;
		}

		public ClassMetricsBuilder currentMethods(List<MethodMetrics> methods) {
			this.currentMethods = methods;
			return this;
		}

		public ClassMetricsBuilder refactoredMethods(List<MethodMetrics> methods) {
			this.refactoredMethods = methods;
			return this;
		}

		public ClassMetricsBuilder currentSource(String source) {
			this.currentSource = source;
			return this;
		}

		public ClassMetricsBuilder refactoredSource(String source) {
			this.refactoredSource = source;
			return this;
		}

		public ClassMetricsBuilder complexityThreshold(int v) {
			this.complexityThreshold = v;
			return this;
		}

		public ClassMetrics build() {
			return new ClassMetrics(this);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(analysisDate, currentMethods, currentSource, name, refactoredMethods, refactoredSource,
				complexityThreshold);
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
		return Objects.equals(analysisDate, other.analysisDate) && Objects.equals(currentMethods, other.currentMethods)
				&& Objects.equals(currentSource, other.currentSource) && Objects.equals(name, other.name)
				&& Objects.equals(refactoredMethods, other.refactoredMethods)
				&& Objects.equals(refactoredSource, other.refactoredSource)
				&& complexityThreshold == other.complexityThreshold;
	}

	@Override
	public String toString() {
		return "ClassMetrics [name=" + name + ", analysisDate=" + analysisDate + ", currentMethods=" + currentMethods
				+ ", refactoredMethods=" + refactoredMethods + ", currentSource=" + currentSource
				+ ", refactoredSource=" + refactoredSource + ", complexityThreshold=" + complexityThreshold + "]";
	}

}