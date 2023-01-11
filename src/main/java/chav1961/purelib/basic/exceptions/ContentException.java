package chav1961.purelib.basic.exceptions;

import java.net.URI;

/**
 * <p>This exception describes any problems on content parsing of data. Differ to {@link SyntaxException}
 * it not contains row and column localization for problems detected</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @last.update 0.0.7
 */
public class ContentException extends PureLibException {
	private static final long serialVersionUID = -2374543706560983123L;

	/**
	 * <p>Constructor of the class</p>
	 */
	public ContentException() {
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 */
	public ContentException(final String message) {
		super(message);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param cause exception cause
	 */
	public ContentException(final Throwable cause) {
		super(cause);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 * @param cause exception cause
	 */
	public ContentException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * <p>Constructor of the class for localizable messages</p>
	 * @param cause exception cause. Can be null
	 * @param messageId message id. Can't be null, and must have format &lt;localizerURI#messageId&gt;
	 * @param parameters message parameters. Can be empty but not null
	 * @since 0.0.7
	 */
	public ContentException(Throwable cause, URI messageId, Object... parameters) {
		super(cause, messageId, parameters);
	}

	/**
	 * <p>Constructor of the class for localizable messages</p>
	 * @param messageId message id. Can't be null, and must have format &lt;localizerURI#messageId&gt;
	 * @param parameters message parameters. Can be empty but not null
	 * @since 0.0.7
	 */
	public ContentException(URI messageId, Object... parameters) {
		super(messageId, parameters);
	}
}
