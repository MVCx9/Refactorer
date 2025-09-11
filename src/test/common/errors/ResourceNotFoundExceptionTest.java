package test.common.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.common.error.ResourceNotFoundException;

class ResourceNotFoundExceptionTest {
	private final String message = "resource missing";
	private final Throwable cause = new IllegalArgumentException("missing");

	private void toStrictEqual(Object expected, Object actual) { assertEquals(expected, actual); }

	@Test
	@DisplayName("given_message_when_constructResourceNotFoundException_should_setMessage")
	void given_message_when_constructResourceNotFoundException_should_setMessage() {
		final ResourceNotFoundException ex = new ResourceNotFoundException(this.message);
		assertNotNull(ex);
		toStrictEqual(this.message, ex.getMessage());
	}

	@Test
	@DisplayName("given_messageAndCause_when_constructResourceNotFoundException_should_setMessageAndCause")
	void given_messageAndCause_when_constructResourceNotFoundException_should_setMessageAndCause() {
		final ResourceNotFoundException ex = new ResourceNotFoundException(this.message, this.cause);
		toStrictEqual(this.message, ex.getMessage());
		assertSame(this.cause, ex.getCause());
	}
}