package chav1961.purelib.basic.interfaces;

import chav1961.purelib.basic.SimpleTimerTask;

/**
 * <p>This interface describes maintenable entity. It's implementation can be maintened periodically by calling {@linkplain #maintenance(Object)} method</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @param <T> maintenable content to process
 * @see SimpleTimerTask
 */
@FunctionalInterface
public interface Maintenable<T> {
	/**
	 * <p>Execute maintenance call.</p>
	 * @param content content to pass to maintenance
	 */
	void maintenance(T content);

	/**
	 * <p>Get maintenance period in milliseconds.</p>
	 * @return maintenance period
	 */
	default int getMaintenancePeriod() {
		return 1000;
	}
}
