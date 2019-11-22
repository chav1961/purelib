package chav1961.purelib.ui.interfaces;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public interface ValueChangeAdapter<T> {
	@FunctionalInterface
	public interface ValueChangeListener<T> {
		void valueChanged(ContentNodeMetadata metadata, T oldValue, T newValue);
	}

	void addValueChangeListener(final ValueChangeListener<T> listener);
	void removeValueChangeListener(final ValueChangeListener<T> listener);
}
