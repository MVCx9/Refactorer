package main.common.error;

public class AnalyzeException extends PluginException {

	private static final long serialVersionUID = 1L;

	public AnalyzeException(String message) {
		super(message);
	}

	public AnalyzeException(String message, Throwable cause) {
		super(message, cause);
	}

}
