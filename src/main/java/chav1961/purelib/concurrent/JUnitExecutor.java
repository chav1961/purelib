package chav1961.purelib.concurrent;

import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.testing.TestingUtils;

public class JUnitExecutor<Command,Response> {
	@FunctionalInterface
	public interface CommandProcessor<Command,Response> {
		Response process(Command command, Object... parameters) throws Throwable;
	}
	
	private final Exchanger<Command>		cmd = new Exchanger<>();
	private final BlockingQueue<Object>		resp = new ArrayBlockingQueue<>(10);
	private final PrintStream				ps = TestingUtils.err();
	private final boolean 					trace;
	private volatile Object[]				parameters = null;

	public JUnitExecutor(){
		this(false);
	}
	
	public JUnitExecutor(final boolean trace){
		this.trace = trace;
	}

	public void waitCommand(CommandProcessor<Command, Response> proc) throws InterruptedException {
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
	
	public boolean execute(final Command command, final long timeoutMillis, final Object... parameters) {
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
	
	public boolean hasResponse() {
		return !resp.isEmpty();
	}
	
	public Response getResponse(final long timeoutMillis) throws FlowException, InterruptedException {
		if (timeoutMillis <= 0) {
			throw new IllegalArgumentException("Timeout ["+timeoutMillis+"] must be positive");
		}
		else {
			final Object	result = resp.poll(timeoutMillis,TimeUnit.MILLISECONDS);
	
			if (result instanceof Throwable) {
				throw new FlowException((Throwable)result); 
			}
			else {
				return (Response)result;
			}
		}
	}

	public Response call(final Command cmd, final Object... parameters) throws FlowException, InterruptedException {
		if (execute(cmd,10000,parameters)) {
			return getResponse(10000);
		}
		else {
			throw new FlowException("Async call failed");
		}
	}
}
