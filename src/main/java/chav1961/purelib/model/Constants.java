package chav1961.purelib.model;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>This class contains a set of well-known constants for the model entities</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @last.update 0.0.7
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
	public static final String	MODEL_APPLICATION_SCHEME_SCHEMA = "schema";
	public static final String	MODEL_APPLICATION_SCHEME_TABLE = "table";
	public static final String	MODEL_APPLICATION_SCHEME_SEQUENCE = "sequence";
	public static final String	MODEL_APPLICATION_SCHEME_COLUMN = "column";
	public static final String	MODEL_APPLICATION_SCHEME_ID = "id";
	public static final String	MODEL_APPLICATION_SCHEME_NAVIGATOR = "navigator";
	public static final String	MODEL_APPLICATION_SCHEME_ACTION = "action";
	public static final String	MODEL_APPLICATION_SCHEME_BUILTIN_ACTION = "builtin";
	public static final String	MODEL_APPLICATION_SCHEME_REF = "ref";
	
	public static final String	MODEL_BUILTIN_LANGUAGE = "builtin.languages";
	public static final String	MODEL_BUILTIN_LAF = "builtin.lookAndFeel";
	public static final String	MODEL_BUILTIN_LRU = "builtin.LRU";
	
	/**
	 * <p>This enumeration contains all built-in menu types supported</p>
	 * @since 0.0.6
	 * @last.update 0.0.7
	 */
	public static enum Builtin {
		BUILTIN_LANGUAGE(MODEL_BUILTIN_LANGUAGE),
		BUILTIN_LAF(MODEL_BUILTIN_LAF),
		BUILTIN_LRU(MODEL_BUILTIN_LRU);
		
		private final String	constantName; 
		
		private Builtin(final String constantName) {
			this.constantName = constantName;
		}
		
		/**
		 * <p>Get constant name associated with the given item</p>
		 * @return constant name. Can't be null
		 */
		public String getConstantName() {
			return constantName;					
		}
		
		/**
		 * <p>Convert constant name to one of the builtins</p>
		 * @param constantName constant name to convert. Can't be null or empty
		 * @return builtin converted. Can't be null
		 * @throws IllegalArgumentException when constant name is null, empty or unknown 
		 */
		public static Builtin forConstant(final String constantName) throws IllegalArgumentException {
			if (constantName == null || constantName.isEmpty()) {
				throw new IllegalArgumentException("Constant name can't be nullor empty"); 
			}
			else {
				for (Builtin item : values()) {
					if (constantName.equals(item.getConstantName())) {
						return item;
					}
				}
				throw new NullPointerException("Constant name ["+constantName+"] not found in the builtins"); 
			}
		}
	}
	
	static final Set<String>	MODEL_AVAILABLE_BUILTINS = new HashSet<>();
	
	static {
		Constants.MODEL_AVAILABLE_BUILTINS.add(Constants.MODEL_BUILTIN_LANGUAGE);
		Constants.MODEL_AVAILABLE_BUILTINS.add(Constants.MODEL_BUILTIN_LAF);
		Constants.MODEL_AVAILABLE_BUILTINS.add(Constants.MODEL_BUILTIN_LRU);
	}
}
