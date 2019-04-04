package chav1961.purelib.sql;

public class RsMetaDataElement implements Comparable<RsMetaDataElement> {
	private final String	name;
	private final String	description;
	private final String	typeName;
	private final int		location;
	private final int		type;
	private final int		length;
	private final int		frac;

	public RsMetaDataElement(final String name, final String description, final String typeName, final int type, final int length, final int frac) {
		this(name,description,typeName,0,type,length,frac);
	}

	public RsMetaDataElement(final String name, final String description, final String typeName, final int location, final int type, final int length, final int frac) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name can't be null or empty");
		}
		else if (typeName == null || typeName.isEmpty()) {
			throw new IllegalArgumentException("Type name can't be null or empty");
		}
		else if (location < 0) {
			throw new IllegalArgumentException("Location ["+location+"] can't be negative");
		}
		else if (length < 0) {
			throw new IllegalArgumentException("Length ["+length+"] can't be negative");
		}
		else if (frac < 0) {
			throw new IllegalArgumentException("Frac ["+frac+"] can't be negative");
		}
		else if (length > 0 && frac >= length) {
			throw new IllegalArgumentException("Frac ["+frac+"] must be less than length ["+length+"]");
		}
		else if (SQLUtils.typeIdByTypeName(typeName) == SQLUtils.UNKNOWN_TYPE) {
			throw new IllegalArgumentException("Unknown or unsupported SQL type ["+typeName+"]");
		}
		else if (SQLUtils.typeNameByTypeId(type) == null) {
			throw new IllegalArgumentException("Unknown or unsupported SQL type id ["+type+"]");
		}
		else {
			this.name = name;
			this.description = description;
			this.typeName = typeName.toUpperCase();
			this.location = location;
			this.type = type;
			this.length = length;
			this.frac = frac;
		}
	}
	
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getTypeName() {
		return typeName;
	}

	public int getType() {
		return type;
	}

	public int getLength() {
		return length;
	}

	public int getFrac() {
		return frac;
	}

	int getLocation() {
		return location;
	}

	@Override
	public int compareTo(final RsMetaDataElement another) {
		if (another == null) {
			return 1;
		}
		else if (another.location == location) {
			return name.compareTo(another.name);
		}
		else {
			return location - another.location;
		}
	}
	
	@Override
	public String toString() {
		return "RsMetaDataElement [name=" + name + ", description=" + description + ", typeName=" + typeName + ", type=" + type + ", length=" + length + ", frac=" + frac + ", location=" + location + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + frac;
		result = prime * result + length;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + type;
		result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		RsMetaDataElement other = (RsMetaDataElement) obj;
		if (frac != other.frac) return false;
		if (length != other.length) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (type != other.type) return false;
		if (typeName == null) {
			if (other.typeName != null) return false;
		} else if (!typeName.equals(other.typeName)) return false;
		return true;
	}
}