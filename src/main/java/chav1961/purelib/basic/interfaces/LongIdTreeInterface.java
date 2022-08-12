package chav1961.purelib.basic.interfaces;


public interface LongIdTreeInterface<T> {
	@FunctionalInterface
	public static interface WalkCallback<T> {
		boolean process(long id, T content);
	}
	
	boolean contains(final long id);
	T get(final long id);

	LongIdTreeInterface<T> put(final long id, final T cargo);
	T remove(final long id);
	void clear();

	void walk(final WalkCallback<T> callback);
}
