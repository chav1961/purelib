package chav1961.purelib.net.self;

import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

public class SelfHandlerProvider extends URLStreamHandlerProvider {
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
