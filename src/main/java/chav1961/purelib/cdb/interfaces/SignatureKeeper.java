package chav1961.purelib.cdb.interfaces;

/**
 * <p>This interface describes any entity that have a signature</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.9">Java signature</a>
 */
@FunctionalInterface
public interface SignatureKeeper {
	/**
	 * <p>Get entity signature</p>
	 * @return entity signature. Can't be null and must contain valid Java entity signature.
	 */
	String getSignature();
}
