package chav1961.purelib.json;

import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.json.interfaces.JsonNodeType;

public class JsonNode extends SyntaxNode<JsonNodeType,JsonNode> {
	public JsonNode(final boolean value) {
		this(0,0,null,0,null);
	}

	public JsonNode(final long value) {
		this(0,0,null,0,null);
	}

	public JsonNode(final double value) {
		this(0,0,null,0,null);
	}

	public JsonNode(final String value) {
		this(0,0,null,0,null);
	}

	public JsonNode() {
		this(0,0,null,0,null);
	}
	
	public JsonNode(final JsonNodeType type, final JsonNode... children) {
		this(0,0,type,0,children);
	}
	
	protected JsonNode(final JsonNodeType type, final long value, final Object cargo, final JsonNode... children) {
		this(0,0,type,value,cargo,children);
	}

	protected JsonNode(final int row, final int col, final JsonNodeType type, final long value, final Object cargo, final JsonNode... children) {
		super(row, col, type, value, cargo, children);
	}

	protected JsonNode(final JsonNode another) {
		super(another);
	}
	
	public JsonNodeType getType() {
		return super.getType();
	}

	public boolean hasName() {
		return (cargo instanceof String) && !((String)cargo).isEmpty();
	}
	
	public String getName() {
		return cargo != null ? cargo.toString() : null;
	}

	public JsonNode setName(final String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name to set is null or empty. To remove name, call removeName() instead");
		}
		else {
			cargo = name;
			return this;
		}
	}
	
	public JsonNode removeName() {
		cargo = null;
		return this;
	}
	
	public boolean getBooleanValue() {
		if (getType() != JsonNodeType.JsonBoolean) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else {
			return value != 0;
		}
	}

	public long getLongValue() {
		if (getType() != JsonNodeType.JsonInteger) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else {
			return value;
		}
	}

	public double getDoubleValue() {
		if (getType() != JsonNodeType.JsonReal) {
			throw new IllegalStateException("This method can't be called, when node type is ["+getType()+"]"); 
		}
		else {
			return Double.longBitsToDouble(value);
		}
	}
	
	public String getStringValue() {
		return null;
	}
	
	public JsonNode setValue(final boolean value) {
		return this;
	}

	public JsonNode setValue(final long value) {
		return this;
	}

	public JsonNode setValue(final double value) {
		return this;
	}

	public JsonNode setValue(final String value) {
		return this;
	}

	public JsonNode setNullValue() {
		return this;
	}
	
	public int childrenCount() {
		return 0;
	}
	
	public JsonNode[] children() {
		return null;
	}
	
	public JsonNode addChild(final JsonNode child) {
		return this;
	}
	
	public JsonNode setChild(final int childIndex, final JsonNode child) {
		return this;
	}
	
	public JsonNode removeChild(final int childIndex) {
		return this;
	}
	
	public JsonNode getChild(final int childIndex) {
		return null;
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
					if (children != null && children.length > 0) {
						sb.append('\"').append(children[0]).append('\"');
					}
					else {
						sb.append("<value is missing>");
					}
					break;
				default:
			}
			return sb.toString();
		}
	}
}