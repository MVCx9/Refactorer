package refactor;

import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.ltk.core.refactoring.*;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import neo.algorithms.Sequence;

/**
 * Fabrica objetos {@link Change} (LTK) a partir de las posiciones AST
 * recogidas en una {@link Sequence} devuelta por el motor de búsqueda.
 *
 * <p>  Flujo:  <pre>
 *   Sequence  →  ASTRewrite + TextEdit  →  TextFileChange
 *           ↘                                   ↘
 *            (método extraído)                   CompositeChange
 * </pre>
 */
public final class ChangeBuilder {
	
    private ChangeBuilder() {}

    /**
     * Devuelve un {@link CompositeChange} que incluye:
     * <ul>
     *   <li>La inserción del nuevo método extraído.</li>
     *   <li>La sustitución del código original por la llamada al nuevo método.</li>
     * </ul>
     *
     * @param seq  secuencia con posiciones (start/len) a extraer
     * @param icu  unidad de compilación JDT
     * @param cu   AST parseado de la misma unidad
     */
    public static CompositeChange buildChangeForSequence(Sequence seq, ICompilationUnit icu, CompilationUnit cu) throws JavaModelException, CoreException {

        /* ---------- 1. Localizar nodos implicados ---------- */
        AST ast = cu.getAST();
        ASTRewrite rw = ASTRewrite.create(ast);

        // Cogemos el primer y el último vértice de la secuencia para calcular el lenght
        int offset = seq.getSiblingNodes().get(0).getStartPosition();
        int end = seq.getSiblingNodes().get(seq.getSiblingNodes().size()-1).getStartPosition() + seq.getSiblingNodes().get(seq.getSiblingNodes().size()-1).getLength();
        int lenght = end - offset;
        
        // Fragmento a extraer
        ASTNode extractedNode = NodeFinder.perform(cu, offset, lenght);

        /* (a) Crear declaración del nuevo método */
        MethodDeclaration newMethod = createExtractedMethod(ast, extractedNode, seq, icu);

        /* (b) Añadirlo al final de la clase contenedora */
        TypeDeclaration typeDecl = enclosingType(extractedNode);
        ListRewrite bodyRewrite =
                rw.getListRewrite(typeDecl, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        bodyRewrite.insertLast(newMethod, null);

        /* (c) Reemplazar el código original por llamada */
        Statement callSite = createInvocationStatement(ast, newMethod, extractedNode);
        rw.replace(extractedNode, callSite, null);

        /* ---------- 2. Empaquetar en TextFileChange ---------- */
        TextEdit rootEdit = new MultiTextEdit();
        rootEdit.addChild(rw.rewriteAST());

        IFile file = (IFile) icu.getResource();
        TextFileChange fileChange =
                new TextFileChange("Extract «" + newMethod.getName().getIdentifier() + "»", file);
        fileChange.setEdit(rootEdit);
        fileChange.setTextType("java");

        /* ---------- 3. Composite, por si necesitáramos + cambios ---------- */
        CompositeChange composite =
                new CompositeChange("Extract Method – " + icu.getElementName());
        composite.add(fileChange);

        return composite;
    }
    
    /** Devuelve la declaración de clase más cercana. */
    private static TypeDeclaration enclosingType(ASTNode n) {
        while (n != null && !(n instanceof TypeDeclaration)) {
            n = n.getParent();
        }
        return (TypeDeclaration) Objects.requireNonNull(n,
                "TypeDeclaration no encontrado.");
    }

    /** Genera la declaración del método extraído. */
    private static MethodDeclaration createExtractedMethod(AST ast, ASTNode extracted, Sequence seq, ICompilationUnit icu) {

    	// ---- parámetros & return (simplificado) ----
        //   * identificamos variables externas al fragmento;
        //   * si la secuencia produce una expresión → return <tipo expr>;
        //   * si no, void.
    	
        MethodDeclaration m = ast.newMethodDeclaration();
        String methodName = autoMethodName(extracted, seq.getCompilationUnit());
        m.setName(ast.newSimpleName(methodName));
        m.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
        m.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

        // copiamos el fragmento original        
        Block body = ast.newBlock();
        ASTNode copied = ASTNode.copySubtree(ast, extracted);
        if (copied instanceof Statement stmt) {
            body.statements().add(stmt);
        } else if (copied instanceof Expression expr) {
            body.statements().add(ast.newExpressionStatement(expr));
        }
        m.setBody(body);

        return m;
    }

    /** Construye la llamada al nuevo método para reemplazar el código extraído. */
    private static Statement createInvocationStatement(AST ast, MethodDeclaration newMethod, ASTNode extracted) {
        MethodInvocation invoke = ast.newMethodInvocation();
        invoke.setName(ast.newSimpleName(
                newMethod.getName().getIdentifier()));

        if (extracted instanceof Expression)
            return ast.newExpressionStatement(invoke); 
        else
            return invokeToStatement(ast, invoke);
    }

    private static Statement invokeToStatement(AST ast, MethodInvocation inv) {
        return ast.newExpressionStatement(inv);
    }
    
    private static String autoMethodName(ASTNode fragment, CompilationUnit cu) {
        int line = cu.getLineNumber(fragment.getStartPosition());
        return "extractedFragment_L" + line;
    }


}

