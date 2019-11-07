package chav1961.purelib.basic.interfaces;

@FunctionalInterface
public interface OnlineCharGetter  extends OnlineGetter {
	char get();

	static OnlineCharGetter forValue(final char value) {
		return ()->value;
	}
}
