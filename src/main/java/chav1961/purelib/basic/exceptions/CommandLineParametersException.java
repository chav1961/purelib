package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems on command line parameters. Don't use this exception by another way</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @last.update 0.0.3
 */
public class CommandLineParametersException extends ContentException {
	private static final long serialVersionUID = -1146095723752754080L;

	/**
	 * <p>Constructor of the class</p>
	 */
	public CommandLineParametersException() {
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 */
	public CommandLineParametersException(final String message) {
		super(message);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param cause exception cause
	 */
	public CommandLineParametersException(final Throwable cause) {
		super(cause);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 * @param cause exception cause
	 */
	public CommandLineParametersException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
