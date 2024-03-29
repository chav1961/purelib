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
import java.util.Hashtable;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.net.namingrepo.NamingRepoHandlerProvider;

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
 * @since 0.0.2
 * @last.update 0.0.4
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
			if (url.getProtocol().equalsIgnoreCase(PROTOCOL)) {
				try{final URI	uri = url.toURI();				
					
					return new SelfURLConnection(url,loader,uri.getFragment());
				} catch (URISyntaxException e) {
					throw new IOException("URL ["+url+"] - syntax error: "+e.getLocalizedMessage());
				}
			}
			else {
				throw new IOException("URL ["+url+"]: scheme ["+url.getProtocol()+"] is not supported by this stream handler, use ["+PROTOCOL+"] only!");
			}
		}
	
		private static class SelfURLConnection extends URLConnection {
			private final String		content;
			private final Hashtable<String,String[]>	query;
			private boolean				closed = false;
			
			protected SelfURLConnection(final URL url, final ClassLoader loader, final String content) throws IOException {
				super(url);
				final int 	queryIndex = content.indexOf('?'); 
				
				if (queryIndex >= 0) {
					this.content = content.substring(0,queryIndex);
					this.query = URIUtils.parseQuery(content.substring(queryIndex+1));
				}
				else {
					this.content = content;
					this.query = new Hashtable<>();
				}
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
			
			@Override
			public String getContentEncoding() {
				if (query.containsKey("encoding")) {
					return query.get("encoding")[0];
				}
				else {
					return super.getContentEncoding();
				}
			}
		}
	}
}
