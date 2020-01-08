package chav1961.purelib.basic;

import java.awt.datatransfer.MimeTypeParseException;

public class MimeType {
	private final String	primaryType, subType;

	public MimeType(final String primaryType, final String subtype) throws MimeTypeParseException {
		this.primaryType = primaryType;
		this.subType = subtype;
	}

	public MimeType() {
		this.primaryType = "";
		this.subType = "";
	}

	public MimeType(final String mime) throws MimeTypeParseException {
		final String[]	parts = mime.split("/");
		
		this.primaryType = parts[0];
		this.subType = parts[1];
	}

	public String getSubType() {
		return subType;
	}

	public String getPrimaryType() {
		return primaryType;
	}

	public boolean match(final MimeType targetType) {
		return equals(targetType);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((primaryType == null) ? 0 : primaryType.hashCode());
		result = prime * result + ((subType == null) ? 0 : subType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MimeType other = (MimeType) obj;
		if (primaryType == null) {
			if (other.primaryType != null) return false;
		} else if (!primaryType.equals(other.primaryType)) return false;
		if (subType == null) {
			if (other.subType != null) return false;
		} else if (!subType.equals(other.subType)) return false;
		return true;
	}

	@Override
	public String toString() {
		return primaryType + "/" + subType;
	}
}
