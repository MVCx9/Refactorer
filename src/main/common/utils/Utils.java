package main.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

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
    
    public static String formatJava(String source) {
        if (source == null || source.isEmpty()) return source;
        CodeFormatter formatter = ToolFactory.createCodeFormatter(JavaCore.getOptions());
        TextEdit edit = formatter.format(CodeFormatter.K_COMPILATION_UNIT, source, 0, source.length(), 0, System.lineSeparator());
        if (edit == null) return source; // probablemente hay errores de sintaxis; deja como está
        Document doc = new Document(source);
        try {
            edit.apply(doc);
            return doc.get();
        } catch (Exception e) {
            return source;
        }
    }
}
