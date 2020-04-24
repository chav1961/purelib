package chav1961.purelib.ui.interfaces;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

/**
 * <p>This interface describes UI controller. UI controller is used to control auto-built GUI entities. Class implemented this interface, will be
 * called after calling appropriative {@linkplain FormManager} methods. To exclude implementation dependencies, all methods of this interface make
 * control by returning implementation-independent commands. Syntax of these commands is:</p>
 * <ul>
 * </li><b>disable</b> &lt;fieldName&gt;,...</li>
 * </li><b>enable</b> &lt;fieldName&gt;,...</li>
 * </li><b>immutable</b> &lt;fieldName&gt;,...</li>
 * </li><b>mutable</b> &lt;fieldName&gt;,...</li>
 * </li><b>invisible</b> &lt;fieldName&gt;,...</li>
 * </li><b>visible</b> &lt;fieldName&gt;,...</li>
 * </li><b>nonfocusable</b> &lt;fieldName&gt;,...</li>
 * </li><b>focusable</b> &lt;fieldName&gt;,...</li>
 * </ul>
 * </p>One or more commands in returned string must be separated with semicolon (;). Unparsed commands in the string are ignored without any notices, unknown fields throws exceptions<p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
public interface UIController {
	/**
	 * <p>This method is called before showing any controls on the screen</p>
	 * @param node root metadata associated with UI form
	 * @return command string as described in the class description. Can be null
	 */
	String before(ContentNodeMetadata node);
	
	/**
	 * <p>This method is called after calling {@linkplain FormManager#onField(Object, Object, String, Object)} method.</p>
	 * @param node root metadata for the controlled instance (the same first parameter in the {@linkplain FormManager#onField(Object, Object, String, Object)} call)
	 * @param fieldName field name passed (third parameter in the {@linkplain FormManager#onField(Object, Object, String, Object)} call)
	 * @param mode value returned by {@linkplain FormManager#onField(Object, Object, String, Object)} call
	 * @return command string as described in the class description. Can be null
	 */
	String afterField(ContentNodeMetadata node, String fieldName, RefreshMode mode);
	
	/**
	 * <p>This method is called after calling {@linkplain FormManager#onRecord(RecordFormManager.Action, Object, Object, Object, Object)} method.</p>
	 * @param node root metadata for the controlled instance (second or forth parameter in the {@linkplain FormManager#onRecord(RecordFormManager.Action, Object, Object, Object, Object)} call - dependent on action parameter)
	 * @param action action passed to {@linkplain FormManager#onRecord(RecordFormManager.Action, Object, Object, Object, Object)} call 
	 * @param mode value returned by {@linkplain FormManager#onRecord(RecordFormManager.Action, Object, Object, Object, Object)} call
	 * @return command string as described in the class description. Can be null
	 */
	String afterRecord(ContentNodeMetadata node, RecordFormManager.Action action, RefreshMode mode);
	
	/**
	 * <p>This method is called after calling {@linkplain FormManager#onAction(Object, Object, String, Object)} method.</p>
	 * @param node root metadata for the controlled instance (the same first parameter in the {@linkplain FormManager#onAction(Object, Object, String, Object)} call)
	 * @param action action passed (third parameter in the {@linkplain FormManager#onAction(Object, Object, String, Object)} call)
	 * @param mode value returned by {@linkplain FormManager#onAction(Object, Object, String, Object)} call
	 * @return command string as described in the class description. Can be null
	 */
	String afterAction(ContentNodeMetadata node, String action, RefreshMode mode);
}
