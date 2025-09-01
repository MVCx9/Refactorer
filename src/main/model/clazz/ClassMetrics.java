package main.model.clazz;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import main.model.change.ExtractionPlan;
import main.model.common.ComplexityStats;
import main.model.common.Identifiable;
import main.model.common.LocStats;
import main.model.method.MethodMetrics;
import main.refactor.Utils;

public class ClassMetrics implements Identifiable, ComplexityStats, LocStats {

	private final String name;
	private final LocalDateTime analysisDate;
	private final List<MethodMetrics> currentMethods;
	private final List<MethodMetrics> refactoredMethods;

	public ClassMetrics(ClassMetricsBuilder classMetricsBuilder) {
		super();
		this.name = classMetricsBuilder.name;
		this.analysisDate = classMetricsBuilder.analysisDate;
		this.currentMethods = classMetricsBuilder.currentMethods == null ? Collections.emptyList() : Collections.unmodifiableList(classMetricsBuilder.currentMethods);
		this.refactoredMethods = classMetricsBuilder.refactoredMethods == null ? Collections.emptyList() : Collections.unmodifiableList(classMetricsBuilder.refactoredMethods);
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
	
	public int getCurrentMethodCount() {
		return currentMethods.size();
	}
	
	public int getRefactoredMethodCount() {
		return refactoredMethods.size();
	}

	public List<ExtractionPlan> getDoPlan() {
		return refactoredMethods.stream().map(MethodMetrics::getDoPlan).toList();
	}
	
	public List<ExtractionPlan> getUndoPlan() {
		return Utils.reverse(refactoredMethods.stream().map(MethodMetrics::getUndoPlan).toList());
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

		public ClassMetrics build() {
			return new ClassMetrics(this);
		}
	}

}