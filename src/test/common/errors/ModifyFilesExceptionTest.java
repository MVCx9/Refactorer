package test.common.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.common.error.ModifyFilesException;
import main.common.error.PluginException;

class ModifyFilesExceptionTest {
	private final String message = "modify files error";
	private final Throwable cause = new IllegalArgumentException("cause");

	private void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("given_message_when_constructModifyFilesException_should_setMessage")
	void given_message_when_constructModifyFilesException_should_setMessage() {
		final ModifyFilesException ex = new ModifyFilesException(this.message);
		assertNotNull(ex);
		toStrictEqual(this.message, ex.getMessage());
	}

	@Test
	@DisplayName("given_messageAndCause_when_constructModifyFilesException_should_setMessageAndCause")
	void given_messageAndCause_when_constructModifyFilesException_should_setMessageAndCause() {
		final ModifyFilesException ex = new ModifyFilesException(this.message, this.cause);
		toStrictEqual(this.message, ex.getMessage());
		assertSame(this.cause, ex.getCause());
	}

	@Test
	@DisplayName("given_modifyFilesException_when_checkInheritance_should_extendPluginException")
	void given_modifyFilesException_when_checkInheritance_should_extendPluginException() {
		final ModifyFilesException ex = new ModifyFilesException(this.message);
		assertTrue(ex instanceof PluginException);
	}

	@Test
	@DisplayName("given_modifyFilesException_when_checkInheritance_should_extendRuntimeException")
	void given_modifyFilesException_when_checkInheritance_should_extendRuntimeException() {
		final ModifyFilesException ex = new ModifyFilesException(this.message);
		assertTrue(ex instanceof RuntimeException);
	}

	@Test
	@DisplayName("given_nullMessage_when_constructModifyFilesException_should_acceptNullMessage")
	void given_nullMessage_when_constructModifyFilesException_should_acceptNullMessage() {
		final ModifyFilesException ex = new ModifyFilesException(null);
		assertNull(ex.getMessage());
	}

	@Test
	@DisplayName("given_nullCause_when_constructModifyFilesException_should_acceptNullCause")
	void given_nullCause_when_constructModifyFilesException_should_acceptNullCause() {
		final ModifyFilesException ex = new ModifyFilesException(this.message, null);
		toStrictEqual(this.message, ex.getMessage());
		assertNull(ex.getCause());
	}

	@Test
	@DisplayName("given_emptyMessage_when_constructModifyFilesException_should_acceptEmptyMessage")
	void given_emptyMessage_when_constructModifyFilesException_should_acceptEmptyMessage() {
		final ModifyFilesException ex = new ModifyFilesException("");
		toStrictEqual("", ex.getMessage());
	}

	@Test
	@DisplayName("given_nestedCause_when_constructModifyFilesException_should_preserveCauseChain")
	void given_nestedCause_when_constructModifyFilesException_should_preserveCauseChain() {
		final Throwable rootCause = new IllegalStateException("root");
		final Throwable middleCause = new RuntimeException("middle", rootCause);
		final ModifyFilesException ex = new ModifyFilesException(this.message, middleCause);
		assertSame(middleCause, ex.getCause());
		assertSame(rootCause, ex.getCause().getCause());
	}
}