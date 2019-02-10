package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception is a root of all Pule Library exceptions.</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 *
 */
public class PureLibException extends Exception {
	private static final long serialVersionUID = -6282248007936959334L;

	public PureLibException() {
		super();
	}

	public PureLibException(String message, Throwable cause) {
		super(message, cause);
	}

	public PureLibException(String message) {
		super(message);
	}

	public PureLibException(Throwable cause) {
		super(cause);
	}
}
