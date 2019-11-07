package chav1961.purelib.basic.interfaces;

@FunctionalInterface
public interface OnlineStringGetter extends OnlineObjectGetter<String>{
	String get();

	static OnlineStringGetter forValue(final String value) {
		return ()->value;
	}
}
