package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems on command string processing (from console or command line).
 * It's strongly recommended to use it in this case only</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.3
 */
public class ConsoleCommandException extends ContentException {
	private static final long serialVersionUID = 8876250986756751250L;

	/**
	 * <p>Constructor of the class</p>
	 */
	public ConsoleCommandException() {
		super();
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 * @param cause exception cause
	 */
	public ConsoleCommandException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 */
	public ConsoleCommandException(final String message) {
		super(message);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param cause exception cause
	 */
	public ConsoleCommandException(final Throwable cause) {
		super(cause);
	}
}
