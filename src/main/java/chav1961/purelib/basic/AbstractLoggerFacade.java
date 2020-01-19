package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import chav1961.purelib.basic.exceptions.intern.ReducedExceptionWrapper;
import chav1961.purelib.basic.interfaces.LoggerFacade;

/**
 * <p>This class implements basic functionality for the {@link LoggerFacade} interface. To use this class, you need implement two methods:</p>
 * <ul>
 * <li>{link {@link #getAbstractLoggerFacade(String, Class)} - create logger instance for the new transaction</li>
 * <li>{link {@link #toLogger(chav1961.purelib.basic.interfaces.LoggerFacade.Severity, String, Throwable)} - log message by any way</li>
 * </ul>
 * 
 * <p>The good examples of the implementation are {@link SystemErrLoggerFacade} and {@link DefaultLoggerFacade} classes in this package.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see LoggerFacade
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.4
 */

public abstract class AbstractLoggerFacade implements LoggerFacade {
	private final List<Set<Reducing>>		stack = new ArrayList<Set<Reducing>>();
	private final Set<StackTraceElement>	repeatables = new HashSet<StackTraceElement>();
	private final boolean					inTransaction;
	private final Class<?>					transactionRoot;
	private final List<TransactionMessage>	messages;
	
	public AbstractLoggerFacade() {
		this.inTransaction = false;
		this.transactionRoot = null;
		this.messages = null;
		this.stack.add(0,new HashSet<Reducing>());
	}

	protected AbstractLoggerFacade(final String mark, final Class<?> root, final Set<Reducing> reducing) {
		this.inTransaction = true;
		this.transactionRoot = root;
		this.messages = new ArrayList<TransactionMessage>();
		this.stack.add(0,reducing);
	}

	protected abstract AbstractLoggerFacade getAbstractLoggerFacade(final String mark, final Class<?> root);
	protected abstract void toLogger(final Severity level, final String text, final Throwable throwable);
	
	@Override
	public void close() {
		if (messages != null) {
			for (TransactionMessage item : messages) {
				toLogger(item.level,item.text,item.exception);
			}
			messages.clear();
		}
		repeatables.clear();
		stack.clear();
	}
	
