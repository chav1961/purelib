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
		this.name = name;
		this.description = description;
		this.typeName = typeName;
		this.location = location;
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

}