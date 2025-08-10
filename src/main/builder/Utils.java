package main.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class Utils {


	private Utils() { }

    public static CompilationUnit parse(IFile file) throws CoreException {
        ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);
        ASTParser parser = ASTParser.newParser(AST.JLS21);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setSource(icu);
        parser.setUnitName(file.getName());
        parser.setCompilerOptions(JavaCore.getOptions());
        return (CompilationUnit) parser.createAST(null);
    }

    /** LOC (líneas de código) de un método, contando sólo líneas no vacías. */
    public static int loc(MethodDeclaration md) {
        CompilationUnit cu = (CompilationUnit) md.getRoot();
        int startLine = cu.getLineNumber(md.getStartPosition());
        int endLine   = cu.getLineNumber(md.getStartPosition() + md.getLength());
        return endLine - startLine + 1;
    }
}
