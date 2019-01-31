package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception is used by testing entities to fire testing exceptions. Don't use this exception in any other purposes!</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 *
 */
public class TestException extends ContentException {
	private static final long serialVersionUID = -4537967619964315186L;

	public TestException() {
		super();
	}

	public TestException(String message, Throwable cause) {
		super(message, cause);
	}

	public TestException(String message) {
		super(message);
	}

	public TestException(Throwable cause) {
		super(cause);
	}
}
