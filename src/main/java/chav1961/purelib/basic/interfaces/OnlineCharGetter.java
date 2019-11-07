package chav1961.purelib.basic.interfaces;

@FunctionalInterface
public interface OnlineCharGetter {
	char get();

	static OnlineCharGetter forValue(final char value) {
		return ()->value;
	}
}
