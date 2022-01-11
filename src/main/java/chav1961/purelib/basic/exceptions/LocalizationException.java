package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems in the library environment, related to localization environment.</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.5
 */
public class LocalizationException extends PreparationException {
	private static final long serialVersionUID = -1213036033104355164L;

	/**
	 * <p>Constructor of the class</p>
	 */
	public LocalizationException() {
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 */
	public LocalizationException(final String message) {
		super(message);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param cause exception cause
	 */
	public LocalizationException(final Throwable cause) {
		super(cause);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 * @param cause exception cause
	 */
	public LocalizationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
