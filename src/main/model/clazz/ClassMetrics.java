package main.model.clazz;

import java.util.Collections;
import java.util.List;

import main.model.change.ExtractionPlan;
import main.model.common.ComplexityStats;
import main.model.common.Identifiable;
import main.model.common.LocStats;
import main.model.method.MethodMetrics;

public class ClassMetrics implements Identifiable, ComplexityStats, LocStats {

	private final String name;
	private final List<MethodMetrics> currentMethods;
	private final List<MethodMetrics> refactoredMethods;

	public ClassMetrics(ClassMetricsBuilder classMetricsBuilder) {
		super();
		this.name = classMetricsBuilder.name;
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

	public int getAverageCurrentCc() {
		return averageCc(MethodMetrics::getCc);
	}
	
	public int getAverageRefactoredCc() {
		return averageCc(MethodMetrics::getCc);
	}
	
	public int getTotalExtractedLinesOfCode() {
		return currentMethods.stream().mapToInt(MethodMetrics::getLoc).sum() -
		   refactoredMethods.stream().mapToInt(MethodMetrics::getLoc).sum();
	}

	public int getCurrentMethodCount() {
		return currentMethods.size();
	}
	
	public int getRefactoredMethodCount() {
		return refactoredMethods.size();
	}

	public List<ExtractionPlan> getDoPlan() {
		return currentMethods.stream().map(MethodMetrics::getDoPlan).toList();
	}
	
	public List<ExtractionPlan> getUndoPlan() {
		return currentMethods.stream().map(MethodMetrics::getUndoPlan).toList();
	}
	
	private int averageCc(java.util.function.ToIntFunction<MethodMetrics> mapper) {
		return (int) Math.round(currentMethods.stream().mapToInt(mapper).average().orElse(0.0));
	}

	public static class ClassMetricsBuilder {
		private String name = "<unnamed>";
		private List<MethodMetrics> currentMethods = Collections.emptyList();
		private List<MethodMetrics> refactoredMethods = Collections.emptyList();

		public ClassMetricsBuilder() {
		}

		public ClassMetricsBuilder name(String name) {
			this.name = name;
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