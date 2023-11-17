package chav1961.purelib.net.namingrepo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import javax.naming.CompositeName;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.fsys.FileSystemFactory;

/**
 * <p>This class is  handler to support "self" schema URL. Format of self URL is:</p>
 * <code><b>self:/#</b>&lt;base64Content&gt;[?encoding=&lt;encoding&gt;]</code>
 * <ul>
 * <li>base64Content - content that will be returned as byte stream on {@linkplain URL#openStream()} operation</li>
 * <li>encoding - content encoding for character data. This parameter can be accessed by {@linkplain URLConnection#getContentEncoding()} method</li>
 * </ul>
 * @see URLStreamHandler   
 * @see NamingRepoHandlerProvider   
 * @see URIUtils#convert2selfURI(byte[])
 * @see URIUtils#convert2selfURI(char[], String)
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public class Handler extends URLStreamHandler {
	public static final String	PROTOCOL = "namingrepo";
	
	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		return new NamingRepoStreamHandler(){
			public URLConnection openConnection(final URL url) throws IOException {
				return super.openConnection(url);
			};
		}.openConnection(url);
	}

	private static class NamingRepoStreamHandler extends URLStreamHandler {
		private final ClassLoader	loader;
		
		/**
		 * <p>Create class instance</p>
		 */
		public NamingRepoStreamHandler() {
			this(NamingRepoStreamHandler.class.getClassLoader());
		}
		
		/**
		 * <p>Create class instance</p>
		 * @param loader loader to use in the {@link FileSystemFactory#createFileSystem(URI, ClassLoader)} call
		 */
		public NamingRepoStreamHandler(final ClassLoader loader) {
			if (loader == null) {
				throw new NullPointerException("Class loader can't be null"); 
			}
			else {
				this.loader = loader;
			}
		}	
	
		@Override
		protected URLConnection openConnection(final URL url) throws IOException {
			if (url.getProtocol().equalsIgnoreCase(PROTOCOL)) {
				return new NamingRepoURLConnection(url,loader);
			}
			else {
				throw new IOException("URL ["+url+"]: scheme ["+url.getProtocol()+"] is not supported by this stream handler, use ["+PROTOCOL+"] only!");
			}
		}
	
		private static class NamingRepoURLConnection extends URLConnection {
			private final InitialContext	initialContext;
			private final Name				name;
			private boolean					closed = false;
			
			protected NamingRepoURLConnection(final URL url, final ClassLoader loader) throws IOException {
				super(url);
				try {
					initialContext = new InitialContext();
					name = new CompositeName("protocol/"+url.getPath());
				} catch (NamingException e) {
					throw new IOException(e);
				}
			}
	
			@Override
			public void connect() throws IOException {
				if (!getDoInput() && !getDoOutput()) {
					throw new IOException("Neither setDoInput(), nor setDoOutput() was required on the connection. Call one of these method before");
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
					try {
						final Object	value = initialContext.lookup(name);
						
						if (value instanceof byte[]) {
							return new ByteArrayInputStream((byte[])value); 
						}
						else {
							return new ByteArrayInputStream(new byte[0]);
						}
					} catch (NamingException e) {
						throw new IOException(e);
					} finally {
						closed = true;
					}
				}
			}
			
			@Override
			public OutputStream getOutputStream() throws IOException {
				if (closed) {
					throw new IllegalStateException("This method can be called exactly once. Reconnect to data source!"); 
				}
				else {
					closed = true;
					return new ByteArrayOutputStream() {
						@Override
						public void close() throws IOException {
							try{
								super.close();
								initialContext.bind(name, this.toByteArray());
							} catch (NamingException e) {
								throw new IOException(e);
							}
						}
					};
				}
	        }
			
			@Override
			public String getContentEncoding() {
				return super.getContentEncoding();
			}
		}
	}
}
