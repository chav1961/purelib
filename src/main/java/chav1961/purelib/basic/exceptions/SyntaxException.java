package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems on syntax during parsing any kind of data. It's a special form of exception, that
 * contains explicit row and column numbers for better problem localization. It always uses in the case, when we need localize 
 * syntax problems in some entity</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1 
 * @lastUpdate 0.0.3
 */
public class SyntaxException extends ContentException {
	private static final long serialVersionUID = 8141880743233589596L;
	
	private final long	lineNo, pos;

	public SyntaxException(final long lineNo, final long pos, final String message, final Throwable t) {
		super("Line "+lineNo+", pos "+pos+": "+message,t);
		this.lineNo = lineNo;
		this.pos = pos;
	}

	public SyntaxException(final long lineNo, final long pos, final String message) {
		super("Line "+lineNo+", pos "+pos+": "+message);
		this.lineNo = lineNo;
		this.pos = pos;
	}
	
	public long getRow() {
		return lineNo;
	}
	
	public long getCol() {
		return pos;
	}
}
