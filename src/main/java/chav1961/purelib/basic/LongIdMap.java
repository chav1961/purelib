package chav1961.purelib.basic;

import java.lang.reflect.Array;
import java.util.Arrays;

import chav1961.purelib.basic.interfaces.LongIdTreeInterface;

/**
 * <p>This class implements a special kind of map, oriented to use with {@linkplain AndOrTree} class. It can be also used to manipulate
 * with the identifiers was generated and used by {@linkplain AndOrTree}. To increase memory usage, long ID values must occupy 
 * contiguous range.</p>
 * 
 * @param <T> any kind of data associated with the tree elements
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @last.update 0.0.8
 */
public class LongIdMap<T> implements LongIdTreeInterface<T> {
	private static final int	RANGE_STEP = 64;
	
	private final Class<T>		contentType;
	private T[][][][]			content;
	private long				maxValue = 0;
	private long				count = 0;
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param contentType content type associated with the longs
	 * @throws NullPointerException if contentType is null
	 */
	@SuppressWarnings("unchecked")
	public LongIdMap(final Class<T> contentType) throws NullPointerException {
		if (contentType == null) {
			throw new NullPointerException("Content type ref can't be null");
		}
		else {
			this.contentType = contentType;
			this.content = (T[][][][]) Array.newInstance(contentType, RANGE_STEP, 0, 0, 0);
		}
	}
	
	/**
	 * <p>Put key/value pait into map</p>
	 * @param id key of the pair
	 * @param cargo value of the pair
	 * @return self
	 * @throws NullPointerException cargo is null
	 */
	@SuppressWarnings("unchecked")
	public LongIdMap<T> put(final long id, final T cargo) throws NullPointerException {
		if (cargo == null) {
			throw new NullPointerException("Cargo to put can't be null");
		}
		else {
			final int	part1 = (int)((id >> 48) & 0xFFFF), part2 = (int)((id >> 32) & 0xFFFF), part3 = (int)((id >> 16) & 0xFFFF), part4 = (int)((id >> 0) & 0xFFFF);
			T[][][][]	temp = content;
	
			if (part1 >= temp.length) {
				content = temp = Arrays.copyOf(temp, nearest2N(part1));
			}
			if (temp[part1] == null) {
				temp[part1] = (T[][][]) Array.newInstance(contentType, nearest2N(part2), 0, 0);
			}
			else if (part2 >= temp[part1].length) {
				temp[part1] = Arrays.copyOf(temp[part1], nearest2N(part2));
			}
			if (temp[part1][part2] == null) {
				temp[part1][part2] = (T[][]) Array.newInstance(contentType, nearest2N(part3), 0);
			}
			else if (part3 >= temp[part1][part2].length) {
				temp[part1][part2] = Arrays.copyOf(temp[part1][part2], nearest2N(part3));
			}
			if (temp[part1][part2][part3] == null) {
				temp[part1][part2][part3] = (T[]) Array.newInstance(contentType, nearest2N(part4));
			}
			else if (part4 >= temp[part1][part2][part3].length) {
				temp[part1][part2][part3] = Arrays.copyOf(temp[part1][part2][part3], nearest2N(part4));
			}
			if (temp[part1][part2][part3][part4] == null) {
				count++;
			}
			temp[part1][part2][part3][part4] = cargo;
			maxValue = Math.max(maxValue, id);
			return this;
		}
	}

	/**
	 * <p>Does id contain in the map</p>
	 * @param id id to test
	 * @return true if contains
	 */
	public boolean contains(final long id) {
		final int		part1 = (int)((id >> 48) & 0xFFFF), part2 = (int)((id >> 32) & 0xFFFF), part3 = (int)((id >> 16) & 0xFFFF), part4 = (int)((id >> 0) & 0xFFFF);
		final T[][][][]	root = content;
		
		if (part1 >= root.length || root[part1] == null) {
			return false;
		}
		else {
			final T[][][]	level1 = root[part1];
			
			if (part2 >= level1.length || level1[part2] == null) {
				return false;
			}
			else {
				final T[][]	level2 = level1[part2];
				
				if (part3 >= level2.length || level2[part3] == null) {
					return false;
				}
				else {
					final T[]	level3 = level2[part3];
					
					if (part4 >= level3.length) {
						return false;
					}
					else {
						return level3[part4] != null; 
					}
				}
			}
		}
	}
	
