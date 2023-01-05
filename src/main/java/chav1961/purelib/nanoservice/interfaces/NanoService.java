package chav1961.purelib.nanoservice.interfaces;

import java.io.IOException;
import java.net.InetSocketAddress;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.nanoservice.NanoServiceFactory;

/**
 * <p>This interface describes any Nano services in the Pure Library. It supports {@linkplain ExecutionControl} life cycle of the Nano service,
 * deploying/undeployung pluggable processing classes into it, and get access to the static content root. Pluggable processing classes to deploy 
 * must me annotated with:</p>
 * <ul>
 * <li>{@linkplain RootPath} annotation</li>
 * <li>{@linkplain Path} annotation(s)</li>
 * <li>{@linkplain FromPath} annotation(s)</li>
 * <li>{@linkplain FromQuery} annotation(s)</li>
 * <li>{@linkplain FromHeader} annotation(s)</li>
 * <li>{@linkplain FromBody} annotation(s)</li>
 * <li>{@linkplain ToHeader} annotation(s)</li>
 * <li>{@linkplain ToBody} annotation(s)</li>
 * </ul> 
 * @see NanoServiceFactory
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @last.update 0.0.5
 */
public interface NanoService extends ExecutionControl {
	@Override
	void start() throws IOException;
	@Override
	void suspend() throws IOException;
	@Override
	void resume() throws IOException;
	@Override
	void stop() throws IOException;
	
	@Override
	boolean isStarted();
	
	@Override
	boolean isSuspended();

	/**
	 * <p>Deploy processing class to the given path of Nano service. Class must be annotated with annotations were mentioned above</p>
	 * @param path path to deploy processing class to
	 * @param instance2deploy pluggable singleton instance to process all requests for the given path and all subpaths
	 * @throws IOException on any I/O errors
	 * @throws ContentException on any errors in the class 
	 * @throws SyntaxException on any errors in the class annotations
	 */
	void deploy(String path, Object instance2deploy) throws IOException, ContentException, SyntaxException;
	
	/**
	 * <p>Undeploy processing class from the given path of Nano service</p>
	 * @param path path to undeploy processing class from
	 * @return pluggable singleton instance was deployed earlier of null
	 * @throws IOException on any I/O errors
	 */
	Object undeploy(final String path) throws IOException;
	
	/**
	 * <p.Get root of static content for the Nano service</p>
	 * @return root static content or null
	 */
	FileSystemInterface getServiceRoot();
	
	/**
	 * <p>Get listening address of the server</p>
	 * @return listening address. Can be null if server is not started yet
	 * @since 0.0.5
	 */
	InetSocketAddress getServerAddress();
}
