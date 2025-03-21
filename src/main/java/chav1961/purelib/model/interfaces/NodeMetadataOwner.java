package chav1961.purelib.model.interfaces;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

/**
 * <p>This interface describes model node owner. Any class containing model node inside can be marked with this interface</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface NodeMetadataOwner {
	/**
	 * <p>Get mode metadata</p>
	 * @return model node associated. Can't be null
	 */
	ContentNodeMetadata getNodeMetadata();
	
	/**
	 * <p>Has owner any associated model node at the x/y coordinated inside</p>  
	 * @param x x-coordinate to check node existence.
	 * @param y y-coordinate to check node existence.
	 * @return true if owner has any model note at the x/y coordinate, false otherwise.
	 */
	default boolean hasNodeMetadata(int x, int y) {
		return true;
	}
	
	/**
	 * <p>Get mode metadata located at the x/y coordinate</p>
	 * @param x x-coordinate to get model node.
	 * @param y y-coordinate to get model node.
	 * @return model mode associated or null
	 */
	default ContentNodeMetadata getNodeMetadata(int x, int y) {
		return getNodeMetadata();
	}

	/**
	 * <p>Has owner any child of the associated node with the given name</p>
	 * @param childName name to check existence. Can be neither null nor empty
	 * @return true if child with the given name exists, false otherwise 
	 */
	default boolean hasNodeMetadata(String childName) {
		return true;
	}
	
	/**
	 * <p>Get child mode metadata with the given name</p>
	 * @param childName name to get node. Can be neither null nor empty
	 * @return child model mode associated or null
	 */
	default ContentNodeMetadata getNodeMetadata(String childName) {
		return getNodeMetadata();
	}
	
	/**
	 * <p>Get names of all children for model node associated</p>
	 * @return array of children names. Can't be null but can be empty
	 */
	default String[] getMetadataChildrenNames() {
		return new String[0];
	}
}
