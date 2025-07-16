package model.project;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import model.clazz.ClassMetrics;
import model.common.ComplexityStats;
import model.common.Identifiable;
import model.common.LocStats;

public class ProjectMetrics implements Identifiable, ComplexityStats, LocStats {

    private final String name;
    private final List<ClassMetrics> classes;
    
    public ProjectMetrics(ProjectMetricsBuilder projectMetricsBuilder) {
		super();
		this.name = projectMetricsBuilder.name;
		this.classes = projectMetricsBuilder.classes;
	}
    
    public static ProjectMetricsBuilder builder() {
    	return new ProjectMetricsBuilder();
    }

	// getters …
    @Override public String getName() { return name; }
    public int getClassCount() { return classes.size(); }

    /**
     * Suma de la complejidad cognitiva actual de todas las clases 
     */
    public int getCurrentLoc() { 
    	return classes.stream().mapToInt(ClassMetrics::getCurrentLoc).sum(); 
    }
    
    /**
     * Suma de la complejidad cognitiva tras refactorizar de todas las clases 
     */
    public int getRefactoredLoc() { 
    	return classes.stream().mapToInt(ClassMetrics::getRefactoredLoc).sum(); 
    }

    /**
     * Media de la suma de la complejidad cognitiva actual de todas las clases 
     */
    public int getCurrentCc() {
    	return averageCc(ClassMetrics::getCurrentCc);
    }
    
    /**
     * Media de la suma de la complejidad cognitiva tras refactorizar de todas las clases 
     */
    public int getRefactoredCc() {
    	return averageCc(ClassMetrics::getRefactoredCc);
    }

    /**
     * Cantidad de métodos actuales de todas las clases
     */
    public int getCurrentMethodCount() { 
    	return classes.stream().mapToInt(ClassMetrics::getCurrentMethodCount).sum(); 
    }
    
    /**
     * Cantidad de métodos tras refactorizar de todas las clases
     */
    public int getRefactoredMethodCount() { 
    	return classes.stream().mapToInt(ClassMetrics::getRefactoredMethodCount).sum(); 
    }

    // Función para calcular la media dado un mapper
    private int averageCc(java.util.function.ToIntFunction<ClassMetrics> mapper) {
        return (int) Math.round(classes.stream().mapToInt(mapper).average().orElse(0.0));
    }
    
    public static class ProjectMetricsBuilder {
    	private String name = "<unnamed>";
        private List<ClassMetrics> classes = Collections.emptyList();
        
        public ProjectMetricsBuilder() {}
        
        public ProjectMetricsBuilder name(String name) {
        	this.name = name;
        	return this;
        }
        
        public ProjectMetricsBuilder classes(List<ClassMetrics> classes) {
        	this.classes = classes;
        	return this;
        }
        
        public ProjectMetrics build() {
        	return new ProjectMetrics(this);
        }
    }

	@Override
	public int hashCode() {
		return Objects.hash(classes, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectMetrics other = (ProjectMetrics) obj;
		return Objects.equals(classes, other.classes) && Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return "ProjectMetrics [name=" + name + ", classes=" + classes + "]";
	}
    
}
