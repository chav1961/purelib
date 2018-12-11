package chav1961.purelib.ui.interfacers;

import chav1961.purelib.ui.interfacers.ContentMetadataInterface.ContentNodeMetadata;

public interface ContentDescriptionInterface {
	public interface ContentNodeView {
		<T> T getValue(Class<T> awaited);
		<T> void setValue(Class<T> awaited, T newValue);
		ContentNodeMetadata getMetadata();
	}

	ContentMetadataInterface getMetadata();
	ContentDescriptionInterface getRoot();
}
