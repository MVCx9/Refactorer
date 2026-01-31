package test.common.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.common.error.PluginException;
import main.common.error.ValidationException;

class ValidationExceptionTest {
	private final String message = "validation failed";
	private final Throwable cause = new IllegalArgumentException("invalid");

	private void toStrictEqual(Object expected, Object actual) {
		assertEquals(expected, actual);
	}

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

	@Test
	@DisplayName("given_validationException_when_checkInheritance_should_extendPluginException")
	void given_validationException_when_checkInheritance_should_extendPluginException() {
		final ValidationException ex = new ValidationException(this.message);
		assertTrue(ex instanceof PluginException);
	}

	@Test
	@DisplayName("given_validationException_when_checkInheritance_should_extendRuntimeException")
	void given_validationException_when_checkInheritance_should_extendRuntimeException() {
		final ValidationException ex = new ValidationException(this.message);
		assertTrue(ex instanceof RuntimeException);
	}

	@Test
	@DisplayName("given_nullMessage_when_constructValidationException_should_acceptNullMessage")
	void given_nullMessage_when_constructValidationException_should_acceptNullMessage() {
		final ValidationException ex = new ValidationException(null);
		assertNull(ex.getMessage());
	}

	@Test
	@DisplayName("given_nullCause_when_constructValidationException_should_acceptNullCause")
	void given_nullCause_when_constructValidationException_should_acceptNullCause() {
		final ValidationException ex = new ValidationException(this.message, null);
		toStrictEqual(this.message, ex.getMessage());
		assertNull(ex.getCause());
	}

	@Test
	@DisplayName("given_emptyMessage_when_constructValidationException_should_acceptEmptyMessage")
	void given_emptyMessage_when_constructValidationException_should_acceptEmptyMessage() {
		final ValidationException ex = new ValidationException("");
		toStrictEqual("", ex.getMessage());
	}

	@Test
	@DisplayName("given_nestedCause_when_constructValidationException_should_preserveCauseChain")
	void given_nestedCause_when_constructValidationException_should_preserveCauseChain() {
		final Throwable rootCause = new IllegalStateException("root");
		final Throwable middleCause = new RuntimeException("middle", rootCause);
		final ValidationException ex = new ValidationException(this.message, middleCause);
		assertSame(middleCause, ex.getCause());
		assertSame(rootCause, ex.getCause().getCause());
	}
}