package test.refactor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;

import main.refactor.CodeExtractionEngine;

class CodeExtractionEngineTest {

    @Test
    void given_nullNode_when_analyseAndPlan_should_returnEmpty() throws Exception {
        assertTrue(CodeExtractionEngine.analyseAndPlan(stubCompilationUnit(), null, null, 100, 10).isEmpty());
    }

    @Test
    void given_nullCompilationUnit_when_analyseAndPlan_should_returnEmpty() throws Exception {
        assertTrue(CodeExtractionEngine.analyseAndPlan(null, null, stubMethod(), 100, 10).isEmpty());
    }

    @Test
    void given_complexityBelowThreshold_when_analyseAndPlan_should_returnEmpty() throws Exception {
        assertTrue(CodeExtractionEngine.analyseAndPlan(stubCompilationUnit(), null, stubMethod(), 5, 10).isEmpty());
    }

    @Test
    void given_complexityEqualsThreshold_when_analyseAndPlan_should_returnEmpty() throws Exception {
        assertTrue(CodeExtractionEngine.analyseAndPlan(stubCompilationUnit(), null, stubMethod(), 10, 10).isEmpty());
    }

    private CompilationUnit stubCompilationUnit() {
        return org.eclipse.jdt.core.dom.AST.newAST(org.eclipse.jdt.core.dom.AST.JLS21, false).newCompilationUnit();
    }

    private MethodDeclaration stubMethod() {
        return org.eclipse.jdt.core.dom.AST.newAST(org.eclipse.jdt.core.dom.AST.JLS21, false).newMethodDeclaration();
    }
}
