package chav1961.purelib.basic.interfaces;

@FunctionalInterface
public interface OnlineLongGetter {
	long get();

	static OnlineLongGetter forValue(final long value) {
		return ()->value;
	}
}
