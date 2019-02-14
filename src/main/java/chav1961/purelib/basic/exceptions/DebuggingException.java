package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception is used for rmote debuggers inly and sounl not be use on any other purposes.</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class DebuggingException extends PureLibException {
	private static final long serialVersionUID = 8712884354411140827L;

	public DebuggingException() {
		super();
	}

	public DebuggingException(String message, Throwable cause) {
		super(message, cause);
	}

	public DebuggingException(String message) {
		super(message);
	}

	public DebuggingException(Throwable cause) {
		super(cause);
	}
}
