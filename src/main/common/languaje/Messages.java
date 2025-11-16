package main.common.languaje;

import org.eclipse.core.resources.IProject;
import main.preferences.ProjectPreferences;

/**
 * Clase utilitaria para gestionar mensajes internacionalizados.
 * Soporta Castellano e Inglés.
 */
public final class Messages {
	
	private Messages() {}
	
	private static boolean isEnglish(IProject project) {
		if (project == null) {
			return false; // Default a Castellano
		}
		String language = ProjectPreferences.getPluginLanguage(project);
		return "English".equals(language);
	}
	
	// ==================== DIÁLOGOS Y TÍTULOS ====================
	
	public static String getDialogTitleClass(IProject project) {
		return isEnglish(project) 
			? "Cognitive Complexity Analysis and Planning for a class"
			: "Análisis y planificación de complejidad cognitiva de una clase";
	}
	
	public static String getDialogTitleProject(IProject project) {
		return isEnglish(project)
			? "Cognitive Complexity Analysis and Planning for a project"
			: "Análisis y planificación de complejidad cognitiva de un proyecto";
	}
	
	public static String getDialogTitleWorkspace() {
		return "Cognitive Complexity Analysis and Planning for the workspace";
	}
	
	public static String getDialogMessageClass(IProject project) {
		return isEnglish(project)
			? "of a class"
			: "de una clase";
	}
	
	public static String getDialogMessageProject(IProject project) {
		return isEnglish(project)
			? "of a project"
			: "de un proyecto";
	}
	
	public static String getDialogMessageWorkspace() {
		return "of the workspace";
	}
	
	public static String getAnalyzedOn(IProject project) {
		return isEnglish(project)
			? "Analyzed on"
			: "Analizado el";
	}
	
	// ==================== BOTONES ====================
	
	public static String getButtonClose(IProject project) {
		return isEnglish(project) ? "Close" : "Cerrar";
	}
	
	public static String getButtonApplyExtractions(IProject project) {
		return isEnglish(project)
			? "Apply code extractions"
			: "Aplicar extracciones de código";
	}
	
	public static String getButtonUndoExtractions(IProject project) {
		return isEnglish(project)
			? "Undo code extractions"
			: "Deshacer extracciones de código";
	}
	
	public static String getButtonExportCSV(IProject project) {
		return isEnglish(project)
			? "Export to CSV file"
			: "Exportar a archivo CSV";
	}
	
	// ==================== SECCIONES DE CÓDIGO ====================
	
	public static String getCodeSectionCurrent(IProject project) {
		return isEnglish(project) ? "Current" : "Actual";
	}
	
	public static String getCodeSectionRefactored(IProject project) {
		return isEnglish(project) ? "Refactored" : "Refactorizado";
	}
	
	// ==================== LEYENDA DE DIFF ====================
	
	public static String getLegendDeleted(IProject project) {
		return isEnglish(project) ? "Deleted" : "Eliminado";
	}
	
	public static String getLegendAdded(IProject project) {
		return isEnglish(project) ? "Added" : "Añadido";
	}
	
	public static String getLegendModified(IProject project) {
		return isEnglish(project) ? "Modified" : "Modificado";
	}
	
	// ==================== MÉTRICAS ====================
	
	public static String getMetricLOC(IProject project) {
		return isEnglish(project)
			? "Lines of code (LOC)"
			: "Líneas de código (LOC)";
	}
	
	public static String getMetricCC(IProject project) {
		return isEnglish(project)
			? "Cognitive complexity (CC)"
			: "Complejidad cognitiva (CC)";
	}
	
	public static String getMetricMethods(IProject project) {
		return isEnglish(project) ? "Methods" : "Métodos";
	}
	
	public static String getMetricClasses(IProject project) {
		return isEnglish(project) ? "Classes" : "Clases";
	}
	
	public static String getMetricProjects(IProject project) {
		return isEnglish(project) ? "Projects" : "Proyectos";
	}
	
	public static String getMetricAverageLOCPerMethod(IProject project) {
		return isEnglish(project)
			? "Average LOC per method"
			: "Media LOC por método";
	}
	
	public static String getMetricAverageCCPerMethod(IProject project) {
		return isEnglish(project)
			? "Average CC per method"
			: "Media CC por método";
	}
	
