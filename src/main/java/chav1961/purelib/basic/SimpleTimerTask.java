package chav1961.purelib.basic;

import java.util.Date;
import java.util.TimerTask;

import chav1961.purelib.basic.interfaces.Maintenable;

/**
 * <p>This class is a simple extension of {@linkplain TimerTask} to use it in lambda-styles calls.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @last.update 0.0.7
 */
public class SimpleTimerTask extends TimerTask {
	private final Runnable	runnable;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param runnable lambda-styled periodical task to call for the given timer task.
	 * @throws NullPointerException when runnable reference is null
	 */
	public SimpleTimerTask(final Runnable runnable) throws NullPointerException{
		if (runnable == null) {
			throw new NullPointerException("Runnable can't be null");
		}
		else {
			this.runnable = runnable;
		}
	}
	
	@Override
	public void run() {
		try{runnable.run();
		} catch (Exception exc) {
		}
	}
	
	/**
	 * <p>Create and schedule timer task</p>
	 * @param runnable lambda-styled periodical task to call for the given timer task. Can't be null
	 * @param delay start delay
	 * @return timer task created. Can't be null
	 * @see java.util.Timer#schedule(TimerTask, long)
	 * @since 0.0.5
	 */
	public static SimpleTimerTask start(final Runnable runnable, final long delay) {
		final SimpleTimerTask	stt = new SimpleTimerTask(runnable);
		
		PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(stt, delay);
		return stt;
	}

	/**
	 * <p>Create and schedule timer task</p>
	 * @param runnable lambda-styled periodical task to call for the given timer task. Can't be null
	 * @param time start time
	 * @return timer task created. Can't be null
	 * @see java.util.Timer#schedule(TimerTask, Date)
	 * @since 0.0.5
	 */
	public static SimpleTimerTask start(final Runnable runnable, final Date time) {
		final SimpleTimerTask	stt = new SimpleTimerTask(runnable);
		
		PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(stt, time);
		return stt;
	}

	/**
	 * <p>Create and schedule timer task</p>
	 * @param runnable lambda-styled periodical task to call for the given timer task. Can't be null
	 * @param delay start delay
	 * @param period task period
	 * @return timer task created. Can't be null
	 * @see java.util.Timer#schedule(TimerTask, long, long)
	 * @since 0.0.5
	 */
	public static SimpleTimerTask start(final Runnable runnable, final long delay, final long period) {
		final SimpleTimerTask	stt = new SimpleTimerTask(runnable);
		
		PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(stt, delay, period);
		return stt;
	}
	
	/**
	 * <p>Create and schedule timer task</p>
	 * @param runnable lambda-styled periodical task to call for the given timer task. Can't be null
	 * @param time start time
	 * @param period task period
	 * @return timer task created. Can't be null
	 * @see java.util.Timer#schedule(TimerTask, Date, long)
	 * @since 0.0.5
	 */
	public static SimpleTimerTask start(final Runnable runnable, final Date time, final long period) {
		final SimpleTimerTask	stt = new SimpleTimerTask(runnable);
		
		PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(stt, time, period);
		return stt;
	}

	/**
	 * <p>Create and schedule timer task at fixed rate</p>
	 * @param runnable lambda-styled periodical task to call for the given timer task. Can't be null
	 * @param delay start delay
	 * @param period task period
	 * @return timer task created. Can't be null
	 * @see java.util.Timer#scheduleAtFixedRate(TimerTask, long, long)
	 * @since 0.0.5
	 */
	public static SimpleTimerTask startAtFixedRate(final Runnable runnable, final long delay, final long period) {
		final SimpleTimerTask	stt = new SimpleTimerTask(runnable);
		
		PureLibSettings.COMMON_MAINTENANCE_TIMER.scheduleAtFixedRate(stt, delay, period);
		return stt;
	}
	
	/**
	 * <p>Create and schedule timer task at fixed rate</p>
	 * @param runnable lambda-styled periodical task to call for the given timer task. Can't be null
	 * @param time start time
	 * @param period task period
	 * @return timer task created. Can't be null
	 * @see java.util.Timer#scheduleAtFixedRate(TimerTask, Date, long)
	 * @since 0.0.5
	 */
	public static SimpleTimerTask startAtFixedRate(final Runnable runnable, final Date time, final long period) {
		final SimpleTimerTask	stt = new SimpleTimerTask(runnable);
		
		PureLibSettings.COMMON_MAINTENANCE_TIMER.scheduleAtFixedRate(stt, time, period);
		return stt;
	}
	
	/**
	 * <p>Start maintenable task</p>
	 * @param <T> maintenable content to pass into teh task 
	 * @param maintenable maintenable task to execute. Can't be null
	 * @param content content to pass to maintenable task. Can't be null
	 * @return timer task created. Can't be null
	 * @throws NullPointerException on any parameter is null
	 * @since 0.0.7
	 */
	public static <T> SimpleTimerTask startMaintenance(final Maintenable<T> maintenable, final T content) throws NullPointerException {
		if (maintenable == null) {
			throw new NullPointerException("Maintenable can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			return start(()->maintenable.maintenance(content), maintenable.getMaintenancePeriod(), maintenable.getMaintenancePeriod());
		}
	}
}
