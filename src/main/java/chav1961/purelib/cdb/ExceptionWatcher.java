package chav1961.purelib.cdb;

import java.util.List;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.Field;
import com.sun.jdi.Value;
import com.sun.jdi.StringReference;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Location;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.request.ExceptionRequest;

public class ExceptionWatcher<Inst extends Throwable> extends AbstractDebugWatcher {
	@FunctionalInterface
	public static interface ExceptionEventProcessor<Inst extends Throwable>  {
		void process(final Class<Inst> type, String classSignature, String methodName, String methodSignature, String message, int line);
	}
	
	private final Class<Inst>	clazz;
	private final ExceptionEventProcessor<Inst>	proc;
	private ReferenceType		ref = null;
	private Field				f;
	private ExceptionRequest	erq = null;
	private boolean				prepared = false;
	
	public ExceptionWatcher(final Class<Inst> clazz, final ExceptionEventProcessor<Inst> proc) {
		if (clazz == null) {
			throw new NullPointerException("Class to watch can't be null"); 
		}
		else if (proc == null) {
			throw new NullPointerException("Event processor can't be null"); 
		}
		else {
			this.clazz = clazz;
			this.proc = proc;
		}
	}
	
	@Override
	<T extends Event> void processEvent(final T event) {
		if (prepared && (event instanceof ExceptionEvent) && ((ObjectReference)(((ExceptionEvent)event).exception())).referenceType().equals(ref)) {
			final Location			location = ((ExceptionEvent)event).catchLocation();
			final ObjectReference	ref = (ObjectReference)(((ExceptionEvent)event).exception());
			final Value				value = ref.getValue(f);
			
			proc.process(clazz, location.declaringType().genericSignature(), location.method().name(), 
						location.method().signature(), value instanceof StringReference ? ((StringReference)value).value() : null, 
						location.lineNumber());
		}
	}

	@Override
	void prepareEventRequests(final VirtualMachine vm) {
		final List<ReferenceType>	list = vm.classesByName(clazz.getCanonicalName());
		
		if (!list.isEmpty()) {
			erq = vm.eventRequestManager().createExceptionRequest(ref = list.get(0), true, true);
			f = ref.fieldByName("detailMessage");
			prepared = true;
		}
		else {
			prepared = false;
		}
	}

	@Override
	void enableEventRequests(final VirtualMachine vm) {
		if (prepared) {
			erq.enable();
		}
	}

	@Override
	void disableEventRequests(final VirtualMachine vm) {
		if (prepared) {
			erq.disable();
		}
	}

	@Override
	void unprepareEventRequests(final VirtualMachine vm) {
		if (prepared) {
			vm.eventRequestManager().deleteEventRequest(erq);
		}
	}
}
