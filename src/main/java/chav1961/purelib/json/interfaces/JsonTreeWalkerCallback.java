package chav1961.purelib.json.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.json.JsonNode;

@FunctionalInterface
public interface JsonTreeWalkerCallback {
	ContinueMode process(NodeEnterMode mode, JsonNode node, Object... path) throws ContentException;
}