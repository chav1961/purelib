package chav1961.purelib.cdb;

import com.sun.jdi.event.Event;

import chav1961.purelib.cdb.interfaces.InnerWatchersKeeper;
import chav1961.purelib.cdb.interfaces.SignatureKeeper;

public class ClassWatcher extends AbstractDebugWatcher implements SignatureKeeper {
	private final String	classSignature;
	
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
	protected <T extends Event> void processEvent(T event) {
		// TODO Auto-generated method stub
		
	}
}
