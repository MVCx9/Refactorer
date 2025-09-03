package main.common.error;

public class ValidationException extends PluginException {

	private static final long serialVersionUID = 1L;

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

}
