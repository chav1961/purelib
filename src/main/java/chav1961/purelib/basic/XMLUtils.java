package chav1961.purelib.basic;

import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public class XMLUtils {

	@FunctionalInterface
	public interface XMLWalkerCallback {
		ContinueMode process(NodeEnterMode mode, Element node);
	}
	
	public static ContinueMode walkDownXML(final Element root, final XMLWalkerCallback callback) throws NullPointerException {
		return walkDownXML(root,-1L,callback);
	}

	public static ContinueMode walkDownXML(final Element root, final long nodeTypes, final XMLWalkerCallback callback) throws NullPointerException {
		if (root == null) {
			throw new NullPointerException("Root element can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Walker callback can't be null"); 
		}
		else {
			return walkDownXMLInternal(root, nodeTypes, callback); 
		}
	}

	public static boolean contains(final Element node, final String attribute) {
		return false;
	}	
	
	public static <T> T getAttribute(final Element node, final String name, final Class<T> awaited) {
		return null;
	}
	
	public static Properties getAttributes(final Element node) {
		return null;
	}
	
	public static Properties joinAttributes(final Element node, final Properties toJoin, final boolean retainExistent) {
		return null;
	}

	private static ContinueMode walkDownXMLInternal(final Element node, final long nodeTypes, final XMLWalkerCallback callback) {
		ContinueMode	before = null, after = ContinueMode.CONTINUE;
		
		if (node != null && (nodeTypes & (1 << node.getNodeType())) != 0) {
			switch (before = callback.process(NodeEnterMode.ENTER, node)) {
				case CONTINUE		:
					final NodeList	list = node.getChildNodes();
					
					for (int index = 0, maxIndex = list.getLength(); index < maxIndex; index++) {
						final Node	item = list.item(index);
						
						if (item instanceof Element) {
							if ((after = walkDownXMLInternal((Element)item,nodeTypes,callback)) != ContinueMode.CONTINUE) {
								break;
							}
						}
					}
					after = Utils.resolveContinueMode(after,callback.process(NodeEnterMode.EXIT, node));
					break;
				case SKIP_CHILDREN : case STOP :
					after = callback.process(NodeEnterMode.EXIT, node);
					break;
				default:
					throw new IllegalStateException("Unwaited continue mode ["+before+"] for walking down");
			}
			return Utils.resolveContinueMode(before, after);
		}
		else {
			return ContinueMode.CONTINUE;
		}
	}
}
