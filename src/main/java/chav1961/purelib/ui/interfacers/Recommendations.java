package chav1961.purelib.ui.interfacers;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public interface Recommendations {
	public enum Location {
		NORTH, EAST, SOUTH, WEST, CENTER
	}
	
	public enum FunctionalUnit {
		MENU, NAVIGATOR
	}
	
	public interface About {
		Location getPreferredLocation(); // null - any
		float getPreferredWidth();  // 0 - any
		float getPreferredHeight(); // 0 - any
		int getPreferredOrder();	// -1 - any
		String getPreferredStyle(); // null - any
		FunctionalUnit getPreferredFunctionalUnit(); // null - any
	}
	
	About forNode(ContentNodeMetadata node);
}
