package refactor;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.ltk.core.refactoring.*;
import org.eclipse.text.edits.TextEdit;
import neo.algorithms.Sequence;

/**
 * Fabrica objetos Change a partir de una Sequence devuelta
 * por el algoritmo original ( AST positions  ).
 */
public final class ChangeBuilder {

    /** Crea un CompositeChange que encapsula el extract-method */
    public static Change buildChangeForSequence(Sequence seq,
                                                ICompilationUnit icu,
                                                CompilationUnit cu) throws JavaModelException {

        /* 1. Preparar ASTRewrite */
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());

        // --- lógica de transformación real (se puede copiar
        //     de ApplyCodeExtractions.java de tu zip) ---
        //     Utiliza seq.getStartPos(), getLength() …

        /* Ejemplo de extracción mínima (stub) */
        MethodDeclaration extracted = /* … construir declaracion … */;
        rewriter.getListRewrite((ASTNode) seq.getParent(), Block.STATEMENTS_PROPERTY)
                .insertLast(extracted, null);

        /* 2. Empaquetar en TextFileChange */
        TextEdit edits = rewriter.rewriteAST();
        TextFileChange tfc = new TextFileChange(
                "Extract method " + seq.getId(), (IFile) icu.getResource());
        tfc.setEdit(edits);
        return tfc;                 // el propio LTK generará el undo
    }
}

