package main.common.languaje;

import main.preferences.PluginPreferences;

public final class Messages {
	
	private Messages() {}
	
	private static boolean isEnglish() {
		return PluginPreferences.isEnglish();
	}
	
	// ==================== DIÁLOGOS Y TÍTULOS ====================
	
	public static String getDialogTitleClass() {
		return isEnglish() 
			? "Cognitive Complexity Analysis and Planning for a class"
			: "Análisis y planificación de complejidad cognitiva de una clase";
	}
	
	public static String getDialogTitleProject() {
		return isEnglish()
			? "Cognitive Complexity Analysis and Planning for a project"
			: "Análisis y planificación de complejidad cognitiva de un proyecto";
	}
	
	public static String getDialogTitleWorkspace() {
		return isEnglish()
			? "Cognitive Complexity Analysis and Planning for the workspace"
			: "Análisis y planificación de complejidad cognitiva del workspace";
	}
	
	public static String getDialogMessageClass() {
		return isEnglish()
			? "of a class"
			: "de una clase";
	}
	
	public static String getDialogMessageProject() {
		return isEnglish()
			? "of a project"
			: "de un proyecto";
	}
	
	public static String getDialogMessageWorkspace() {
		return isEnglish()
			? "of the workspace"
			: "del workspace";
	}
	
	public static String getAnalyzedOn() {
		return isEnglish()
			? "Analyzed on"
			: "Analizado el";
	}
	
	// ==================== BOTONES ====================
	
	public static String getButtonClose() {
		return isEnglish() ? "Close" : "Cerrar";
	}
	
	public static String getButtonApplyExtractions() {
		return isEnglish()
			? "Apply code extractions"
			: "Aplicar extracciones de código";
	}
	
	public static String getButtonUndoExtractions() {
		return isEnglish()
			? "Undo code extractions"
			: "Deshacer extracciones de código";
	}
	
	public static String getButtonExportCSV() {
		return isEnglish()
			? "Export to CSV file"
			: "Exportar a archivo CSV";
	}
	
	// ==================== SECCIONES DE CÓDIGO ====================
	
	public static String getCodeSectionCurrent() {
		return isEnglish() ? "Current" : "Actual";
	}
	
	public static String getCodeSectionRefactored() {
		return isEnglish() ? "Refactored" : "Refactorizado";
	}
	
	// ==================== LEYENDA DE DIFF ====================
	
	public static String getLegendDeleted() {
		return isEnglish() ? "Deleted" : "Eliminado";
	}
	
	public static String getLegendAdded() {
		return isEnglish() ? "Added" : "Añadido";
	}
	
	public static String getLegendModified() {
		return isEnglish() ? "Modified" : "Modificado";
	}
	
	// ==================== MÉTRICAS ====================
	
	public static String getMetricLOC() {
		return isEnglish()
			? "Lines of code (LOC)"
			: "Líneas de código (LOC)";
	}
	
	public static String getMetricCC() {
		return isEnglish()
			? "Cognitive complexity (CC)"
			: "Complejidad cognitiva (CC)";
	}
	
	public static String getMetricMethods() {
		return isEnglish() ? "Methods" : "Métodos";
	}
	
	public static String getMetricClasses() {
		return isEnglish() ? "Classes" : "Clases";
	}
	
	public static String getMetricProjects() {
		return isEnglish() ? "Projects" : "Proyectos";
	}
	
	public static String getMetricAverageLOCPerMethod() {
		return isEnglish()
			? "Average LOC per method"
			: "Media LOC por método";
	}
	
	public static String getMetricAverageCCPerMethod() {
		return isEnglish()
			? "Average CC per method"
			: "Media CC por método";
	}
	
	public static String getMetricAverageMethodsPerClass() {
		return isEnglish()
			? "Average methods per class"
			: "Media métodos por clase";
	}
	
	public static String getMetricAverageLOCPerClass() {
		return isEnglish()
			? "Average LOC per class"
			: "Media LOC por clase";
	}
	
	public static String getMetricAverageCCPerClass() {
		return isEnglish()
			? "Average CC per class"
			: "Media CC por clase";
	}
	
	public static String getMetricAverageMethodsPerProject() {
		return isEnglish()
			? "Average methods per project"
			: "Media métodos por proyecto";
	}
	
	public static String getMetricAverageLOCPerProject() {
		return isEnglish()
			? "Average LOC per project"
			: "Media LOC por proyecto";
	}
	
	public static String getMetricAverageCCPerProject() {
		return isEnglish()
			? "Average CC per project"
			: "Media CC por proyecto";
	}
	
	public static String getMetricExtractedMethods() {
		return isEnglish()
			? "Extracted methods"
			: "Métodos extraídos";
	}
	
	public static String getMetricComplexityThreshold() {
		return isEnglish()
			? "Complexity threshold"
			: "Umbral de Complejidad";
	}
	
	// ==================== TABLA DE REFACTORIZACIONES ====================
	
	public static String getTableTitleRefactoredMethods() {
		return isEnglish()
			? "Details of refactored methods"
			: "Detalle de métodos refactorizados";
	}
	
