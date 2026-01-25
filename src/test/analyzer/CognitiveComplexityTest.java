package test.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import main.neo.cem.Utils;

class CognitiveComplexityTest {

	private CompilationUnit parse(String src) {
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(src.toCharArray());
		parser.setResolveBindings(false);
		return (CompilationUnit) parser.createAST(null);
	}

	private MethodDeclaration getMethod(CompilationUnit cu, String methodName) {
		TypeDeclaration type = (TypeDeclaration) cu.types().get(0);
		for (MethodDeclaration md : type.getMethods()) {
			if (md.getName().getIdentifier().equals(methodName)) {
				return md;
			}
		}
		return null;
	}

	private int computeComplexity(String classSource, String methodName) {
		CompilationUnit cu = parse(classSource);
		MethodDeclaration md = getMethod(cu, methodName);
		return Utils.computeAndAnnotateAccumulativeCognitiveComplexity(md);
	}

	// ==================== BASIC IF STATEMENT TESTS ====================
	
	@Nested
	@DisplayName("If Statements")
	class IfStatements {
		
		@Test
		@DisplayName("Simple if: +1")
		void simpleIf() {
			String src = "class A { void m() { if (a) {} } }";
			assertEquals(1, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("If with else: +1 for if, +1 for else = 2")
		void ifWithElse() {
			String src = "class A { void m() { if (a) {} else {} } }";
			assertEquals(2, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("If else-if: +1 for if, +1 for else-if = 2")
		void ifElseIf() {
			String src = "class A { void m() { if (a) {} else if (b) {} } }";
			assertEquals(2, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("If else-if else: +1 + +1 + +1 = 3")
		void ifElseIfElse() {
			String src = "class A { void m() { if (a) {} else if (b) {} else {} } }";
			assertEquals(3, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Chain of else-if: each adds +1 without extra nesting")
		void chainOfElseIf() {
			String src = "class A { void m() { if (a) {} else if (b) {} else if (c) {} else if (d) {} else {} } }";
			assertEquals(5, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Nested if: +1 for outer, +1+1(nesting) for inner = 3")
		void nestedIf() {
			String src = "class A { void m() { if (a) { if (b) {} } } }";
			assertEquals(3, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Nested if in else: nesting should apply")
		void nestedIfInElse() {
			String src = "class A { void m() { if (a) {} else { if (b) {} } } }";
			assertEquals(4, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("else-if does NOT increase nesting for subsequent content")
		void elseIfNoExtraNesting() {
			String src = "class A { void m() { if (a) {} else if (b) { if (c) {} } } }";
			assertEquals(4, computeComplexity(src, "m"));
		}
	}

	// ==================== LOOP TESTS ====================
	
	@Nested
	@DisplayName("Loops")
	class Loops {
		
		@Test
		@DisplayName("For loop: +1")
		void forLoop() {
			String src = "class A { void m() { for (int i=0; i<10; i++) {} } }";
			assertEquals(1, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Enhanced for loop: +1")
		void enhancedForLoop() {
			String src = "class A { void m(Object[] arr) { for (Object o : arr) {} } }";
			assertEquals(1, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("While loop: +1")
		void whileLoop() {
			String src = "class A { void m() { while (a) {} } }";
			assertEquals(1, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Do-while loop: +1")
		void doWhileLoop() {
			String src = "class A { void m() { do {} while (a); } }";
			assertEquals(1, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Nested loops: nesting increments")
		void nestedLoops() {
			String src = "class A { void m() { for (int i=0; i<10; i++) { for (int j=0; j<10; j++) {} } } }";
			assertEquals(3, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("For with nested if")
		void forWithNestedIf() {
			String src = "class A { void m() { for (int i=0; i<10; i++) { if (a) {} } } }";
			assertEquals(3, computeComplexity(src, "m"));
		}
	}

	// ==================== SWITCH TESTS ====================
	
	@Nested
	@DisplayName("Switch Statement")
	class SwitchStatementTests {
		
		@Test
		@DisplayName("Simple switch: +1")
		void simpleSwitch() {
			String src = "class A { void m() { switch(x) { case 1: break; case 2: break; } } }";
			assertEquals(1, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Switch with nested if: nesting applies")
		void switchWithNestedIf() {
			String src = "class A { void m() { switch(x) { case 1: if (a) {} break; } } }";
			assertEquals(3, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Switch without cases: complexity 1")
		void switchWithoutCases() {
			String src = "class A { void m() { switch(x) {} } }";
			assertEquals(1, computeComplexity(src, "m"));
		}
	}

	// ==================== TRY-CATCH TESTS ====================
	
	@Nested
	@DisplayName("Try-Catch-Finally")
	class TryCatchFinally {
		
		@Test
		@DisplayName("Try without catch: 0 (try itself adds nothing)")
		void tryWithoutCatch() {
			String src = "class A { void m() { try {} finally {} } }";
			assertEquals(0, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Try with one catch: +1 for catch")
		void tryWithOneCatch() {
			String src = "class A { void m() { try {} catch (Exception e) {} } }";
			assertEquals(1, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Try with multiple catches: +1 each")
		void tryWithMultipleCatches() {
			String src = "class A { void m() { try {} catch (Exception e) {} catch (Error e) {} } }";
			assertEquals(2, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Nested catch with if: nesting applies to content inside catch")
		void nestedCatchWithIf() {
			String src = "class A { void m() { try {} catch (Exception e) { if (a) {} } } }";
			assertEquals(3, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Catch inside nested structure")
		void catchInsideNestedStructure() {
			String src = "class A { void m() { if (a) { try {} catch (Exception e) {} } } }";
			assertEquals(3, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Nested try-catch")
		void nestedTryCatch() {
			// Inner catch: +1, Outer catch: +1 = 2 (try blocks don't add complexity)
			String src = "class A { void m() { try { try {} catch (Exception e) {} } catch (Error e) {} } }";
			assertEquals(2, computeComplexity(src, "m"));
		}
	}

	// ==================== TERNARY OPERATOR TESTS ====================
	
	@Nested
	@DisplayName("Ternary Operator")
	class TernaryOperatorTests {
		
		@Test
		@DisplayName("Simple ternary: +1")
		void simpleTernary() {
			String src = "class A { int m(int a, int b) { return a > b ? a : b; } }";
			assertEquals(1, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Two ternary operators: +1 each = 2")
		void twoTernaryOperators() {
			String src = "class A { int m(int a, int b) { int c = a>b?b:a; return c>20?4:7; } }";
			assertEquals(2, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Nested ternary: nesting applies")
		void nestedTernary() {
			String src = "class A { int m(int a, int b, int c) { return a > 0 ? (b > 0 ? 1 : 2) : 3; } }";
			assertEquals(3, computeComplexity(src, "m"));
		}
	}

	// ==================== LOGICAL OPERATOR TESTS ====================
	
	@Nested
	@DisplayName("Logical Operators")
	class LogicalOperators {
		
		@Test
		@DisplayName("Single &&: +1 sequence")
		void singleAnd() {
			String src = "class A { void m() { if (a && b) {} } }";
			assertEquals(2, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Multiple && same type: still +1 sequence")
		void multipleAndSameType() {
			String src = "class A { void m() { if (a && b && c && d && e) {} } }";
			assertEquals(2, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Single ||: +1 sequence")
		void singleOr() {
			String src = "class A { void m() { if (a || b) {} } }";
			assertEquals(2, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Multiple || same type: still +1 sequence")
		void multipleOrSameType() {
			String src = "class A { void m() { if (a || b || c || d || e) {} } }";
			assertEquals(2, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("&& followed by ||: 2 sequences")
		void andFollowedByOr() {
			String src = "class A { void m() { if (a && b || c) {} } }";
			assertEquals(3, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Alternating operators: each change is a new sequence")
		void alternatingOperators() {
			String src = "class A { void m() { if (a && b || c && d || e) {} } }";
			assertEquals(5, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Logical operators inside function call are separate")
		void logicalInsideFunctionCall() {
			String src = "class A { boolean m() { return a && b || foo(b && c); } boolean foo(boolean x) { return x; } }";
			assertEquals(3, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Parentheses are ignored when counting sequences (mixed)")
		void parenthesesIgnoredMixed() {
			// a && (b || c) || d -> flattened: && then || (2 sequences) + if = 3
			String src = "class A { void m() { if (a && (b || c) || d) {} } }";
			assertEquals(3, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Parentheses with same operator: still one sequence")
		void parenthesesSameOperator() {
			String src = "class A { void m() { if (a || (b || c)) {} } }";
			assertEquals(2, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Return with logical operators (no if)")
		void returnWithLogicalOperators() {
			String src = "class A { boolean m() { return a && b && c; } }";
			assertEquals(1, computeComplexity(src, "m"));
		}
		
		@Test
		@DisplayName("Logical in loop condition")
		void logicalInLoopCondition() {
			String src = "class A { void m() { while (a > 0 && b < 10) {} } }";
			assertEquals(2, computeComplexity(src, "m"));
		}
		
		@Test
		@DisplayName("Logical in do-while condition")
		void logicalInDoWhileCondition() {
			String src = "class A { void m() { do {} while (a > 0 || b != 0); } }";
			assertEquals(2, computeComplexity(src, "m"));
		}
		
		@Test
		@DisplayName("Logical in for condition")
		void logicalInForCondition() {
			String src = "class A { void m() { for (int i = 0; i < 10 && j > 20; i++) {} } }";
			assertEquals(2, computeComplexity(src, "m"));
		}
	}

	// ==================== BREAK/CONTINUE WITH LABEL TESTS ====================
	
	@Nested
	@DisplayName("Break and Continue with Labels")
	class BreakContinueWithLabels {
		
		@Test
		@DisplayName("Break without label: no complexity")
		void breakWithoutLabel() {
			String src = "class A { void m() { for (int i=0; i<10; i++) { break; } } }";
			assertEquals(1, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Break with label: +1")
		void breakWithLabel() {
			String src = "class A { void m(Object[] objects) { outer: for(Object o : objects) { break outer; } } }";
			assertEquals(2, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Continue without label: no complexity")
		void continueWithoutLabel() {
			String src = "class A { void m() { for (int i=0; i<10; i++) { continue; } } }";
			assertEquals(1, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Continue with label: +1")
		void continueWithLabel() {
			String src = "class A { void m() { outer: for(int i=0; i<10; i++) { for(int j=0; j<10; j++) { continue outer; } } } }";
			assertEquals(4, computeComplexity(src, "m"));
		}
	}

	// ==================== LAMBDA TESTS ====================
	
	@Nested
	@DisplayName("Lambda Expressions")
	class LambdaExpressions {
		
		@Test
		@DisplayName("Lambda itself adds no complexity, but increases nesting")
		void lambdaIncreasesNesting() {
			String src = "class A { void m() { Runnable r = () -> { if (a) {} }; } }";
			assertEquals(2, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Deeply nested lambda")
		void deeplyNestedLambda() {
			String src = "class A { void m() { if (a) { Runnable r = () -> { if (b) {} }; } } }";
			assertEquals(4, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Lambda with logical operators")
		void lambdaWithLogicalOperators() {
			String src = "class A { void m() { Predicate p = t -> t.x() || t.y(); } }";
			assertEquals(1, computeComplexity(src, "m"));
		}
	}

	// ==================== ANONYMOUS CLASS TESTS ====================
	
	@Nested
	@DisplayName("Anonymous Classes")
	class AnonymousClasses {
		
		@Test
		@DisplayName("Content of anonymous class counts toward enclosing method")
		void anonymousClassContentCounts() {
			String src = "class A { void m() { Runnable r = new Runnable() { public void run() { if (a) {} } }; } }";
			assertEquals(2, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Nested anonymous class")
		void nestedAnonymousClass() {
			String src = "class A { void m() { if (x) { Runnable r = new Runnable() { public void run() { if (a) {} } }; } } }";
			assertEquals(4, computeComplexity(src, "m"));
		}
	}

	// ==================== LOCAL CLASS TESTS ====================
	
	@Nested
	@DisplayName("Local Classes")
	class LocalClasses {
		
		@Test
		@DisplayName("Methods in local class count toward enclosing method")
		void localClassMethodCounts() {
			String src = "class A { void m() { class Local { boolean foo() { return a && b || c && d; } } } }";
			assertEquals(3, computeComplexity(src, "m"));
		}
	}

	// ==================== RECURSION TESTS ====================
	
	@Nested
	@DisplayName("Recursion")
	class RecursionTests {
		
		@Test
		@DisplayName("Recursive call adds +1")
		void recursiveCall() {
			String src = "class A { int factorial(int n) { if (n <= 1) return 1; return n * factorial(n-1); } }";
			assertEquals(2, computeComplexity(src, "factorial"));
		}

		@Test
		@DisplayName("Recursive call with this.method()")
		void recursiveCallWithThis() {
			String src = "class A { int factorial(int n) { if (n <= 1) return 1; return n * this.factorial(n-1); } }";
			assertEquals(2, computeComplexity(src, "factorial"));
		}
		
		@Test
		@DisplayName("isPalindrome example from SonarSource")
		void isPalindromeExample() {
			String src = "class A { boolean isPalindrome(char[] s, int len) { if(len < 2) return true; else return s[0] == s[len-1] && isPalindrome(s, len-2); } }";
			assertEquals(4, computeComplexity(src, "isPalindrome"));
		}
	}

	// ==================== COMPLEX EXAMPLES FROM SONARSOURCE ====================
	
	@Nested
	@DisplayName("Complex Examples from SonarSource")
	class ComplexExamples {
		
		@Test
		@DisplayName("doFilter example: complexity 13")
		void doFilterExample() {
			String src = """
				class A {
					void doFilter() {
						if (consumedByStaticFile) { return; }
						try {} catch (Exception e) {} catch (Error e) {}
						if (body && responseWrapper) { body = ""; }
						if (body && hasOtherHandlers) { if (servletRequest) { return; } }
						if (body && !externalContainer) { log(); }
						if (body) { serialize(); } else if (chain != null) { chain.doFilter(); }
					}
				}
				""";
			assertEquals(13, computeComplexity(src, "doFilter"));
		}

		@Test
		@DisplayName("sumOfNonPrimes example: complexity 9")
		void sumOfNonPrimesExample() {
			String src = """
				class A {
					int sumOfNonPrimes(int limit) {
						int sum = 0;
						OUTER: for (int i = 0; i < limit; ++i) {
							if (i <= 2) { continue; }
							for (int j = 2; j < i; ++j) {
								if (i % j == 0) { continue OUTER; }
							}
							sum += i;
						}
						return sum;
					}
				}
				""";
			assertEquals(9, computeComplexity(src, "sumOfNonPrimes"));
		}

		@Test
		@DisplayName("getWeight example: complexity 4 (linear if chain)")
		void getWeightExample() {
			String src = """
				class A {
					String getWeight(int i) {
						if (i <= 0) { return "no weight"; }
						if (i < 10) { return "light"; }
						if (i < 20) { return "medium"; }
						if (i < 30) { return "heavy"; }
						return "very heavy";
					}
				}
				""";
			assertEquals(4, computeComplexity(src, "getWeight"));
		}

		@Test
		@DisplayName("noNestingForIfElseIf example: complexity 21")
		void noNestingForIfElseIfExample() {
			String src = """
				class A {
					void noNestingForIfElseIf() {
						while (true) {
							if (true) {
								for (;;) {
									if (true) {
									} else if (true) {
									} else {
										if (true) {}
									}
									if (true) {}
								}
							}
						}
					}
				}
				""";
			assertEquals(21, computeComplexity(src, "noNestingForIfElseIf"));
		}

		@Test
		@DisplayName("Switch with nested conditions: complexity 12")
		void switchWithNestedConditions() {
			String src = """
				class A {
					void switch2() {
						switch(foo) {
							case 1: break;
							case 2:
								if (lhs) {
									if (a && b && c || d) {}
									if (element) { out.remove(); } else { out.add(); }
								}
								break;
						}
					}
				}
				""";
			assertEquals(12, computeComplexity(src, "switch2"));
		}

		@Test
		@DisplayName("bulkActivate example: complexity 6")
		void bulkActivateExample() {
			String src = """
				class A {
					void bulkActivate(Iterator rules) {
						try {
							while (rules.hasNext()) {
								try { if (!changes.isEmpty()) {} } 
								catch (Exception e) {}
							}
						} finally {
							if (condition) { doTheThing(); }
						}
					}
				}
				""";
			assertEquals(6, computeComplexity(src, "bulkActivate"));
		}

		@Test
		@DisplayName("getValueToEval example: complexity 6")
		void getValueToEvalExample() {
			String src = """
				class A {
					String getValueToEval(int alertLevel, int foo) {
						if (alertLevel == 1 && foo == 2) {
							return "error";
						} else if (alertLevel == 2) {
							return "warn";
						} else {
							while (true) { doTheThing(); }
							throw new RuntimeException();
						}
					}
				}
				""";
			assertEquals(6, computeComplexity(src, "getValueToEval"));
		}

		@Test
		@DisplayName("Main with lambdas and anonymous class: complexity 4")
		void mainWithLambdasAndAnonymous() {
			String src = """
				class A {
					void main(String[] args) {
						Runnable r = () -> { if (condition) {} };
						r = new Runnable() { public void run() { if (condition) {} } };
					}
				}
				""";
			assertEquals(4, computeComplexity(src, "main"));
		}

		@Test
		@DisplayName("extraConditions example: complexity 10")
		void extraConditionsExample() {
			String src = """
				class A {
					void extraConditions() {
						if (a < b) {}
						if (a == b || c > 3 || b == c) { while (a > 0 && b < 10) {} }
						do {} while (a > 0 || b != 0);
						for (int i = 0; i < 10 && j > 20; i++) {}
					}
				}
				""";
			assertEquals(10, computeComplexity(src, "extraConditions"));
		}

		@Test
		@DisplayName("to method example: complexity 7")
		void toMethodExample() {
			String src = """
				class A {
					Object to(Object u) {
						Object result = null;
						String[] args = new String[10];
						int[] chain = new int[10];
						boolean debug = false;
						for (int ctr=0; ctr<args.length; ctr++)
							if (args[ctr].equals("-debug"))
								debug = true;
						for (int i = chain.length - 1; i >= 0; i--)
							result = chain[i];
						if (foo)
							for (int i = 0; i < 10; i++)
								doTheThing();
						return result;
					}
				}
				""";
			assertEquals(7, computeComplexity(src, "to"));
		}
	}

	// ==================== EDGE CASES ====================
	
	@Nested
	@DisplayName("Edge Cases")
	class EdgeCases {
		
		@Test
		@DisplayName("Empty method: complexity 0")
		void emptyMethod() {
			String src = "class A { void m() {} }";
			assertEquals(0, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Method with only return: complexity 0")
		void methodWithOnlyReturn() {
			String src = "class A { int m() { return 1; } }";
			assertEquals(0, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Method with variable declaration: complexity 0")
		void methodWithVariableDeclaration() {
			String src = "class A { void m() { int x = 5; } }";
			assertEquals(0, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Getter method: complexity 0")
		void getterMethod() {
			String src = "class A { int m() { return this.x; } }";
			assertEquals(0, computeComplexity(src, "m"));
		}

		@Test
		@DisplayName("Setter method: complexity 0")
		void setterMethod() {
			String src = "class A { void m(int x) { this.x = x; } }";
			assertEquals(0, computeComplexity(src, "m"));
		}
	}
}
