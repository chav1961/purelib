package chav1961.purelib.basic.interfaces;

import chav1961.purelib.basic.GettersAndSettersFactory;

/**
 * <p>This class is used in conjunction with Java 9 module system to grant access for all on-the-fly generated classes inside the given class to external modules.
 * This ability usually requests to grant access for {@linkplain GettersAndSettersFactory} generated classes.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public interface ModuleExporter {
	/**
	 * <p>Get access to unnamed module inside the the class</p>
	 * @return unnamed module description or null if nothing to grant access for
	 */
	Module getUnnamedModule();
}
