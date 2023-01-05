package chav1961.purelib.model.interfaces;

import chav1961.purelib.model.ContentNodeFilter;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

/**
 * <p>This interface is used to accept/reject nodes by filter</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @last.update 0.0.6
 */
@FunctionalInterface
public interface BlackAndWhiteListCallback {
	/**
	 * <p>Test node to accept it to filter</p>
	 * @param node node to test
	 * @return true if node accepts
	 */
	boolean accept(final ContentNodeMetadata node);
	
	/**
	 * <p>Convert mode before inserting it into filtered content</p>
	 * @param node node to convert. Can't be null
	 * @param inherited 'inherited' parameter value passed to {@linkplain ContentNodeFilter} constructors
	 * @param mutable 'mutable' parameter value passed to {@linkplain ContentNodeFilter} constructors
	 * @return node converted. Can't be null
	 * @since 0.0.6
	 */
	default ContentNodeMetadata convert(final ContentNodeMetadata node, final boolean inherited, final boolean mutable) {
		return node;
	}
}