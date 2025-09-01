package test.refactor;

import main.refactor.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.List;

import test.mother.ListMother;

import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {

	private final ListMother mother = new ListMother();

	private static void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

	@Test
	void given_nullList_when_asImmutable_should_returnEmptyImmutableList() {
		final List<String> result = Utils.asImmutable(null);
		toStrictEqual(0, result.size());
		assertThrows(UnsupportedOperationException.class, () -> result.add("x"));
	}

	@Test
	void given_emptyList_when_asImmutable_should_returnEmptyImmutableList() {
	final List<String> empty = this.mother.emptyStringList();
		final List<String> result = Utils.asImmutable(empty);
		toStrictEqual(0, result.size());
		assertThrows(UnsupportedOperationException.class, () -> result.add("x"));
	}

	@Test
	void given_populatedList_when_asImmutable_should_returnImmutableCopy() {
	final List<String> original = this.mother.stringListABC();
		final List<String> result = Utils.asImmutable(original);
		toStrictEqual(original, result);
		assertThrows(UnsupportedOperationException.class, () -> result.add("d"));
	}

	@Test
	void given_originalListModifiedAfter_asImmutable_when_checkingReturned_should_remainUnchanged() {
	final List<String> original = this.mother.stringListABC();
		final List<String> result = Utils.asImmutable(original);
		original.add("z");
		toStrictEqual(Arrays.asList("a", "b", "c"), result);
	}

	@Test
	void given_list_when_asImmutable_should_changesInReturnedNotAllowedButOriginalMutable() {
	final List<String> original = this.mother.stringListABC();
		final List<String> result = Utils.asImmutable(original);
		assertThrows(UnsupportedOperationException.class, () -> result.remove("a"));
		original.remove("a");
		toStrictEqual(2, original.size());
		toStrictEqual(3, result.size());
	}

	@Test
	void given_emptyList_when_reverse_should_returnEmptyImmutableList() {
	final List<String> empty = this.mother.emptyStringList();
		final List<String> reversed = Utils.reverse(empty);
		toStrictEqual(0, reversed.size());
		assertThrows(UnsupportedOperationException.class, () -> reversed.add("x"));
	}

	@Test
	void given_singleElementList_when_reverse_should_returnSameSingleElementList() {
	final List<String> single = this.mother.singleStringList();
		final List<String> reversed = Utils.reverse(single);
		toStrictEqual(single, reversed);
		assertThrows(UnsupportedOperationException.class, () -> reversed.add("y"));
	}

	@Test
	void given_populatedList_when_reverse_should_returnReversedImmutableList() {
	final List<String> original = this.mother.stringListABC();
		final List<String> reversed = Utils.reverse(original);
		toStrictEqual(Arrays.asList("c", "b", "a"), reversed);
		assertThrows(UnsupportedOperationException.class, () -> reversed.add("d"));
	}

	@Test
	void given_originalList_when_reverse_should_notModifyOriginalList() {
	final List<String> original = this.mother.stringListABC();
		final List<String> reversed = Utils.reverse(original);
		toStrictEqual(Arrays.asList("a", "b", "c"), original);
		toStrictEqual(Arrays.asList("c", "b", "a"), reversed);
	}

	@Test
	void given_originalListModifiedAfter_reverse_when_checkingReturned_should_remainIndependent() {
	final List<String> original = this.mother.stringListABC();
		final List<String> reversed = Utils.reverse(original);
		original.add("z");
		toStrictEqual(Arrays.asList("c", "b", "a"), reversed);
		toStrictEqual(4, original.size());
	}

	@Test
	void given_nullList_when_reverse_throws_NullPointerException() {
		final Executable exec = () -> Utils.reverse(null);
		assertThrows(NullPointerException.class, exec);
	}
}
