package main.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.handlers.HandlerUtil;

import main.builder.ClassAnalysis;
import main.builder.ProjectFilesAnalyzer;
import main.error.AnalyzeException;
import main.error.ResourceNotFoundException;
import main.model.clazz.ClassAnalysisMetricsMapper;
import main.model.clazz.ClassMetrics;
import main.model.method.MethodMetrics;
import main.session.ActionType;
import main.session.SessionAnalysisStore;
import main.ui.AnalysisMetricsDialog;

public class AnalyzeSingleFileHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object classSelected = selection.getFirstElement();

		if (classSelected == null) {
			throw new ResourceNotFoundException("No hay fichero seleccionado para analizar.");
		}

		IFile file = null;
		ICompilationUnit icu = null;

		if (classSelected instanceof ICompilationUnit) {
			ICompilationUnit unit = (ICompilationUnit) classSelected;
			try {
				file = (IFile) unit.getCorrespondingResource();
				icu = unit;
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}

		if (file == null && classSelected instanceof IAdaptable) {
			file = ((IAdaptable) classSelected).getAdapter(IFile.class);
		}

		ProjectFilesAnalyzer pfa = new ProjectFilesAnalyzer();
		try {
			ClassAnalysis analysis = pfa.analyzeFile(file);
			ClassMetrics cm = ClassAnalysisMetricsMapper.toClassMetrics(analysis);
			SessionAnalysisStore.getInstance().register(ActionType.CLASS, cm);

			// Build sources for comparator
			if (icu == null && analysis.getIcu() != null) {
				icu = analysis.getIcu();
			}
			String leftSource = safeGetSource(icu);
			String rightSource = leftSource;

			try {
				rightSource = applyPlannedEditsInMemory(file, leftSource, cm);
			} catch (Exception ignored) {
				// fallback: keep original content
				rightSource = leftSource;
			}

			new AnalysisMetricsDialog(
					HandlerUtil.getActiveShell(event), 
					ActionType.CLASS, 
					cm, 
					leftSource, 
					rightSource)
				.open();
			
			return null;
		} catch (CoreException e) {
			throw new AnalyzeException("Error analyzing class", e);
		}
	}

	private String applyPlannedEditsInMemory(IFile file, String baseSource, ClassMetrics cm) throws Exception {
		IDocument doc = new Document(baseSource);
		
		if (cm.getRefactoredMethods() == null) {
			return baseSource;
		}
		
		for (MethodMetrics mm : cm.getRefactoredMethods()) {
			if (mm == null || mm.getDoPlan() == null || mm.getDoPlan().changes() == null || mm.getDoPlan().changes().isEmpty()) {
				continue;
			}
			
			for (Change ch : mm.getDoPlan().changes()) {
				applyChangeToDocument(file, doc, ch);
			}
		}
		return doc.get();
	}

	private void applyChangeToDocument(IFile file, IDocument doc, Change change) throws Exception {
		if (change == null) {
			return;
		}
		
		if (change instanceof CompositeChange) {
			for (Change child : ((CompositeChange) change).getChildren()) {
				applyChangeToDocument(file, doc, child);
			}
			
			return;
		}
		
		if (change instanceof TextChange) {
			TextChange tc = (TextChange) change;
			Object element = tc.getModifiedElement();
			IFile target = null;
			
			if (element instanceof IFile) {
				target = (IFile) element;
				
			} else if (element instanceof ICompilationUnit) {
				target = (IFile) ((ICompilationUnit) element).getCorrespondingResource();
			}
			
			if (target != null && target.equals(file)) {
				TextEdit edit = tc.getEdit();
				if (edit != null) {
					edit.copy().apply(doc);
				}
			}
		}
		
		if (change instanceof TextFileChange) {
			TextFileChange tfc = (TextFileChange) change;
			
			if (!file.equals(tfc.getFile())) {
				return; // skip changes for other files
			}
			
			TextEdit edit = tfc.getEdit();
			if (edit != null) {
				edit.copy().apply(doc);
			}
			
			return;
		}
	}

	private String safeGetSource(ICompilationUnit icu) {
		if (icu == null)
			return "";
		try {
			return icu.getSource();
		} catch (JavaModelException e) {
			return "";
		}
	}

}