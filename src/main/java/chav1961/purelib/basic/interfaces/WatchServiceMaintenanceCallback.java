package chav1961.purelib.basic.interfaces;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * <p>This interface is a callback to process directory event watcher messages.</p>
 * @see java.nio.file.WatchService 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
@FunctionalInterface 
public interface WatchServiceMaintenanceCallback {
	/**
	 * <p>This enumerations describes watch service event type.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	public static enum EventType {
		/**
		 * <p>Watch service event type - overflow</p>
		 */
		OVERFOW(StandardWatchEventKinds.OVERFLOW),
		/**
		 * <p>Watch service event type - new file created</p>
		 */
		CREATED(StandardWatchEventKinds.ENTRY_CREATE),
		/**
		 * <p>Watch service event type - existent file modified</p>
		 */
		MODIFIED(StandardWatchEventKinds.ENTRY_MODIFY),
		/**
		 * <p>Watch service event type - existent file removed</p>
		 */
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