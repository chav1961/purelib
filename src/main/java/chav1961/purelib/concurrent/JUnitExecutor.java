package chav1961.purelib.concurrent;

import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.testing.TestingUtils;

/**
 * <p>This class is especially used inside JUnit tests to test multi-threaded components. It supports interaction between two {@linkplain Thread} instances
 * and strongly synchronized their rendezvou points. Typical scenario to use this class is:</p>
 * <ul>
 * <li>master thread starts slave thread and sends the JUnitExecutor instance to it. The only action in the slave thread is to call {@linkplain #waitCommand(CommandProcessor)} method on it</li>
 * <li>master thread calls {@linkplain #execute(Object, long, Object...)} method to send any command to the slave one.</li>
 * <li>slave thread executes callback passed in the {@linkplain #waitCommand(CommandProcessor)}</li>
 * <li>master thread calls {@linkplain #getResponse(long)} to take response from the slave thread and test it</li>
 * </ul>
 * <p>Both of the {@linkplain #execute(Object, long, Object...)} and {@linkplain #getResponse(long)} can be interrupted by {@linkplain Thread#interrupt()} call and by timeout. It's guaranteed, that
 * return from {@linkplain #execute(Object, long, Object...)} method in the master is exactly before call {@linkplain #waitCommand(CommandProcessor)} method callback in the slave. It's guaranteed, that return 
 * from {@linkplain #getResponse(long)} method in the master are exactly after return from {@linkplain #waitCommand(CommandProcessor)} method callback in the slave.</p>
 * <p>If any exception has been thrown in the {@linkplain #waitCommand(CommandProcessor)} method callback, it fires {@linkplain DebuggingException} in {@linkplain #getResponse(long)} method call. Method
 * {@linkplain DebuggingException#getCause()} can be used to get source exception from the {@linkplain #waitCommand(CommandProcessor)} method callback.</p>    
 * <p>Example to use this class see JUnit test case for it.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @param <Command> any type to use as a "command"
 * @param <Response> any type to use as a "response"
 */
public class JUnitExecutor<Command,Response> {
	/**
	 * <p>This interface is used for lambda-styled call to process "command" inside thread</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 * @param <Command> any type to use as a "command"
	 * @param <Response> any type to use as a "response"
	 */
	@FunctionalInterface
	public interface CommandProcessor<Command,Response> {
		/**
		 * <p>Process "command" with the given parameters and return "response".</p>
		 * @param command any object to identify "command" (for example, {@linkplain String} "callMethod1")
		 * @param parameters any additional parameters to use with the given "command"
		 * @return any object to identify "response" from threads
		 * @throws Throwable if any exception are detected in the thread
		 */
		Response process(Command command, Object... parameters) throws Throwable;
	}
	
	private final Exchanger<Command>		cmd = new Exchanger<>();
	private final BlockingQueue<Object>		resp = new ArrayBlockingQueue<>(10);
	private final PrintStream				ps = TestingUtils.err();
	private final boolean 					trace;
	private volatile Object[]				parameters = null;

	/**
	 * <p>Constructor of the class</p>
	 */
	public JUnitExecutor(){
		this(false);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param trace turn on debugging trace while test execution. Use this parameter to find problems in your JUnit test only 
	 */
	public JUnitExecutor(final boolean trace) {
		this.trace = trace;
	}

	/**
	 * <p>This call must be used in the slave thread to get commands from the master one.</p>
	 * @param proc callback to process commands
	 * @throws InterruptedException when the slave thread was interrupted
	 * @throws NullPointerException when callback is null
	 */
	public void waitCommand(final CommandProcessor<Command, Response> proc) throws InterruptedException, NullPointerException {
		if (proc == null) {
			throw new NullPointerException("Command processor can't be null");
		}
		else {
			try{if (trace) {
					ps.println("Before getting command...");
				}
				final Command	c = cmd.exchange(null);
				
				if (trace) {
					ps.println("Got command: "+c);
				}
				final Response	r = proc.process(c,parameters);
				
				if (trace) {
					ps.println("After processing command "+c+": "+r);
				}
				resp.put(r);
			} catch (InterruptedException e) {
				throw e;
			} catch (Throwable e) {
				resp.put(e);
			}
		}
	}
	
	/**
	 * <p>Send command and parameters to slave thread.</p>
	 * @param command any instance to identify command
	 * @param timeoutMillis timeout to execute method
	 * @param parameters additional parameters for the command
	 * @return true if command was successfully sent to the slave thread, false otherwise
	 * @throws NullPointerException when command instance is null
	 * @throws IllegalArgumentException when timeout is zero or negative
	 */
	public boolean execute(final Command command, final long timeoutMillis, final Object... parameters) throws NullPointerException, IllegalArgumentException {
		if (command == null) {
			throw new NullPointerException("Command can't be null");
		}
		else if (timeoutMillis <= 0) {
			throw new IllegalArgumentException("Timeout ["+timeoutMillis+"] must be positive");
		}
		else {
			try{this.parameters = parameters;
				cmd.exchange(command,timeoutMillis,TimeUnit.MILLISECONDS);
				return true;
			} catch (InterruptedException | TimeoutException e) {
				return false;
			}
		}
	}
	
	/**
	 * <p>Has response form the slave thread</p>
	 * @return true if yes
	 */
	public boolean hasResponse() {
		return !resp.isEmpty();
	}
	
	/**
	 * <p>Get response form the slave thread</p>
	 * @param timeoutMillis timeout to wait response
	 * @return response instance from the slave thread
	 * @throws DebuggingException when the save thread threw exception during callback processing
	 * @throws InterruptedException when the master treads interrupts
	 * @throws IllegalArgumentException when timeout is zero or negative
	 */
	@SuppressWarnings("unchecked")
	public Response getResponse(final long timeoutMillis) throws DebuggingException, InterruptedException, IllegalArgumentException {
		if (timeoutMillis <= 0) {
			throw new IllegalArgumentException("Timeout ["+timeoutMillis+"] must be positive");
		}
		else {
			final Object	result = resp.poll(timeoutMillis,TimeUnit.MILLISECONDS);
	
			if (result instanceof Throwable) {
				throw new DebuggingException((Throwable)result); 
			}
			else {
				return (Response)result;
			}
		}
	}

	/**
	 * <p>Synchronously call slave thread with the command and return it's response</p>
	 * @param cmd any instance to identify command
	 * @param parameters additional parameters for the command
	 * @return response instance
	 * @throws DebuggingException when the save thread threw exception during callback processing
	 * @throws InterruptedException when the master treads interrupts
	 * @throws NullPointerException when command instance is null
	 */
	public Response call(final Command cmd, final Object... parameters) throws DebuggingException, InterruptedException, NullPointerException {
		if (execute(cmd,10000,parameters)) {
			return getResponse(10000);
		}
		else {
			throw new DebuggingException("Async call failed");
		}
	}
}
