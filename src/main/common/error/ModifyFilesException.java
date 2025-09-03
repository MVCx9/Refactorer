package main.common.error;

public class ModifyFilesException extends PluginException {

	private static final long serialVersionUID = 1L;

	public ModifyFilesException(String message) {
		super(message);
	}

	public ModifyFilesException(String message, Throwable cause) {
		super(message, cause);
	}

}