	public static String getMetricAverageMethodsPerClass(IProject project) {
		return isEnglish(project)
			? "Average methods per class"
			: "Media métodos por clase";
	}
	
	public static String getMetricAverageLOCPerClass(IProject project) {
		return isEnglish(project)
			? "Average LOC per class"
			: "Media LOC por clase";
	}
	
	public static String getMetricAverageCCPerClass(IProject project) {
		return isEnglish(project)
			? "Average CC per class"
			: "Media CC por clase";
	}
	
	public static String getMetricAverageMethodsPerProject(IProject project) {
		return isEnglish(project)
			? "Average methods per project"
			: "Media métodos por proyecto";
	}
	
	public static String getMetricAverageLOCPerProject(IProject project) {
		return isEnglish(project)
			? "Average LOC per project"
			: "Media LOC por proyecto";
	}
	
	public static String getMetricAverageCCPerProject(IProject project) {
		return isEnglish(project)
			? "Average CC per project"
			: "Media CC por proyecto";
	}
	
	public static String getMetricExtractedMethods(IProject project) {
		return isEnglish(project)
			? "Extracted methods"
			: "Métodos extraídos";
	}
	
	public static String getMetricComplexityThreshold(IProject project) {
		return isEnglish(project)
			? "Complexity threshold"
			: "Umbral de Complejidad";
	}
	
	// ==================== TABLA DE REFACTORIZACIONES ====================
	
	public static String getTableTitleRefactoredMethods(IProject project) {
		return isEnglish(project)
			? "Details of refactored methods"
			: "Detalle de métodos refactorizados";
	}
	
	public static String getTableSummaryClassesAffected(IProject project) {
		return isEnglish(project)
			? "Classes affected:"
			: "Clases afectadas:";
	}
	
	public static String getTableSummaryMethodsAffected(IProject project) {
		return isEnglish(project)
			? "Original methods with refactor:"
			: "Métodos originales con refactor:";
	}
	
	// Columnas de tabla
	public static String getTableColumnNumber(IProject project) {
		return isEnglish(project) ? "No." : "Nº";
	}
	
	public static String getTableColumnProject(IProject project) {
		return isEnglish(project) ? "Project" : "Proyecto";
	}
	
	public static String getTableColumnThreshold(IProject project) {
		return isEnglish(project) ? "Threshold" : "Umbral";
	}
	
	public static String getTableColumnClass(IProject project) {
		return isEnglish(project) ? "Class" : "Clase";
	}
	
	public static String getTableColumnOriginalMethod(IProject project) {
		return isEnglish(project) ? "Original method" : "Método original";
	}
	
	public static String getTableColumnOriginalCC(IProject project) {
		return isEnglish(project)
			? "Original method CC"
			: "CC método original";
	}
	
	public static String getTableColumnRefactoredMethod(IProject project) {
		return isEnglish(project)
			? "Refactored method"
			: "Método refactorizado";
	}
	
	public static String getTableColumnRefactoredCC(IProject project) {
		return isEnglish(project)
			? "Refactored method CC"
			: "CC método refactorizado";
	}
	
	public static String getTableColumnAlgorithm(IProject project) {
		return isEnglish(project) ? "Algorithm" : "Algoritmo";
	}
	
	// ==================== EXPORTACIÓN CSV ====================
	
	public static String getCSVExportDialogTitle(IProject project) {
		return isEnglish(project)
			? "Export table to CSV"
			: "Exportar tabla a CSV";
	}
	
	public static String getCSVFilterName(IProject project) {
		return isEnglish(project)
			? "CSV (comma separated)"
			: "CSV (separado por comas)";
	}
	
	public static String getCSVExportError(IProject project) {
		return isEnglish(project)
			? "Could not export file:"
			: "No se pudo exportar el archivo:";
	}
	
	public static String getCSVExportSuccess(IProject project) {
		return isEnglish(project)
			? "Export completed"
			: "Exportación completada";
	}
	
	public static String getCSVExportSuccessMessage(IProject project, String path) {
		return isEnglish(project)
			? "CSV file exported to: " + path
			: "Archivo CSV exportado en: " + path;
	}
	
