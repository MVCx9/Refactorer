package test.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import main.neo.core.jdt.CognitiveComplexityVisitor;

class CognitiveComplexityCheckTest {

    private static final String SOURCE = """
            class CognitiveComplexityCheck<T, U> {

                boolean a, b, c, d, e, f, g, h, i, j, k, l, m;
                int foo;
                boolean condition;
                boolean consumedByStaticFile;
                boolean externalContainer;
                boolean hasOtherHandlers;
                int args[];
                boolean debug;
                Object[] chain;
                Body body;
                Object responseWrapper;
                Object httpResponse;
                Object serializerChain;
                Object httpRequest;
                Object chainObj;
                Object uri;
                Object LOG;
                Object specifiedBy;
                Object result;
                Object out;
                Object element;
                Object lhs;
                Object symbol;
                Object tuples;
                Object parentIndex;
                Object parentReference;
                Object changes;
                Object condition2;

                interface Tree { interface Kind { Object IDENTIFIER = null; Object ASSIGNMENT = null; } }
                interface ServletRequest {}
                interface HttpRequestWrapper extends ServletRequest { void notConsumed(boolean b); }
                static class HaltException extends RuntimeException {}
                static class Body { boolean notSet(){return false;} boolean isSet(){return false;} void set(String s){} void serializeTo(Object a, Object b, Object c){} }
                static class Measure { enum Level { ERROR, WARN; static boolean equals(Object x){return false;} } }
                static class Color { static final Color YELLOW = null; }
                static final Color YELLOW = null;
                static class BoundTransportAddress {}
                static class TransportAddress { Inet address(){return null;} }
                static class Inet { Inet getAddress(){return null;} boolean isLinkLocalAddress(){return false;} boolean isLoopbackAddress(){return false;} }
                static class JoinTuple {}
                static class JoinTupleIterator { JoinTupleIterator(Object a, Object b, Object c){} }
                static class BadRequestException extends RuntimeException {}
                static class Condition { Object getErrorThreshold(){return null;} Object getWarningThreshold(){return null;} }
                Condition condition3;
                static class TypeOfText { static final TypeOfText ANNOTATION = null, CONSTANT = null, CPP_DOC = null; }
                enum HighlightingType { ANNOTATION, CONSTANT, CPP_DOC; }
                static final int ASSIGNMENT = 2;
                java.util.Iterator<String> rules;
                String getRuleKeysAsString(Object o){return "";}
                void doTheThing(){}
                void doTheOtherThing(){}
                void doSomethingElse(){}
                boolean foo(boolean x){return x;}
                static class MyRunnable implements Runnable { public void run(){} }

                public int ternaryOp(int a, int b) {
                    int c = a > b ? b : a;
                    return c > 20 ? 4 : 7;
                }

                public boolean extraConditions() {
                    return a && b || foo(b && c);
                }

                public boolean extraConditions2() {
                    return a && (b || c) || d;
                }

                public void extraConditions3() {
                    if (a && b || c || d) {}
                }

                public void extraConditions4() {
                    if (a && b || c && d || e) {}
                }

                public void extraConditions5() {
                    if (a || b && c || d && e) {}
                }

                public void extraConditions6() {
                    if (a && b && c || d || e) {}
                }

                public void extraConditions7() {
                    if (a) {}
                }

                public void extraConditions8() {
                    if (a && b && c && d && e) {}
                }

                public void extraConditions9() {
                    if (a || b || c || d || e) {}
                }

                public void extraCondition10() {
                    if (a && b && c || d || e && f) {}
                }

                public void switch2() {
                    switch (foo) {
                        case 1:
                            break;
                        case ASSIGNMENT:
                            if (lhs instanceof Object) {
                                if (a && b && c || d) {
                                }
                                if (element instanceof Object) {
                                    doTheThing();
                                } else {
                                    doTheThing();
                                }
                            }
                            break;
                    }
                }

                public void extraCondition11() {
                    if (a || (b || c)) {}
                }

                public void extraConditions12() {
                    if (
                        a
                        && b
                        && c
                        || d
                        || e
                        && f
                        && g
                        || (h
                        || (i
                        && j
                        || k))
                        || l
                        || m
                    ) {}
                }

                public void breakWithLabel(java.util.Collection<Boolean> objects) {
                    doABarrelRoll:
                    for (Object o : objects) {
                        break doABarrelRoll;
                    }
                }

                public void doFilter(ServletRequest servletRequest) {
                    if (consumedByStaticFile) {
                        return;
                    }
                    try {
                    } catch (HaltException halt) {
                    } catch (Exception generalException) {
                    }
                    if (body.notSet() && a) {
                        body.set("");
                    }
                    if (body.notSet() && hasOtherHandlers) {
                        if (servletRequest instanceof HttpRequestWrapper) {
                            ((HttpRequestWrapper) servletRequest).notConsumed(true);
                            return;
                        }
                    }
                    if (body.notSet() && !externalContainer) {
                        doTheThing();
                    }
                    if (body.isSet()) {
                        body.serializeTo(httpResponse, serializerChain, httpRequest);
                    } else if (chainObj != null) {
                        doTheThing();
                    }
                }

                public final T to(U u) {
                    for (int ctr = 0; ctr < args.length; ctr++)
                        if (args[ctr] == 0)
                            debug = true;
                    for (int i = chain.length - 1; i >= 0; i--)
                        result = chain[i];
                    if (foo > 0)
                        for (int i = 0; i < 10; i++)
                            doTheThing();
                    return (T) result;
                }

                static boolean enforceLimits(BoundTransportAddress boundTransportAddress) {
                    Iterable<JoinTuple> itr = () -> new JoinTupleIterator(null, null, null);
                    java.util.function.Predicate<TransportAddress> isLoopbackOrLinkLocalAddress = t -> t.address().getAddress().isLinkLocalAddress()
                            || t.address().getAddress().isLoopbackAddress();
                    return true;
                }

                String bulkActivate(java.util.Iterator<String> rules) {
                    Object result = null;
                    try {
                        while (rules.hasNext()) {
                            try {
                                if (a) {}
                            } catch (BadRequestException ex) {}
                        }
                    } finally {
                        if (condition) {
                            doTheThing();
                        }
                    }
                    return "";
                }

                private String getValueToEval(Measure.Level alertLevel, Color foo) {
                    if (Measure.Level.equals(alertLevel) && foo == YELLOW) {
                        return condition3.getErrorThreshold().toString();
                    } else if (Measure.Level.equals(alertLevel)) {
                        return condition3.getWarningThreshold().toString();
                    } else {
                        while (true) {
                            doTheThing();
                        }
                    }
                }

                void extraConditionsLong() {
                    if (foo < 10) {
                        doTheThing();
                    }
                    if (foo == 1 || foo > 3 || foo - 7 == 0) {
                        while (foo-- > 0 && foo++ < 10) {
                            doTheOtherThing();
                        }
                    }
                    do {
                    } while (foo-- > 0 || foo != 0);
                    for (int x = 0; x < 10 && foo > 20; x++) {
                        doSomethingElse();
                    }
                }

                public void main(String[] args) {
                    Runnable r = () -> {
                        if (condition) {
                            doTheThing();
                        }
                    };
                    r = new MyRunnable();
                    r = new Runnable() {
                        public void run() {
                            if (condition) {
                                doTheThing();
                            }
                        }
                    };
                }

                int sumOfNonPrimes(int limit) {
                    int sum = 0;
                    OUTER: for (int x = 0; x < limit; ++x) {
                        if (x <= 2) {
                            continue;
                        }
                        for (int y = 2; y < 1; ++y) {
                            if (x % y == 0) {
                                continue OUTER;
                            }
                        }
                        sum += x;
                    }
                    return sum;
                }

                String getWeight(int x) {
                    if (x <= 0) return "no weight";
                    if (x < 10) return "light";
                    if (x < 20) return "medium";
                    if (x < 30) return "heavy";
                    return "very heavy";
                }

                public HighlightingType toProtocolType(TypeOfText textType) {
                    switch (foo) {
                        case 1: {
                            return HighlightingType.ANNOTATION;
                        }
                        case 2:
                            return HighlightingType.CONSTANT;
                        case 3:
                            return HighlightingType.CPP_DOC;
                        default:
                            throw new IllegalArgumentException();
                    }
                }

                public String getSpecifiedByKeysAsCommaList() {
                    return getRuleKeysAsString(specifiedBy);
                }

                void localClasses() {
                    class local {
                        boolean plop() {
                            return a && b || c && d;
                        }
                    }
                }

                void noNestingForIfElseIf() {
                    while (true) {
                        if (a) {
                            for (;;) {
                                if (b) {
                                } else if (c) {
                                } else {
                                    if (d) {}
                                }
                                if (e) {}
                            }
                        }
                    }
                }
            }
            """;

