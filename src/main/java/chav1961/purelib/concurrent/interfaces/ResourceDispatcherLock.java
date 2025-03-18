package chav1961.purelib.concurrent.interfaces;

import chav1961.purelib.concurrent.SimpleBitmapResourceDispatcher;

/**
 * <p>This interface implements resource lock for {@linkplain SimpleBitmapResourceDispatcher} class.
 * Example to use see appropriative classes description</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @see SimpleBitmapResourceDispatcher
 */
public interface ResourceDispatcherLock extends AutoCloseable {
	@Override
	void close() throws InterruptedException;
}
