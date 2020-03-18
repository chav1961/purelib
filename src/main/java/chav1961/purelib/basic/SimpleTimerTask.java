package chav1961.purelib.basic;

import java.util.TimerTask;

/**
 * <p>This class is a simple extension of {@linkplain TimerTask} to use it in lambda-styles calls.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
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
		runnable.run();
	}
}
