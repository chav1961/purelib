package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems on any calculations. Don't use this exception by another way</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public class CalculationException extends Exception {
	private static final long serialVersionUID = 497057975715289865L;

	public CalculationException() {
		super();
	}

	public CalculationException(String message, Throwable cause) {
		super(message, cause);
	}

	public CalculationException(String message) {
		super(message);
	}

	public CalculationException(Throwable cause) {
		super(cause);
	}
}
