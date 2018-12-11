package sun.net.www.protocol.fsys;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import chav1961.purelib.fsys.FileSystemURLStreamHandler;

public class Handler extends URLStreamHandler {
	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		return new FileSystemURLStreamHandler(){
			public URLConnection openConnection(final URL url) throws IOException {
				return super.openConnection(url);
			};
		}.openConnection(url);
	}
}
