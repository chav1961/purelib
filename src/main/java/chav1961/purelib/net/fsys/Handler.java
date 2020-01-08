package chav1961.purelib.net.fsys;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import chav1961.purelib.fsys.FileSystemURLStreamHandler;

public class Handler extends URLStreamHandler {
	public static final String	PROTOCOL = "fsys";
	
	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		return new FileSystemURLStreamHandler(){
			private URLConnection	conn;
			boolean					wasConnected = false;
			
			public URLConnection openConnection(final URL url) throws IOException {
				return conn = super.openConnection(url);
			};
		}.openConnection(url);
	}
}
