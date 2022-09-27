package chav1961.purelib.sql.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * <p>This class is an implementation for versions of something. It contains parts splitted by dots. Any depth of dots is supported. 
 * This class implements {@linkplain Comparable} interface and can be user for sorting, comparison, ordering etc. Parts can have any nature
 * (not only numbers) so will be compared in lexical order</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public class SimpleDottedVersion implements Comparable<SimpleDottedVersion>, Serializable {
	private static final long 				serialVersionUID = -3748545032045896481L;
	public static final SimpleDottedVersion	INITIAL_VERSION = new SimpleDottedVersion("0.0"); 
	
	private final String[]			parts;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param version String with version. Can't be null or empty;
	 * @throws IllegalArgumentException versions string is null or empty
	 */
	public SimpleDottedVersion(final String version) {
		if (version == null || version.isEmpty()) {
			throw new IllegalArgumentException("Version string can't be null or empty"); 
		}
		else {
			this.parts = version.split("\\.");
			for (int index = 0; index < parts.length; index++) {
				parts[index] = parts[index].trim(); 
			}
		}
	}

	/**
	 * <p>Get string representation for the given version</p>
	 * @return String representation (neither null nor empty).
	 */
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
		return getVersion();
	}
}
