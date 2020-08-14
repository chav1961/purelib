package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception is used for remote debuggers only and should not be use on any other purposes.</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class DebuggingException extends PureLibException {
	private static final long serialVersionUID = 8712884354411140827L;

	/**
	 * <p>Constructor of the class</p>
	 */
	public DebuggingException() {
		super();
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 * @param cause exception cause
	 */
	public DebuggingException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 */
	public DebuggingException(final String message) {
		super(message);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param cause exception cause
	 */
	public DebuggingException(final Throwable cause) {
		super(cause);
	}
}
