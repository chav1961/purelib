package chav1961.purelib.basic;

import java.net.URI;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.ContinueMode;

public abstract class BPlusTree<K extends Comparable<? super K>, V> {
	public static final String	BPLUS_TREE_SCHEMA = "bplustree";
	
	public static class BPlusTreeContentException extends ContentException {
		private static final long serialVersionUID = -5557152301088161210L;

		public BPlusTreeContentException() {
			super();
		}

		public BPlusTreeContentException(String message, Throwable cause) {
			super(message, cause);
		}

		public BPlusTreeContentException(String message) {
			super(message);
		}

		public BPlusTreeContentException(Throwable cause) {
			super(cause);
		}
	}

	@FunctionalInterface
	public interface WalkerCallback<K,V> {
		ContinueMode process(K key, V value) throws BPlusTreeContentException;
	}
	
	private final Class<?>	idClass;
	private final Class<K>	keyClass;
	private final Class<V>	valueClass;
	
	protected BPlusTree(final Class<?> idClass, final Class<K> keyClass, final Class<V> valueClass) throws NullPointerException {
		if (idClass == null) {
			throw new NullPointerException("Id class can't be null");
		}
		else if (keyClass == null) {
			throw new NullPointerException("Key class can't be null");
		}
		else if (valueClass == null) {
			throw new NullPointerException("Value class can't be null");
		}
		else {
			this.idClass = idClass;
			this.keyClass = keyClass;
			this.valueClass = valueClass;
		}
	}

	public abstract V get(final K key) throws BPlusTreeContentException, NullPointerException;
	public abstract V[] get(final K keyFrom, final K keyTo, final boolean nearestFrom, final boolean nearestTo) throws BPlusTreeContentException, NullPointerException;
	public abstract void insert(final K key, final V value) throws BPlusTreeContentException, NullPointerException;	
	public abstract void update(final K key, final V value) throws BPlusTreeContentException, NullPointerException;	
	public abstract V delete(final K key) throws BPlusTreeContentException, NullPointerException;
	public abstract boolean walk(final WalkerCallback<K,V> callback) throws BPlusTreeContentException, NullPointerException;
	public abstract boolean walk(final K keyFrom, final K keyTo, final boolean nearestFrom, final boolean nearestTo, final WalkerCallback<K,V> callback) throws BPlusTreeContentException, NullPointerException;
	
	public Class<?> getIdClass() {
		return idClass;
	}
	
	public Class<K> getKeyClass() {
		return keyClass;
	}

	public Class<V> getValueClass() {
		return valueClass;
	}
	
	public static class Factory {
		public static <K extends Comparable<? super K>, V> BPlusTree<K, V> newInstance(final URI btree) {
			return null;			
		}
	}
}
