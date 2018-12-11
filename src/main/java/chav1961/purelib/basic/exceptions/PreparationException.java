package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems on preparation of any kind of entities</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 *
 */
public class PreparationException extends Exception {
	private static final long serialVersionUID = -1189827491666686473L;

	public PreparationException() {
		super();
	}

	public PreparationException(String message, Throwable cause) {
		super(message, cause);
	}

	public PreparationException(String message) {
		super(message);
	}

	public PreparationException(Throwable cause) {
		super(cause);
	}
}
