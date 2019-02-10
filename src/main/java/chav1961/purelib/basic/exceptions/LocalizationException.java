package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems in the library environment, related to localization environment.</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2 last update 0.0.3
 *
 */
public class LocalizationException extends EnvironmentException {
	private static final long serialVersionUID = -1213036033104355164L;

	public LocalizationException() {
	}

	public LocalizationException(String message) {
		super(message);
	}

	public LocalizationException(Throwable cause) {
		super(cause);
	}

	public LocalizationException(String message, Throwable cause) {
		super(message, cause);
	}
}
