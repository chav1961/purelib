package chav1961.purelib.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;
import chav1961.purelib.concurrent.interfaces.RequestProcessor;
import chav1961.purelib.concurrent.interfaces.StagedFuture;

public class ExecutionPipe<Stages extends Enum<?>, F, T> implements ExecutionControl {
	private final Stages[]					values;
	private final Consumer<RequestProcessor<?,?>>[]	processors;
	private final ExecutorService[]			services;
	private final BlockingQueue<SpecialFuture<?,?,?>>	queue = new LinkedTransferQueue<>();
	private volatile Thread					queueThread;
	private volatile boolean				started = false;
	private volatile boolean				suspended = false;
	
	@FunctionalInterface
	public static interface PipeThreadFactory<Stages extends Enum<?>> {
		Thread create(final Stages stage, final Runnable runnable);
	}
	
	@SafeVarargs
	public ExecutionPipe(final Stages stages, final Consumer<RequestProcessor<?,?>>... processors) {
		this(stages,(s,r)->new Thread(r),processors);
	}
	
	@SafeVarargs
	public ExecutionPipe(final Stages stages, final PipeThreadFactory<Stages> threadFactory, final Consumer<RequestProcessor<?,?>>... processors) {
		if (stages == null) {
			throw new NullPointerException("Stages can't be null");
		}
		else if (threadFactory == null) {
			throw new NullPointerException("Thread factory can't be null");
		}
		else if (processors == null || processors.length == 0 || Utils.checkArrayContent4Nulls(processors) >= 0) {
			throw new IllegalArgumentException("Processors list is null, empty or contains nulls inside");
		}
		else if (stages.getClass().getEnumConstants().length != processors.length) {
			throw new IllegalArgumentException("Stages conains ["+stages.getClass().getEnumConstants().length+"] items, but number of processors is ["+processors.length+"]. They must be equals");
		}
		else {
			this.values = (Stages[])stages.getClass().getEnumConstants();
			this.processors = processors;
			this.services = new ExecutorService[processors.length];
			
			for(int index = 0; index < services.length; index++) {
				final int	currentIndex = index;
				
				services[index] = Executors.newCachedThreadPool((r)->threadFactory.create(values[currentIndex], r));
			}
		}
	}
	
	@Override
	public synchronized void start() throws IllegalStateException {
		if (isStarted()) {
			throw new IllegalStateException("Pipe is started already");
		}
		else {
			queueThread = new Thread(()->processQueue());
			queueThread.start();
			started = true;
		}
	}

	@Override
	public synchronized void suspend() throws IllegalStateException, InterruptedException {
		if (!isStarted()) {
			throw new IllegalStateException("Pipe is not started yet or was already stopped ealrier");
		}
		else if (isSuspended()) {
			throw new IllegalStateException("Pipe is suspended already");
		}
		else {
			queueThread.interrupt();
			queueThread.join();
			queueThread = null;
			suspended = true;
		}
	}

	@Override
	public synchronized void resume() throws IllegalStateException {
		if (!isStarted()) {
			throw new IllegalStateException("Pipe is not started yet or was already stopped ealrier");
		}
		else if (!isSuspended()) {
			throw new IllegalStateException("Pipe is not suspended yer");
		}
		else {
			queueThread = new Thread(()->processQueue());
			queueThread.start();
			suspended = false;
		}
	}

	@Override
	public synchronized void stop() throws IllegalStateException, InterruptedException {
		if (!isStarted()) {
			throw new IllegalStateException("Pipe is not started yet or was already stopped ealrier");
		}
		else {
			for(ExecutorService item : services) {
				item.shutdown();
				if (!item.awaitTermination(5, TimeUnit.SECONDS)) {
					item.shutdownNow();
				}
			}
			queueThread.interrupt();
			queueThread.join();
			queueThread = null;
			suspended = false;
			started = false;
		}
	}

	@Override
	public synchronized boolean isStarted() {
		return started;
	}

	@Override
	public synchronized boolean isSuspended() {
		return suspended;
	}

	public StagedFuture<Stages, T> submit(final F data) throws InterruptedException {
		return submit(data, values[0], values[values.length-1]);
	}

	public StagedFuture<Stages, T> submit(final F data, final Stages from, final Stages to) throws InterruptedException {
		if (data == null) {
			throw new NullPointerException("Data to submit can't be null");
		}
		else if (from == null) {
			throw new NullPointerException("From stage can't be null");
		}
		else if (to == null) {
			throw new NullPointerException("To stage can't be null");
		}
		else if (to.ordinal() < from.ordinal()) {
			throw new IllegalArgumentException("To stage ["+to+"] is earlier than from stage ["+from+"]");
		}
		else if (!isSuspended()) {
			final SpecialFuture<Stages, F, T>	sf = new SpecialFuture<Stages, F, T>(from, to, data, (fut)->{
															try{queue.put(fut);
															} catch (InterruptedException e) {
																fut.fail(e);
															}
														});
			
			queue.put(sf);
			return sf;
		}
		else {
			return null;
		}
	}

	private void processQueue() {
		while(!Thread.interrupted()) {
			try{final SpecialFuture<?,?,?>	item = queue.take();
			
				if (!item.isCancelled() && !item.isDone()) {
					services[item.currentStage().ordinal()].submit(()->processors[item.currentStage().ordinal()].accept(item));
				}
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
	private static class SpecialFuture<Stages extends Enum<?>, F, T> extends LightWeightFuture<F, T> implements StagedFuture<Stages,T> {
		private final Stages	to;
		private final Consumer<SpecialFuture<Stages, F, T>>	consumer;
		private volatile Object	temp;
		private volatile Stages	currentStage;
		
		public SpecialFuture(final Stages from, final Stages to, final F data, final Consumer<SpecialFuture<Stages, F, T>> consumer) {
			super(data);
			this.to = to;
			this.consumer = consumer;
			this.currentStage = from;
			this.temp = data;
		}

		@Override
		public F take() {
			return (F)temp;
		}

		@Override
		public void complete(final T result) {
			if (currentStage() == to) {
				super.complete(result);
			}
			else {
				temp = result;
				currentStage = (Stages) currentStage.getClass().getEnumConstants()[currentStage.ordinal() + 1]; 
				consumer.accept(this);
			}
		}

		@Override
		public Stages currentStage() {
			return currentStage;
		}
	}
}
