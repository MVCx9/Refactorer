package test.analyzer.mother;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public final class CompilationUnitMother {
    private CompilationUnitMother() {}

    public static CompilationUnit withSingleType(final TypeDeclaration type, final MethodDeclaration method, final int startLine, final int endLine) {
        final AST ast = AST.newAST(AST.JLS17);
        final CompilationUnit cu = ast.newCompilationUnit();
        cu.types().add(type);
        return cu;
    }

    public static CompilationUnit withTwoTypes(final TypeDeclaration first, final TypeDeclaration second, final MethodDeclaration m1, final MethodDeclaration m2,
                                               final int l1Start, final int l1End, final int l2Start, final int l2End) {
        final AST ast = AST.newAST(AST.JLS17);
        final CompilationUnit cu = ast.newCompilationUnit();
        cu.types().add(first);
        cu.types().add(second);
        return cu;
    }
}
