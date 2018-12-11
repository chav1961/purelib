package chav1961.purelib.sql;

class RsMetaDataElement {
	private final String	name;
	private final String	description;
	private final String	typeName;
	private final int		type;
	private final int		length;
	private final int		frac;

	RsMetaDataElement(final String name, final String description, final String typeName, final int type, final int length, final int frac) {
		this.name = name;
		this.description = description;
		this.typeName = typeName;
		this.type = type;
		this.length = length;
		this.frac = frac;
	}

	String getName() {
		return name;
	}

	String getDescription() {
		return description;
	}

	String getTypeName() {
		return typeName;
	}

	int getType() {
		return type;
	}

	int getLength() {
		return length;
	}

	int getFrac() {
		return frac;
	}

	@Override
	public String toString() {
		return "RsMetaDataElement [name=" + name + ", description=" + description + ", typeName=" + typeName + ", type=" + type + ", length=" + length + ", frac=" + frac + "]";
	}
}