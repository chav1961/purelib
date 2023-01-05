package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception was developed especially for indicating problems in the static blocks of your classes. Template to fire it is:</p>
 * <code>
 * <b>static boolean</b> errorsInStatic = <b>false</b>;<br>
 * <b>static</b> {<br>
 * &nbsp;<b>try</b> {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;. . .<br>
 * &nbsp;} <b>catch</b> (Exception exc) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;errorsInStatic = <b>true</b>;<br>
 * &nbsp;}<br>
 * }<br>
 * . . .<br>
 * <b>public void</b> myMethod(. . .) {<br>
 * &nbsp;<b>if</b> (errorsInStatic) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<b>throw new</b> PreparationException(. . .);<br>
 * &nbsp;}<br>
 * &nbsp;<b>else</b> {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;. . .<br>
 * &nbsp;}<br>
 * }<br>
 * </code>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @last.update 0.0.3
 */
public class PreparationException extends RuntimeException {
	private static final long serialVersionUID = -1189827491666686473L;

	/**
	 * <p>Constructor of the class</p>
	 */
	public PreparationException() {
		super();
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 * @param cause exception cause
	 */
	public PreparationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param message exception message
	 */
	public PreparationException(final String message) {
		super(message);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param cause exception cause
	 */
	public PreparationException(final Throwable cause) {
		super(cause);
	}
}
