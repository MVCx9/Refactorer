package test.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class AssertionsStrict {
    private AssertionsStrict() {}
    public static void toStrictEqual(Object expected, Object actual) { assertEquals(expected, actual); }
    public static void toStrictEqual(int expected, int actual) { assertEquals(expected, actual); }
}