    private static Map<String, MethodDeclaration> methodIndex;

    @BeforeAll
    static void parseSource() {
        final ASTParser parser = ASTParser.newParser(AST.JLS21);
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final Map options = new Hashtable<>(JavaCore.getOptions());
        JavaCore.setComplianceOptions(JavaCore.VERSION_21, options);
        parser.setCompilerOptions(options);
        parser.setSource(SOURCE.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        methodIndex = new LinkedHashMap<>();
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(final MethodDeclaration node) {
                if (isTopLevelMethod(node)) {
                    methodIndex.putIfAbsent(node.getName().getIdentifier(), node);
                }
                return true;
            }
        });
    }

    private static boolean isTopLevelMethod(final MethodDeclaration node) {
        var parent = node.getParent();
        return parent instanceof AbstractTypeDeclaration
                && "CognitiveComplexityCheck".equals(((AbstractTypeDeclaration) parent).getName().getIdentifier());
    }

    static Stream<Arguments> expectedComplexities() {
        final Map<String, Integer> cases = new HashMap<>();
        cases.put("ternaryOp", 2);
        cases.put("extraConditions", 3);
        cases.put("extraConditions2", 2);
        cases.put("extraConditions3", 3);
        cases.put("extraConditions4", 5);
        cases.put("extraConditions5", 5);
        cases.put("extraConditions6", 3);
        cases.put("extraConditions7", 1);
        cases.put("extraConditions8", 2);
        cases.put("extraConditions9", 2);
        cases.put("extraCondition10", 4);
        cases.put("switch2", 12);
        cases.put("extraCondition11", 2);
        cases.put("extraConditions12", 7);
        cases.put("breakWithLabel", 2);
        cases.put("doFilter", 13);
        cases.put("to", 7);
        cases.put("enforceLimits", 1);
        cases.put("bulkActivate", 6);
        cases.put("getValueToEval", 6);
        cases.put("extraConditionsLong", 10);
        cases.put("main", 4);
        cases.put("sumOfNonPrimes", 9);
        cases.put("getWeight", 4);
        cases.put("toProtocolType", 1);
        cases.put("getSpecifiedByKeysAsCommaList", 0);
        cases.put("localClasses", 3);
        cases.put("noNestingForIfElseIf", 21);
        return cases.entrySet().stream()
                .map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

    @ParameterizedTest(name = "given_{0}_when_computeCognitiveComplexity_should_return_{1}")
    @MethodSource("expectedComplexities")
    void given_method_when_computeCognitiveComplexity_should_returnExpectedValue(
            final String methodName, final int expected) {
        final MethodDeclaration md = methodIndex.get(methodName);
        assertNotNull(md, "Method not found in fixture: " + methodName);
        final int actual = CognitiveComplexityVisitor.methodComplexity(md).complexity;
        assertEquals(expected, actual,
                () -> "CC mismatch for " + methodName + ": expected=" + expected + ", actual=" + actual);
    }
}
