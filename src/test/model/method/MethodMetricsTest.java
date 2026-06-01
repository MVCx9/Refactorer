package test.model.method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import main.model.method.MethodMetrics;

class MethodMetricsTest {

    @Test
    void given_defaults_when_build_should_returnDefaultValues() {
        final MethodMetrics m = MethodMetrics.builder().build();
        assertEquals("<unnamed>", m.getName());
        assertEquals(0, m.getCc());
        assertEquals(0, m.getLoc());
        assertFalse(m.isUsedILP());
    }

    @Test
    void given_allFields_when_build_should_returnConfiguredValues() {
        final MethodMetrics m = MethodMetrics.builder().name("foo").cc(7).loc(20).usedILP(true).build();
        assertEquals("foo", m.getName());
        assertEquals(7, m.getCc());
        assertEquals(20, m.getLoc());
        assertTrue(m.isUsedILP());
    }

    @Test
    void given_methodMetrics_when_getNameUpper_should_returnUpperCase() {
        final MethodMetrics m = MethodMetrics.builder().name("foo").build();
        assertEquals("FOO", m.getNameUpper());
    }
}
