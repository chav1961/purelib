package chav1961.purelib.cdb;

import java.util.Iterator;

import com.sun.jdi.event.Event;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.cdb.interfaces.SignatureKeeper;

public class FieldWatcher extends Abstract2LevelDebugWatcher<MonitorWatcher> implements SignatureKeeper {
	private final String	fieldSignature;

	public FieldWatcher(final String fieldSignature, final MonitorWatcher... watchers) {
		this(fieldSignature);
		if (watchers == null || Utils.checkArrayContent4Nulls(watchers) >= 0) {
			throw new IllegalArgumentException("Watcher's list is null or contains nulls inside"); 
		}
		else {
			for (MonitorWatcher item : watchers) {
				addMonitorWatcher(item);
			}
		}
	}	
	
	public FieldWatcher(final String fieldSignature) {
		if (fieldSignature == null || fieldSignature.isEmpty()) {
			throw new IllegalArgumentException("Field signature can't be null or empty");
		}
		else {
			this.fieldSignature = fieldSignature;
		}
	}
	
	@Override
	public String getSignature() {
		return fieldSignature;
	}
	
	@Override
	public Iterator<MonitorWatcher> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addWatcher(final MonitorWatcher watcher) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeWatcher(final MonitorWatcher watcher) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected <T extends Event> void processEvent(T event) {
		// TODO Auto-generated method stub
		
	}

	
	private void addMonitorWatcher(final MonitorWatcher item) {
		// TODO Auto-generated method stub
		
	}

	private void removeMonitorWatcher(final MonitorWatcher item) {
		// TODO Auto-generated method stub
		
	}
}
