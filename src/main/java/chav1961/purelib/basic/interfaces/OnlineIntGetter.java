package chav1961.purelib.basic.interfaces;

@FunctionalInterface
public interface OnlineIntGetter {
	int get();
	
	static OnlineIntGetter forValue(final int value) {
		return ()->value;
	}
}
