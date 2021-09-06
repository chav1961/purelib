package chav1961.purelib.concurrent;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>This class is a rejectable timer task, that can use lambdas to implement it's body instead of using inheritance.</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.5
 */
public class OptionalTimerTask extends TimerTask {
	private final Runnable		runnable;
	private final AtomicBoolean	cancel = new AtomicBoolean(false);
	
	/**
	 * <p>Constructor of the class</p>
	 * @param runnable runnable to execute on start
	 * @throws NullPointerException when runnable is null
	 */
	public OptionalTimerTask(final Runnable runnable) throws NullPointerException {
		if (runnable == null) {
			throw new NullPointerException("Runnable interface ca't be null"); 
		}
		else {
			this.runnable = runnable;
		}
	}

	/**
	 * <p>Reject task. If the task is already running, does nothing</p>
	 */
	public void reject() {
		cancel.set(true);
	}
	
	@Override
	public void run() {
		try {
			if (!cancel.get()) {
				runnable.run();
			}
		} catch (Exception exc) {
		}
	}
}
