package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception is a root of all Pure Library exceptions.</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class PureLibException extends Exception {
	private static final long serialVersionUID = -6282248007936959334L;

	/**
	 * <p>Constructor of the class</p>
	 */
	public PureLibException() {
		super();
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 * @param cause exception cause
	 */
	public PureLibException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 */
	public PureLibException(final String message) {
		super(message);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param cause exception cause
	 */
	public PureLibException(final Throwable cause) {
		super(cause);
	}
}
