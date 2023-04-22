package chav1961.purelib.concurrent.interfaces;

import chav1961.purelib.concurrent.SimpleBitmapResourceDispatcher;
import chav1961.purelib.concurrent.SimpleObjectResourceDispatcher;

/**
 * <p>This interface implements resource lock for {@linkplain SimpleBitmapResourceDispatcher} and {@link SimpleObjectResourceDispatcher} classes.
 * Example to use see appropriative classes description</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @see SimpleBitmapResourceDispatcher
 * @see SimpleObjectResourceDispatcher
 */
public interface ResourceDispatcherLock extends AutoCloseable {
	@Override
	void close() throws InterruptedException;
}
