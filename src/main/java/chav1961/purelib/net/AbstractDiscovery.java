package chav1961.purelib.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import chav1961.purelib.basic.SimpleTimerTask;
import chav1961.purelib.basic.interfaces.Maintenable;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;
import chav1961.purelib.net.interfaces.DiscoveryEventType;
import chav1961.purelib.net.interfaces.DiscoveryListener;
import chav1961.purelib.net.interfaces.MediaAdapter;
import chav1961.purelib.net.interfaces.MediaDescriptor;
import chav1961.purelib.net.interfaces.MediaItemDescriptor;

public abstract class AbstractDiscovery <Broadcast extends Serializable, Query extends Serializable> implements Closeable, ExecutionControl, Maintenable<Object> {
	public static final int		DEFAULT_RECORD_SIZE = 1024;
	public static final int		DEFAULT_DISCOVERY_PERIOD = 30 * 1000;
	
	private final LightWeightListenerList<DiscoveryListener>	listeners = new LightWeightListenerList<>(DiscoveryListener.class);
	private final MediaAdapter				adapter;
	private final MediaDescriptor			mediaDesc;
	private final byte[]					buffer;
	private final int						discoveryPeriod;
	private final Vector<TimeoutCache>		cache = new Vector<>();
	private final List<Neighbour>			neighbours = new ArrayList<>();
	private volatile Thread					listener = null;
	private volatile SimpleTimerTask		stt = null;
	private volatile boolean	started = false;
	private volatile boolean	suspended = false;

	public AbstractDiscovery(final MediaAdapter adapter, final MediaDescriptor mediaDesc) throws IOException {
		this(adapter, mediaDesc, DEFAULT_RECORD_SIZE, DEFAULT_DISCOVERY_PERIOD);
	}
	
	public AbstractDiscovery(final MediaAdapter adapter, final MediaDescriptor mediaDesc, final int maxBufferSize, final int discoveryPeriod) throws IOException {
		if (adapter == null) {
			throw new NullPointerException("Media adapter can't be null");
		}
		else {
			this.adapter = adapter;
			this.mediaDesc = mediaDesc;
			this.buffer = new byte[maxBufferSize];
			this.discoveryPeriod = discoveryPeriod;
		}
	}

	protected abstract Broadcast getBroadcastInfo();
	protected abstract Query getQueryInfo();

	@Override
	public synchronized void start() throws IOException {
		if (isStarted()) {
			throw new IllegalStateException("Discovery already started");
		}
		else {
			stt = SimpleTimerTask.startMaintenance(this, this);
			listener = new Thread(()->receiveLoop());
			listener.setDaemon(true);
			listener.start();
			sendBroadcast(DiscoveryEventType.START, mediaDesc);
			started = true;	// order of the assignment is important
		}
	}

