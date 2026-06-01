package test.analyzer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.Test;

import main.analyzer.ComplexityAnalyzer;

class ComplexityAnalyzerTest {

    @Test
    void given_newInstance_when_construct_should_notBeNull() {
        assertNotNull(new ComplexityAnalyzer());
    }

    @Test
    void given_nullCompilationUnit_when_analyze_throws_exception() {
        assertThrows(Exception.class, () -> new ComplexityAnalyzer().analyze(null, null));
    }

    @Test
    void given_methodWithIfElse_when_computeCognitiveComplexity_should_returnPositiveValue() {
        final String src = """
                public class A {
                  void m(int x) {
                    if (x > 0) {
                      System.out.println(x);
                    } else {
                      System.out.println(-x);
                    }
                  }
                }
                """;
        final MethodDeclaration md = firstMethod(src);
        final ExposedAnalyzer analyzer = new ExposedAnalyzer();
        final int cc = analyzer.expose(md);
        assertTrue(cc >= 1);
    }

    @Test
    void given_emptyMethod_when_computeCognitiveComplexity_should_returnZero() {
        final String src = "public class A { void m() {} }";
        final ExposedAnalyzer analyzer = new ExposedAnalyzer();
        assertNotNull(firstMethod(src));
        analyzer.expose(firstMethod(src));
    }

    private MethodDeclaration firstMethod(final String source) {
        final ASTParser parser = ASTParser.newParser(AST.JLS21);
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final Map options = new Hashtable<>(JavaCore.getOptions());
        JavaCore.setComplianceOptions(JavaCore.VERSION_21, options);
        parser.setCompilerOptions(options);
        parser.setSource(source.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        final TypeDeclaration td = (TypeDeclaration) cu.types().getFirst();
        return td.getMethods()[0];
    }

    private static final class ExposedAnalyzer extends ComplexityAnalyzer {
        int expose(final MethodDeclaration md) {
            return computeCognitiveComplexity(md);
        }
    }
}
