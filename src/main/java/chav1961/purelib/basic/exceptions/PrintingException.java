package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems on writing data into the stream (not only printers)</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.3
 */
public class PrintingException extends ContentException {
	private static final long serialVersionUID = 6747558191548783494L;

	/**
	 * <p>Constructor of the class</p>
	 */
	public PrintingException() {
		super();
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 */
	public PrintingException(final String message) {
		super(message);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param cause exception cause
	 */
	public PrintingException(final Throwable cause) {
		super(cause);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 * @param cause exception cause
	 */
	public PrintingException(final String message, final Throwable cause) {
		super(message,cause);
	}
}
