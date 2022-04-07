package chav1961.purelib.cdb;

import java.util.Iterator;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.VMStartEvent;

import chav1961.purelib.cdb.VMWatcher.VMEventProcessor.EventType;

import com.sun.jdi.event.VMDeathEvent;

public class VMWatcher extends Abstract2LevelDebugWatcher<ThreadWatcher> {
	@FunctionalInterface
	public static interface VMEventProcessor  {
		public enum EventType {
			START,
			TERMINATE;
		}
		
		void process(final EventType event, ThreadReference thread);
	}
	
	private final VMEventProcessor	proc;
	
	public VMWatcher(final VMEventProcessor proc) {
		if (proc == null) {
			throw new NullPointerException("Thread event processor can't be null");
		}
		else {
			this.proc = proc;
		}
	}

	@Override
	public Iterator<ThreadWatcher> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addWatcher(ThreadWatcher watcher) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeWatcher(ThreadWatcher watcher) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected <T extends com.sun.jdi.event.Event> void processEvent(T event) {
		if (isStarted() && !isSuspended()) {
			if (event instanceof VMStartEvent) {
				proc.process(EventType.START, ((VMStartEvent)event).thread());
			}
			else if (event instanceof VMDeathEvent) {
				proc.process(EventType.TERMINATE, ((VMStartEvent)event).thread());
			}
		}
	}

}
