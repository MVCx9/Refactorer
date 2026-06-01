package test.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import main.common.utils.Utils;

class UtilsTest {

    @Test
    void given_nullList_when_asImmutable_should_returnEmptyList() {
        final List<Object> result = Utils.asImmutable(null);
        assertTrue(result.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> result.add(new Object()));
    }

    @Test
    void given_populatedList_when_asImmutable_should_returnUnmodifiableCopy() {
        final List<String> input = new ArrayList<>(List.of("a", "b"));
        final List<String> result = Utils.asImmutable(input);
        assertEquals(List.of("a", "b"), result);
        assertNotSame(input, result);
        assertThrows(UnsupportedOperationException.class, () -> result.add("c"));
        input.add("c");
        assertEquals(2, result.size());
    }

    @Test
    void given_populatedList_when_reverse_should_returnReversedImmutable() {
        final List<Integer> input = List.of(1, 2, 3);
        final List<Integer> reversed = Utils.reverse(input);
        assertEquals(List.of(3, 2, 1), reversed);
        assertThrows(UnsupportedOperationException.class, () -> reversed.add(4));
    }

    @Test
    void given_emptyList_when_reverse_should_returnEmpty() {
        final List<Integer> reversed = Utils.reverse(Collections.emptyList());
        assertTrue(reversed.isEmpty());
    }

    @Test
    void given_nullSource_when_formatJava_should_returnNull() {
        assertEquals(null, Utils.formatJava(null));
    }

    @Test
    void given_emptySource_when_formatJava_should_returnEmpty() {
        assertEquals("", Utils.formatJava(""));
    }

    @Test
    void given_invalidSource_when_formatJava_should_returnOriginal() {
        final String invalid = "not java {{{";
        final String result = Utils.formatJava(invalid);
        assertSame(invalid, result);
    }

    @Test
    void given_validSource_when_formatJava_should_returnNonEmptyString() {
        final String formatted = Utils.formatJava("class A { void m(){} }");
        assertTrue(formatted != null && formatted.contains("class A"));
    }
}
