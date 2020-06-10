package chav1961.purelib.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.exceptions.ContentException;

public abstract class MappedAdamClass<K,V> implements Map<K,V> {
	protected MappedAdamClass() {
	}
	
	protected abstract K[] getKeys();
	protected abstract V[] getValues();
	protected abstract V setValue(int index, V value);	
	
	@Override
	public int size() {
		return getKeys().length;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean containsKey(final Object key) {
		return getKeyIndex(key) >= 0;
	}

	@Override
	public boolean containsValue(final Object value) {
		for (V item : getValues()) {
			if (item.equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public V get(final Object key) {
		final int	index = getKeyIndex(key);
		
		if (index >= 0) {
			return getValues()[index];
		}
		else {
			return null;
		}
	}

	@Override
	public V put(K key, V value) {
		if (containsKey(key)) {
			return setValue(getKeyIndex(key),value);
		}
		else {
			throw new UnsupportedOperationException("This map doesn't support creating new key ["+key+"]. Only "+Arrays.toString(getKeys())+" keys are available");
		}
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException("This map doesn't support removing keys");
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Entry<? extends K, ? extends V> item : m.entrySet()) {
			put(item.getKey(),item.getValue());
		}
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("This map doesn't support removing keys");
	}

	@Override
	public Set<K> keySet() {
		return Set.of(getKeys());
	}

	@Override
	public Collection<V> values() {
		return Arrays.asList(getValues());
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		final Set<Entry<K, V>>	result = new HashSet<>();
		final K[]				keys = getKeys();
		
		for (int index = 0, maxIndex = size(); index < maxIndex; index++) {
			final int	tmp = index;
			
			result.add(new Entry<K,V>() {
				@Override public K getKey() {return keys[tmp];}
				@Override public V getValue() {return get(getKey());}
				@Override public V setValue(V value) {return MappedAdamClass.this.setValue(getKeyIndex(getKey()), value);}
			});
		}
		return result;
	}

	protected int getKeyIndex(Object key) {
		if (key == null) {
			return -1;
		}
		else {
			final K[]	keys = getKeys();
			
			for (int index = 0, maxIndex = keys.length; index < maxIndex; index++) {
				if (key.equals(keys[index])) {
					return index;
				}
			}
			return -1;
		}
	}
	
	protected static void throwNullPointerException(final String field) {
		throw new NullPointerException("Assigning null value for primitive type, field name is ["+field+"]");
	}
}
