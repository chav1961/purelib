package chav1961.purelib.cdb;

import com.sun.jdi.event.Event;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.ThreadDeathEvent;

import chav1961.purelib.cdb.ThreadWatcher.ThreadEventProcessor.EventType;

public class ThreadWatcher extends AbstractDebugWatcher {
	@FunctionalInterface
	public static interface ThreadEventProcessor  {
		public enum EventType {
			START,
			TERMINATE;
		}
		
		void process(final EventType event, ThreadReference thread);
	}
	
	private final ThreadEventProcessor	proc;
	
	public ThreadWatcher(final ThreadEventProcessor proc) {
		if (proc == null) {
			throw new NullPointerException("Thread event processor can't be null");
		}
		else {
			this.proc = proc;
		}
	}
	
	@Override
	protected <T extends Event> void processEvent(T event) {
		if (isStarted() && !isSuspended()) {
			if (event instanceof ThreadStartEvent) {
				proc.process(EventType.START, ((ThreadStartEvent)event).thread());
			}
			else if (event instanceof ThreadDeathEvent) {
				proc.process(EventType.TERMINATE, ((ThreadDeathEvent)event).thread());
			}
		}
	}
}
