package chav1961.purelib.model;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>This class contains a set of well-known constants for the model entities</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */

public class Constants {
	public static final String	MODEL_NAVIGATION_TOP_PREFIX = "navigation.top";
	public static final String	MODEL_NAVIGATION_NODE_PREFIX = "navigation.node";
	public static final String	MODEL_NAVIGATION_LEAF_PREFIX = "navigation.leaf";
	public static final String	MODEL_NAVIGATION_SEPARATOR = "navigation.separator";
	public static final String	MODEL_NAVIGATION_KEYSET_PREFIX = "navigation.keyset";

	public static final String	MODEL_KEYSET_KEY_PREFIX = "keyset.key";
	
	public static final String	MODEL_APPLICATION_SCHEME_CLASS = "class";
	public static final String	MODEL_APPLICATION_SCHEME_FIELD = "field";
	public static final String	MODEL_APPLICATION_SCHEME_TABLE = "table";
	public static final String	MODEL_APPLICATION_SCHEME_COLUMN = "column";
	public static final String	MODEL_APPLICATION_SCHEME_ID = "id";
	public static final String	MODEL_APPLICATION_SCHEME_NAVIGATOR = "navigator";
	public static final String	MODEL_APPLICATION_SCHEME_ACTION = "action";
	public static final String	MODEL_APPLICATION_SCHEME_BUILTIN_ACTION = "builtin";
	public static final String	MODEL_APPLICATION_SCHEME_REF = "ref";
	
	public static final String	MODEL_BUILTIN_LANGUAGE = "builtin.languages";
	public static final String	MODEL_BUILTIN_LAF = "builtin.lookAndFeel";

	static final Set<String>	MODEL_AVAILABLE_BUILTINS = new HashSet<>();
	
	static {
		Constants.MODEL_AVAILABLE_BUILTINS.add(Constants.MODEL_BUILTIN_LANGUAGE);
		Constants.MODEL_AVAILABLE_BUILTINS.add(Constants.MODEL_BUILTIN_LAF);
	}
}
