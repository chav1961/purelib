package chav1961.purelib.model.interfaces;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

@FunctionalInterface
public interface NodeMetadataOwner {
	ContentNodeMetadata getNodeMetadata();
	
	default ContentNodeMetadata getNodeMetadata(int x, int y) {
		return getNodeMetadata();
	}
}
