package chav1961.purelib.fsys.interfaces;

import java.io.Serializable;
import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * <p>Don't use this interface for any purposes! It's a special for the RMI file system only.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface RMIDataWrapperInterface extends Remote, Serializable {
	String[] list(URI path, String pattern) throws RemoteException;
	void mkDir(URI path) throws RemoteException;
	void create(URI path) throws RemoteException;
	void setName(URI path,String name) throws RemoteException;
	void delete(URI path) throws RemoteException;
	void store(URI path, byte[] data, boolean append) throws RemoteException;
	byte[] load(URI path) throws RemoteException;
	Map<String, Object> getAttributes(URI path) throws RemoteException;
	void linkAttributes(URI path,Map<String, Object> attributes) throws RemoteException;
}
