package chav1961.purelib.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;

public abstract class AbstractSelectorBasedDispatcher implements Closeable, LoggerFacadeOwner, ExecutionControl {
	private final LoggerFacade	logger;
	private final Selector		selector;
	private final boolean		asServer;
	private volatile boolean	started = false, suspended = false;
	private volatile Thread		t = null;

	@FunctionalInterface
	public interface AddressCallback {
		void process(UUID uuid, SocketAddress addr, Selector sel) throws IOException;
	}
	
    public AbstractSelectorBasedDispatcher(final LoggerFacade logger, final boolean asServer) throws IOException {
    	if (logger == null) {
    		throw new NullPointerException("Logger can't be null");
    	}
    	else {
	    	this.logger = logger;
	        this.selector = Selector.open();
	        this.asServer = asServer;
    	}
    }

	protected abstract void beforeStart(final Selector sel) throws IOException;
	protected abstract void afterStop(final Selector sel) throws IOException;
	protected abstract void registerConnect(final Selector sel, final SelectionKey key) throws IOException;
	protected abstract void registerAccept(final Selector sel, final SelectionKey key) throws IOException;
	protected abstract void read(final Selector sel, final SelectionKey key) throws IOException;
	protected abstract void write(final Selector sel, final SelectionKey key) throws IOException;
    
	@Override
	public LoggerFacade getLogger() {
		return logger;
	}
	
	@Override
	public synchronized void close() throws IOException {
		if (isStarted()) {
			stop();
		}
		selector.close();
	}	

	@Override
	public synchronized void start() throws IOException {
		if (isStarted()) {
			throw new IllegalStateException("Dispatcher is already started");
		}
		else {
			beforeStart(getSelector());
			this.t = new Thread(this::run);
			this.t.setDaemon(true);
			this.t.setName(this.getClass().getSimpleName()+"@"+this.hashCode());
			this.t.start();
		}
	}

	@Override
	public synchronized void suspend() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Dispatcher is not started yet");
		}
		else if (isSuspended()) {
			throw new IllegalStateException("Dispatcher was suspended earlier");
		}
		else {
			suspended = true;
		}
	}

	@Override
	public synchronized void resume() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Dispatcher is not started yet");
		}
		else if (!isSuspended()) {
			throw new IllegalStateException("Dispatcher is not suspended yet");
		}
		else {
			suspended = false;
		}
	}

	@Override
	public synchronized void stop() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Dispatcher is not started or was stopped earlier");
		}
		else {
			t.interrupt();
			try{t.join(1000);} catch (InterruptedException e) {}
			afterStop(getSelector());
		}
	}

	@Override
	public synchronized boolean isStarted() {
		return started;
	}

	@Override
	public synchronized boolean isSuspended() {
		return suspended;
	}

	protected Selector getSelector() {
		return selector;
	}


	protected boolean isServer() {
		return asServer;
	}
	
	private void run() {
	    while (!Thread.currentThread().isInterrupted()) {
	        try{getSelector().select();
	        
		        final Set<SelectionKey> 		selectedKeys = getSelector().selectedKeys();
		        final Iterator<SelectionKey>	iter = selectedKeys.iterator();
		        
		        while (iter.hasNext()) {
		        	final SelectionKey 	key = iter.next();
		
		            if (!isSuspended() && key.isValid()) {
			            if (key.isAcceptable()) {
			                registerAccept(getSelector(), key);
			            }
			            if (key.isWritable()) {
			            	write(getSelector(), key);
			            }
			            if (key.isConnectable()) {
			                registerConnect(getSelector(), key);
			            }
			            if (key.isReadable()) {
			            	read(getSelector(), key);
			            }
		            }
		            iter.remove();
		        }
			} catch (IOException e) {
				getLogger().message(Severity.error, e.getLocalizedMessage());
			}
	    }
	}
}
