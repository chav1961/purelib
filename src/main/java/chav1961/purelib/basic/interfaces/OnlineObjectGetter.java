package chav1961.purelib.basic.interfaces;

@FunctionalInterface
public interface OnlineObjectGetter<T> extends OnlineGetter {
	T get();

	static <T> OnlineObjectGetter<T> forValue(final T value) {
		return ()->value;
	}
}