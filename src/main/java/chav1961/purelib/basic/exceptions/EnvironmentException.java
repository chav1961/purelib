package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems in the library environment</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.3
 */
public class EnvironmentException extends PureLibException {
	private static final long serialVersionUID = 5963350383021796097L;

	public EnvironmentException() {
	}

	public EnvironmentException(String message) {
		super(message);
	}

	public EnvironmentException(Throwable cause) {
		super(cause);
	}

	public EnvironmentException(String message, Throwable cause) {
		super(message, cause);
	}
}
