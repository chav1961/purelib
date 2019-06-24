package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems on command string processing (from console or command line).
 * It's strongly recommended to use it in this case only</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.3
 */
public class ConsoleCommandException extends ContentException {
	private static final long serialVersionUID = 8876250986756751250L;

	public ConsoleCommandException() {
		super();
	}

	public ConsoleCommandException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ConsoleCommandException(String arg0) {
		super(arg0);
	}

	public ConsoleCommandException(Throwable arg0) {
		super(arg0);
	}
}
