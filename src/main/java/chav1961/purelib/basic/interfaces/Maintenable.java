package chav1961.purelib.basic.interfaces;

@FunctionalInterface
public interface Maintenable<T> {
	void maintenance(T content);

	default int getMaintenancePeriod() {
		return 0;
	}
}
