package chav1961.purelib.basic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.interfaces.LongIdTreeInterface;

public class CompactLongIdMap<T> implements LongIdTreeInterface<T> {
	private static final int			INITIAL_SIZE = 4;
	
	private final Class<T>				contentType;
	private final Class<?>[]			levelTypes;
	private final T[][][][][][][][]		content;
	private final byte[][][][][][][][]	tree;
	
	public CompactLongIdMap(final Class<T> contentType) {
		if (contentType == null) {
			throw new NullPointerException("Content type can't be null"); 
		}
		else {
			this.contentType = contentType;
			
			final List<Class<?>>	classList = new ArrayList<>();
			Class<?>				currentClass = contentType;
			
			for (int index = 7; index >= 0; index--) {
				final Object currentItem = Array.newInstance(currentClass, 1);
				
				classList.add(0, currentClass = currentItem.getClass());
			}
			this.levelTypes = classList.toArray(new Class<?>[classList.size()]);
			this.content = (T[][][][][][][][]) Array.newInstance(levelTypes[0], INITIAL_SIZE);
			this.tree = new byte[INITIAL_SIZE][][][][][][][];
		}
	}
	
	@Override
	public boolean contains(long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T get(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LongIdTreeInterface<T> put(long id, T cargo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T remove(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void walk(WalkCallback<T> callback) {
		// TODO Auto-generated method stub
		
	}

	
}
