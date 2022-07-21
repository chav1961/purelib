package chav1961.purelib.basic;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;

public class AndOrTreeMap<T> extends AndOrTree<T> implements NavigableMap<CharSequence, T>{
	private static final Comparator<CharSequence>	CSC = (s1,s2)->{
																if (s1 == s2) {
																	return 0;
																}
																else if (s1 == null) {
																	return 1;
																}
																else if (s2 == null) {
																	return -1;
																}
																else {
																	for (int index = 0, maxIndex = Math.min(s1.length(),s2.length()); index < maxIndex; index++) {
																		final int delta = s2.charAt(index) - s1.charAt(index);
																		
																		if (delta != 0) {
																			return delta;
																		}
																	}
																	return s2.length()-s1.length();
																}
															}; 

	@Override
	public Comparator<? super CharSequence> comparator() {
		return CSC;
	}

	@Override
	public CharSequence firstKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharSequence lastKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<CharSequence> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<T> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Entry<CharSequence, T>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T get(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T put(CharSequence key, T value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends CharSequence, ? extends T> m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Entry<CharSequence, T> lowerEntry(CharSequence key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharSequence lowerKey(CharSequence key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry<CharSequence, T> floorEntry(CharSequence key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharSequence floorKey(CharSequence key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry<CharSequence, T> ceilingEntry(CharSequence key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharSequence ceilingKey(CharSequence key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry<CharSequence, T> higherEntry(CharSequence key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharSequence higherKey(CharSequence key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry<CharSequence, T> firstEntry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry<CharSequence, T> lastEntry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry<CharSequence, T> pollFirstEntry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry<CharSequence, T> pollLastEntry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableMap<CharSequence, T> descendingMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<CharSequence> navigableKeySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<CharSequence> descendingKeySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableMap<CharSequence, T> subMap(CharSequence fromKey, boolean fromInclusive, CharSequence toKey,
			boolean toInclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableMap<CharSequence, T> headMap(CharSequence toKey, boolean inclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableMap<CharSequence, T> tailMap(CharSequence fromKey, boolean inclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedMap<CharSequence, T> subMap(CharSequence fromKey, CharSequence toKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedMap<CharSequence, T> headMap(CharSequence toKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedMap<CharSequence, T> tailMap(CharSequence fromKey) {
		// TODO Auto-generated method stub
		return null;
	}
}
