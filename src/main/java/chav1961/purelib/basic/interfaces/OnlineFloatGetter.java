package chav1961.purelib.basic.interfaces;

@FunctionalInterface
public interface OnlineFloatGetter  extends OnlineGetter {
	float get();

	static OnlineFloatGetter forValue(final float value) {
		return ()->value;
	}
}

