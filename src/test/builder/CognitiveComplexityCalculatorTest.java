package test.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.builder.CognitiveComplexityCalculator;

class CognitiveComplexityCalculatorTest {

	@Test
	@DisplayName("given_nullMethod_when_compute_then_returnsZero")
	void given_nullMethod_when_compute_then_returnsZero() {
		assertEquals(0, CognitiveComplexityCalculator.compute(null));
	}

	@Test
	@DisplayName("given_methodWithCondition_when_compute_then_matchesNeoComputation")
	void given_methodWithCondition_when_compute_then_matchesNeoComputation() {
		MethodDeclaration method = firstMethodFrom("class A { void m(){ if(a){ b(); } } }");
		int actual = CognitiveComplexityCalculator.compute(method);
		int expected = main.neo.cem.Utils.computeAndAnnotateAccumulativeCognitiveComplexity(method);
		assertEquals(expected, actual);
	}

	private MethodDeclaration firstMethodFrom(String source) {
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source.toCharArray());
		parser.setResolveBindings(false);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		assertFalse(cu.types().isEmpty());
		org.eclipse.jdt.core.dom.TypeDeclaration type = (org.eclipse.jdt.core.dom.TypeDeclaration) cu.types().get(0);
		return type.getMethods()[0];
	}
}
