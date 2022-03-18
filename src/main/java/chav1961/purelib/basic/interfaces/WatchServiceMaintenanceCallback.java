package chav1961.purelib.basic.interfaces;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

@FunctionalInterface 
public interface WatchServiceMaintenanceCallback {
	public static enum EventType {
		OVERFOW(StandardWatchEventKinds.OVERFLOW),
		CREATED(StandardWatchEventKinds.ENTRY_CREATE),
		MODIFIED(StandardWatchEventKinds.ENTRY_MODIFY),
		REMOVED(StandardWatchEventKinds.ENTRY_DELETE);
		
		private final WatchEvent.Kind<?>	kind;
		
		EventType(final WatchEvent.Kind<?> kind) {
			this.kind = kind;
		}
		
		public static WatchServiceMaintenanceCallback.EventType valueOf(final WatchEvent.Kind<?> kind) {
			if (kind == null) {
				throw new NullPointerException("Kind to get EventType for can'tbe null"); 
			}
			else {
				for(WatchServiceMaintenanceCallback.EventType item : EventType.values()) {
					if (item.kind == kind) {
						return item;
					}
				}
				throw new IllegalArgumentException("Kind ["+kind+"] can't be converted to EventType"); 
			}
		}
	}
	
	void process(WatchServiceMaintenanceCallback.EventType eventType, File file) throws IOException;
}