package chav1961.purelib.ui.swing.interfaces;

import javax.swing.JComponent;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

@FunctionalInterface
public interface JComponentMonitor {
	public enum MonitorEvent {
		Loading,
		FocusGained,
		FocusLost,
		Validation,
		Rollback,
		Saving,
		Action,
		Exit
	}
	boolean process(MonitorEvent event, ContentNodeMetadata metadata, JComponent component, Object... parameters) throws ContentException;
}
