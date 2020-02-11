package chav1961.purelib.fsys.interfaces;

import java.io.IOException;
import java.net.URI;
import java.util.ServiceLoader;

import javax.swing.Icon;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.ui.swing.useful.JFileSystemChanger;

/**
 * <p>This interface describes file system drivers in user-friendly form. In conjunction with {@linkplain JFileSystemChanger}
 * allows to select any file system by Swing form</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public interface FileSystemInterfaceDescriptor {
	/**
	 * <p>Get class name of the file system driver. Good idea is to use driver.getClass().getSimpleName() as a result</p>
	 * @return file system driver class name. Can't be null
	 */
	String getClassName();
	
	/**
	 * <p>Return driver version</p>
	 * @return driver version. Can't be null
	 */
	String getVersion();
	
	/**
	 * <p>Get localizer associated with the given driver. It can be used to localize all string returns for this interface methods</p>
	 * @return localizer associated. Can be null
	 */
	URI getLocalizerAssociated();
	
	/**
	 * <p>Get description of the file system. Value returned can be used as key for localizer associated</p> 
	 * @return Description string. Can be null
	 */
	String getDescriptionId();
	
	/**
	 * <p>Get vendor ID for the file system. Value returned can be used as key for localizer associated</p>
	 * @return Vendor id string. Can be null
	 */
	String getVendorId();
	
	/**
	 * <p>Get icon associated with the given driver.</p>
	 * @return Icon instance. Can be null
	 */
	Icon getIcon();
	
	/**
	 * <p>Get license identifier for the given driver. Value returned can be used as key for localizer associated</p>
	 * @return license id. Can't be null
	 */
	String getLicenseId();
	
	/**
	 * <p>Get license content identifier for the given driver (usually license text). Value returned can be used as key for localizer associated</p>
	 * @return license content id. Can be null
	 */
	String getLicenseContentId();
	
	/**
	 * <p>Get help id for the given driver. Value returned can be used as key for localizer associated</p>
	 * @return Help id. Can be null
	 */
	String getHelpId();
	
	/**
	 * <p>Get URI template for the given driver. URI must be absolute and contain at least File system URI scheme and this diver sub-scheme. 
	 * It's strongly recommended to use URI pointed to root of the file system (path content contains '/' only)</p> 
	 * @return URI template. Can't be null
	 */
	URI getUriTemplate();
	
	/**
	 * <p>Is the file system always read-only</p>
	 * @return true if yes
	 */
	default boolean isReadOnly() {
		return false;
	}
	
	/**
	 * <p>Create file system instance. Must returns the same instance as call to {@linkplain ServiceLoader#load(Class)} method.</p>
	 * @return File system instance returned. Cn't be null
	 * @throws EnvironmentException if File system instance can't be created
	 */
	FileSystemInterface getInstance() throws EnvironmentException;
	
	/**
	 * <p>Test connection to the given file system with the given URI</p>
	 * @param connection URI to connect to the file system. Must be absolute and it's scheme and sub-scheme must match URi returned by {@linkplain #getUriTemplate()} method
	 * @param logger logger to print connection messages to
	 * @return true is connection successful, false otherwise
	 * @throws IOException any problems on connection
	 */
	boolean testConnection(final URI connection, final LoggerFacade logger) throws IOException;
}
