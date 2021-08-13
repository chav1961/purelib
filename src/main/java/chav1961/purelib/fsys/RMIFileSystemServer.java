package chav1961.purelib.fsys;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.RMIDataWrapperInterface;

/**
 * <p>This class is a server to the {@link FileSystemOnRMI} class. It's constructor gets an URI to register in the RMI server, and and <i>nested</i> file system will be controlled by the
 * {@link FileSystemOnRMI} client on the server side. The last path of the URI is a remote server instance <b>id</b> and it should be used in the {@link FileSystemOnRMI} constructor to get
 * access to the registered server</p>   
 * 
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface
 * @see chav1961.purelib.fsys.FileSystemOnRMI
 * @see chav1961.purelib.fsys JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class RMIFileSystemServer extends UnicastRemoteObject implements RMIDataWrapperInterface, Closeable {
	private static final long serialVersionUID = -256570471312940635L;
	
	private final FileSystemInterface	nested;
	private final URI					root;

	public RMIFileSystemServer(final URI root/* "rmi://localhost/BillingService" */, final FileSystemInterface nested) throws RemoteException {
		this.nested = nested;			this.root = root;
		
		try{Naming.rebind(root.toString(),this);
		} catch (MalformedURLException e) {
			throw new RemoteException(e.getMessage());
		}	
	}

	public RMIFileSystemServer(final URI root, final FileSystemInterface nested, final int port, final RMIClientSocketFactory clientFactory, final RMIServerSocketFactory socketFactory) throws RemoteException {
		super(port,clientFactory,socketFactory);
		this.nested = nested;			this.root = root;
	}

	public RMIFileSystemServer(final URI root, final FileSystemInterface nested, final int port) throws RemoteException {
		super(port);
		this.nested = nested;			this.root = root;
	}

	@Override
	public void close() throws RemoteException {
		try{Naming.unbind(root.toString());
		} catch (MalformedURLException | NotBoundException e) {
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public String[] list(final URI path, final String pattern) throws RemoteException {
		try(final FileSystemInterface	fsi = nested.clone().open(path.toString())) {
			return fsi.list(pattern);
		} catch (IOException e) {
			throw new RemoteException(e.getMessage()); 
		}
	}

	@Override
	public void mkDir(final URI path) throws RemoteException {
		try(final FileSystemInterface	fsi = nested.clone().open(path.toString())) {
			fsi.mkDir();
		} catch (IOException e) {
			throw new RemoteException(e.getMessage()); 
		}
	}

	@Override
	public void create(final URI path) throws RemoteException {
		try(final FileSystemInterface	fsi = nested.clone().open(path.toString())) {
			fsi.create();
		} catch (IOException e) {
			throw new RemoteException(e.getMessage()); 
		}
	}

	@Override
	public void setName(final URI path, final String name) throws RemoteException {
		try(final FileSystemInterface	fsi = nested.clone().open(path.toString())) {
			fsi.rename(name);
		} catch (IOException e) {
			throw new RemoteException(e.getMessage()); 
		}
	}

	@Override
	public void delete(final URI path) throws RemoteException {
		try(final FileSystemInterface	fsi = nested.clone().open(path.toString())) {
			fsi.delete();
		} catch (IOException e) {
			throw new RemoteException(e.getMessage()); 
		}
	}

	@Override
	public void store(final URI path, final byte[] data, final boolean append) throws RemoteException {
		try(final FileSystemInterface	fsi = nested.clone().open(path.toString());
			final OutputStream			os = append ? fsi.append().write() : fsi.create().write()) {
			
			Utils.copyStream(new ByteArrayInputStream(data),os);
		} catch (IOException e) {
			throw new RemoteException(e.getMessage()); 
		}
	}

	@Override
	public byte[] load(final URI path) throws RemoteException {
		try(final FileSystemInterface	fsi = nested.clone().open(path.toString());
			final InputStream			is = fsi.read();
			final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				
			Utils.copyStream(is,baos);
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RemoteException(e.getMessage()); 
		}
	}

	@Override
	public Map<String, Object> getAttributes(final URI path) throws RemoteException {
		try(final FileSystemInterface	fsi = nested.clone().open(path.toString())) {
			return fsi.getAttributes();
		} catch (IOException e) {
			throw new RemoteException(e.getMessage()); 
		}
	}

	@Override
	public void linkAttributes(final URI path, final Map<String, Object> attributes) throws RemoteException {
		try(final FileSystemInterface	fsi = nested.clone().open(path.toString())) {
			fsi.setAttributes(attributes);
		} catch (IOException e) {
			throw new RemoteException(e.getMessage()); 
		}
	}

	@Override
	public boolean tryLock(final String path, final boolean sharedMode) throws RemoteException {
		try(final FileSystemInterface	fsi = nested.clone().open(path.toString())) {
			return fsi.tryLock(path, sharedMode);
		} catch (IOException e) {
			throw new RemoteException(e.getMessage()); 
		}
	}

	@Override
	public void lock(final String path, final boolean sharedMode) throws RemoteException {
		try(final FileSystemInterface	fsi = nested.clone().open(path.toString())) {
			fsi.lock(path, sharedMode);
		} catch (IOException e) {
			throw new RemoteException(e.getMessage()); 
		}
	}

	@Override
	public void unlock(final String path, final boolean sharedMode) throws RemoteException {
		try(final FileSystemInterface	fsi = nested.clone().open(path.toString())) {
			fsi.unlock(path, sharedMode);
		} catch (IOException e) {
			throw new RemoteException(e.getMessage()); 
		}
	}
}
