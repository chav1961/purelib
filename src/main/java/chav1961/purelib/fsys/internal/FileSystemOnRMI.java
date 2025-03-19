package chav1961.purelib.fsys.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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
import chav1961.purelib.fsys.RMIFileSystemServer;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
import chav1961.purelib.fsys.interfaces.RMIDataWrapperInterface;
import chav1961.purelib.i18n.internal.PureLibLocalizer;

/**
 * <p>This class implements the file system interface on remote server using standard Java RMI protocol.
 * //localhost:"+Registry.REGISTRY_PORT+"/testRMI
 * </p>
 * 
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface
 * @see chav1961.purelib.fsys.RMIFileSystemServer
 * @see chav1961.purelib.fsys JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @last.update 0.0.5
 */

public class FileSystemOnRMI extends AbstractFileSystem implements FileSystemInterfaceDescriptor {
	private static final URI	SERVE = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":rmi:/");
	private static final String	DESCRIPTION = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnRMI.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_DESCRIPTION_SUFFIX;
	private static final String	VENDOR = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnRMI.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_VENDOR_SUFFIX;
	private static final String	LICENSE = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnRMI.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_SUFFIX;
	private static final String	LICENSE_CONTENT = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnRMI.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_CONTENT_SUFFIX;
	private static final String	HELP = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnRMI.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_HELP_SUFFIX;
	private static final Icon	ICON = new ImageIcon(FileSystemOnRMI.class.getResource("rmiIcon.png"));
	
	private final URI						remote;
	private final RMIDataWrapperInterface	server;
	
	/**
	 * <p>This constructor is an entry for the SPI service only. Don't use it in any purposes</p> 
	 */
	public FileSystemOnRMI(){
		this.remote = null;
		this.server = null;
	}
	
	
	/**
	 * <p>Create the file system for the given remote connection.  
	 * @param remote remote uri for the remote file system server. Need be absolute URI with the schema 'rmi', for example <code>'rmi://localhost/rmiServerName'</code>. Tail of URI (rmiServerName) 
	 * need be corresponding with the registered RMI server instance name (see {@link RMIFileSystemServer}) 
	 * @throws IOException if any exception was thrown
	 */
	public FileSystemOnRMI(final URI remote) throws IOException {
		super(remote);
		this.remote = remote;
		try{final Object	server = Naming.lookup(remote.toString());
			
			if (server instanceof RMIDataWrapperInterface) {
				this.server = (RMIDataWrapperInterface)server;
			}
			else {
				throw new IOException("Remote server ["+remote+"] not found");
			}
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			throw new IOException(e.getMessage());
		}
	}

	private FileSystemOnRMI(final FileSystemOnRMI another) {
		super(another);
		this.remote = another.remote;
		this.server = another.server;
	}

	@Override
	public boolean canServe(final URI resource) {
		return URIUtils.canServeURI(resource,SERVE);
	}
	
	@Override
	public FileSystemInterface newInstance(final URI resource) throws EnvironmentException {
		if (!canServe(resource)) {
			throw new EnvironmentException("Resource URI ["+resource+"] is not supported by the class. Valid URI must be ["+SERVE+"...]");
		}
		else {
			try{return new FileSystemOnRMI(URI.create(resource.getRawSchemeSpecificPart()));
			} catch (IOException e) {
				throw new EnvironmentException("I/O error creation file system on RMI: "+e.getLocalizedMessage(),e);
			}
		}
	}

	@Override
	public FileSystemInterface clone() {
		return new FileSystemOnRMI(this);
	}

	@Override
	public DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException {
		return new RemoteDataWrapper(server,actualPath);
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
	public Icon getIcon() {
		return ICON;
	}
	
	@Override
	public String getVendorId() {
		return VENDOR;
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
	
	private static class RemoteDataWrapper implements DataWrapperInterface {
		private final RMIDataWrapperInterface	remote;
		private final URI						path; 
		
		RemoteDataWrapper(final RMIDataWrapperInterface remote, final URI path) {
			this.remote = remote;
			this.path = path;
		}

		@Override
		public URI[] list(Pattern pattern) throws IOException {
			try{final String[]	call = remote.list(path,pattern.pattern());
				final URI[]		result = new URI[call.length];
				
				for (int index = 0; index < result.length; index++) {
					result[index] = URI.create(call[index]);
				}
				return result;
			} catch (RemoteException exc) {
				throw new IOException(exc.getMessage());
			}
		}

		@Override
		public void mkDir() throws IOException {
			try{remote.mkDir(path);
			} catch (RemoteException exc) {
				throw new IOException(exc.getMessage());
			}
		}

		@Override
		public void create() throws IOException {
			try{remote.create(path);
			} catch (RemoteException exc) {
				throw new IOException(exc.getMessage());
			}
		}

		@Override
		public void setName(String name) throws IOException {
			try{remote.setName(path,name);
			} catch (RemoteException exc) {
				throw new IOException(exc.getMessage());
			}
		}

		@Override
		public void delete() throws IOException {
			try{remote.delete(path);
			} catch (RemoteException exc) {
				throw new IOException(exc.getMessage());
			}
		}

		@Override
		public OutputStream getOutputStream(boolean append) throws IOException {
			return new StoredByteArrayOutputStream(remote,path,append);
		}

		@Override
		public InputStream getInputStream() throws IOException {
			try{return new ByteArrayInputStream(remote.load(path));
			} catch (RemoteException exc) {
				throw new IOException(exc.getMessage());
			}
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			try{return remote.getAttributes(path);
			} catch (RemoteException exc) {
				throw new IOException(exc.getMessage());
			}
		}

		@Override
		public void linkAttributes(Map<String, Object> attributes) throws IOException {
			try{remote.linkAttributes(path,attributes);
			} catch (RemoteException exc) {
				throw new IOException(exc.getMessage());
			}
		}

		@Override
		public boolean tryLock(final String path, final boolean sharedMode) throws IOException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void lock(final String path, final boolean sharedMode) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void unlock(final String path, final boolean sharedMode) throws IOException {
			// TODO Auto-generated method stub
			
		}
	}

	private static class StoredByteArrayOutputStream extends ByteArrayOutputStream {
		private final RMIDataWrapperInterface	remote;
		private final URI 						path;
		private final  boolean 					append;
		
		
		public StoredByteArrayOutputStream(final RMIDataWrapperInterface remote, final URI path, final boolean append) {
			this.remote = remote;				this.path = path;
			this.append = append;
		}
		
		@Override
		public void close() throws IOException {
			super.close();
			remote.store(path,toByteArray(), append);
		}
	}
}
