package chav1961.purelib.basic;

class CancellableJob {
	private static final int	STATE_INITIAL = 0;
	private static final int	STATE_FIRST_PROCESS = 1;
	private static final int	STATE_NEXT_PROCESS = 2;
	
	private final Object				sync = new Object(); 
	private final boolean				firstImmediate;
	private final int					timeout;
	private volatile int				state = STATE_INITIAL;
	private volatile SimpleTimerTask	stt = null;
	
	public CancellableJob(final boolean firstImeediate, final int timeout) {
		if (timeout <= 0) {
			throw new IllegalArgumentException("Timeout must be positive"); 
		}
		else {
			this.firstImmediate = firstImeediate;
			this.timeout = timeout;
		}
	}
	
	public void placeJob(final Runnable job) {
		if (job == null) {
			throw new NullPointerException("Job to execute can't be null");
		}
		else {
			synchronized (sync) {
				switch (state) {
					case STATE_INITIAL			:
						if (firstImmediate) {
							state = STATE_FIRST_PROCESS;
							stt = SimpleTimerTask.start(()->callJob(job), 0);
						}
						else {
							state = STATE_NEXT_PROCESS;
							stt = SimpleTimerTask.start(()->callJob(job), timeout);
						}
						break;
					case STATE_FIRST_PROCESS	:
						stt.cancel();
						state = STATE_NEXT_PROCESS;
						stt = SimpleTimerTask.start(()->callJob(job), timeout);
						break;
					case STATE_NEXT_PROCESS		:
						stt.cancel();
						state = STATE_NEXT_PROCESS;
						stt = SimpleTimerTask.start(()->callJob(job), timeout);
						break;
				}
			}
		}
	}
	
	private void callJob(final Runnable job) {
		try{job.run();
		} finally {
			synchronized (sync) {
				state = STATE_INITIAL;
				stt = null;
			}
		}
	}
}
