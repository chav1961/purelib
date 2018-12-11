package chav1961.purelib.enumerations;

/**
 * <p>This enumeration describes continue mode for any walkers (firstly tree walker). </p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public enum ContinueMode {
	/**
	 * <p>Continue walking</p>
	 */
	CONTINUE,
	/**
	 * <p>Move to siblings only, don't walk to parent or children nodes</p>
	 */
	SIBLINGS_ONLY,
	/**
	 * <p>Walk from current node to the root only </p>
	 */
	PARENT_ONLY,
	/**
	 * <p>Skip siblings and return to the previous level</p>
	 */
	SKIP_SIBLINGS,
	/**
	 * <p>Skip children and continue on the current level</p>
	 */
	SKIP_CHILDREN, 
	/**
	 * <p>Don't walk up to the root</p>
	 */
	SKIP_PARENT,
	/**
	 * <p>Stop walking immediately</p>
	 */
	STOP
}