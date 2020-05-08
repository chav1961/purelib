package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface must be used to grant access from inner unnamed modules to any own modules for on-the-fly code was built</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
@FunctionalInterface
public interface ModuleAccessor {
	/**
	 * Grant access for all unnamed modules from names module.
	 * @param unnamedModules unnamed modules to grant access to
	 */
	void allowUnnamedModuleAccess(Module... unnamedModules);
}