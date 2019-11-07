package chav1961.purelib.basic.interfaces;

@FunctionalInterface
public interface OnlineDoubleGetter  extends OnlineGetter {
	double get();

	static OnlineDoubleGetter forValue(final double value) {
		return ()->value;
	}
}
