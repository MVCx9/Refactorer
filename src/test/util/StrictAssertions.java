package test.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class StrictAssertions {
    private StrictAssertions() {}
    public static <T> void toStrictEqual(T expected, T actual) {
        assertEquals(expected, actual);
    }
}
