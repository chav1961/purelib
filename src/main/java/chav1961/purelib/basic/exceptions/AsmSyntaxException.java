package chav1961.purelib.basic.exceptions;

import java.io.IOException;

/**
 * <p>This exception describes any problems on syntax parsing of assembly data</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 *
 */
public class AsmSyntaxException extends IOException {
	private static final long serialVersionUID = -2503248491829944827L;
	
	public AsmSyntaxException() {super();}
	public AsmSyntaxException(String message, Throwable cause) {super(message, cause);}
	public AsmSyntaxException(String message) {super(message);}
	public AsmSyntaxException(Throwable cause) {super(cause);}
}

