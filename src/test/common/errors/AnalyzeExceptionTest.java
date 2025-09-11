package test.common.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.common.error.AnalyzeException;

class AnalyzeExceptionTest {
	private final String message = "analyze error";
	private final Throwable cause = new IllegalStateException("root");

	private void toStrictEqual(Object expected, Object actual) { assertEquals(expected, actual); }

	@Test
	@DisplayName("given_message_when_constructAnalyzeException_should_setMessage")
	void given_message_when_constructAnalyzeException_should_setMessage() {
		final AnalyzeException ex = new AnalyzeException(this.message);
		assertNotNull(ex);
		toStrictEqual(this.message, ex.getMessage());
	}

	@Test
	@DisplayName("given_messageAndCause_when_constructAnalyzeException_should_setMessageAndCause")
	void given_messageAndCause_when_constructAnalyzeException_should_setMessageAndCause() {
		final AnalyzeException ex = new AnalyzeException(this.message, this.cause);
		toStrictEqual(this.message, ex.getMessage());
		assertSame(this.cause, ex.getCause());
	}
}