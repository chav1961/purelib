package chav1961.purelib.fsys.interfaces;

import java.io.IOException;
import java.net.URI;

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
	 * @return file system driver class name
	 */
	String getClassName();
	String getVersion();
	URI getLocalizerAssociated();
	String getDescriptionId();
	String getVendorId();
	String getLicenseId();
	String getLicenseContentId();
	String getHelpId();
	URI getUriTemplate();
	boolean testConnection(final URI connection, final LoggerFacade logger) throws IOException;
}
