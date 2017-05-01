package chav1961.purelib.basic.exceptions;

public class PrintingException extends Exception {
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
