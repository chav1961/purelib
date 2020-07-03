package chav1961.purelib.json;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.json.interfaces.JsonTreeWalkerCallback;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class JsonTree {
	private static JsonPath	ALL_CONTENT = compilePath("//"); 
	
	private JsonNode	root = null;
	
	public JsonTree() {
		
	}
	
	public JsonTree(final JsonStaxParser parser) throws SyntaxException {
		if (parser == null) {
			throw new NullPointerException("Parser to load content from can't be null");
		}
		else {
			if (parser.hasNext()) {
				parser.next();
			}
			this.root = JsonUtils.loadJsonTree(parser);
		}
	}

	public void load(final JsonStaxParser parser) throws SyntaxException {
		if (parser == null) {
			throw new NullPointerException("Parser to load content from can't be null");
		}
		else {
			if (parser.hasNext()) {
				parser.next();
			}
			this.root = JsonUtils.loadJsonTree(parser);
		}
	}
	
	public void save(final JsonStaxPrinter printer) throws PrintingException {
		if (printer == null) {
			throw new NullPointerException("Parser to load content from can't be null");
		}
		else {
			JsonUtils.unloadJsonTree(getRoot(),printer);
		}
	}

	public boolean exists(final JsonPath path) {
		if (path == null) {
			throw new NullPointerException("Path to check can't be null");
		}
		else {
			return walkDown(getRoot(),path,
				(mode,node,curPath)->{
					return ContinueMode.STOP;
				}
			) == ContinueMode.STOP;
		}
	}
	
	public JsonNode get(final JsonPath path) {
		if (path == null) {
			throw new NullPointerException("Path to get can't be null");
		}
		else {
			final JsonNode[] result = new JsonNode[1];
			
			if (walkDown(getRoot(),path,
					(mode,node,curPath)->{
						result[0] = node;
						return ContinueMode.STOP;
					}
				) == ContinueMode.STOP) {
				
				return result[0];
			}
			else {
				return null;
			}
		}
	}
	
	public JsonTree remove(final JsonPath path) {
		if (path == null) {
			throw new NullPointerException("Path to remove can't be null or empty");
		}
		else {
			walkDown(getRoot(),path,
				(mode,node,curPath)->{
					if (mode == NodeEnterMode.EXIT) {
						if (node == getRoot()) {
							root = null;
							return ContinueMode.STOP;
						}
						else {
							final JsonNode	parent = node.parent;
							
							for (int index = 0; index < parent.children.length; index++ ) {
								if (parent.children[index] == node) {
									final JsonNode[]	newList = new JsonNode[parent.children.length-1];
									
									if (index > 0) {
										System.arraycopy(parent.children,0,newList,0,index);
									}
									if (index < parent.children.length-1) {
										System.arraycopy(parent.children,index+1,newList,index,parent.children.length-index-1);
									}
									parent.children = newList;
								}
							}
						}
					}
					return ContinueMode.CONTINUE;
				}
			);
			return this;
		}
	}

	public JsonTree set(final JsonNode value, final JsonPath path) {
		if (value == null) {
			throw new NullPointerException("Value to set can't be null");
		}
		else if (path == null) {
			throw new NullPointerException("Path to set can't be null");
		}
		else {
			walkDown(getRoot(),path,
				(mode,node,curPath)->{
					node.row = value.row;
					node.col = value.col;
					node.type = value.type;
					node.value = value.value;
					node.cargo = value.cargo;
					node.children = value.children.clone();
					return ContinueMode.STOP;
				}
			);
			return this;
		}
	}

	public JsonTree addChild(final JsonNode value, final JsonPath path) {
		if (value == null) {
			throw new NullPointerException("Value to set can't be null");
		}
		else if (path == null) {
			throw new NullPointerException("Path to add can't be null");
		}
		else {
			walkDown(getRoot(),path,
				(mode,node,curPath)->{
					node.children = Arrays.copyOf(node.children,node.children.length+1);
					node.children[node.children.length-1] = new JsonNode(value);
					node.children[node.children.length-1].parent = node; 
					return ContinueMode.STOP;
				}
			);
			return this;
		}
	}

	public ContinueMode walkDown(final JsonTreeWalkerCallback callback) {
		if (callback == null) {
			throw new NullPointerException("Walk callback can't be null");
		}
		else if (root == null) {
			throw new IllegalStateException("Tree root is null, so can't be walking");
		}
		else {
			return walkDown(root,callback);
		}
	}
	
	public ContinueMode walkDown(final JsonNode root, final JsonTreeWalkerCallback callback) {
		if (root == null) {
			throw new IllegalStateException("Tree root is null, so can't be walking");
		}
		else if (callback == null) {
			throw new NullPointerException("Walk callback can't be null");
		}
		else {
			return walkDown(root,ALL_CONTENT,callback);
		}
	}

	public ContinueMode walkDown(final JsonNode root, final JsonPath path, final JsonTreeWalkerCallback callback) {
		if (root == null) {
			throw new IllegalStateException("Tree root is null, so can't be walking");
		}
		else if (path == null) {
			throw new NullPointerException("Path to walk can't be null or empty");
		}
		else if (callback == null) {
			throw new NullPointerException("Walk callback can't be null");
		}
		else {
			return internalWalkDown(root,path,new GrowableCharArray<>(false),callback);
		}
	}
	
	public JsonNode getRoot() {
		return root;
	}

	public Iterable<JsonNode> iterate(final JsonPath... paths) {
		if (paths == null || paths.length == 0) {
			throw new IllegalArgumentException("Path list to walk can't be null or empty");
		}
		else if (Utils.checkArrayContent4Nulls(paths) >= 0 ) {
			throw new IllegalArgumentException("Path list contains nulls inside");
		}
		else {
			final List<JsonNode> result = new ArrayList<>();
			
			for (JsonPath item : paths) {
				
				walkDown(getRoot(),item,
						(mode,node,curPath)->{
							if (mode == NodeEnterMode.ENTER) {
								result.add(node);
							}
							return ContinueMode.CONTINUE;
						}
				);
			}
			return result;
		}
	}

	public static JsonPath compilePath(final CharSequence path) {
		if (path == null || path.length() == 0) {
			throw new IllegalArgumentException("Path to walk can't be null or empty");
		}
		else {
			return null;
		}		
	}	
	
	private static ContinueMode internalWalkDown(final JsonNode node, final JsonPath path, final GrowableCharArray<?> growableCharArray, final JsonTreeWalkerCallback callback) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public static class JsonPath {
		public JsonPath(final CharSequence path) {
			
		}
	}
}
