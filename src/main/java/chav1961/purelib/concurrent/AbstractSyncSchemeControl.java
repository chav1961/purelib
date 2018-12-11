package chav1961.purelib.concurrent;

import chav1961.purelib.basic.interfaces.LoggerFacade;

class AbstractSyncSchemeControl<S,T> implements SyncSchemeControl {
	private static final int	WINDOW_SIZE = 64;
	
	private final LoggerFacade	logger;
	private final long[]		window = new long[WINDOW_SIZE];
	private ControlState		state = ControlState.INITIAL;
	private int					threadCount = 0, threadLimit = 0;
	private int					windowCursor = 0;	
	private boolean				autoBalanced = false;
	
	protected AbstractSyncSchemeControl(final LoggerFacade logger) {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else {
			this.logger = logger;
		}
	}
	
	
	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		stop();
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void suspend() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ControlState getCurrentState() {
		return state;
	}

	@Override
	public long getControlPerformance() {
		long	result = 0;
		
		for (long item : window) {
			result += item;
		}
		return result;
	}

	@Override
	public int getControlThreads() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getControlThreadsLimit() {
		return threadLimit;
	}

	@Override
	public SyncSchemeControl setControlThreads(final int numberofControlThreads) {
		if (numberofControlThreads < 0) {
			throw new IllegalArgumentException("Number of control threads ["+numberofControlThreads+"] can't be negative");
		}
		else if (getControlThreadsLimit() > 0 && numberofControlThreads > getControlThreadsLimit()) {
			throw new IllegalArgumentException("Number of control threads ["+numberofControlThreads+"] out of range 1.."+getControlThreadsLimit());
		}
		else {
			this.threadCount = numberofControlThreads;
			return this;
		}
	}

	@Override
	public SyncSchemeControl setControlThreadsLimit(final int controlThreadsLimit) {
		if (controlThreadsLimit < 0) {
			throw new IllegalArgumentException("Control thread limit ["+controlThreadsLimit+"] can't be negative");
		}
		else {
			// TODO Auto-generated method stub
			
			return this;
		}
	}

	@Override
	public boolean isAutoBalanced() {
		return autoBalanced;
	}

	@Override
	public SyncSchemeControl setAutoBalanced(final boolean autoBalanced) {
		this.autoBalanced = autoBalanced; 
		return this;
	}

	protected void setState(final ControlState newState) {
		this.state = newState;
	}
	
	protected T process(final S source) {
		return null;
	}

	private T internalProcess(final S source) {
		final long 	startTime = System.nanoTime();
		final T		result = process(source);
		final long	duration = System.nanoTime() - startTime;
		
		window[windowCursor++] = duration;
		if (windowCursor >= window.length) {
			windowCursor = 0;
		}
		return result;
	}
}
