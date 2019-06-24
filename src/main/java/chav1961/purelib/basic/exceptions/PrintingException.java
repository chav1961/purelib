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

	public PrintingException() {
		super();
	}

	public PrintingException(final String message) {
		super(message);
	}

	public PrintingException(final Throwable t) {
		super(t);
	}
	
	public PrintingException(final String message, final Throwable t) {
		super(message,t);
	}
}
