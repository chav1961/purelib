package chav1961.purelib.cdb;

import com.sun.jdi.ThreadReference;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.event.Event;
import chav1961.purelib.cdb.VMWatcher.VMEventProcessor.EventType;

import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.request.VMDeathRequest;

public class VMWatcher extends Abstract2LevelDebugWatcher<ThreadWatcher> {
	@FunctionalInterface
	public static interface VMEventProcessor  {
		public enum EventType {
			START,
			DISCONNECT,
			TERMINATE;
		}
		
		void process(final EventType event, ThreadReference thread);
	}
	
	private final VMEventProcessor	proc;
	private VMDeathRequest			vmd = null;
	
	public VMWatcher(final VMEventProcessor proc) {
		super(ThreadWatcher.class);
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
			if (event instanceof VMStartEvent) {
				proc.process(EventType.START, ((VMStartEvent)event).thread());
			}
			else if (event instanceof VMDeathEvent) {
				proc.process(EventType.TERMINATE, ((VMStartEvent)event).thread());
			}
		}
	}

	@Override
	void prepareEventRequests(final VirtualMachine vm) {
		vmd = vm.eventRequestManager().createVMDeathRequest();
	}

	@Override
	void enableEventRequests(final VirtualMachine vm) {
		vmd.enable();
	}

	@Override
	void disableEventRequests(final VirtualMachine vm) {
		vmd.disable();
	}

	@Override
	void unprepareEventRequests(final VirtualMachine vm) {
		vm.eventRequestManager().deleteEventRequest(vmd);
	}
}
