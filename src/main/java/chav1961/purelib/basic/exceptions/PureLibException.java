package chav1961.purelib.basic.exceptions;

import java.net.URI;

import chav1961.purelib.i18n.interfaces.Localizer;

/**
 * <p>This exception is a root of all Pure Library exceptions.</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @last.update 0.0.7
 */
public class PureLibException extends Exception {
	private static final long serialVersionUID = -6282248007936959334L;
	
	private final URI		messageId;
	private final Object[]	parameters;

	/**
	 * <p>Constructor of the class</p>
	 */
	public PureLibException() {
		this.messageId = null;
		this.parameters = null;
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 * @param cause exception cause
	 */
	public PureLibException(final String message, final Throwable cause) {
		super(message, cause);
		this.messageId = null;
		this.parameters = null;
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 */
	public PureLibException(final String message) {
		super(message);
		this.messageId = null;
		this.parameters = null;
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param cause exception cause
	 */
	public PureLibException(final Throwable cause) {
		super(cause);
		this.messageId = null;
		this.parameters = null;
	}

	/**
	 * <p>Constructor of the class for localizable messages</p>
	 * @param messageId message id. Can't be null, and must have format &lt;localizerURI#messageId&gt;
	 * @param parameters message parameters. Can be empty but not null
	 * @since 0.0.7
	 */
	public PureLibException(final URI messageId, final Object... parameters) {
		if (messageId == null) {
			throw new NullPointerException("Message Id can't be null"); 
		}
		else {
			this.messageId = null;
			this.parameters = null;
		}
	}

	/**
	 * <p>Constructor of the class for localizable messages</p>
	 * @param cause exception cause. Can be null
	 * @param messageId message id. Can't be null, and must have format &lt;localizerURI#messageId&gt;
	 * @param parameters message parameters. Can be empty but not null
	 * @since 0.0.7
	 */
	public PureLibException(final Throwable cause, final URI messageId, final Object... parameters) {
		super(cause);
		if (messageId == null) {
			throw new NullPointerException("Message Id can't be null"); 
		}
		else {
			this.messageId = null;
			this.parameters = null;
		}
	}

	/**
	 * <p>Get message by using {@linkplain Localizer}.</p>
	 * @since 0.0.7
	 */
	@Override
	public String getMessage() {
		return getLocalizedMessage();
	}
	
	/**
	 * <p>Get localized message by using {@linkplain Localizer}.</p>
	 * @since 0.0.7
	 */
	@Override
	public String getLocalizedMessage() {
		if (isLocalized()) {
			return Localizer.getValue(messageId).formatted(parameters);
		}
		else {
			return super.getLocalizedMessage();
		}
	}
	
	protected boolean isLocalized() {
		return messageId != null;
	}
}
