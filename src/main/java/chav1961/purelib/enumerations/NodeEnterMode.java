package chav1961.purelib.enumerations;

/**
 * <p>This enumeration describes entry type for any walkers (firstly tree walker). In conjunction with {@linkplain ContinueMode} 
 * it supports walking on the linked structures for different walking methods. This enumeration means:</p>
 * <ul>
 * <li>enter mode - walker enters to current node</li> 
 * <li>exit node - walker exits from current node</li> 
 * </ul>
 * <p>Walker can walk any linked structures related to current node after entering to it, but always returns from walking before exit. 
 * Any non-exceptable walking is guaranteed, that any entering mode will have appropriative exiting mode</p>
 * @see ContinueMode 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public enum NodeEnterMode {
	ENTER, EXIT
}