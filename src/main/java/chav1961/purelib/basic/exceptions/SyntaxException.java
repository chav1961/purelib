package chav1961.purelib.basic.exceptions;

public class SyntaxException extends Exception {
	private static final long serialVersionUID = 8141880743233589596L;

	public SyntaxException(final int lineNo, final int pos, final String message, final Throwable t) {
		super("Line "+lineNo+", pos "+pos+": "+message,t);
	}

	public SyntaxException(final int lineNo, final int pos, final String message) {
		super("Line "+lineNo+", pos "+pos+": "+message);
	}
}
