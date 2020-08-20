package chav1961.purelib.json.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.json.JsonUtils.ArrayRoot;
import chav1961.purelib.json.JsonUtils.ObjectRoot;


/**
 * <p>This interface is used in conjunction with {@linkplain JsonUtils#walkDownJson(JsonNode, JsonTreeWalkerCallback)} method</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
@FunctionalInterface
public interface JsonTreeWalkerCallback {
	/**
	 * <p>Process current JSON tree node. Path in the parameter is a chain of nodes from root of the tree to the current node. It can contains:</p>
	 * <ul>
	 * <li>{@linkplain ArrayRoot} instance to mark array node (usually root)</li> 
	 * <li>{@linkplain ObjectRoot} instance to mark object node (usually root)</li> 
	 * <li>{@linkplain Integer} instance to mark array item inside array node</li> 
	 * <li>{@linkplain String} instance to mark name inside object node</li> 
	 * <li>{@linkplain JsonNode} instance to mark value in the leaf of the tree</li> 
	 * </ul> 
	 * @param mode node enter mode
	 * @param node current node to process
	 * @param path path from root to current node
	 * @return continue mode. Can't be null
	 * @throws ContentException on any errors processing current node
	 */
	ContinueMode process(NodeEnterMode mode, JsonNode node, Object... path) throws ContentException;
}