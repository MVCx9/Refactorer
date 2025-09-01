package test.analyzer.mother;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

public final class MethodDeclarationMother {
    private MethodDeclarationMother() {}

    public static MethodDeclaration simple(final String name, final int start, final int length) {
        final AST ast = AST.newAST(AST.JLS17);
        final MethodDeclaration md = ast.newMethodDeclaration();
        final SimpleName sn = ast.newSimpleName(name);
        md.setName(sn);
        // Fijar posiciones simuladas mediante API interna (no expuesta) -> omitimos.
        return md;
    }
}
