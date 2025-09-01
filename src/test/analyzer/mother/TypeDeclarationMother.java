package test.analyzer.mother;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public final class TypeDeclarationMother {
    private TypeDeclarationMother() {}

    public static TypeDeclaration withMethods(final String name, final MethodDeclaration... methods) {
        final AST ast = AST.newAST(AST.JLS17);
        final TypeDeclaration td = ast.newTypeDeclaration();
        final SimpleName sn = ast.newSimpleName(name);
        td.setName(sn);
        td.setInterface(false);
        td.setMethods(methods);
        return td;
    }
}
