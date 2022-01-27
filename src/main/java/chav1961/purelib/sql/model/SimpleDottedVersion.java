package chav1961.purelib.sql.model;

import java.util.Arrays;

public class SimpleDottedVersion implements Comparable<SimpleDottedVersion> {
	private final String[]	parts;
	
	public SimpleDottedVersion(final String version) {
		if (version == null || version.isEmpty()) {
			throw new NullPointerException("Version string can't be null or empty"); 
		}
		else {
			this.parts = version.split("\\.");
			for (int index = 0; index < parts.length; index++) {
				parts[index] = parts[index].trim(); 
			}
		}
	}

	public String getVersion() {
		return String.join(".", parts);
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(parts);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SimpleDottedVersion other = (SimpleDottedVersion) obj;
		if (!Arrays.equals(parts, other.parts)) return false;
		return true;
	}
	
	@Override
	public int compareTo(final SimpleDottedVersion o) {
		if (o == null) {
			return 1;
		}
		else {
			final int	commonPart = Math.min(this.parts.length, o.parts.length);
			int			result;
			
			for (int index = 0; index < commonPart; index++) {
				result = parts[index].compareTo(o.parts[index]);
				
				if (result != 0) {
					return result;
				}
			}
			return parts.length - o.parts.length;
		}
	}

	@Override
	public String toString() {
		return "SimpleDottedVersion [parts=" + getVersion() + "]";
	}
}
