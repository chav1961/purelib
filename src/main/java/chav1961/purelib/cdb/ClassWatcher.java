package chav1961.purelib.cdb;

import com.sun.jdi.VirtualMachine;

import com.sun.jdi.event.Event;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;

import chav1961.purelib.cdb.interfaces.InnerWatchersKeeper;
import chav1961.purelib.cdb.interfaces.SignatureKeeper;

public class ClassWatcher extends AbstractDebugWatcher implements SignatureKeeper {
	private final String		classSignature;
	private ClassPrepareRequest	cpr = null;
	private ClassUnloadRequest	cur = null;
	
	public ClassWatcher(final String classSignature) {
		if (classSignature == null || classSignature.isEmpty()) {
			throw new IllegalArgumentException("Class signature can't be null or empty");
		}
		else {
			this.classSignature = classSignature;
		}
	}

	@Override
	public String getSignature() {
		return classSignature;
	}

	public InnerWatchersKeeper<FieldWatcher> getFields() {
		return null;
	}
	
	public InnerWatchersKeeper<MethodWatcher> getMethods() {
		return null;
	}
	
	@Override
	<T extends Event> void processEvent(T event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void prepareEventRequests(final VirtualMachine vm) {
		cpr = vm.eventRequestManager().createClassPrepareRequest();
		cpr.addClassFilter(getSignature());
		cur = vm.eventRequestManager().createClassUnloadRequest();
		cur.addClassFilter(getSignature());
	}

	@Override
	void enableEventRequests(final VirtualMachine vm) {
		cpr.enable();
		cur.enable();
	}

	@Override
	void disableEventRequests(final VirtualMachine vm) {
		cpr.disable();
		cur.disable();
	}

	@Override
	void unprepareEventRequests(final VirtualMachine vm) {
		vm.eventRequestManager().deleteEventRequest(cpr);
		vm.eventRequestManager().deleteEventRequest(cur);
	}
}
