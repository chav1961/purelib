package chav1961.purelib.net.self;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Base64;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This class is  handler to support "self" schema URL. Format of self URL is:</p>
 * <code><b>self:/#</b>&lt;base64Content&gt;</code>
 * <ul>
 * <li>base64Content - content that will be returned as byte stream on {@linkplain URL#openStream()} operation</li>
 * </ul>
 * @see URLStreamHandler   
 * @see SelfHandlerProvider   
 * @see URIUtils#convert2selfURI(byte[])
 * @see URIUtils#convert2selfURI(char[], String)
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.4
 */
public class Handler extends URLStreamHandler {
	public static final String	PROTOCOL = "self";
	
	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		return new SelfStreamHandler(){
//			private URLConnection	conn;
//			boolean					wasConnected = false;
			
			public URLConnection openConnection(final URL url) throws IOException {
				return /*conn =*/ super.openConnection(url);
			};
		}.openConnection(url);
	}

	private static class SelfStreamHandler extends URLStreamHandler {
		private final ClassLoader	loader;
		
		/**
		 * <p>Create class instance</p>
		 */
		public SelfStreamHandler() {
			this(SelfStreamHandler.class.getClassLoader());
		}
		
		/**
		 * <p>Create class instance</p>
		 * @param loader loader to use in the {@link FileSystemFactory#createFileSystem(URI, ClassLoader)} call
		 */
		public SelfStreamHandler(final ClassLoader loader) {
			if (loader == null) {
				throw new NullPointerException("Class loader can't be null"); 
			}
			else {
				this.loader = loader;
			}
		}	
	
		@Override
		protected URLConnection openConnection(final URL url) throws IOException {
			if (url.getProtocol().equalsIgnoreCase("self")) {
				try{final URI	uri = url.toURI();				
					
					return new SelfURLConnection(url,loader,uri.getFragment());
				} catch (URISyntaxException e) {
					throw new IOException("URL ["+url+"] - syntax error: "+e.getLocalizedMessage());
				}
			}
			else {
				throw new IOException("URL ["+url+"]: scheme ["+url.getProtocol()+"] is not supported by this stream handler, use ["+FileSystemInterface.FILESYSTEM_URI_SCHEME+"] only!");
			}
		}
	
		private static class SelfURLConnection extends URLConnection {
//			private final ClassLoader	loader;
			private final String		content;
//			private FileSystemInterface	fsi = null;
			private boolean				closed = false;
			
			protected SelfURLConnection(final URL url, final ClassLoader loader, final String content) throws IOException {
				super(url);
//				this.loader = loader;
				this.content = content;
			}
	
			@Override
			public void connect() throws IOException {
				if (!getDoInput() && !getDoOutput()) {
					throw new IOException("Neither setDoInput(), nor setDoOutput() was required on the connection. Call one of these method before");
				}
				else if (getDoOutput()) {
					throw new IOException("This URL is a read-only and can't be modified");
				}
				else {
					closed = false;
				}
			}
			
			@Override
			public InputStream getInputStream() throws IOException {
				if (closed) {
					throw new IllegalStateException("This method can be called exactly once. Reconnect to data source!"); 
				}
				else {
					closed = true;
					return new ByteArrayInputStream(Base64.getDecoder().decode(content)); 
				}
			}
			
			@Override
			public OutputStream getOutputStream() throws IOException {
				throw new IOException("This URL is a read-only and can't be modified");
	        }
		}
	}
}
