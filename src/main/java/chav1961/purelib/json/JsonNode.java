package chav1961.purelib.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.json.interfaces.JsonNodeType;

/**
 * <p>This class is a JSON node of the JSON tree. It can be used similar to DOM tree in the XML. Once was created, type of the node can't be changed, 
 * but it's value and it's children (if exists) can be changed anyway. Any node can have or not have name with some restrictions:</p>
 * <ul>
 * <li>stand-alone item can have or can not have name</li>
 * <li>{@linkplain JsonNodeType#JsonArray} children must not have names</li>
 * <li>{@linkplain JsonNodeType#JsonObject} children must have unique names</li>
 * <li></li>
 * </ul>
 * <p>Almost all the methods throw {@linkplain IllegalStateException} exception when requested operation is not compatible with node type</p>
 * @see JsonNodeType
 * @see JsonUtils
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.5
 */
public class JsonNode  implements Serializable, Cloneable {
	private static final long 	serialVersionUID = -7063561517162821362L;
	
	private final JsonNodeType	type;
	private long				value;
	private String				name = null;
	private String				strValue = null;
	private List<JsonNode>		children = null;

	/**
	 * <p>Constructor of the {@linkplain JsonNodeType#JsonNull} JSON node</p>
	 */
	public JsonNode() {
		this.type = JsonNodeType.JsonNull;
	}
	
	/**
	 * <p>Constructor of the {@linkplain JsonNodeType#JsonBoolean} JSON node</p>
	 * @param value boolean value of the node
	 */
	public JsonNode(final boolean value) {
		this.type = JsonNodeType.JsonBoolean;
		this.value = value ? 1 : 0;
	}

	/**
	 * <p>Constructor of the {@linkplain JsonNodeType#JsonInteger} JSON node</p>
	 * @param value long integer value of the node
	 */
	public JsonNode(final long value) {
		this.type = JsonNodeType.JsonInteger;
		this.value = value;
	}

	/**
	 * <p>Constructor of the {@linkplain JsonNodeType#JsonReal} JSON node</p>
	 * @param value double value of the node
	 */
	public JsonNode(final double value) {
		this.type = JsonNodeType.JsonReal;
		this.value = Double.doubleToLongBits(value);
	}

	/**
	 * <p>Constructor of the {@linkplain JsonNodeType#JsonString} JSON node</p>
	 * @param value string value of the node (can be null)
	 */
	public JsonNode(final String value) {
		this.type = JsonNodeType.JsonString;
		this.strValue = value;
	}

	/**
	 * <p>Constructor of the {@linkplain JsonNodeType#JsonArray} or {@linkplain JsonNodeType#JsonObject} JSON node</p>
	 * @param type type of the node. Can be {@linkplain JsonNodeType#JsonArray} or {@linkplain JsonNodeType#JsonObject} only
	 * @param children optional children of the node (can be changed later)
	 * @throws IllegalArgumentException invalid or null type, named children for {@linkplain JsonNodeType#JsonArray} or unnamed ones for {@linkplain JsonNodeType#JsonObject}  
	 * @throws NullPointerException child list is null or contains nulls inside
	 */
	public JsonNode(final JsonNodeType type, final JsonNode... children) throws NullPointerException, IllegalArgumentException {
		if (type == null || (type != JsonNodeType.JsonArray && type != JsonNodeType.JsonObject)) {
			throw new IllegalArgumentException("Null or invalid node type ["+type+"] for this constructor. Only ["+JsonNodeType.JsonArray+"] and ["+JsonNodeType.JsonObject+"] are supported");
		}
		else if (children == null) {
			throw new NullPointerException("Children lost can't be null");
		}
		else if (Utils.checkArrayContent4Nulls(children) >= 0) {
			throw new NullPointerException("Nulls inside chilren!");
		}
		else {
			if (type == JsonNodeType.JsonArray) {
				for (JsonNode item : children) {
					if (item.hasName()) {
						throw new IllegalArgumentException("Some children items have names. Only unnamed items can be placed into array!");
					}
				}
			}
			else {
				for (JsonNode item : children) {
					if (!item.hasName()) {
						throw new IllegalArgumentException("Some children items doesn't have names. Only named items can be placed into object!");
					}
				}
			}
			
			this.type = type;
			this.children = new ArrayList<>();
			this.children.addAll(Arrays.asList(children));
		}
	}