	public static String getCSVFileNameClass() {
		return "Refactorer_class_";
	}
	
	public static String getCSVFileNameProject() {
		return "Refactorer_project_";
	}
	
	public static String getCSVFileNameWorkspace() {
		return "Refactorer_workspace_";
	}
	
	// ==================== MENSAJES DE ERROR ====================
	
	public static String getErrorTitle(IProject project) {
		return isEnglish(project) ? "Error" : "Error";
	}
	
	public static String getErrorApplyingExtractions(IProject project) {
		return isEnglish(project)
			? "Error applying code extractions"
			: "Error aplicando extracciones de código";
	}
	
	public static String getErrorRevertingExtractions(IProject project) {
		return isEnglish(project)
			? "Error reverting code extractions"
			: "Error revirtiendo extracciones de código";
	}
	
	// ==================== DIÁLOGO SIN REFACTORIZACIONES ====================
	
	public static String getNoRefactorTitle(IProject project) {
		return isEnglish(project)
			? "No refactorings found"
			: "No se encontraron refactorizaciones";
	}
	
	public static String getNoRefactorMessageClass(IProject project, String className, int threshold) {
		return isEnglish(project)
			? "The class '" + className + "' does not have methods with cognitive complexity above the threshold (" + threshold + ")."
			: "La clase '" + className + "' no tiene métodos con complejidad cognitiva superior al umbral (" + threshold + ").";
	}
	
	public static String getNoRefactorMessageProject(IProject project, String projectName, int threshold) {
		return isEnglish(project)
			? "The project '" + projectName + "' does not have methods with cognitive complexity above the threshold (" + threshold + ")."
			: "El proyecto '" + projectName + "' no tiene métodos con complejidad cognitiva superior al umbral (" + threshold + ").";
	}
	
	public static String getNoRefactorMessageWorkspace(int threshold) {
		return "The workspace does not have methods with cognitive complexity above the threshold (" + threshold + ").";
	}
	
	// ==================== HISTORIAL ====================
	
	public static String getHistoryTitle(IProject project) {
		return isEnglish(project)
			? "Analysis History"
			: "Análisis anteriores";
	}
	
	public static String getHistoryEmptyMessage(IProject project) {
		return isEnglish(project)
			? "No analysis history available"
			: "No hay historial de análisis disponible";
	}
	
	public static String getHistoryColumnDate(IProject project) {
		return isEnglish(project) ? "Date" : "Fecha";
	}
	
	public static String getHistoryColumnType(IProject project) {
		return isEnglish(project) ? "Type" : "Tipo";
	}
	
	public static String getHistoryColumnTarget(IProject project) {
		return isEnglish(project) ? "Target" : "Objetivo";
	}
	
	public static String getHistoryTypeClass(IProject project) {
		return isEnglish(project) ? "Class" : "Clase";
	}
	
	public static String getHistoryTypeProject(IProject project) {
		return isEnglish(project) ? "Project" : "Proyecto";
	}
	
	public static String getHistoryTypeWorkspace() {
		return "Workspace";
	}
	
	public static String getHistoryButtonView(IProject project) {
		return isEnglish(project) ? "View" : "Ver";
	}
	
	public static String getHistoryButtonClear(IProject project) {
		return isEnglish(project) ? "Clear History" : "Limpiar análisis anteriores";
	}
	
	// ==================== ERRORES DETALLADOS ====================
	
	public static String getErrorDetailsTitle(IProject project) {
		return isEnglish(project)
			? "Error Details"
			: "Detalles del Error";
	}
	
	public static String getErrorDetailsMessage(IProject project) {
		return isEnglish(project)
			? "An error occurred during the analysis:"
			: "Ocurrió un error durante el análisis:";
	}
	
	// ==================== ANÁLISIS EN PROGRESO ====================
	
	public static String getAnalyzingClass(IProject project) {
		return isEnglish(project)
			? "Analyzing class..."
			: "Analizando clase...";
	}
	
	public static String getAnalyzingProject(IProject project) {
		return isEnglish(project)
			? "Analyzing project..."
			: "Analizando proyecto...";
	}
	
	public static String getAnalyzingWorkspace() {
		return "Analyzing workspace...";
	}
}
