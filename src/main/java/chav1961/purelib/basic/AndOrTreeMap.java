package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;

class AndOrTreeMap<T> extends AndOrTree<T> implements NavigableMap<CharSequence, T>{
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

	private final CharSequenceKeeper	keeper = new CharSequenceKeeper();
	private final boolean[]				forBooleanResult = new boolean[1];
	private final CharSequence[]		forCharSequenceResult = new CharSequence[1];
															
	@Override
	public Comparator<? super CharSequence> comparator() {
		return CSC;
	}

	@Override
	public CharSequence firstKey() {
		forCharSequenceResult[0] = null;
		
		walk((name, len, id, cargo)->{
			try{final CharSequenceKeeper	temp = (CharSequenceKeeper) keeper.clone();
			
				temp.content = name.clone();
				temp.from = 0;
				temp.to = len;
				forCharSequenceResult[0] = temp;
				return false;
			} catch (CloneNotSupportedException e) {
				throw new UnsupportedOperationException(e.getLocalizedMessage(), e); 
			}
		});
		return forCharSequenceResult[0];
	}

	@Override
	public CharSequence lastKey() {
		forCharSequenceResult[0] = null;
		
		walkBack((name, len, id, cargo)->{
			try{final CharSequenceKeeper	temp = (CharSequenceKeeper) keeper.clone();
			
				temp.content = name.clone();
				temp.from = 0;
				temp.to = len;
				forCharSequenceResult[0] = temp;
				return false;
			} catch (CloneNotSupportedException e) {
				throw new UnsupportedOperationException(e.getLocalizedMessage(), e); 
			}
		});
		return forCharSequenceResult[0];
	}

	@Override
	public Set<CharSequence> keySet() {
		final Set<CharSequence>	result = new HashSet<>();
		
		walk((name, len, id, cargo)->{
			try{final CharSequenceKeeper	temp = (CharSequenceKeeper) keeper.clone();
			
				temp.content = name.clone();
				temp.from = 0;
				temp.to = len;
				result.add(temp);
				return true;
			} catch (CloneNotSupportedException e) {
				throw new UnsupportedOperationException(e.getLocalizedMessage(), e); 
			}
		});
		return result;
	}

	@Override
	public Collection<T> values() {
		final List<T>	result = new ArrayList<>();

		walk((name, len, id, cargo)->{
			result.add(cargo);
			return true;
		});
		return result;
	}

	@Override
	public Set<Entry<CharSequence, T>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		return longSize() == 0;
	}

	@Override
	public boolean containsKey(final Object key) {
		if (key instanceof CharSequence) {
			// TODO Auto-generated method stub
			return false;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean containsValue(final Object value) {
		if (value != null) {
			forBooleanResult[0] = false;
			
			walk((name, len, id, cargo)->{
				if (Objects.equals(cargo, value)) {
					forBooleanResult[0] = true;
					return false;
				}
				else {
					return true;
				}
			});
			return forBooleanResult[0];
		}
		else {
			return false;
		}
	}

	@Override
	public T get(final Object key) {
		// TODO Auto-generated method stub
		if (key instanceof CharSequence) {
			return null;
		}
		else {
			return null;
		}
	}

	@Override
	public T put(CharSequence key, T value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T remove(final Object key) {
		// TODO Auto-generated method stub
		if (key instanceof CharSequence) {
			return null;
		}
		else {
			return null;
		}
	}

	@Override
	public void putAll(Map<? extends CharSequence, ? extends T> m) {
		if (m == null) {
			throw new NullPointerException("Map to put content can't be null"); 
		}
		else {
			for (Entry<? extends CharSequence, ? extends T> item : m.entrySet()) {
				put(item.getKey(), item.getValue());
			}
		}
	}

	@Override
	public Entry<CharSequence, T> lowerEntry(CharSequence key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharSequence lowerKey(final CharSequence key) {
		return lowerEntry(key).getKey();
	}

	@Override
	public Entry<CharSequence, T> floorEntry(final CharSequence key) {
		return lowerEntry(key);
	}

	@Override
	public CharSequence floorKey(final CharSequence key) {
		return lowerKey(key);
	}

	@Override
	public Entry<CharSequence, T> ceilingEntry(final CharSequence key) {
		return higherEntry(key);
	}

	@Override
	public CharSequence ceilingKey(CharSequence key) {
		return higherKey(key);
	}

	@Override
	public Entry<CharSequence, T> higherEntry(CharSequence key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharSequence higherKey(final CharSequence key) {
		return higherEntry(key).getKey();
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
	public NavigableMap<CharSequence, T> subMap(CharSequence fromKey, boolean fromInclusive, CharSequence toKey, boolean toInclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableMap<CharSequence, T> headMap(final CharSequence toKey, final boolean inclusive) {
		return subMap(firstKey(), true, lastKey(), inclusive);
	}

	@Override
	public NavigableMap<CharSequence, T> tailMap(final CharSequence fromKey, final boolean inclusive) {
		return subMap(fromKey, inclusive, lastKey(), true);
	}

	@Override
	public SortedMap<CharSequence, T> subMap(final CharSequence fromKey, final CharSequence toKey) {
		return subMap(fromKey, true, toKey, true);
	}

	@Override
	public SortedMap<CharSequence, T> headMap(final CharSequence toKey) {
		return headMap(toKey, true);
	}

	@Override
	public SortedMap<CharSequence, T> tailMap(final CharSequence fromKey) {
		return tailMap(fromKey, true);
	}
	
	@Override
	public int size() {
		return super.size();
	}

	private static class CharSequenceKeeper implements CharSequence, Cloneable {
		private char[]	content;
		private int		from, to;
		
		@Override
		public int length() {
			return to - from;
		}

		@Override
		public char charAt(final int index) {
			return content[index-from];
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			try{final CharSequenceKeeper	result = (CharSequenceKeeper) super.clone();
				
				result.from += start;
				result.to = result.from + end; 
				return result;
			} catch (CloneNotSupportedException e) {
				throw new UnsupportedOperationException(e.getLocalizedMessage(), e);
			}
		}
	
		public Object clone() throws CloneNotSupportedException {
			final Object	result = super.clone();
			
			return result;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(content);
			result = prime * result + Objects.hash(from, to);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			CharSequenceKeeper other = (CharSequenceKeeper) obj;
			return Arrays.equals(content, other.content) && from == other.from && to == other.to;
		}

		@Override
		public String toString() {
			return "CharSequenceKeeper [content=" + Arrays.toString(content) + ", from=" + from + ", to=" + to + "]";
		}
	}
	
	private static class EntryKeeper<T> implements Entry<CharSequence, T> {
		private final long		id;
		
		private EntryKeeper(final long id) {
			this.id = id;
		}

		@Override
		public CharSequence getKey() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public T getValue() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public T setValue(T value) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
