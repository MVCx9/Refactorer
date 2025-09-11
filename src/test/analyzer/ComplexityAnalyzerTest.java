package test.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.analyzer.ComplexityAnalyzer;
import main.builder.ClassAnalysis;

class ComplexityAnalyzerTest {
	private final String pluginId = "test";
	private final String oneMethodSource = "public class SampleOne { public void a(){ int x=0; if(x==0){x++;} } }";
	private final String noMethodSource = "public class Empty {}";
	private void toStrictEqual(Object expected, Object actual) { assertEquals(expected, actual); }

	private static class TestableComplexityAnalyzer extends ComplexityAnalyzer {
		private final int fixed;
		private TestableComplexityAnalyzer(int fixed) { this.fixed = fixed; }
		@Override
		protected int computeCognitiveComplexity(org.eclipse.jdt.core.dom.MethodDeclaration md) { return this.fixed; }
	}

	@Test
	@DisplayName("given_classWithOneMethod_when_analyze_should_returnSingleCurrentMethodAndRefactoredEqualsCurrent")
	void given_classWithOneMethod_when_analyze_should_returnSingleCurrentMethodAndRefactoredEqualsCurrent() throws Exception {
		final CompilationUnit cu = parse(this.oneMethodSource);
		final ICompilationUnit icu = proxyICompilationUnit(this.oneMethodSource, "SampleOne.java", false);
		final ComplexityAnalyzer analyzer = new TestableComplexityAnalyzer(42);
		final ClassAnalysis analysis = analyzer.analyze(cu, icu);
		assertNotNull(analysis);
		toStrictEqual("SampleOne.java", analysis.getClassName());
		toStrictEqual(1, analysis.getCurrentMethods().size());
		toStrictEqual(analysis.getCurrentMethods().size(), analysis.getRefactoredMethods().size());
		toStrictEqual(analysis.getCurrentMethods().get(0).getMethodName(), analysis.getRefactoredMethods().get(0).getMethodName());
		toStrictEqual(42, analysis.getCurrentMethods().get(0).getCc());
		assertTrue(analysis.getCurrentSource().contains("class SampleOne"));
	}

	@Test
	@DisplayName("given_classWithNoMethods_when_analyze_should_returnEmptyMethodLists")
	void given_classWithNoMethods_when_analyze_should_returnEmptyMethodLists() throws Exception {
		final CompilationUnit cu = parse(this.noMethodSource);
		final ICompilationUnit icu = proxyICompilationUnit(this.noMethodSource, "Empty.java", false);
		final ComplexityAnalyzer analyzer = new TestableComplexityAnalyzer(5);
		final ClassAnalysis analysis = analyzer.analyze(cu, icu);
		toStrictEqual(0, analysis.getCurrentMethods().size());
		toStrictEqual(0, analysis.getRefactoredMethods().size());
		toStrictEqual("Empty.java", analysis.getClassName());
	}

	@Test
	@DisplayName("given_classWithExtractedMethod_when_analyze_should_skipExtractedMethodInCurrentMethods")
	void given_classWithExtractedMethod_when_analyze_should_skipExtractedMethodInCurrentMethods() throws Exception {
		final String src = "public class WithExt { public void a(){} public void a_ext_0(){} }";
		final CompilationUnit cu = parse(src);
		final ICompilationUnit icu = proxyICompilationUnit(src, "WithExt.java", false);
		final ComplexityAnalyzer analyzer = new TestableComplexityAnalyzer(1);
		final ClassAnalysis analysis = analyzer.analyze(cu, icu);
		toStrictEqual(1, analysis.getCurrentMethods().size());
		toStrictEqual("a", analysis.getCurrentMethods().get(0).getMethodName());
	}

	@Test
	@DisplayName("given_classWithNullSource_when_analyze_should_setCurrentSourceNull")
	void given_classWithNullSource_when_analyze_should_setCurrentSourceNull() throws Exception {
		final String src = "public class NullSrc { public void a(){} }";
		final CompilationUnit cu = parse(src);
		final ICompilationUnit icu = proxyICompilationUnit(null, "NullSrc.java", false);
		final ComplexityAnalyzer analyzer = new TestableComplexityAnalyzer(3);
		final ClassAnalysis analysis = analyzer.analyze(cu, icu);
		toStrictEqual(null, analysis.getCurrentSource());
	}

	@Test
	@DisplayName("given_classWithOverloadedMethods_when_analyze_should_processBothOverloads")
	void given_classWithOverloadedMethods_when_analyze_should_processBothOverloads() throws Exception {
		final String src = "public class Over { void a(){} void a(int x){} }";
		final CompilationUnit cu = parse(src);
		final ICompilationUnit icu = proxyICompilationUnit(src, "Over.java", false);
		final ComplexityAnalyzer analyzer = new TestableComplexityAnalyzer(9);
		final ClassAnalysis analysis = analyzer.analyze(cu, icu);
		toStrictEqual(2, analysis.getCurrentMethods().size());
		toStrictEqual(9, analysis.getCurrentMethods().get(0).getCc());
		toStrictEqual(9, analysis.getCurrentMethods().get(1).getCc());
	}

	private CompilationUnit parse(String src) {
		final ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(src.toCharArray());
		parser.setResolveBindings(false);
		return (CompilationUnit) parser.createAST(null);
	}

	private ICompilationUnit proxyICompilationUnit(String source, String elementName, boolean failWorkingCopy) {
		final Map<String,Object> state = new HashMap<>();
		state.put("source", source);
		state.put("name", elementName);
		state.put("fail", failWorkingCopy);
		InvocationHandler h = (Object proxy, Method method, Object[] args) -> {
			final String m = method.getName();
			if (m.equals("getSource")) return state.get("source");
			if (m.equals("getElementName")) return state.get("name");
			if (m.equals("getWorkingCopy")) {
				if ((boolean) state.get("fail")) throw new CoreException(new Status(Status.ERROR, this.pluginId, "fail"));
				return proxy;
			}
			if (m.equals("discardWorkingCopy")) return null;
			return defaultValue(method.getReturnType());
		};
		return (ICompilationUnit) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { ICompilationUnit.class }, h);
	}

	private Object defaultValue(Class<?> type) {
		if (!type.isPrimitive()) return null;
		if (type == boolean.class) return false;
		if (type == byte.class) return (byte)0;
		if (type == short.class) return (short)0;
		if (type == int.class) return 0;
		if (type == long.class) return 0L;
		if (type == float.class) return 0f;
		if (type == double.class) return 0d;
		if (type == char.class) return '\0';
		return null;
	}
}