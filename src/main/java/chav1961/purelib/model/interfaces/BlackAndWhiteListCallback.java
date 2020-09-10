package chav1961.purelib.model.interfaces;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

/**
 * <p>This interface is used to accept/reject nodes by filter</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
@FunctionalInterface
public interface BlackAndWhiteListCallback {
	/**
	 * <p>Test node to accept it to filter</p>
	 * @param node node to test
	 * @return true if node accepts
	 */
	boolean accept(final ContentNodeMetadata node);
}