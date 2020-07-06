package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface is a parent for set of interfaces to get dynamic access to linked variables</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public interface OnlineGetter {
	/**
	 * <p>Is linked variable immutable (for example, 'variable' is an expression)</p>
	 * @return true if yes
	 */
	default boolean isImmutable() {
		return true;
	}
}
