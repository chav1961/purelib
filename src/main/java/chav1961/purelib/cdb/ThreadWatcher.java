package chav1961.purelib.cdb;

import com.sun.jdi.event.Event;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.request.ThreadStartRequest;
import com.sun.jdi.request.ThreadDeathRequest;

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
	private ThreadStartRequest	tsr = null;	
	private ThreadDeathRequest	tdr = null;	
	
	public ThreadWatcher(final ThreadEventProcessor proc) {
		if (proc == null) {
			throw new NullPointerException("Thread event processor can't be null");
		}
		else {
			this.proc = proc;
		}
	}
	
	@Override
	<T extends Event> void processEvent(T event) {
		if (isStarted() && !isSuspended()) {
			if (event instanceof ThreadStartEvent) {
				proc.process(EventType.START, ((ThreadStartEvent)event).thread());
			}
			else if (event instanceof ThreadDeathEvent) {
				proc.process(EventType.TERMINATE, ((ThreadDeathEvent)event).thread());
			}
		}
	}

	@Override
	void prepareEventRequests(final VirtualMachine vm) {
		tsr = vm.eventRequestManager().createThreadStartRequest();
		tdr = vm.eventRequestManager().createThreadDeathRequest();
	}

	@Override
	void enableEventRequests(final VirtualMachine vm) {
		tsr.enable();
		tdr.enable();
	}

	@Override
	void disableEventRequests(final VirtualMachine vm) {
		tsr.disable();
		tdr.disable();
	}

	@Override
	void unprepareEventRequests(final VirtualMachine vm) {
		vm.eventRequestManager().deleteEventRequest(tsr);
		vm.eventRequestManager().deleteEventRequest(tdr);
	}
}