	@Override
	public LoggerFacade message(final Severity level, final String format, final Object... parameters) throws NullPointerException {
		if (level == null) {
			throw new NullPointerException("Level can't be null");
		}
		else if (format == null) {
			throw new NullPointerException("Message format can't be null");
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
	public LoggerFacade message(final Severity level, final LoggerCallbackInterface callback) throws NullPointerException {
		if (level == null) {
			throw new NullPointerException("Level can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Message callback can't be null or empty");
		}
		else if (isLoggedNow(level)) {
			send(level,callback.process());
		}
		return this;
	}
	
	@Override
	public LoggerFacade message(final Severity level, final Throwable exception, final String format, final Object... parameters) throws NullPointerException {
		if (level == null) {
			throw new NullPointerException("Level can't be null");
		}
		else if (exception == null) {
			throw new NullPointerException("Exception can't be null");
		}
		else if (format == null) {
			throw new NullPointerException("Message format can't be null");
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
	public LoggerFacade message(final Severity level, final Throwable exception, final LoggerCallbackInterface callback) throws NullPointerException {
		if (level == null) {
			throw new NullPointerException("Level can't be null");
		}
		else if (exception == null) {
			throw new NullPointerException("Exception can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Message callback can't be null or empty");
		}
		else if (isLoggedNow(level)) {
			sendWithThrowable(level,callback.process(),exception);
		}
		return this;
	}
	
	@Override 
	public boolean isLoggedNow(final Severity level) {
		if (level == null) {
			throw new NullPointerException("Severity level to test can't be null");
		}
		else {
			return true;
		}
	}

	@Override
	public Set<Reducing> getReducing() {
		return stack.get(0);
	}

	@Override
	public LoggerFacade setReducing(final Set<Reducing> reducing) {
		if (reducing == null) {
			throw new NullPointerException("Reducing set can't be null");
		}
		else {
			stack.set(0,reducing);
			return this;
		}
	}

	@Override
	public LoggerFacade setReducing(final Reducing... reducing) throws NullPointerException {
		if (reducing == null) {
			throw new NullPointerException("Reducing list can't be null");
		}
		else if (Utils.checkArrayContent4Nulls(reducing) >= 0) {
			throw new NullPointerException("Nulls inside redicung list");
		}
		else {
			final Set<Reducing>	set = new HashSet<>();
			
			set.addAll(Arrays.asList(reducing));
			return setReducing(set);
		}
	}

	@Override
	public LoggerFacade pushReducing(final Set<Reducing> reducing) throws NullPointerException {
		if (reducing == null) {
			throw new NullPointerException("Reducing set can't be null");
		}
		else {
			stack.add(0,reducing);
			return this;
		}
	}

	@Override
	public LoggerFacade pushReducing(final Reducing... reducing) throws NullPointerException {
		if (reducing == null) {
			throw new NullPointerException("Reducing list can't be null");
		}
		else {
			final Set<Reducing>	set = new HashSet<>();
			
			set.addAll(Arrays.asList(reducing));
			return pushReducing(set);
		}
	}
	
	@Override
	public LoggerFacade popReducing() {
		if (stack.size() <= 1) {
			throw new IllegalStateException("Reducing stack exhausted!");
		}
		else {
			stack.remove(0);
			return this;
		}
	}

	@Override
	public LoggerFacade transaction(final String mark) {
		if (mark == null || mark.isEmpty()) {
			throw new IllegalArgumentException("String mark ca't be null or empty");
		}
		else {
			try{final StackTraceElement[]	stack = Thread.currentThread().getStackTrace();
				final Class<?>				root = stack.length > 1 ? Thread.currentThread().getContextClassLoader().loadClass(stack[1].getClassName()) : this.getClass();
				
				return transaction(mark,root); 
			} catch (ClassNotFoundException e) {
				return transaction(mark,this.getClass()); 
			}
		}
	}

	@Override
	public LoggerFacade transaction(final String mark, final Class<?> root) throws NullPointerException, IllegalArgumentException {
		if (mark == null || mark.isEmpty()) {
			throw new IllegalArgumentException("String mark can't be null or empty");
		}
		else if (root == null) {
			throw new NullPointerException("Root class can't be null");
		}
		else {
			return getAbstractLoggerFacade(mark,root); 
		}
	}
	
	
	@Override
	public void rollback() {
		if (messages != null) {
			messages.clear();
		}
	}

	private void send(final Severity level, final String text) {
		if (inTransaction) {
			messages.add(new TransactionMessage(level, text));
		}
		else {
			toLogger(level,text,null);
		}
	}

	private void send(final Severity level, final String text, final Throwable convert) {
		if (inTransaction) {
			messages.add(new TransactionMessage(level, text, convert));
		}
		else {
			toLogger(level,text,convert);
		}
	}
	
	private void sendWithThrowable(final Severity level, final String text, final Throwable exception) {
		if (needPrintException(exception,getReducing(),repeatables)) {
			send(level,text,convert(exception,getReducing(),transactionRoot));
		}
	}

	static boolean needPrintException(final Throwable exception, final Set<Reducing> reducing, final Set<StackTraceElement> repeatables) {
		if (reducing.contains(Reducing.reduceRuntimeExceptions)) {
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
	
	static Throwable convert(final Throwable sourceException, final Set<Reducing> reducing, final Class<?> transactionRoot) {
		Throwable			exception = sourceException;
		StackTraceElement[]	list = exception.getStackTrace();		
		
		if (reducing.contains(Reducing.reduceCause) && exception.getCause() != null) {
			final StringBuilder	sb = new StringBuilder();
			
			for(Throwable forStack = exception; forStack != null; forStack = forStack.getCause()){
				sb.append('\n').append(forStack.getClass().getSimpleName()).append(": ").append(forStack.getLocalizedMessage());
				list = forStack.getStackTrace();
			}
			exception = new ReducedExceptionWrapper(sb.substring(1),exception);
			exception.setStackTrace(list);
		}
		
		final List<StackTraceElement>	source = new ArrayList<StackTraceElement>();
		
		source.addAll(Arrays.asList(list));
		if (reducing.contains(Reducing.reduceJREPath)) {
			for (int index = source.size()-1; index >= 0; index--) {
				final String	className = source.get(index).getClassName();
				
				if (className.startsWith("java.") || className.startsWith("javax.") || className.startsWith("sun.")) {
					source.remove(index);
				}
			}
		}
		if (reducing.contains(Reducing.reduceOverSubtree) && transactionRoot != null) {
			final String	subtreeRoot = transactionRoot.getCanonicalName();
			
			for (int index = 0, maxIndex = source.size(); index < maxIndex; index++) {
				if (source.get(index).getClassName().equals(subtreeRoot)) {
					while (source.size() > index+1) {
						source.remove(index);
					}
					break;
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
		
		TransactionMessage(final Severity level, final String text, final Throwable exception) {
			this.level = level;
			this.text = text;
			this.exception = exception;
		}
		
		TransactionMessage(final Severity level, final String text) {
			this.level = level;
			this.text = text;
			this.exception = null;
		}

		@Override
		public String toString() {
			return "TransactionMessage [level=" + level + ", text=" + text + ", exception=" + exception + "]";
		}
	}
}
