package chav1961.purelib.fsys;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This class implements standard {@link URLStreamHandler} functionality to get access to the file systems thru it's URLs.</p>
 * <p>URL format for the implementation is:</p>
 * <code><b>{@linkplain FileSystemInterface#FILESYSTEM_URI_SCHEME}</b>:&lt;filesystem_uri&gt;<b>#</b>&lt;object_inside&gt;</code>
 * <ul>
 * <li><i>filesystem_uri</i> need be valid uri according to {@link FileSystemFactory#createFileSystem(URI)} requirements</li>
 * <li><i>object_inside</i> need be valid file or directory name inside the file system</li>
 * </ul>
 * <p>When a file name is typed in <i>object_inside</i>, you can read or write it's content. When a directory name is typed in <i>object_inside</i>,
 * you can only read it's content. The directory 'content' is a list of item single names inside it. Every name in the list is terminated with '\n' char.</p>   
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface
 * @see chav1961.purelib.fsys JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public class FileSystemURLStreamHandler extends URLStreamHandler {
	private final ClassLoader	loader;

	/**
	 * <p>Create class instance</p>
	 */
	public FileSystemURLStreamHandler() {
		this(FileSystemURLStreamHandler.class.getClassLoader());
	}
	
	/**
	 * <p>Create class instance</p>
	 * @param loader loader to use in the {@link FileSystemFactory#createFileSystem(URI, ClassLoader)} call
	 */
	public FileSystemURLStreamHandler(final ClassLoader loader) {
		if (loader == null) {
			throw new NullPointerException("Class loader can't be null"); 
		}
		else {
			this.loader = loader;
		}
	}	

	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		if (url.getProtocol().equalsIgnoreCase(FileSystemInterface.FILESYSTEM_URI_SCHEME)) {
			try{final URI					uri = url.toURI();				
				
				return new FileSystemURLConnection(url,uri,loader,uri.getFragment());
			} catch (URISyntaxException e) {
				throw new IOException("URL ["+url+"] - syntax error: "+e.getLocalizedMessage());
			}
		}
		else {
			throw new IOException("URL ["+url+"]: scheme ["+url.getProtocol()+"] is not supported by this stream handler, use ["+FileSystemInterface.FILESYSTEM_URI_SCHEME+"] only!");
		}
	}

	private static class FileSystemURLConnection extends URLConnection {
		private final URI			fileSystemUri;
		private final ClassLoader	loader;
		private final String		path;
		private FileSystemInterface	fsi = null;
		private boolean				closed = false;
		
		protected FileSystemURLConnection(final URL url, final URI fileSystemUri, final ClassLoader loader, final String path) throws IOException {
			super(url);
			this.fileSystemUri = fileSystemUri;
			this.loader = loader;
			this.path = path;
		}

		@Override
		public void connect() throws IOException {
			if (!getDoInput() && !getDoOutput()) {
				throw new IOException("Neither setDoInput(), nor setDoOutput() was required on the connection. Call one of these method before");
			}
			else if (getDoOutput()) {
				if (path.endsWith("/")) {
					throw new IOException("Filesystem path ["+path+"] points to the directory. You can't manage directory by the URL meshanism");
				}
				else {
					final FileSystemInterface 	temp = FileSystemFactory.createFileSystem(fileSystemUri,loader).open(path);
					
					try{if (temp.exists()) {
							if (temp.isDirectory()) {
								throw new IOException("Filesystem path ["+path+"] points to the existent directory, not a file!");
							}
							else if (temp.canWrite()) {
								throw new IOException("Filesystem path ["+path+"] points to read-only file!");
							}
						}
						else {
							temp.create();
						}
						fsi = temp;
						closed = false;
					} catch (IOException exc) {
						temp.close();
						throw exc;
					}
				}
			}
			else {
				final FileSystemInterface 	temp = FileSystemFactory.createFileSystem(fileSystemUri,loader).open(path);
				
				try{if (!temp.exists()) {
						throw new IOException("Filesystem path ["+path+"] is not exists");
					}
					else if (!temp.canRead()) {
						throw new IOException("Filesystem path ["+path+"] exists, but is not accessible for you");
					}
					fsi = temp;
					closed = false;
				} catch (IOException exc) {
					temp.close();
					throw exc;
				}
			}
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			if (fsi == null) {
				connect();
//				throw new IllegalStateException("Attempt to get input stream before calling connect() method. Call connect() firstly"); 
			}
			if (closed) {
				throw new IllegalStateException("This method can be called exactly once. Reconnect to data source!"); 
			}
			else if (getDoOutput()) {
				closed = true;
				return new ByteArrayInputStream(new byte[0]);
			}
			else if (fsi.isFile()) {
				closed = true;
				return new DelegatedInputStream(fsi.read(),fsi);
			}
			else {
				final StringBuilder	sb = new StringBuilder();
				
				for (String item : fsi.list()) {
					item = item.replace('\\','/');
					sb.append(item.substring(item.lastIndexOf('/')+1)).append('\n');
				}
				fsi.close();
				closed = true;
				return new ByteArrayInputStream(sb.toString().getBytes()); 
			}
		}
		
		@Override
		public OutputStream getOutputStream() throws IOException {
			if (fsi == null) {
				throw new IllegalStateException("Attempt to get input stream before calling connect() method. Call connect() firstly"); 
			}
			else if (closed) {
				throw new IllegalStateException("This method can be called exactly once. Reconnect to data source!"); 
			}
			else if (!getDoOutput()) {
				throw new IOException("Attempt to get output stream without calling setDoOutput() before."); 
			}
			else if (!getDoInput()) {
				return new DelegatedOutputStream(fsi.write(),fsi);
			}
			else {
				return fsi.write();
			}
        }
	}
	
	private static class DelegatedInputStream extends InputStream {
		private final InputStream			delegate;
		private final FileSystemInterface	fsi;
		
		DelegatedInputStream(final InputStream delegate, final FileSystemInterface fsi) {
			this.delegate = delegate;
			this.fsi = fsi;
		}

		@Override
		public int read() throws IOException {
			return delegate.read();
		}

		@Override
		public int read(byte b[], int off, int len) throws IOException {
			return delegate.read(b, off, len);
		}
		
		@Override
		public long skip(long n) throws IOException {
			return delegate.skip(n);
		}
		
		@Override
		public void close() throws IOException {
			delegate.close();
			fsi.close();
		}
	}

	private static class DelegatedOutputStream extends OutputStream {
		private final OutputStream			delegate;
		private final FileSystemInterface	fsi;
		
		DelegatedOutputStream(final OutputStream delegate, final FileSystemInterface fsi) {
			this.delegate = delegate;
			this.fsi = fsi;
		}
		
		@Override
		public void write(int b) throws IOException {
			delegate.write(b);
		}

		@Override
		public void write(byte b[], int off, int len) throws IOException {
			delegate.write(b,off,len);
		}

		@Override
		public void flush() throws IOException {
			delegate.flush();
		}

		@Override
		public void close() throws IOException {
			delegate.close();
			fsi.close();
		}
	}
}