	public static String getTableSummaryClassesAffected() {
		return isEnglish()
			? "Classes affected:"
			: "Clases afectadas:";
	}
	
	public static String getTableSummaryMethodsAffected() {
		return isEnglish()
			? "Original methods with refactor:"
			: "Métodos originales con refactor:";
	}
	
	// Columnas de tabla
	public static String getTableColumnNumber() {
		return isEnglish() ? "No." : "Nº";
	}
	
	public static String getTableColumnProject() {
		return isEnglish() ? "Project" : "Proyecto";
	}
	
	public static String getTableColumnThreshold() {
		return isEnglish() ? "Threshold" : "Umbral";
	}
	
	public static String getTableColumnClass() {
		return isEnglish() ? "Class" : "Clase";
	}
	
	public static String getTableColumnOriginalMethod() {
		return isEnglish() ? "Original method" : "Método original";
	}
	
	public static String getTableColumnOriginalCC() {
		return isEnglish()
			? "Original method CC"
			: "CC método original";
	}
	
	public static String getTableColumnRefactoredMethod() {
		return isEnglish()
			? "Refactored method"
			: "Método refactorizado";
	}
	
	public static String getTableColumnRefactoredCC() {
		return isEnglish()
			? "Refactored method CC"
			: "CC método refactorizado";
	}
	
	public static String getTableColumnAlgorithm() {
		return isEnglish() ? "Algorithm" : "Algoritmo";
	}
	
	// ==================== EXPORTACIÓN CSV ====================
	
	public static String getCSVExportDialogTitle() {
		return isEnglish()
			? "Export table to CSV"
			: "Exportar tabla a CSV";
	}
	
	public static String getCSVFilterName() {
		return isEnglish()
			? "CSV (comma separated)"
			: "CSV (separado por comas)";
	}
	
	public static String getCSVExportError() {
		return isEnglish()
			? "Could not export file:"
			: "No se pudo exportar el archivo:";
	}
	
	public static String getCSVExportSuccess() {
		return isEnglish()
			? "Export completed"
			: "Exportación completada";
	}
	
