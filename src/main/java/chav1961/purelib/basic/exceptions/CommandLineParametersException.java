package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems on command line parameters. Don't use this exception by another way</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2 last update 0.0.3
 */
public class CommandLineParametersException extends ContentException {
	private static final long serialVersionUID = -1146095723752754080L;

	public CommandLineParametersException() {
	}

	public CommandLineParametersException(String arg0) {
		super(arg0);
	}

	public CommandLineParametersException(Throwable arg0) {
		super(arg0);
	}

	public CommandLineParametersException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
