package chav1961.purelib.basic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import chav1961.purelib.basic.interfaces.LoggerFacade;

/**
 * <p>This class implements basic functionality for the {@link LoggerFacade} interface. To use this class, you need implement two methods:</p>
 * <ul>
 * <li>{link {@link #getAbstractLoggerFacade(String, Class)} - create logger instance for the new transaction</li>
 * <li>{link {@link #toLogger(chav1961.purelib.basic.interfaces.LoggerFacade.Severity, String, Throwable)} - log message by any way</li>
 * </ul>
 * 
 * <p>The good examples of the implementation are {@link SystemErrLoggerFacade} and {@link DefaultLoggerFacade} classes in this peckage.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see LoggerFacade
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public abstract class AbstractLoggerFacade implements LoggerFacade {
	private final List<Set<Reducing>>		stack = new ArrayList<Set<Reducing>>(){{add(0,new HashSet<Reducing>());}};
	private final Set<StackTraceElement>	repeatables = new HashSet<StackTraceElement>();
	private final boolean					inTransaction;
	private final Class<?>					transactionRoot;
	private final List<TransactionMessage>	messages;
	
	public AbstractLoggerFacade() {
		this.inTransaction = false;
		this.transactionRoot = null;
		this.messages = null;
	}

	protected AbstractLoggerFacade(final String mark, final Class<?> root) {
		this.inTransaction = true;
		this.transactionRoot = root;
		this.messages = new ArrayList<TransactionMessage>();
	}

	protected abstract AbstractLoggerFacade getAbstractLoggerFacade(String mark, Class<?> root);
	protected abstract void toLogger(Severity level, String text, Throwable throwable);
	
	@Override
	public void close() {
		if (messages != null) {
			for (TransactionMessage item : messages) {
				toLogger(item.level,item.text,item.exception);
			}
			messages.clear();
		}
		stack.clear();
	}
	
	@Override
	public LoggerFacade message(Severity level, String format, Object... parameters) {
		if (level == null) {
			throw new IllegalArgumentException("Level can't be null");
		}
		else if (format == null || format.isEmpty()) {
			throw new IllegalArgumentException("Message format can't be null or empty");
		}
		else if (isLoggedNow(level)) {
			if (parameters != null && parameters.length > 0) {
				send(level,String.format(format,parameters));
			}
			else {
				send(level,format);
			}
		}
		return this;
	}

	@Override
	public LoggerFacade message(Severity level, LoggerCallbackInterface callback) {
		if (level == null) {
			throw new IllegalArgumentException("Level can't be null");
		}
		else if (callback == null) {
			throw new IllegalArgumentException("Message callback can't be null or empty");
		}
		else if (isLoggedNow(level)) {
			send(level,callback.process());
		}
		return this;
	}
	
	@Override
	public LoggerFacade message(Severity level, Throwable exception, String format, Object... parameters) {
		if (level == null) {
			throw new IllegalArgumentException("Level can't be null");
		}
		else if (exception == null) {
			throw new IllegalArgumentException("Exception can't be null");
		}
		else if (format == null || format.isEmpty()) {
			throw new IllegalArgumentException("Message format can't be null or empty");
		}
		else if (isLoggedNow(level)) {
			if (parameters != null && parameters.length > 0) {
				send(level,String.format(format,parameters),exception);
			}
			else {
				sendWithThrowable(level,format,exception);
			}
		}
		return this;
	}

	@Override
	public LoggerFacade message(Severity level, Throwable exception, LoggerCallbackInterface callback) {
		if (level == null) {
			throw new IllegalArgumentException("Level can't be null");
		}
		else if (exception == null) {
			throw new IllegalArgumentException("Exception can't be null");
		}
		else if (callback == null) {
			throw new IllegalArgumentException("Message callback can't be null or empty");
		}
		else if (isLoggedNow(level)) {
			sendWithThrowable(level,callback.process(),exception);
		}
		return this;
	}
	
	@Override 
	public boolean isLoggedNow(Severity level) {
		return true;
	}

	@Override
	public Set<Reducing> getReducing() {
		return stack.get(0);
	}

	@Override
	public LoggerFacade setReducing(Set<Reducing> reducing) {
		if (reducing == null) {
			throw new IllegalArgumentException("Reducing set can't be null");
		}
		else {
			stack.set(0,reducing);
			return this;
		}
	}

	@Override
	public LoggerFacade pushReducing(Set<Reducing> reducing) {
		if (reducing == null) {
			throw new IllegalArgumentException("Reducing set can't be null");
		}
		else {
			stack.add(0,reducing);
			return this;
		}
	}

	@Override
	public LoggerFacade popReducing() {
		if (stack.size() == 0) {
			throw new IllegalStateException("Reducing stack exhausted!");
		}
		else {
			stack.remove(0);
			return this;
		}
	}

	@Override
	public LoggerFacade transaction(final String mark) {
		final Throwable		t = new Throwable();
		try{final Class<?>	root = Class.forName(t.getStackTrace()[1].getClassName());
			return getAbstractLoggerFacade(mark,root); 
		} catch (ClassNotFoundException e) {
			return getAbstractLoggerFacade(mark,this.getClass()); 
		}
	}

	@Override
	public void rollback() {
		messages.clear();
	}

	private void send(Severity level, String text) {
		if (inTransaction) {
			messages.add(new TransactionMessage(level, text));
		}
		else {
			toLogger(level,text,null);
		}
	}

	private void send(Severity level, String text, Throwable convert) {
		if (inTransaction) {
			messages.add(new TransactionMessage(level, text, convert));
		}
		else {
			toLogger(level,text,convert);
		}
	}
	
	private void sendWithThrowable(Severity level, String text, Throwable exception) {
		if (needPrintException(exception)) {
			send(level,text,convert(exception));
		}
	}

	private boolean needPrintException(final Throwable exception) {
		if (getReducing().contains(Reducing.reduceRuntimeExceptions)) {
			if (exception instanceof RuntimeException) {
				if (repeatables.contains(exception.getStackTrace()[0])) {
					return false;
				}
				else {
					repeatables.add(exception.getStackTrace()[0]);
					return true;					
				}
			}
			else {
				return true;
			}
		}
		else {
			return true;
		}
	}
	
	private Throwable convert(Throwable exception) {
		final StringBuilder	sb = new StringBuilder(exception.getMessage());
		StackTraceElement[]	list = exception.getStackTrace();		
		
		if (getReducing().contains(Reducing.reduceCause) && exception.getCause() != null) {
			for(Throwable forStack = exception; forStack != null; forStack = forStack.getCause()){
				sb.append('\n').append(forStack.getMessage());
				list = exception.getStackTrace();
			}
			try{final Field		causeField = exception.getClass().getDeclaredField("cause"), messageField = exception.getClass().getDeclaredField("detailMessage");
				
				causeField.setAccessible(true);			causeField.set(exception,null);
				messageField.setAccessible(true);		messageField.set(exception,sb.toString());
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			}  
		}
		
		final List<StackTraceElement>	source = new ArrayList<StackTraceElement>();
		
		source.addAll(Arrays.asList(list));
		if (getReducing().contains(Reducing.reduceJREPath)) {
			for (int index = source.size()-1; index >= 0; index--) {
				if (source.get(index).getClassName().startsWith("java.") || source.get(index).getClassName().startsWith("javax.") || source.get(index).getClassName().startsWith("sun.")) {
					source.remove(index);
				}
			}
		}
		if (getReducing().contains(Reducing.reduceOverSubtree) && transactionRoot != null) {
			for (int index = source.size()-1; index >= 0; index--) {
				if (source.get(index).getClassName().startsWith(transactionRoot.getPackage().getName())) {
					source.remove(index);
				}
			}
		}
		exception.setStackTrace(source.toArray(new StackTraceElement[source.size()]));
		return exception;
	}
	
	private static class TransactionMessage {
		final Severity 	level;
		final String	text;
		final Throwable	exception;
		
		TransactionMessage(Severity level, String text, Throwable exception) {
			this.level = level;
			this.text = text;
			this.exception = exception;
		}
		
		TransactionMessage(Severity level, String text) {
			this.level = level;
			this.text = text;
			this.exception = null;
		}
	}
}
