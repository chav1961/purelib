package chav1961.purelib.nanoservice.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sun.net.httpserver.*;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.nanoservice.NanoServiceFactory;

/**
 * <p>This interface is an internal interface for {@linkplain NanoServiceFactory} implementation. When any pluggable processing class was installed into Nano service,
 * wrapper class will be created for it. This wrapper must implements this interface to process HTTP-requests to it.</p>  
 * @see NanoService
 * @see NanoServiceFactory
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface MethodExecutor {
	/**
	 * <p>Process HTTP request and return HTTP response code
	 * @param type query type
	 * @param path path from browser address string
	 * @param query query from browser address string 
	 * @param requestHeaders list of request headers from HTTP request
	 * @param responseHeaders list of HTTP headers for HTTP response
	 * @param is request body
	 * @param os stream for response body
	 * @return HTTP response code
	 * @throws IOException on any I/O errors
	 * @throws ContentException on any content errors
	 * @throws FlowException on any flow errors
	 * @throws EnvironmentException in any environment errors
	 */
	int execute(QueryType type, char[] path, char[] query, Headers requestHeaders, Headers responseHeaders, InputStream is, OutputStream os) throws IOException, ContentException, FlowException, EnvironmentException;
}
