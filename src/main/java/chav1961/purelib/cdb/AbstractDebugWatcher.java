package chav1961.purelib.cdb;

import chav1961.purelib.cdb.interfaces.DebugWatcher;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.concurrent.LightWeightListenerList.LightWeightListenerCallback;

import java.util.function.Consumer;

import com.sun.jdi.request.EventRequest;

abstract class AbstractDebugWatcher implements DebugWatcher {
	interface ContentChangeListener {
		enum ChangeType {
			CONTENT_ADDED,
			CONTENT_REMOVED;
		}
		
		void process(DebugWatcher owner, ChangeType type, DebugWatcher item);
	}
	
	protected enum WatcherState {
		PREPARED,
		BOUNDED,
		SUSPENDED,
		TERMINATED;
	}
	
	private final LightWeightListenerList<ContentChangeListener>	listeners = new LightWeightListenerList<>(ContentChangeListener.class);  	
	private boolean			started = false;
	private boolean			suspended = false;
	private WatcherState	state = WatcherState.PREPARED;
	private AbstractDebugWatcher	parent = null; 
	private EventRequest	request = null;
	
	AbstractDebugWatcher() {
	}

	AbstractDebugWatcher(final AbstractDebugWatcher parent) {
		if (parent == null) {
			throw new NullPointerException("Parent ref can't be null"); 
		}
		else {
			this.parent = parent;
		}
	}

	protected abstract <T extends com.sun.jdi.event.Event> void processEvent(T event);
	
	@Override
	public void start() throws Exception {
		if (isStarted()) {
			throw new IllegalStateException("Entity already started"); 
		}
		else {
			started = true;
		}
	}

	@Override
	public void suspend() throws Exception {
		if (!isStarted()) {
			throw new IllegalStateException("Entity was not started yet"); 
		}
		else if (isSuspended()) {
			throw new IllegalStateException("Entity already suspended"); 
		}
		else {
			suspended = true;
			if (getEventRequest() != null) {
				getEventRequest().disable();
			}
		}
	}

	@Override
	public void resume() throws Exception {
		if (!isStarted()) {
			throw new IllegalStateException("Entity was not started yet"); 
		}
		else if (!isSuspended()) {
			throw new IllegalStateException("Entity was not suspended yet"); 
		}
		else {
			suspended = false;
			if (getEventRequest() != null) {
				getEventRequest().enable();
			}
		}
	}

	@Override
	public void stop() throws Exception {
		if (!isStarted()) {
			throw new IllegalStateException("Entity was not started yet"); 
		}
		else {
			if (getEventRequest() != null) {
				getEventRequest().disable();
			}
			started = false;
		}
	}

	@Override
	public boolean isStarted() {
		if (started) {
			return getParent() != null ? getParent().isStarted() : started; 
		}
		else {
			return started;
		}
	}

	@Override
	public boolean isSuspended() {
		if (suspended) {
			return getParent() != null ? getParent().isSuspended() : suspended; 
		}
		else {
			return suspended;
		}
	}
	
	protected WatcherState getState() {
		return state;
	}
	
	protected void setState(final WatcherState state) {
		if (state == null) {
			this.state = state;
		}
		else {
			this.state = state;
		}
	}
	
	protected void setParent(final AbstractDebugWatcher parent) {
		if (parent == null) {
			throw new NullPointerException("Parent ref can't be null"); 
		}
		else {
			this.parent = parent;
		}
	}
	
	protected void clearParent() {
		this.parent = null;
	}
	
	protected AbstractDebugWatcher getParent() {
		return parent;
	}
	
	protected <T extends EventRequest> void associateEventRequest(final T request) {
		this.request = this.request;
	}
	
	protected <T extends EventRequest> T getEventRequest() {
		return (T) request;
	}
	
	void addContentChangeListener(final ContentChangeListener listener) {
		listeners.addListener(listener);
	}

	void removeContentChangeListener(final ContentChangeListener listener) {
		listeners.removeListener(listener);
	}
	
	void fireEvent(final LightWeightListenerCallback<ContentChangeListener> callback) {
		listeners.fireEvent(callback);
	}
}
