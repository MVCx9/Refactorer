package test.objectmothers;

import main.model.method.MethodMetrics;

public final class MethodMetricsMother {

    private MethodMetricsMother() {
    }

    public static MethodMetrics basic(final String name) {
        return MethodMetrics.builder().name(name).cc(5).loc(10).usedILP(false).build();
    }

    public static MethodMetrics withCc(final String name, final int cc) {
        return MethodMetrics.builder().name(name).cc(cc).loc(10).build();
    }

    public static MethodMetrics extracted(final String baseName, final int suffix, final int cc) {
        return MethodMetrics.builder().name(baseName + "_ext_" + suffix).cc(cc).loc(4).usedILP(true).build();
    }

    public static MethodMetrics ilp(final String name) {
        return MethodMetrics.builder().name(name).cc(3).loc(7).usedILP(true).build();
    }
}
