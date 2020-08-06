package chav1961.purelib.json.interfaces;

import chav1961.purelib.json.JsonNode;

/**
 * <p>This enumeration contains all types of {@linkplain JsonNode} nodes in the JSON tree</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
public enum JsonNodeType {
	JsonObject,
	JsonArray,
	JsonBoolean,
	JsonInteger,
	JsonReal,
	JsonString,
	JsonNull
}