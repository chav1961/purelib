package chav1961.purelib.basic.exceptions;

import java.net.URI;

/**
 * <p>This exception describes any problems on any calculations. Don't use this exception by another way</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @last.update 0.0.3
 */
public class CalculationException extends PureLibException {
	private static final long serialVersionUID = 497057975715289865L;

	/**
	 * <p>Constructor of the class</p>
	 */
	public CalculationException() {
		super();
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 * @param cause exception cause
	 */
	public CalculationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 */
	public CalculationException(final String message) {
		super(message);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param cause exception cause
	 */
	public CalculationException(final Throwable cause) {
		super(cause);
	}

	/**
	 * <p>Constructor of the class for localizable messages</p>
	 * @param cause exception cause. Can be null
	 * @param messageId message id. Can't be null, and must have format &lt;localizerURI#messageId&gt;
	 * @param parameters message parameters. Can be empty but not null
	 * @since 0.0.7
	 */
	public CalculationException(final Throwable cause, final URI messageId, final Object... parameters) {
		super(cause, messageId, parameters);
	}

	/**
	 * <p>Constructor of the class for localizable messages</p>
	 * @param messageId message id. Can't be null, and must have format &lt;localizerURI#messageId&gt;
	 * @param parameters message parameters. Can be empty but not null
	 * @since 0.0.7
	 */
	public CalculationException(final URI messageId, final Object... parameters) {
		super(messageId, parameters);
	}
}
