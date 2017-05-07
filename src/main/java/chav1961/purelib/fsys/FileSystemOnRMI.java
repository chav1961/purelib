package chav1961.purelib.fsys;

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
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.regex.Pattern;

import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.RMIDataWrapperInterface;

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
 */

public class FileSystemOnRMI extends AbstractFileSystem {
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
		this.remote = remote;
		try{this.server = (RMIDataWrapperInterface)Naming.lookup(remote.toString());
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
	public boolean canServe(String scheme) {
		return "rmi".equals(scheme);
	}

	@Override
	public FileSystemInterface clone() {
		return new FileSystemOnRMI(this);
	}

	@Override
	public DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException {
		return new RemoteDataWrapper(server,actualPath);
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