	/**
	 * <p>Get current node type</p>
	 * @return node type. Can't be null
	 */
	public JsonNodeType getType() {
		return type;
	}

	/**
	 * <p>Has the node name</p>
	 * @return true if has
	 */
	public boolean hasName() {
		return name != null && !name.isEmpty();
	}
	
	/**
	 * <p>Get node name.</p>
	 * @return get node name. If doesn't have name, null returns
	 */
	public String getName() {
		return hasName() ? name : null;
	}

	/**
	 * <p>Set node name</p>
	 * @param name node name to set
	 * @return self (can be used in chained operations)
	 * @throws IllegalArgumentException node name is null or empty
	 */
	public JsonNode setName(final String name) throws IllegalArgumentException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name to set is null or empty. To remove name, call removeName() instead");
		}
		else {
			this.name = name;
			return this;
		}
	}
	
	/**
	 * <p>Remove node name<.p>
	 * @return self (can be used in chained operations)
	 */
	public JsonNode removeName() {
		this.name = null;
		return this;
	}

	/**
	 * <p>Get boolean value from the node</p>
	 * @return current value
	 * @throws IllegalStateException see class description
	 */
	public boolean getBooleanValue() throws IllegalStateException {
		if (getType() != JsonNodeType.JsonBoolean) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else {
			return value != 0;
		}
	}

	/**
	 * <p>Get long value from the node</p>
	 * @return current value
	 * @throws IllegalStateException see class description
	 */
	public long getLongValue() throws IllegalStateException {
		if (getType() != JsonNodeType.JsonInteger) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else {
			return value;
		}
	}

	/**
	 * <p>Get double value from the node</p>
	 * @return current value
	 * @throws IllegalStateException see class description
	 */
	public double getDoubleValue() throws IllegalStateException {
		if (getType() != JsonNodeType.JsonReal) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else {
			return Double.longBitsToDouble(value);
		}
	}
	
	/**
	 * <p>Get string value from the node</p>
	 * @return current value
	 * @throws IllegalStateException see class description
	 */
	public String getStringValue() throws IllegalStateException {
		if (getType() != JsonNodeType.JsonString) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else {
			return strValue;
		}
	}

	/**
	 * <p>Set boolean value in the node</p>
	 * @param value value to set
	 * @return self (can be used in chained operations)
	 * @throws IllegalStateException see class description
	 */
	public JsonNode setValue(final boolean value) throws IllegalStateException {
		if (getType() != JsonNodeType.JsonBoolean) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else {
			this.value = value ? 1 : 0;
			return this;
		}
	}

	/**
	 * <p>Set long value in the node</p>
	 * @param value value to set
	 * @return self (can be used in chained operations)
	 * @throws IllegalStateException see class description
	 */
	public JsonNode setValue(final long value) throws IllegalStateException {
		if (getType() != JsonNodeType.JsonInteger) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else {
			this.value = value;
			return this;
		}
	}

	/**
	 * <p>Set double value in the node</p>
	 * @param value value to set
	 * @return self (can be used in chained operations)
	 * @throws IllegalStateException see class description
	 */
	public JsonNode setValue(final double value) throws IllegalStateException {
		if (getType() != JsonNodeType.JsonReal) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else {
			this.value = Double.doubleToLongBits(value);
			return this;
		}
	}

	/**
	 * <p>Set string value in the node</p>
	 * @param value value to set
	 * @return self (can be used in chained operations)
	 * @throws IllegalStateException see class description
	 */
	public JsonNode setValue(final String value) throws IllegalStateException {
		if (getType() != JsonNodeType.JsonString) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else {
			this.strValue = value;
			return this;
		}
	}

	/**
	 * <p>Get number of children in the node. This method can be used with {@linkplain JsonNodeType#JsonObject} and {@linkplain JsonNodeType#JsonArray} nodes only</p>
	 * @return number of children
	 * @throws IllegalStateException see class description
	 */
	public int childrenCount() throws IllegalStateException {
		if (getType() != JsonNodeType.JsonArray && getType() != JsonNodeType.JsonObject) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else {
			return children != null ? children.size() : 0;
		}
	}

	/**
	 * <p>Get children of the node. This method can be used with {@linkplain JsonNodeType#JsonObject} and {@linkplain JsonNodeType#JsonArray} nodes only</p>
	 * @return immutable list of children.Can be empty, but not null
	 * @throws IllegalStateException see class description
	 */
	public JsonNode[] children() throws IllegalStateException {
		if (getType() != JsonNodeType.JsonArray && getType() != JsonNodeType.JsonObject) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else {
			return children.toArray(new JsonNode[children.size()]);
		}
	}

	/**
	 * <p>Append new child into the node. This method can be used with {@linkplain JsonNodeType#JsonObject} and {@linkplain JsonNodeType#JsonArray} nodes only</p> 
	 * @param child node to append as child
	 * @return self (can be used in chained operations)
	 * @throws IllegalStateException see class description
	 * @throws NullPointerException node no append is null
	 * @throws IllegalArgumentException named node with {@linkplain JsonNodeType#JsonArray}, or unnamed or non-unique named node with {@linkplain JsonNodeType#JsonObject} 
	 */
	public JsonNode addChild(final JsonNode child) throws IllegalStateException, NullPointerException, IllegalArgumentException {
		if (getType() != JsonNodeType.JsonArray && getType() != JsonNodeType.JsonObject) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else if (child == null) {
			throw new NullPointerException("Child to add can't be null");
		}
		else {
			switch (getType()) {
				case JsonArray	:
					if (child.hasName()) {
						throw new IllegalArgumentException("Child to add into array must not have name!");
					}
					else {
						children.add(child);
					}
					break;
				case JsonObject	:
					if (!child.hasName()) {
						throw new IllegalArgumentException("Child to add into object must have name!");
					}
					else {
						for (JsonNode item : children) {
							if (item.getName().equals(child.getName())) {
								throw new IllegalArgumentException("Child to add has duplicate name ["+item.getName()+"]!");
							}
						}
						children.add(child);
					}
					break;
				default:
					throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
			}
			return this;
		}
	}

	/**
	 * <p>Replace child in the node. This method can be used with {@linkplain JsonNodeType#JsonObject} and {@linkplain JsonNodeType#JsonArray} nodes only</p>
	 * @param childIndex child index to replace
	 * @param child child node to set as new node
	 * @return self (can be used in chained operations)
	 * @throws IllegalStateException see class description
	 * @throws NullPointerException node no append is null
	 * @throws IllegalArgumentException child index out of range, named node with {@linkplain JsonNodeType#JsonArray}, or unnamed or non-unique named node with {@linkplain JsonNodeType#JsonObject} 
	 */
	public JsonNode setChild(final int childIndex, final JsonNode child) throws IllegalStateException, NullPointerException, IllegalArgumentException {
		if (getType() != JsonNodeType.JsonArray && getType() != JsonNodeType.JsonObject) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else if (child == null) {
			throw new NullPointerException("Child to add can't be null");
		}
		else if (childIndex < 0 || childIndex >= children.size()) {
			throw new IllegalArgumentException("Child index ["+childIndex+"] out of range 0.."+(children.size()-1));
		}
		else {
			switch (getType()) {
				case JsonArray	:
					if (child.hasName()) {
						throw new IllegalArgumentException("Child to set in array must not have name!");
					}
					else {
						children.set(childIndex,child);
					}
					break;
				case JsonObject	:
					if (!child.hasName()) {
						throw new IllegalArgumentException("Child to set in object must have name!");
					}
					else {
						if (!children.get(childIndex).getName().equals(child.getName())) {
							for (JsonNode item : children) {
								if (item.getName().equals(child.getName())) {
									throw new IllegalArgumentException("Child to set has duplicate name ["+item.getName()+"]!");
								}
							}
						}
						children.set(childIndex,child);
					}
					break;
				default:
					throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
			}
			return this;
		}
	}

	/**
	 * <p>Remove child from the node. This method can be used with {@linkplain JsonNodeType#JsonObject} and {@linkplain JsonNodeType#JsonArray} nodes only</p> 
	 * @param childIndex child index to remove
	 * @return self (can be used in chained operations)
	 * @throws IllegalStateException see class description
	 * @throws IllegalArgumentException child index out of range 
	 */
	public JsonNode removeChild(final int childIndex) throws IllegalStateException, IllegalArgumentException {
		if (getType() != JsonNodeType.JsonArray && getType() != JsonNodeType.JsonObject) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else if (childIndex < 0 || childIndex >= children.size()) {
			throw new IllegalArgumentException("Child index ["+childIndex+"] out of range 0.."+(children.size()-1));
		}
		else {
			children.remove(childIndex);
			return this;
		}
	}
	
	/**
	 * <p>Get child from the node.  This method can be used with {@linkplain JsonNodeType#JsonObject} and {@linkplain JsonNodeType#JsonArray} nodes only</p>  
	 * @param childIndex child index to get
	 * @return node selected. Can't be null
	 * @throws IllegalStateException see class description
	 * @throws IllegalArgumentException child index out of range 
	 */
	public JsonNode getChild(final int childIndex) throws IllegalStateException, IllegalArgumentException {
		if (getType() != JsonNodeType.JsonArray && getType() != JsonNodeType.JsonObject) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else if (childIndex < 0 || childIndex >= children.size()) {
			throw new IllegalArgumentException("Child index ["+childIndex+"] out of range 0.."+(children.size()-1));
		}
		else {
			return children.get(childIndex);
		}
	}

	/**
	 * <p>Get child from the node. This method can be used with {@linkplain JsonNodeType#JsonObject} node only</p>  
	 * @param childName child name to get
	 * @return node selected. Can't be null
	 * @throws IllegalStateException see class description
	 * @throws IllegalArgumentException node name is null, empty or not exists in the node 
	 */
	public JsonNode getChild(final String childName) throws IllegalStateException, IllegalArgumentException {
		if (getType() != JsonNodeType.JsonObject) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else if (childName == null || childName.isEmpty()) {
			throw new IllegalArgumentException("Child name can't be null or empty");
		}
		else {
			for (JsonNode item : children) {
				if (item.hasName() && childName.equals(item.getName())) {
					return item;
				}
			}
			throw new IllegalArgumentException("Child name ["+childName+"] not found in the children");
		}
	}
	
	/**
	 * <p>Test weather name presents in the node. This method can be used with {@linkplain JsonNodeType#JsonObject} node only</p>  
	 * @param childName child name to test
	 * @return true if presents
	 * @throws IllegalStateException see class description
	 * @throws IllegalArgumentException node name is null, empty or not exists in the node 
	 * @since 0.0.5
	 */
	public boolean hasName(final String childName) throws IllegalStateException, IllegalArgumentException {
		if (getType() != JsonNodeType.JsonObject) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else if (childName == null || childName.isEmpty()) {
			throw new IllegalArgumentException("Child name can't be null or empty");
		}
		else {
			for (JsonNode item : children) {
				if (childName.equals(item.getName())) {
					return true;
				}
			}
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((strValue == null) ? 0 : strValue.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + (int) (value ^ (value >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		JsonNode other = (JsonNode) obj;
		if (children == null) {
			if (other.children != null) return false;
		} else if (!children.equals(other.children)) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (strValue == null) {
			if (other.strValue != null) return false;
		} else if (!strValue.equals(other.strValue)) return false;
		if (type != other.type) return false;
		if (value != other.value) return false;
		return true;
	}

	@Override
	public String toString() {
		if (getType() == null) {
			return super.toString();
		}
		else {
			final StringBuilder sb = new StringBuilder();
			String prefix;
			
			if (hasName()) {
				sb.append(getName()).append(" : ");
			}
			switch (getType()) {
				case JsonArray		:
					prefix = "[";
					if (children != null) {
						for (JsonNode item :children) {
							sb.append(prefix).append(item);
							prefix = " , ";
						}
					}
					sb.append(']');
					break;
				case JsonBoolean	:
					sb.append(value != 0);
					break;
				case JsonInteger	:
					sb.append(value);
					break;
				case JsonNull		:
					sb.append("null");
					break;
				case JsonObject		:
					prefix = "{";
					if (children != null) {
						for (JsonNode item :children) {
							sb.append(prefix).append(item);
							prefix = " , ";
						}
					}
					sb.append('}');
					break;
				case JsonReal		:
					sb.append(Double.longBitsToDouble(value));
					break;
				case JsonString		:
					if (strValue != null) {
						sb.append('\"').append(strValue).append('\"');
					}
					else {
						sb.append("null");
					}
					break;
				default:
			}
			return sb.toString();
		}
	}
	
	@Override
	public JsonNode clone() throws CloneNotSupportedException {
		final JsonNode	result = (JsonNode)super.clone();
		
		if (result.children != null) {
			result.children = new ArrayList<>();
		}
		return result;
	}
}