package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems on syntax parsing of data during I/O operations. It's a special form of exception, that
 * contains explicit row and column numbers for better problem localization. It always uses in the case, when we need localize 
 * syntax problems in some entity</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 *
 */
public class SyntaxException extends Exception {
	private static final long serialVersionUID = 8141880743233589596L;

	public SyntaxException(final long lineNo, final long pos, final String message, final Throwable t) {
		super("Line "+lineNo+", pos "+pos+": "+message,t);
	}

	public SyntaxException(final long lineNo, final long pos, final String message) {
		super("Line "+lineNo+", pos "+pos+": "+message);
	}
}
