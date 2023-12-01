package chav1961.purelib.cdb;


/**
 * <p>This class is used to prepresent Java class Byte Code version</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public class JavaClassVersion implements Comparable<JavaClassVersion> {
	public final int	major;
	public final int	minor;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param major major version number
	 * @param minor moniro version number
	 */
	public JavaClassVersion(final int major, final int minor) {
		this.major = major;
		this.minor = minor;
	}
	
	@Override
	public int compareTo(final JavaClassVersion another) {
		if (another == null) {
			return 1;
		}
		else if (another.major == this.major) {
			return this.minor - another.minor;
		}
		else {
			return this.major - another.major;
		}
	};
	
	@Override
	public int hashCode() {
		return 31 * major + minor;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		else if (obj == this) {
			return true;
		}
		else if (obj.getClass() == this.getClass()) {
			final JavaClassVersion	v = (JavaClassVersion)obj;
			
			return this.major == v.major && this.minor == v.minor;
		}
		else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return major+"."+minor;
	}
}