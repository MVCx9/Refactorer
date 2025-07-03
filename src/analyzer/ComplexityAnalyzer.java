package analyzer;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ComplexityAnalyzer {

    public static void analyze(String sourceCode) {
    	ASTParser parser = ASTParser.newParser(AST.JLS21);
        parser.setSource(sourceCode.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                String methodName = node.getName().toString();
                int complexity = calculateCognitiveComplexity(node);
                System.out.println("///* Método: " + methodName + " | Complejidad: " + complexity);
                return super.visit(node);
            }
        });
    }
    
    private static int calculateCognitiveComplexity(MethodDeclaration method) {
        CognitiveComplexityVisitor visitor = new CognitiveComplexityVisitor();
        method.accept(visitor);
        return visitor.getComplexity();
    }
}

