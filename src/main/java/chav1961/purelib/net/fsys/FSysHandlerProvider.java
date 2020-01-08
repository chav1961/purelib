package chav1961.purelib.net.fsys;

import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

public class FSysHandlerProvider extends URLStreamHandlerProvider {
	@Override
	public URLStreamHandler createURLStreamHandler(final String protocol) {
		if (Handler.PROTOCOL.equalsIgnoreCase(protocol)) {
			return new Handler();
		}
		else {
			return null;
		}
	}
}
