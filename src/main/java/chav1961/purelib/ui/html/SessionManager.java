package chav1961.purelib.ui.html;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class SessionManager<T extends AutoCloseable> implements AutoCloseable {
	private final ConcurrentHashMap<UUID, T>	map = new ConcurrentHashMap<UUID, T>();
	
	public SessionManager(final SubstitutableProperties props, final FileSystemInterface swap) {
		
	}

	public UUID newSessionId(final T sessionContent) {
		final UUID	key = UUID.randomUUID();
		
		map.put(key, sessionContent);
		return key;
	}

	public T getContent(final UUID sessionId) {
		return map.get(sessionId);
	}
	
	@Override
	public void close() throws RuntimeException {
		for (Entry<UUID, T> item : map.entrySet()) {
			try{item.getValue().close();
			} catch (Exception e) {
			}
		}
		map.clear();
	}
}
