package test.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import main.common.error.AnalyzeException;
import main.common.error.ModifyFilesException;
import main.common.error.PluginException;
import main.common.error.ResourceNotFoundException;
import main.common.error.ValidationException;

class PluginExceptionsTest {

    @Test
    void given_message_when_pluginException_should_propagateMessage() {
        assertMessageOnlyConstructor(PluginException::new);
    }

    @Test
    void given_messageAndCause_when_pluginException_should_propagateBoth() {
        assertMessageAndCauseConstructor(PluginException::new);
    }

    @Test
    void given_message_when_analyzeException_should_propagateMessage() {
        assertMessageOnlyConstructor(AnalyzeException::new);
    }

    @Test
    void given_messageAndCause_when_analyzeException_should_propagateBoth() {
        assertMessageAndCauseConstructor(AnalyzeException::new);
        assertTrue(new AnalyzeException("x") instanceof PluginException);
    }

    @Test
    void given_message_when_modifyFilesException_should_propagateMessage() {
        assertMessageOnlyConstructor(ModifyFilesException::new);
    }

    @Test
    void given_messageAndCause_when_modifyFilesException_should_propagateBoth() {
        assertMessageAndCauseConstructor(ModifyFilesException::new);
        assertTrue(new ModifyFilesException("x") instanceof PluginException);
    }

    @Test
    void given_message_when_resourceNotFoundException_should_propagateMessage() {
        assertMessageOnlyConstructor(ResourceNotFoundException::new);
    }

    @Test
    void given_messageAndCause_when_resourceNotFoundException_should_propagateBoth() {
        assertMessageAndCauseConstructor(ResourceNotFoundException::new);
        assertTrue(new ResourceNotFoundException("x") instanceof PluginException);
    }

    @Test
    void given_message_when_validationException_should_propagateMessage() {
        assertMessageOnlyConstructor(ValidationException::new);
    }

    @Test
    void given_messageAndCause_when_validationException_should_propagateBoth() {
        assertMessageAndCauseConstructor(ValidationException::new);
        assertTrue(new ValidationException("x") instanceof PluginException);
    }

    private void assertMessageOnlyConstructor(final Function<String, ? extends RuntimeException> ctor) {
        final RuntimeException ex = ctor.apply("boom");
        assertEquals("boom", ex.getMessage());
        assertNull(ex.getCause());
    }

    private void assertMessageAndCauseConstructor(final BiFunction<String, Throwable, ? extends RuntimeException> ctor) {
        final Throwable cause = new IllegalStateException("inner");
        final RuntimeException ex = ctor.apply("boom", cause);
        assertEquals("boom", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
