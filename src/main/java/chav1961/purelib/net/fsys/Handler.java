package chav1961.purelib.net.fsys;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.FileSystemURLStreamHandler;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This class is handler to support "fsys" protocol URL. Format of URL see {@linkplain FileSystemFactory} class description</p>
 * @see URLStreamHandler   
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.4
 */
public class Handler extends URLStreamHandler {
	public static final String		PROTOCOL = FileSystemInterface.FILESYSTEM_URI_SCHEME;
	
	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		return new FileSystemURLStreamHandler(){
//			private URLConnection	conn;
//			boolean					wasConnected = false;
			
			public URLConnection openConnection(final URL url) throws IOException {
				return /*conn =*/ super.openConnection(url);
			};
		}.openConnection(url);
	}
}
