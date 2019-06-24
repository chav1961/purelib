package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems on flow processing. Don't use this exception by another way</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.3
 */
public class FlowException extends PureLibException {
	private static final long serialVersionUID = -4178612026736414604L;

	public FlowException() {
		super();
	}

	public FlowException(String message, Throwable cause) {
		super(message, cause);
	}

	public FlowException(String message) {
		super(message);
	}

	public FlowException(Throwable cause) {
		super(cause);
	}
}
