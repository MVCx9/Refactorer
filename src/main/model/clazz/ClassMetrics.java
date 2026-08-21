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
	private final String path;

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
		this.path = classMetricsBuilder.path;
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
	
	public String getPath() {
		return path;
	}

	/**
	 * Returns a singleton list with a trimmed view of this class holding only the
	 * original methods for which a refactoring plan was found together with the
	 * methods that plan would extract, or an empty list when the class has no
	 * planned refactoring at all.
	 */
	public List<ClassMetrics> getMethodsWithRefactors() {
		if (getMethodExtractionCount() <= 0) {
			return Collections.emptyList();
		}
		List<MethodMetrics> extracted = refactoredMethods.stream().filter(MethodMetrics::isExtracted)
				.collect(Collectors.toList());
		List<MethodMetrics> plannedOriginals = getPlannedOriginalMethods();
		if (extracted.isEmpty() || plannedOriginals.isEmpty()) {
			return Collections.emptyList();
		}
		return Collections.singletonList(copyWithMethods(plannedOriginals, extracted));
	}

	/**
	 * Returns the original methods whose cognitive complexity exceeds the
	 * configured threshold but for which the analysis could not find any feasible
	 * refactoring plan. They are reported so the user knows they were detected
	 * even though no extraction can be proposed for them.
	 */
	public List<MethodMetrics> getMethodsWithoutRefactors() {
		Set<String> plannedSignatures = plannedMethodSignatures();
		return currentMethods.stream().filter(this::exceedsComplexityThreshold)
				.filter(m -> !plannedSignatures.contains(m.getSignature())).collect(Collectors.toList());
	}

	/**
	 * Returns a copy of this class keeping its identity, sources and threshold but
	 * only the given methods. Used to build the trimmed views consumed by the UI.
	 */
	public ClassMetrics copyWithMethods(List<MethodMetrics> current, List<MethodMetrics> refactored) {
		return ClassMetrics.builder().name(name).analysisDate(analysisDate).currentSource(currentSource)
				.refactoredSource(refactoredSource).currentMethods(current).refactoredMethods(refactored)
				.complexityThreshold(complexityThreshold).path(path).build();
	}

	/**
	 * Returns the original methods that produced at least one extraction. Each
	 * refactored host method is matched back to its original by overload-aware
	 * signature (not by simple name) so overloaded methods stay separated and
	 * non-refactored overloads are never reported as refactored.
	 */
	private List<MethodMetrics> getPlannedOriginalMethods() {
		Set<String> plannedSignatures = plannedMethodSignatures();
		return currentMethods.stream().filter(m -> plannedSignatures.contains(m.getSignature()))
				.collect(Collectors.toList());
	}

	/** Signatures of the host methods for which a refactoring plan was found. */
	private Set<String> plannedMethodSignatures() {
		return refactoredMethods.stream().filter(m -> m.getNumberOfExtractions() > 0).map(MethodMetrics::getSignature)
				.collect(Collectors.toSet());
	}

	private boolean exceedsComplexityThreshold(MethodMetrics method) {
		return method.getCc() > complexityThreshold;
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
		private String path = "";

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

		public ClassMetricsBuilder path(String path) {
			this.path = path;
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
				&& complexityThreshold == other.complexityThreshold
				&& Objects.equals(path, other.path);
	}

	@Override
	public String toString() {
		return "ClassMetrics [name=" + name + ", analysisDate=" + analysisDate + ", currentMethods=" + currentMethods
				+ ", refactoredMethods=" + refactoredMethods + ", currentSource=" + currentSource
				+ ", refactoredSource=" + refactoredSource + ", complexityThreshold=" + complexityThreshold 
				+ ", path=" + path + "]";
	}

}