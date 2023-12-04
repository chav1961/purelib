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
	
	public String toHumanReadableString() {
		switch (major) {
			case JavaByteCodeConstants.MAJOR_1_1	:
				return "1.1";
			case JavaByteCodeConstants.MAJOR_1_2	:
				return "1.2";
			case JavaByteCodeConstants.MAJOR_1_3	:
				return "1.3";
			case JavaByteCodeConstants.MAJOR_1_4	:
				return "1.4";
			case JavaByteCodeConstants.MAJOR_1_5	:
				return "1.5";
			case JavaByteCodeConstants.MAJOR_1_6	:
				return "1.6";
			case JavaByteCodeConstants.MAJOR_1_7	:
				return "1.7";
			case JavaByteCodeConstants.MAJOR_8		:
				return "8";
			case JavaByteCodeConstants.MAJOR_9		:
				return "9";
			case JavaByteCodeConstants.MAJOR_10		:
				return "10";
			case JavaByteCodeConstants.MAJOR_11		:
				return "11";
			case JavaByteCodeConstants.MAJOR_12		:
				return "12";
			case JavaByteCodeConstants.MAJOR_13		:
				return "13";
			case JavaByteCodeConstants.MAJOR_14		:
				return "14";
			case JavaByteCodeConstants.MAJOR_15		:
				return "15";
			case JavaByteCodeConstants.MAJOR_16		:
				return "16";
			case JavaByteCodeConstants.MAJOR_17		:
				return "17";
			case JavaByteCodeConstants.MAJOR_18		:
				return "18";
			case JavaByteCodeConstants.MAJOR_19		:
				return "19";
			case JavaByteCodeConstants.MAJOR_20		:
				return "20";
			case JavaByteCodeConstants.MAJOR_21		:
				return "21";
			default :
				return "<unknown>";
		}
	}
}