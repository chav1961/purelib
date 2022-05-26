package chav1961.purelib.model.interfaces;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

@FunctionalInterface
public interface NodeMetadataOwner {
	ContentNodeMetadata getNodeMetadata();
	
	default boolean hasNodeMetadata(int x, int y) {
		return true;
	}
	
	default ContentNodeMetadata getNodeMetadata(int x, int y) {
		return getNodeMetadata();
	}

	default boolean hasNodeMetadata(String childName) {
		return true;
	}
	
	default ContentNodeMetadata getNodeMetadata(String childName) {
		return getNodeMetadata();
	}
	
	default String[] getMetadataChildrenNames() {
		return new String[0];
	}
}