	/**
	 * <p>Get value by key</p>
	 * @param id key to get value for
	 * @return value associated or null if missing
	 */
	public T get(final long id) {
		final int	part1 = (int)((id >> 48) & 0xFFFF), part2 = (int)((id >> 32) & 0xFFFF), part3 = (int)((id >> 16) & 0xFFFF), part4 = (int)((id >> 0) & 0xFFFF);
		final T[][][][]	root = content;
		
		if (part1 >= root.length || root[part1] == null) {
			return null;
		}
		else {
			final T[][][]	level1 = root[part1];
			
			if (part2 >= level1.length || level1[part2] == null) {
				return null;
			}
			else {
				final T[][]	level2 = level1[part2];
				
				if (part3 >= level2.length || level2[part3] == null) {
					return null;
				}
				else {
					final T[]	level3 = level2[part3];
					
					if (part4 >= level3.length) {
						return null;
					}
					else {
						return level3[part4]; 
					}
				}
			}
		}
	}

	public T remove(final long id) {
		final int	part1 = (int)((id >> 48) & 0xFFFF), part2 = (int)((id >> 32) & 0xFFFF), part3 = (int)((id >> 16) & 0xFFFF), part4 = (int)((id >> 0) & 0xFFFF);
		final T[][][][]	root = content;
		
		if (part1 >= root.length || root[part1] == null) {
			return null;
		}
		else {
			final T[][][]	level1 = root[part1];
			
			if (part2 >= level1.length || level1[part2] == null) {
				return null;
			}
			else {
				final T[][]	level2 = level1[part2];
				
				if (part3 >= level2.length || level2[part3] == null) {
					return null;
				}
				else {
					final T[]	level3 = level2[part3];
					
					if (part4 >= level3.length) {
						return null;
					}
					else {
						final T	result = level3[part4]; 
						
						level3[part4] = null;
						count--;
						return result;
					}
				}
			}
		}
	}
	
	/**
	 * <p>Get size of the map.</p>
	 * @return size of the map.
	 */
	public long size() {
		return count;
	}
	
	/**
	 * <p>Get max ID value stored in the map.</p>
	 * @return max ID value stored.
	 */
	public long maxValue() {
		return maxValue;
	}
	
	/**
	 * <p>Find first free long ID to use.</p>
	 * @return first free ID to use.
	 */
	public long firstFree() {
		for (long index = 0, maxIndex = maxValue(); index < maxIndex; index++) {
			if (!contains(index)) {
				return index;
			}
		}
		return maxValue()+1;
	}

	/**
	 * <p>Clear map content</p>
	 */
	public void clear() {
		for (long index = 0; index < maxValue; index++) {
			remove(index);
		}
		count = 0;
		maxValue = 0;
	}

	/**
	 * <p>Walk map content and process callback on every node</p>
	 * @param callback callback to process. Can't be null.
	 * @throws NullPointerException when callback is null 
	 */
	public void walk(final WalkCallback<T> callback) throws NullPointerException {
		if (callback == null) {
			throw new NullPointerException("Walk callback can't be null"); 
		}
		else {
			for (long id = 0; id <= maxValue(); id++) {
				if (contains(id)) {
					callback.process(id, get(id));
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return "LongIdMap [contentType=" + contentType + ", content=" + Arrays.toString(content) + "]";
	}

	private static int nearest2N(final int source) {
		return Math.min(65536,((source+RANGE_STEP-1)/RANGE_STEP + 1)*RANGE_STEP);
	}
}