	public static String getCSVExportSuccessMessage(String path) {
		return isEnglish()
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
	
	public static String getErrorTitle() {
		return "Error";
	}
	
	public static String getErrorApplyingExtractions() {
		return isEnglish()
			? "Error applying code extractions"
			: "Error aplicando extracciones de código";
	}
	
	public static String getErrorRevertingExtractions() {
		return isEnglish()
			? "Error reverting code extractions"
			: "Error revirtiendo extracciones de código";
	}
	
	// ==================== DIÁLOGO SIN REFACTORIZACIONES ====================
	
	public static String getNoRefactorTitle() {
		return isEnglish()
			? "No refactorings found"
			: "No se encontraron refactorizaciones";
	}
	
	public static String getNoRefactorMessageClass(String className, int threshold) {
		return isEnglish()
			? "The class '" + className + "' does not have methods with cognitive complexity above the threshold (" + threshold + ")."
			: "La clase '" + className + "' no tiene métodos con complejidad cognitiva superior al umbral (" + threshold + ").";
	}
	
	public static String getNoRefactorMessageProject(String projectName, int threshold) {
		return isEnglish()
			? "The project '" + projectName + "' does not have methods with cognitive complexity above the threshold (" + threshold + ")."
			: "El proyecto '" + projectName + "' no tiene métodos con complejidad cognitiva superior al umbral (" + threshold + ").";
	}
	
	public static String getNoRefactorMessageWorkspace(int threshold) {
		return isEnglish()
			? "The workspace does not have methods with cognitive complexity above the threshold (" + threshold + ")."
			: "El workspace no tiene métodos con complejidad cognitiva superior al umbral (" + threshold + ").";
	}
	
	// ==================== HISTORIAL ====================
	
	public static String getHistoryTitle() {
		return isEnglish()
			? "Previous Cognitive Complexity Analyses"
			: "Análisis de Complejidad Cognitiva anteriores";
	}
	
	public static String getHistoryEmptyMessage() {
		return isEnglish()
			? "No cognitive complexity analyses found"
			: "No hay análisis de complejidad cognitiva anteriores";
	}
	
	public static String getHistoryColumnDate() {
		return isEnglish() ? "Date" : "Fecha";
	}
	
	public static String getHistoryColumnType() {
		return isEnglish() ? "Type" : "Tipo";
	}
	
	public static String getHistoryColumnName() {
		return isEnglish() ? "Name" : "Nombre";
	}
	
	public static String getHistoryColumnActions() {
		return isEnglish() ? "Actions" : "Acciones";
	}
	
	public static String getHistoryTypeClass() {
		return isEnglish() ? "Class" : "Clase";
	}
	
	public static String getHistoryTypeProject() {
		return isEnglish() ? "Project" : "Proyecto";
	}
	
	public static String getHistoryTypeWorkspace() {
		return "Workspace";
	}
	
	public static String getHistoryButtonView() {
		return isEnglish() ? "View Analysis" : "Ver Análisis";
	}
	
	// ==================== ERRORES DETALLADOS ====================
	
	public static String getErrorDetailsTitle() {
		return isEnglish()
			? "Error Details"
			: "Detalles del Error";
	}
	
	public static String getErrorDetailsMessage() {
		return isEnglish()
			? "An error occurred during the analysis:"
			: "Ocurrió un error durante el análisis:";
	}
	
	// ==================== ANÁLISIS EN PROGRESO ====================
	
	public static String getAnalyzingClass() {
		return isEnglish()
			? "Analyzing class..."
			: "Analizando clase...";
	}
	
	public static String getAnalyzingProject() {
		return isEnglish()
			? "Analyzing project..."
			: "Analizando proyecto...";
	}
	
	public static String getAnalyzingWorkspace() {
		return isEnglish()
			? "Analyzing workspace..."
			: "Analizando workspace...";
	}
	
	// ==================== DIÁLOGO SIN REFACTORIZACIONES - EXPLICACIÓN ====================
	
	public static String getNoRefactorUnsupportedElement() {
		return isEnglish() 
			? "Unsupported element" 
			: "Elemento no soportado";
	}
	
	public static String getNoRefactorUnsupportedExplanation() {
		return isEnglish() 
			? "The selected element is not a Java class processable by the plugin. It may be a configuration file, enum, interface or record, which are ignored for extraction suggestions."
			: "El elemento seleccionado no es una clase Java procesable por el plugin. Puede tratarse de un fichero de configuración, un enum, una interface o un record, los cuales se ignoran para sugerencias de extracción.";
	}
	
	public static String getNoRefactorExplanation(boolean includeClassThreshold) {
		String base = isEnglish()
			? "The analysis did not identify method extraction opportunities that would reduce cognitive complexity or lines of code without introducing duplication or loss of readability."
			: "El análisis no ha identificado oportunidades de extracción de métodos que reduzcan la complejidad cognitiva o las líneas de código sin introducir duplicidad o pérdida de legibilidad.";
		
		String reasons = isEnglish()
			? "\n\nPossible basic reasons:" +
				"\n • Existing methods are already sufficiently small." +
				"\n • Cognitive complexity is distributed and there are no concentrated blocks that would benefit from separation." +
				"\n • Possible extractions would create trivial methods (e.g. 1-2 lines) that do not provide clarity." +
				"\n • The code mainly contains getters/setters or simple linear operations." +
				"\n • Extraction would generate duplication or break semantic cohesion." +
				(includeClassThreshold ? "" : "\n • Analyzed classes are already at an acceptable complexity threshold.")
			: "\n\nPosibles razones básicas:" +
				"\n • Los métodos existentes ya son suficientemente pequeños." +
				"\n • La complejidad cognitiva está distribuida y no hay bloques concentrados que se beneficien de separar." +
				"\n • Las posibles extracciones crearían métodos triviales (p.ej. 1-2 líneas) que no aportan claridad." +
				"\n • El código contiene sobre todo getters/setters u operaciones simples lineales." +
				"\n • La extracción generaría duplicación o rompería la cohesión semántica." +
				(includeClassThreshold ? "" : "\n • Las clases analizadas ya están en un umbral aceptable de complejidad.");
		
		return base + reasons;
	}
	
	// ==================== MÉTRICAS DIÁLOGO SIN REFACTOR ====================
	
	public static String getMetricTotalMethods() {
		return isEnglish() ? "Methods" : "Métodos";
	}
	
	public static String getMetricTotalLOC() {
		return isEnglish() ? "Total LOC" : "LOC totales";
	}
	
	public static String getMetricTotalCC() {
		return isEnglish() ? "Total CC" : "CC total";
	}
	
	public static String getMetricAverageLOCMethod() {
		return isEnglish() ? "Average LOC / method" : "Media LOC / método";
	}
	
	public static String getMetricAverageCCMethod() {
		return isEnglish() ? "Average CC / method" : "Media CC / método";
	}
	
	public static String getMetricAverageLOCClass() {
		return isEnglish() ? "Average LOC / class" : "Media LOC / clase";
	}
	
	public static String getMetricAverageCCClass() {
		return isEnglish() ? "Average CC / class" : "Media CC / clase";
	}
	
	public static String getMetricAverageMethodsClass() {
		return isEnglish() ? "Average methods / class" : "Media métodos / clase";
	}
	
	public static String getMetricAverageLOCProject() {
		return isEnglish() ? "Average LOC / project" : "Media LOC / proyecto";
	}
	
	public static String getMetricAverageCCProject() {
		return isEnglish() ? "Average CC / project" : "Media CC / proyecto";
	}
	
	public static String getMetricAverageMethodsProject() {
		return isEnglish() ? "Average methods / project" : "Media métodos / proyecto";
	}
	
	public static String getMetricCCAvgProjects() {
		return isEnglish() ? "CC (avg projects)" : "CC (media proyectos)";
	}
}
