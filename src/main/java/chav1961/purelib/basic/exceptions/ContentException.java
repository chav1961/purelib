package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems on content parsing of data. Differ to {@link SyntaxException}
 * it not contains row and column localization for problems detected</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.3
 */
public class ContentException extends PureLibException {
	private static final long serialVersionUID = -2374543706560983123L;

	public ContentException() {
	}

	public ContentException(String message) {
		super(message);
	}

	public ContentException(Throwable cause) {
		super(cause);
	}

	public ContentException(String message, Throwable cause) {
		super(message, cause);
	}
}
