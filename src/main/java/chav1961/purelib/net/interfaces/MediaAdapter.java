package chav1961.purelib.net.interfaces;

public interface MediaAdapter {
	MediaItemDescriptor getDescriptor();
	void sendPackage(MediaItemDescriptor desc, byte[] content);
	MediaItemDescriptor receivePackage(MediaItemDescriptor desc, byte[] content);
}