package chav1961.purelib.basic.interfaces;

@FunctionalInterface
public interface OnlineBooleanGetter extends OnlineGetter {
	boolean get();

	static OnlineBooleanGetter forValue(final boolean value) {
		return ()->value;
	}
}
