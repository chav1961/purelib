package chav1961.purelib.net.interfaces;

import java.io.IOException;

public interface MediaAdapter {
	MediaItemDescriptor getDescriptor();
	void sendPackage(MediaItemDescriptor desc, byte[] content, boolean broadcast) throws IOException;
	MediaItemDescriptor receivePackage(MediaItemDescriptor desc, byte[] content) throws IOException;
}