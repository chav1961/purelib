package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes specific problems on MOME content parsing.</p>  
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4 
 */
public class MimeParseException extends SyntaxException {
	private static final long serialVersionUID = 2129154276870686344L;

	/**
	 * <p>Constructor of the class</p>
	 * @param lineNo line inside content where problem was detected
	 * @param pos position inside content where problem was detected
	 * @param message problem description
	 * @param t exception cause
	 */
	public MimeParseException(final long lineNo, final long pos, final String message, final Throwable t) {
		super(lineNo, pos, message, t);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param lineNo line inside content where problem was detected
	 * @param pos position inside content where problem was detected
	 * @param message problem description
	 */
	public MimeParseException(final long lineNo, final long pos, final String message) {
		super(lineNo, pos, message);
	}
}
