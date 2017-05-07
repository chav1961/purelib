package chav1961.purelib.basic.exceptions;

import chav1961.purelib.basic.ConsoleCommandManager;

/**
 * <p>This exception describes any problems on console command processing. It's a special exception related to {@link ConsoleCommandManager} 
 * class and describes any problems detected on the console command processing stage. To restrict it's usage for this special case, it is declared
 * as final.</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 *
 */
public final class ConsoleCommandException extends Exception {
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
