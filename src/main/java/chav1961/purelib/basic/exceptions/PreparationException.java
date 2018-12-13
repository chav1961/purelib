package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception was developed especially for indicating problems in the static blocks of your classes. Template to fire it is:</p>
 * <code>
 * boolean errorsInStatic = false;
 * static {<br>
 * try{<br>
 * . . .<br>
 * } catch (Exception exc) {<br>
 * errorsInStatic = true;<br>
 * }<br>
 * }<br>
 * . . .<br>
 * public void myMethod(. . .) {<br>
 * if (errorsInStatic) {<br>
 * throw new PreparationException(. . .);<br>
 * }<br>
 * else {<br>
 * . . .<br>
 * }<br>
 * }<br>
 * </code>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2 last updated 0.0.3
 */
public class PreparationException extends RuntimeException {
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
