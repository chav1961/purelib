package chav1961.purelib.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;


//https://www.baeldung.com/java-broadcast-multicast

public class LightWeightDiscovery implements Closeable, ExecutionControl {
	public static enum DiscoveryEventType {
		STARTED,
		SUSPENDED,
		RESUMED,
		STOPPED,
		ASK,
		ANSWER,
		PING,
		ACCEPT,
		CLOSE
	}
	
	@FunctionalInterface
	public interface DiscoveryListener {
		void processEvent(DiscoveryEvent event);
	}
	
	@FunctionalInterface
	public interface PortBroadcastGenerator {
		boolean enumeratePorts(int portNumber, int timeout);
	}

	private final LightWeightListenerList<DiscoveryListener>	listeners = new LightWeightListenerList<>(DiscoveryListener.class); 
	private final UUID				uuid;
	private final Set<String>		bandNames; 
	private final int				discoveryPortNumber;
	private final int				tcpPortNumber;
	private final int				maintenanceTime;
	private final DatagramSocket	broadcastSocket = new DatagramSocket();
	private final DatagramSocket	discoverySocket;
	private boolean					started = false;
	private boolean					suspended = false;
	
	public LightWeightDiscovery(final UUID uuid, final Set<String> bandNames, final int discoveryPortNumber, final int tcpPortNumber, final PortBroadcastGenerator generator, final int maintenanceTime) throws IOException {
		if (uuid == null) {
			throw new NullPointerException("UUID can't be null");
		}
		else if (bandNames == null || bandNames.isEmpty()) {
			throw new IllegalArgumentException("Band name can't be null or empty set");
		}
		else if (generator == null) {
			throw new NullPointerException("Port broadcast generator can't be null");
		}
		else {
			this.uuid = uuid;
			this.bandNames = bandNames;
			this.discoveryPortNumber = discoveryPortNumber;
			this.tcpPortNumber = tcpPortNumber;
			this.maintenanceTime = maintenanceTime;
			this.broadcastSocket.setBroadcast(true);
			this.discoverySocket = new DatagramSocket(discoveryPortNumber);
		}
	}

	@Override
	public void start() throws IOException {
		if (isStarted()) {
			throw new IllegalStateException("Discovery already started");
		}
		else {
			started = true;
		}
	}

	@Override
	public void suspend() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Discovery is not started");
		}
		else if (isSuspended()) {
			throw new IllegalStateException("Discovery already suspended");
		}
		else {
			suspended = true;
		}
	}

	@Override
	public void resume() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Discovery is not started");
		}
		else if (!isSuspended()) {
			throw new IllegalStateException("Discovery already resumed");
		}
		else {
			suspended = false;
		}
	}

	@Override
	public void stop() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Discovery already stopped");
		}
		else {
			started = false;
		}
	}

	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public boolean isSuspended() {
		return suspended;
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
	
	public boolean poll() {
		return poll(null, 0);
	}
	
	public boolean refresh() {
		return true;
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		if (isStarted()) {
			stop();
		}
		
		final DiscoveryEvent	de = new DiscoveryEvent(DiscoveryEventType.CLOSE, null, this);
		
		listeners.fireEvent((l)->l.processEvent(de));
		discoverySocket.close();
		broadcastSocket.close();
	}

	protected boolean poll(final InetAddress addr, final int timeout) {
		return true;
	}
	
	
	protected void sendBroadcast(final PortBroadcastGenerator generator) {
		
	}
	
	public static Iterable<BroadcastNetworkInterface> getAvailableNetworkInterfaces() throws SocketException {
		final List<BroadcastNetworkInterface>	result = new ArrayList<>();
		
		for (NetworkInterface netInt : Collections.list(NetworkInterface.getNetworkInterfaces())) {
			if (!netInt.isLoopback() && netInt.isUp()) {
				for (InterfaceAddress interfAddress : netInt.getInterfaceAddresses()) {
					final InetAddress	broadcast = interfAddress.getBroadcast();
					
					result.add(new BroadcastNetworkInterface(broadcast != null, interfAddress.getAddress(), broadcast));
		        }
			}
		}
		return result;
	}

	protected static byte[] serialize(final DiscoveryRecord record) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final ObjectOutputStream	oos = new ObjectOutputStream(baos)) {
			
			oos.writeObject(record);
			oos.flush();
			oos.reset();
			
			return baos.toByteArray();
		}
	}
	
	protected static DiscoveryRecord deserialize(final byte[] content) throws IOException {
		try(final InputStream		is = new ByteArrayInputStream(content);
			final ObjectInputStream	ois = new ObjectInputStream(is)) {
			
			try{return (DiscoveryRecord)ois.readObject();
			} catch (ClassNotFoundException e) {
				throw new IOException(e.getLocalizedMessage(), e); 
			}
		}
	}
	
	public static class DiscoveryEvent extends EventObject {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5986987465176119261L;

		private final DiscoveryEventType	type;
		private final InetAddress			address;
		
		public DiscoveryEvent(final DiscoveryEventType eventType, final InetAddress address, final Object source) {
			super(source);
			this.type = eventType;
			this.address = address;
		}

		public DiscoveryEventType getEventType() {
			return type;
		}
		
		public InetAddress getInetAddress() {
			return address;
		}
	}
	
	public static class BroadcastNetworkInterface {
		public final boolean		supportsBroadcasting;
		public final InetAddress	networkAddress;
		public final InetAddress	networkBroadcastAddress;
		
		public BroadcastNetworkInterface(final boolean supportsBroadcasting, final InetAddress networkAddress, final InetAddress networkBroadcastAddress) {
			this.supportsBroadcasting = supportsBroadcasting;
			this.networkAddress = networkAddress;
			this.networkBroadcastAddress = networkBroadcastAddress;
		}

		@Override
		public String toString() {
			return "BroadcastNetworkInterface [supportsBroadcasting=" + supportsBroadcasting + ", networkAddress=" + networkAddress + ", networkBroadcastAddress=" + networkBroadcastAddress + "]";
		}
	}
	
	protected static class DiscoveryRecord implements Serializable {
		private static final long serialVersionUID = 471593204403273250L;

		public DiscoveryRecord() {
			
		}
	}

}
