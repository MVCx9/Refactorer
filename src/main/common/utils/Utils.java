package main.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.InlineMethodDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.TextEdit;

import main.common.error.AnalyzeException;

public class Utils {

	public static <T> List<T> asImmutable(List<T> list) {
		return list == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(list));
	}

	public static <T> List<T> reverse(List<T> list) {
		List<T> copy = new ArrayList<>(list);
		Collections.reverse(copy);
		return Collections.unmodifiableList(copy);
	}
	
	public static CompilationUnit parserAST(ICompilationUnit icu) {
		ASTParser parser = ASTParser.newParser(AST.JLS21);
		parser.setSource(icu);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		IJavaProject jp = icu.getJavaProject();
		if (jp != null) {
			parser.setProject(jp);
		}
		try {
			return (CompilationUnit) parser.createAST(new NullProgressMonitor());
		} catch (IllegalStateException ex) {
			ASTParser fallback = ASTParser.newParser(AST.JLS21);
			fallback.setSource(icu);
			fallback.setKind(ASTParser.K_COMPILATION_UNIT);
			fallback.setResolveBindings(false);
			fallback.setBindingsRecovery(false);
			return (CompilationUnit) fallback.createAST(new NullProgressMonitor());
		}
	}
	
	public static String applyPlanOnWorkingCopy(ICompilationUnit wc, Change plan, IProgressMonitor pm) throws AnalyzeException {
        if (pm == null) pm = new NullProgressMonitor();
        try {
            if (plan == null) return wc.getSource();

            RefactoringContribution contribution = RefactoringCore.getRefactoringContribution(IJavaRefactorings.INLINE_METHOD);
            InlineMethodDescriptor descriptor = (InlineMethodDescriptor) contribution.createDescriptor();

            plan.initializeValidationData(pm);
            Refactoring refactoring = descriptor.createRefactoring(new RefactoringStatus());
            RefactoringStatus status = refactoring.checkAllConditions(pm); 
            
            if(status.isOK()) {
            	plan.perform(pm);
            }

            refactoring.checkInitialConditions(pm);

            RefactoringStatus st = plan.isValid(pm);
            if (st.hasFatalError()) {
                throw new AnalyzeException("Plan inválido: " + st.getMessageMatchingSeverity(st.getSeverity()));
            }
            
            // Documento desde el estado REAL del working copy
            IDocument doc = new org.eclipse.jface.text.Document(wc.getBuffer().getContents());

            IFile file = (IFile) wc.getResource();
            List<TextEdit> rootEdits = new ArrayList<>();
            collectEditsForFile(plan, file, rootEdits);

            // Aplica cada árbol de edición (plan por plan) sobre el MISMO documento
            for (TextEdit root : rootEdits) {
                // ¡Importante! aplicar el ÁRBOL completo del plan (MoveSource/MoveTarget juntos)
                root.apply(doc);
            }

            // Vuelca al buffer y reconcilia el modelo JDT
            wc.getBuffer().setContents(doc.get());
            wc.reconcile(ICompilationUnit.NO_AST, true, null, pm);

            return wc.getSource();
        } catch (Exception e) {
            throw new AnalyzeException("Error applying planned edits in memory for file: " + wc.getElementName(), e);
        }
    }

    /** Extrae recursivamente los TextEdit raíz que afectan a 'file'. */
    private static void collectEditsForFile(Change change, IFile file, List<TextEdit> out) {
        if (change == null) return;

        if (change instanceof CompositeChange comp) {
            for (Change c : comp.getChildren()) {
                collectEditsForFile(c, file, out);
            }
            return;
        }

        if (change instanceof TextFileChange tfc) {
            if (file != null && file.equals(tfc.getFile())) {
                TextEdit edit = tfc.getEdit();
                if (edit != null) out.add(edit);
            }
            return;
        }

        if (change instanceof TextChange tc) {
            TextEdit edit = tc.getEdit();
            if (edit != null) out.add(edit);
        }
    }
    
    public static String safeGetSource(ICompilationUnit icu) {
		if (icu == null)
			return "";
		try {
			return icu.getSource();
		} catch (JavaModelException e) {
			return "";
		}
	}
}
