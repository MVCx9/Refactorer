package test.common.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.common.error.ValidationException;

class ValidationExceptionTest {
	private final String message = "validation failed";
	private final Throwable cause = new IllegalArgumentException("invalid");

	private void toStrictEqual(Object expected, Object actual) { assertEquals(expected, actual); }

	@Test
	@DisplayName("given_message_when_constructValidationException_should_setMessage")
	void given_message_when_constructValidationException_should_setMessage() {
		final ValidationException ex = new ValidationException(this.message);
		assertNotNull(ex);
		toStrictEqual(this.message, ex.getMessage());
	}

	@Test
	@DisplayName("given_messageAndCause_when_constructValidationException_should_setMessageAndCause")
	void given_messageAndCause_when_constructValidationException_should_setMessageAndCause() {
		final ValidationException ex = new ValidationException(this.message, this.cause);
		toStrictEqual(this.message, ex.getMessage());
		assertSame(this.cause, ex.getCause());
	}
}