package model.method;


import java.util.Collections;
import java.util.Objects;

import model.change.ExtractionPlan;
import model.common.ComplexityStats;
import model.common.Identifiable;
import model.common.LocStats;

public class MethodMetrics implements Identifiable, ComplexityStats, LocStats {

    private final String name;

    private final int currentLoc;
    private final int refactoredLoc;
    private final int currentCc;
    private final int refactoredCc;
    private final int extractedMethodCount;
    /** cambios que aplicarían las extracciones de código */
    private final ExtractionPlan applyPlan;
    /** cambios que desharían las extracciones de código */
    private final ExtractionPlan undoPlan;

    public MethodMetrics(MethodMetricsBuilder methodMetricsBuilder) {
    	super();
    	this.name = methodMetricsBuilder.name;
		this.currentLoc = methodMetricsBuilder.currentLoc;
		this.refactoredLoc = methodMetricsBuilder.refactoredLoc;
		this.currentCc = methodMetricsBuilder.currentCc;
		this.refactoredCc = methodMetricsBuilder.refactoredCc;
		this.extractedMethodCount = methodMetricsBuilder.extractedMethodCount;
		this.applyPlan = methodMetricsBuilder.applyPlan;
		this.undoPlan = methodMetricsBuilder.undoPlan;
	}
    
    public static MethodMetricsBuilder builder() {
    	return new MethodMetricsBuilder();
    }

	public int getExtractedMethodCount() { return extractedMethodCount; }
	public ExtractionPlan getApplyPlan() { return applyPlan; }
	public ExtractionPlan getUndoPlan() { return undoPlan; }
	@Override public String getName() { return name; }
	@Override public int getCurrentCc()     { return currentCc; }
    @Override public int getRefactoredCc()  { return refactoredCc; }
    @Override public int getCurrentLoc()    { return currentLoc; }
    @Override public int getRefactoredLoc() { return refactoredLoc; }

    // conveniencia
    public boolean isImproved() {
        return refactoredCc < currentCc;
    }
    
    public static class MethodMetricsBuilder {
    	private String name = "<unnamed>";
        private int currentLoc = 0;
        private int refactoredLoc = 0;
        private int currentCc = 0;
        private int refactoredCc = 0;
        private int extractedMethodCount = 0;
        private ExtractionPlan applyPlan = new ExtractionPlan(Collections.emptyList());
        private ExtractionPlan undoPlan = new ExtractionPlan(Collections.emptyList());
        
        public MethodMetricsBuilder () {}
        
        public MethodMetricsBuilder name (String name) {
        	this.name = name;
        	return this;
        }
        
        public MethodMetricsBuilder currentLoc (int currentLoc) {
        	this.currentLoc = currentLoc;
        	return this;
        }
        
        public MethodMetricsBuilder refactoredLoc (int refactoredLoc) {
        	this.refactoredLoc = refactoredLoc;
        	return this;
        }
        
        public MethodMetricsBuilder currentCc (int currentCc) {
        	this.currentCc = currentCc;
        	return this;
        }
        
        public MethodMetricsBuilder refactoredCc (int refactoredCc) {
        	this.refactoredCc = refactoredCc;
        	return this;
        }
        
        public MethodMetricsBuilder extractedMethodCount (int extractedMethodCount) {
        	this.extractedMethodCount = extractedMethodCount;
        	return this;
        }
        
        public MethodMetricsBuilder applyPlan (ExtractionPlan applyPlan) {
        	this.applyPlan = applyPlan;
        	return this;
        }
        
        public MethodMetricsBuilder undoPlan (ExtractionPlan undoPlan) {
        	this.undoPlan = undoPlan;
        	return this;
        }
        
        public MethodMetrics build() {
        	return new MethodMetrics(this);
        }
    }

	@Override
	public int hashCode() {
		return Objects.hash(applyPlan, currentCc, currentLoc, extractedMethodCount, name, refactoredCc, refactoredLoc,
				undoPlan);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodMetrics other = (MethodMetrics) obj;
		return Objects.equals(applyPlan, other.applyPlan) && currentCc == other.currentCc
				&& currentLoc == other.currentLoc && extractedMethodCount == other.extractedMethodCount
				&& Objects.equals(name, other.name) && refactoredCc == other.refactoredCc
				&& refactoredLoc == other.refactoredLoc && Objects.equals(undoPlan, other.undoPlan);
	}

	@Override
	public String toString() {
		return "MethodMetrics [name=" + name + ", currentLoc=" + currentLoc + ", refactoredLoc=" + refactoredLoc
				+ ", currentCc=" + currentCc + ", refactoredCc=" + refactoredCc + ", extractedMethodCount="
				+ extractedMethodCount + ", applyPlan=" + applyPlan + ", undoPlan=" + undoPlan + "]";
	}
    
    
}
