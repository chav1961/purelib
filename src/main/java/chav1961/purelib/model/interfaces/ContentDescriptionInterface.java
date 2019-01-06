package chav1961.purelib.model.interfaces;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public interface ContentDescriptionInterface {
	public interface ContentNodeView {
		<T> T getValue(Class<T> awaited);
		<T> void setValue(Class<T> awaited, T newValue);
		ContentNodeMetadata getMetadata();
	}

	ContentMetadataInterface getMetadata();
	ContentDescriptionInterface getRoot();
}
