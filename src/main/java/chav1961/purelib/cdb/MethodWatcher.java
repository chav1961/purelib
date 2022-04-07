package chav1961.purelib.cdb;

import java.util.Iterator;

import com.sun.jdi.event.Event;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.cdb.interfaces.SignatureKeeper;

public class MethodWatcher extends Abstract2LevelDebugWatcher<BreakpointWatcher> implements SignatureKeeper {
	private final String	methodSignature;

	public MethodWatcher(final String methodSignature, final BreakpointWatcher... watchers) {
		this(methodSignature);
		if (watchers == null || Utils.checkArrayContent4Nulls(watchers) >= 0) {
			throw new IllegalArgumentException("Watcher's list is null or contains nulls inside"); 
		}
		else {
			for (BreakpointWatcher item : watchers) {
				addBreakpointWatcher(item);
			}
		}
	}
	
	public MethodWatcher(final String methodSignature) {
		if (methodSignature == null || methodSignature.isEmpty()) {
			throw new IllegalArgumentException("Method signature can't be null or empty");
		}
		else {
			this.methodSignature = methodSignature;
		}
	}

	@Override
	public String getSignature() {
		return methodSignature;
	}
	
	@Override
	public Iterator<BreakpointWatcher> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addWatcher(BreakpointWatcher watcher) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeWatcher(BreakpointWatcher watcher) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected <T extends Event> void processEvent(T event) {
		// TODO Auto-generated method stub
		
	}
	
	private void addBreakpointWatcher(BreakpointWatcher item) {
		// TODO Auto-generated method stub
		
	}

	private void removeBreakpointWatcher(BreakpointWatcher item) {
		// TODO Auto-generated method stub
		
	}

	
}