	@Override
	public synchronized void suspend() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Discovery is not started");
		}
		else if (isSuspended()) {
			throw new IllegalStateException("Discovery already suspended");
		}
		else {
			suspended = true;	// order of the assignment is important
			sendBroadcast(DiscoveryEventType.SUSPENDED, mediaDesc);
		}
	}

	@Override
	public synchronized void resume() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Discovery is not started");
		}
		else if (!isSuspended()) {
			throw new IllegalStateException("Discovery already resumed");
		}
		else {
			sendBroadcast(DiscoveryEventType.RESUMED, mediaDesc);
			suspended = false;	// order of the assignment is important
		}
	}

	@Override
	public synchronized void stop() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Discovery already stopped");
		}
		else {
			started = false;	// order of the assignment is important
			stt.cancel();
			stt = null;
			listener.interrupt();
			listener = null;
			sendBroadcast(DiscoveryEventType.STOP, mediaDesc);
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
	
	@Override
	public int getMaintenancePeriod() {
		return discoveryPeriod;
	}
	
	public void addDiscoveryListener(final DiscoveryListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to add can't be null"); 
		}
		else {
			listeners.addListener(l);
		}
	}

	public void removeDiscoveryListener(final DiscoveryListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to remove can't be null"); 
		}
		else {
			listeners.removeListener(l);
		}
	}
	
	@Override
	public synchronized void close() throws IOException {
		if (isStarted()) {
			stop();
		}
	}

	public MediaAdapter getMediaAdapter() {
		return adapter;
	}
	
	protected void sendBroadcast(final DiscoveryEventType type, final MediaDescriptor desc) throws IOException {
		final DiscoveryRecord<Broadcast>	record = new DiscoveryRecord<>(type, -1, getBroadcastInfo());
		final long		currentTime = System.currentTimeMillis();
		final byte[]	content = serialize(record);
		
		for (MediaItemDescriptor item : desc.forAllItems()) {
			final TimeoutCache		tc = new TimeoutCache(currentTime + 1000 * item.getTimeout(), record.random);  
	        
			adapter.sendPackage(item, content, true);
			cache.add(tc);
		}
	}

	protected <T extends Serializable> void sendPackage(final DiscoveryEventType type, final MediaItemDescriptor item, T content) throws IOException {
		sendPackage(type, item, Math.random(), content);
	}
	
	protected <T extends Serializable> void sendPackage(final DiscoveryEventType type, final MediaItemDescriptor item, double random, T query) throws IOException {
		final DiscoveryRecord<T>	record = new DiscoveryRecord<>(type, random, query);
		final byte[]				content = serialize(record);
		
		if (type.needCheckTimestamp()) {
			cache.add(new TimeoutCache(System.currentTimeMillis() + 1000 * item.getTimeout(), record.random));
		}
		adapter.sendPackage(item, content, false);
	}
	
	protected void receiveRecord(final MediaItemDescriptor desc) throws IOException {
		final MediaItemDescriptor	rcv = adapter.receivePackage(desc, buffer);
		final DiscoveryRecord<?>	rec = deserialize(buffer);
		
		if (rec.type.needCheckRandom() || rec.type.needCheckRandom()) {
			final long	currentTime = System.currentTimeMillis();
			
			for (int index = cache.size() - 1; index >= 0; index--) {
				final TimeoutCache		tc = cache.get(index);
				
				if (rec.random == tc.random || !rec.type.needCheckRandom()) {
					if (currentTime > tc.timeout) {
						cache.remove(index);
					}
					else {
						processRecord(rec, rcv);
					}
				}
			}
		}
		else {
			processRecord(rec, rcv);
		}
	}
	
	protected void processRecord(final DiscoveryRecord<?> rec, final MediaItemDescriptor desc) throws IOException {
		switch (rec.type) {
			case GET_STATE	:
				sendPackage(DiscoveryEventType.STATE, desc, 0, getStateInfo());
				break;
			case STATE		:
				processState(desc, (Neighbour)rec.info);
				break;
			case SUSPENDED		:
				final DiscoveryEvent	dePaused = new DiscoveryEvent(DiscoveryEventType.SUSPENDED, desc, rec.info);
				
				synchronized (neighbours) {
					for (Neighbour item : neighbours) {
						if (item.desc.equals(desc)) {
							item.paused = false;
							listeners.fireEvent((l)->l.processEvent(dePaused));
							return;
						}
					}
				}
				break;
			case PING		:
				sendPackage(DiscoveryEventType.PONG, desc, 0, null);
				break;
			case PONG		:
				synchronized (neighbours) {
					for (Neighbour item : neighbours) {
						if (item.desc.equals(desc)) {
							item.available = true;
							return;
						}
					}
				}
				break;
			case QUERY_INFO	:
				sendPackage(DiscoveryEventType.INFO, desc, 0, getQueryInfo());
				break;
			case INFO		:
				final DiscoveryEvent	deInfo = new DiscoveryEvent(DiscoveryEventType.INFO, desc, rec.info);
				
				listeners.fireEvent((l)->l.processEvent(deInfo));
				break;
			case RESUMED	:
				final DiscoveryEvent	deResumed = new DiscoveryEvent(DiscoveryEventType.RESUMED, desc, rec.info);
				
				synchronized (neighbours) {
					for (Neighbour item : neighbours) {
						if (item.desc.equals(desc)) {
							item.paused = false;
							listeners.fireEvent((l)->l.processEvent(deResumed));
							return;
						}
					}
				}
				break;
			case START		:
				final DiscoveryEvent	deStart = new DiscoveryEvent(DiscoveryEventType.START, desc, rec.info);
				
				synchronized (neighbours) {
					for (Neighbour item : neighbours) {
						if (item.desc.equals(desc)) {
							item.paused = false;
							return;
						}
					}
					neighbours.add(new Neighbour(desc));
					sendPackage(DiscoveryEventType.STATE, desc, 0, getStateInfo());
				}
				listeners.fireEvent((l)->l.processEvent(deStart));
				break;
			case STOP		:
				final DiscoveryEvent	deStop = new DiscoveryEvent(DiscoveryEventType.STOP, desc, rec.info);
				
				synchronized (neighbours) {
					for (int index = neighbours.size() - 1; index >= 0;index--) {
						final Neighbour	item = neighbours.get(index);
						
						if (item.desc.equals(desc)) {
							neighbours.remove(index);
							listeners.fireEvent((l)->l.processEvent(deStop));
							return;
						}
					}
				}
				break;
			default:
				throw new UnsupportedOperationException("Record type ["+rec.type+"] is not supported yet"); 
		}
	}

	protected Neighbour getStateInfo() {
		return new Neighbour(adapter.getDescriptor(), isStarted(), isSuspended());
	}
	
	protected void processState(final MediaItemDescriptor desc, final Neighbour state) {
		synchronized (neighbours) {
			for (Neighbour item : neighbours) {
				if (item.desc.equals(desc)) {
					final DiscoveryEvent	de = new DiscoveryEvent(DiscoveryEventType.STATE, desc, "available="+state.available+",suspended="+state.paused);
					
					item.paused = state.paused;
					item.available = state.available;
					listeners.fireEvent((l)->l.processEvent(de));
					return;
				}
			}
		}
	}

	private synchronized void stopInternal() {
		if (isStarted()) {
			try{stop();
			} catch (IOException e) {
			}
		}
	}
	
	private void receiveLoop() {
		try{while (!Thread.interrupted() && isStarted()) {
				receiveRecord(adapter.getDescriptor());
			}
		} catch (IOException e) {
			stopInternal();
		}
	}

	private static byte[] serialize(final DiscoveryRecord<?> record) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final ObjectOutputStream	oos = new ObjectOutputStream(baos)) {
			
			oos.writeObject(record);
			oos.flush();
			oos.reset();
			
			return baos.toByteArray();
		}
	}
	
	private static DiscoveryRecord<?> deserialize(final byte[] content) throws IOException {
		try(final InputStream		is = new ByteArrayInputStream(content);
			final ObjectInputStream	ois = new ObjectInputStream(is)) {
			
			try{return (DiscoveryRecord)ois.readObject();
			} catch (ClassNotFoundException e) {
				throw new IOException(e.getLocalizedMessage(), e); 
			}
		}
	}

	protected static class DiscoveryRecord<T extends Serializable> implements Serializable {
		private static final long serialVersionUID = 471593204403273250L;

		private final DiscoveryEventType	type;
		private final double				random;
		private final T						info;
		
		public DiscoveryRecord(final DiscoveryEventType	type, final T info) {
			this(type, Math.random(), info);
		}		
		
		public DiscoveryRecord(final DiscoveryEventType	type, final double random, final T info) {
			this.type = type;
			this.random = random;					
			this.info = info;
		}

		@Override
		public String toString() {
			return "DiscoveryRecord [type=" + type + ", random=" + random + ", info=" + info + "]";
		}
	}

	private static class Neighbour implements Serializable {
		private static final long serialVersionUID = -4614207464016452647L;
		
		private final MediaItemDescriptor	desc;
		private boolean						available;
		private boolean						paused;
		
		public Neighbour(final MediaItemDescriptor desc) {
			this(desc, true, false);
		}
		
		public Neighbour(final MediaItemDescriptor desc, final boolean available, final boolean paused) {
			this.desc = desc;
			this.available = available;
			this.paused = paused;
		}
	}

	private static class TimeoutCache {
		private final long		timeout;
		private final double	random;
		
		public TimeoutCache(long timeout, double random) {
			this.timeout = timeout;
			this.random = random;
		}
	}
}
