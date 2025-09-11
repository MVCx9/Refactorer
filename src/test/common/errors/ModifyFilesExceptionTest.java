package test.common.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.common.error.ModifyFilesException;

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
}