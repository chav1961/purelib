package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems in the Pure Library environment.</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @last.update 0.0.7
 */
public class EnvironmentException extends PreparationException {
	private static final long serialVersionUID = 5963350383021796097L;

	/**
	 * <p>Constructor of the class</p>
	 */
	public EnvironmentException() {
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 */
	public EnvironmentException(final String message) {
		super(message);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param cause exception cause
	 */
	public EnvironmentException(final Throwable cause) {
		super(cause);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 * @param cause exception cause
	 */
	public EnvironmentException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
