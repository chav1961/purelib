package chav1961.purelib.cdb;


import chav1961.purelib.cdb.interfaces.DebugWatcher;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.concurrent.LightWeightListenerList.LightWeightListenerCallback;

import com.sun.jdi.request.EventRequest;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;

abstract class AbstractDebugWatcher implements DebugWatcher {
	static enum ContentChangeType {
		CONTENT_ADDED,
		CONTENT_REMOVED;
	}

	@FunctionalInterface
	interface ContentChangeListener {
		void process(DebugWatcher owner, ContentChangeType type, DebugWatcher item);
	}

	static enum StateChangeType {
		STARTED,
		SUSPENDED,
		RESUMED,
		STOPPED;
	}

	@FunctionalInterface
	interface StateChangeListener {
		void process(StateChangeType type, DebugWatcher item);
	}
	
	private final LightWeightListenerList<ContentChangeListener>	contentListeners = new LightWeightListenerList<>(ContentChangeListener.class);  	
	private final LightWeightListenerList<StateChangeListener>		stateListeners = new LightWeightListenerList<>(StateChangeListener.class);  	
	private boolean			started = false;
	private boolean			suspended = false;
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

	abstract <T extends Event> void processEvent(T event);
	abstract void prepareEventRequests(final VirtualMachine vm);
	abstract void enableEventRequests(final VirtualMachine vm);
	abstract void disableEventRequests(final VirtualMachine vm);
	abstract void unprepareEventRequests(final VirtualMachine vm);
	
	@Override
	public void start() throws Exception {
		if (isStarted()) {
			throw new IllegalStateException("Entity already started"); 
		}
		else {
			started = true;
			fireStateChangeEvent((l)->l.process(StateChangeType.STARTED, this));
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
			fireStateChangeEvent((l)->l.process(StateChangeType.SUSPENDED, this));
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
			fireStateChangeEvent((l)->l.process(StateChangeType.RESUMED, this));
		}
	}

	@Override
	public void stop() throws Exception {
		if (!isStarted()) {
			throw new IllegalStateException("Entity was not started yet"); 
		}
		else {
			started = false;
			fireStateChangeEvent((l)->l.process(StateChangeType.STOPPED, this));
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

	void addContentChangeListener(final ContentChangeListener listener) {
		contentListeners.addListener(listener);
	}

	void removeContentChangeListener(final ContentChangeListener listener) {
		contentListeners.removeListener(listener);
	}
	
	void fireContentChangeEvent(final LightWeightListenerCallback<ContentChangeListener> callback) {
		contentListeners.fireEvent(callback);
	}

	void addStateChangeListener(final StateChangeListener listener) {
		stateListeners.addListener(listener);
	}

	void removeStateChangeListener(final StateChangeListener listener) {
		stateListeners.removeListener(listener);
	}
	
	void fireStateChangeEvent(final LightWeightListenerCallback<StateChangeListener> callback) {
		stateListeners.fireEvent(callback);
	}
}
