package test.common.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.common.error.PluginException;

class PluginExceptionTest {
	private final String message = "plugin error";
	private final Throwable cause = new RuntimeException("plugin cause");

	private void toStrictEqual(Object expected, Object actual) { assertEquals(expected, actual); }

	@Test
	@DisplayName("given_message_when_constructPluginException_should_setMessage")
	void given_message_when_constructPluginException_should_setMessage() {
		final PluginException ex = new PluginException(this.message);
		assertNotNull(ex);
		toStrictEqual(this.message, ex.getMessage());
	}

	@Test
	@DisplayName("given_messageAndCause_when_constructPluginException_should_setMessageAndCause")
	void given_messageAndCause_when_constructPluginException_should_setMessageAndCause() {
		final PluginException ex = new PluginException(this.message, this.cause);
		toStrictEqual(this.message, ex.getMessage());
		assertSame(this.cause, ex.getCause());
	}
}