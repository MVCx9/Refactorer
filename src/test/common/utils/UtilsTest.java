package test.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.common.utils.Utils;

class UtilsTest {
	private final List<Integer> baseList = Arrays.asList(1, 2, 3);
	private final String poorlyFormatted = "public class A{int x;}";
	private final String invalidSource = "public class";
	private final String formattedCandidate = "public class B {\n    int y;\n}";

	private void toStrictEqual(Object expected, Object actual) { assertEquals(expected, actual); }

	@Test
	@DisplayName("given_nullList_when_asImmutable_should_returnEmptyUnmodifiableList")
	void given_nullList_when_asImmutable_should_returnEmptyUnmodifiableList() {
		final List<Object> result = Utils.asImmutable(null);
		toStrictEqual(0, result.size());
		assertThrows(UnsupportedOperationException.class, () -> result.add(1));
	}

	@Test
	@DisplayName("given_nonNullList_when_asImmutable_should_returnIndependentUnmodifiableCopy")
	void given_nonNullList_when_asImmutable_should_returnIndependentUnmodifiableCopy() {
		final List<Integer> original = new ArrayList<>(this.baseList);
		final List<Integer> result = Utils.asImmutable(original);
		toStrictEqual(this.baseList, result);
		original.add(99);
		toStrictEqual(3, result.size());
	}

	@Test
	@DisplayName("given_attemptToModifyReturnedList_when_asImmutable_should_throwUnsupportedOperationException")
	void given_attemptToModifyReturnedList_when_asImmutable_should_throwUnsupportedOperationException() {
		final List<Integer> result = Utils.asImmutable(this.baseList);
		assertThrows(UnsupportedOperationException.class, () -> result.add(4));
	}

	@Test
	@DisplayName("given_nullList_when_reverse_throws_NullPointerException")
	void given_nullList_when_reverse_throws_NullPointerException() {
		assertThrows(NullPointerException.class, () -> Utils.reverse(null));
	}

	@Test
	@DisplayName("given_list_when_reverse_should_returnReversedUnmodifiableList")
	void given_list_when_reverse_should_returnReversedUnmodifiableList() {
		final List<Integer> original = this.baseList;
		final List<Integer> reversed = Utils.reverse(original);
		toStrictEqual(Arrays.asList(3,2,1), reversed);
		toStrictEqual(Arrays.asList(1,2,3), original);
	}

	@Test
	@DisplayName("given_attemptToModifyReturnedList_when_reverse_should_throwUnsupportedOperationException")
	void given_attemptToModifyReturnedList_when_reverse_should_throwUnsupportedOperationException() {
		final List<Integer> reversed = Utils.reverse(this.baseList);
		assertThrows(UnsupportedOperationException.class, () -> reversed.add(10));
	}

	@Test
	@DisplayName("given_emptyList_when_reverse_should_returnEmptyUnmodifiableList")
	void given_emptyList_when_reverse_should_returnEmptyUnmodifiableList() {
		final List<Integer> result = Utils.reverse(List.of());
		toStrictEqual(0, result.size());
		assertThrows(UnsupportedOperationException.class, () -> result.add(1));
	}

	@Test
	@DisplayName("given_nullSource_when_formatJava_should_returnNull")
	void given_nullSource_when_formatJava_should_returnNull() {
		toStrictEqual(null, Utils.formatJava(null));
	}

	@Test
	@DisplayName("given_emptySource_when_formatJava_should_returnEmpty")
	void given_emptySource_when_formatJava_should_returnEmpty() {
		toStrictEqual("", Utils.formatJava(""));
	}

	@Test
	@DisplayName("given_invalidSource_when_formatJava_should_returnOriginal")
	void given_invalidSource_when_formatJava_should_returnOriginal() {
		toStrictEqual(this.invalidSource, Utils.formatJava(this.invalidSource));
	}

	@Test
	@DisplayName("given_poorlyFormattedSource_when_formatJava_should_returnFormattedString")
	void given_poorlyFormattedSource_when_formatJava_should_returnFormattedString() {
		final String formatted = Utils.formatJava(this.poorlyFormatted);
		assertNotNull(formatted);
		assertTrue(formatted.contains("class A"));
		assertTrue(formatted.contains("int x"));
		assertTrue(formatted.contains("{"));
		// Heuristic: expect newline insertion or added space after class name
		assertTrue(formatted.contains("class A ") || formatted.contains("\n"));
	}

	@Test
	@DisplayName("given_formattedSource_when_formatJava_should_beIdempotentOnSecondFormatting")
	void given_formattedSource_when_formatJava_should_beIdempotentOnSecondFormatting() {
		final String first = Utils.formatJava(this.formattedCandidate);
		final String second = Utils.formatJava(first);
		toStrictEqual(first, second);
	}

	@Test
	@DisplayName("given_nullICompilationUnit_when_parserAST_throws_NullPointerException")
	void given_nullICompilationUnit_when_parserAST_throws_NullPointerException() {
		assertThrows(NullPointerException.class, () -> Utils.parserAST(null));
	}
}