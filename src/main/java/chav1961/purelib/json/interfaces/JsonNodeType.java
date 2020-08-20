package chav1961.purelib.json.interfaces;

import chav1961.purelib.json.JsonNode;

/**
 * <p>This enumeration contains all types of {@linkplain JsonNode} nodes in the JSON tree</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
public enum JsonNodeType {
	/**
	 * <p>This item marks <i>object</i> node (for example <b>{</b>"x":10,"y":true<b>}</b>)</p>
	 */
	JsonObject,
	/**
	 * <p>This item marks <i>array</i> node (for example <b>[</b>100,200<b>]</b>)</p>
	 */
	JsonArray,
	/**
	 * <p>This item marks <i>boolean</i> node (for example {"x":10,"y":<b>true</b>})</p>
	 */
	JsonBoolean,
	/**
	 * <p>This item marks <i>integer</i> node (for example {"x":<b>10</b>,"y":true})</p>
	 */
	JsonInteger,
	/**
	 * <p>This item marks <i>real</i> node (for example {"x":<b>10.3</b>,"y":"text"})</p>
	 */
	JsonReal,
	/**
	 * <p>This item marks <i>text</i> node (for example {"x":10.3,"y":<b>"text"</b>})</p>
	 */
	JsonString,
	/**
	 * <p>This item marks <i>null</i> node (for example {"x":<b>null</b>})</p>
	 */
	JsonNull
}