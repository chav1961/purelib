package chav1961.purelib.fsys.internal;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.AbstractFileSystem;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
import chav1961.purelib.i18n.internal.PureLibLocalizer;

public class FileSystemOnFTP extends AbstractFileSystem implements FileSystemInterfaceDescriptor {
	private static final URI	SERVE = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":ftp:/");
	private static final URI	SERVE_S = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":ftp:/");
	private static final String	DESCRIPTION = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_DESCRIPTION_SUFFIX;
	private static final String	VENDOR = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_VENDOR_SUFFIX;
	private static final String	LICENSE = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_SUFFIX;
	private static final String	LICENSE_CONTENT = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_CONTENT_SUFFIX;
	private static final String	HELP = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_HELP_SUFFIX;
	private static final Icon	ICON = new ImageIcon(FileSystemOnXMLReadOnly.class.getResource("xmlIcon.png"));

	private static enum Command {
		UNKNOWN
	}
	
	private final FileSystemOnFTP	parent;
	
	public FileSystemOnFTP(){
		this.parent = null;
	}	
	
	public FileSystemOnFTP(final FileSystemOnFTP parent, final URI rootPath) throws IOException {
		super(rootPath);
		this.parent = parent;
	}

	private FileSystemOnFTP(final FileSystemOnFTP parent) throws IOException {
		super(parent);
		this.parent = parent;
	}
	
	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getVersion() {
		return PureLibSettings.CURRENT_VERSION;
	}

	@Override
	public URI getLocalizerAssociated() {
		return PureLibLocalizer.LOCALIZER_SCHEME_URI;
	}

	@Override
	public String getDescriptionId() {
		return DESCRIPTION;
	}

	@Override
	public String getVendorId() {
		return VENDOR;
	}

	@Override
	public Icon getIcon() {
		return ICON;
	}

	@Override
	public String getLicenseId() {
		return LICENSE;
	}

	@Override
	public String getLicenseContentId() {
		return LICENSE_CONTENT;
	}

	@Override
	public String getHelpId() {
		return HELP;
	}

	@Override
	public URI getUriTemplate() {
		return SERVE;
	}

	@Override
	public FileSystemInterface getInstance() throws EnvironmentException {
		return this;
	}

	@Override
	public boolean testConnection(final URI connection, final LoggerFacade logger) throws IOException {
		if (connection == null) {
			throw new NullPointerException("Connection to test can't be null");
		}
		else {
			try(final FileSystemInterface	inst  = newInstance(connection)) {
				
				return inst.exists();
			} catch (EnvironmentException e) {
				if (logger != null) {
					logger.message(Severity.error, e, "Error testing connection [%1$s]: %2$s",connection,e.getLocalizedMessage());
				}
				throw new IOException(e.getLocalizedMessage(),e);
			}
		}
	}

	@Override
	public boolean canServe(final URI resource) {
		if (resource == null) {
			throw new NullPointerException("Resource can't be null");
		}
		else {
			return URIUtils.canServeURI(resource,SERVE) || URIUtils.canServeURI(resource,SERVE_S);
		}
	}

	@Override
	public FileSystemInterface newInstance(final URI uriSchema) throws EnvironmentException {
		if (uriSchema == null) {
			throw new NullPointerException("URI schema can't be null");
		}
		else if (canServe(uriSchema)) {
			try {
				return new FileSystemOnFTP(this, uriSchema);
			} catch (IOException e) {
				throw new EnvironmentException(e);
			}
		}
		else {
			throw new IllegalArgumentException("URI scheme ["+uriSchema+"] can't be served by this class");
		}
	}

	@Override
	public FileSystemInterface clone() {
		try {
			return new FileSystemOnFTP(this);
		} catch (IOException e) {
			throw new EnvironmentException(e);
		}
	}

	@Override
	public DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException {
		return new FTPDataWrapper(actualPath);
	}
	
	private String sendCommand(final Command command, final Object... parameters) {
		return null;
	}
	
	private int getResponseCode(final String content) {
		return 0;
	}
	
	private void lockControlChannel() {
		
	}
	
	private void unlockControlChannel() {
		
	}
	
	private Socket connectPassive() {
		return null;
	}
	
	private class FTPDataWrapper implements DataWrapperInterface {
		private final String	wrapper;
		
		public FTPDataWrapper(final URI wrapper) {
			this.wrapper = wrapper.toString();
		}

		@Override
		public boolean tryLock(String path, boolean sharedMode) throws IOException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void lock(String path, boolean sharedMode) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void unlock(String path, boolean sharedMode) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public URI[] list(Pattern pattern) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void mkDir() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void create() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setName(String name) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void delete() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public OutputStream getOutputStream(boolean append) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void linkAttributes(Map<String, Object> attributes) throws IOException {
			// TODO Auto-generated method stub
			
		}
		
		private File parseFileDescriptor(final String desc) {
			return null;
		}
	}	
}
