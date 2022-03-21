package chav1961.purelib.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import chav1961.purelib.basic.SimpleTimerTask;
import chav1961.purelib.basic.interfaces.Maintenable;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;
import chav1961.purelib.net.interfaces.DiscoveryListener;
import chav1961.purelib.net.interfaces.MediaAdapter;
import chav1961.purelib.net.interfaces.MediaDescriptor;
import chav1961.purelib.net.interfaces.MediaItemDescriptor;


//https://www.baeldung.com/java-broadcast-multicast

public abstract class LightWeightDiscovery<Broadcast extends Serializable, Query extends Serializable> extends AbstractDiscovery<Broadcast, Query> {
	public static final int		DEFAULT_RECORD_SIZE = 1024;
	public static final int		DEFAULT_DISCOVERY_PERIOD = 30;
	
	@FunctionalInterface
	public interface PortBroadcastGenerator {
		Iterable<InetAddressDescriptor> enumPorts();
		
		public static PortBroadcastGenerator of(final InetAddress addr, final int port, final int timeout) {
			if (addr == null) {
				throw new NullPointerException("Address can't be null"); 
			}
			else if (port < 0 || port >= Short.MAX_VALUE) {
				throw new IllegalArgumentException("Port number ["+port+"] out of range 0.."+Short.MAX_VALUE); 
			}
			else if (timeout <= 0) {
				throw new IllegalArgumentException("Timeout ["+timeout+"] must be positive"); 
			}
			else {
				return new PortBroadcastGenerator() {
					@Override
					public Iterable<InetAddressDescriptor> enumPorts() {
						return new Iterable<InetAddressDescriptor>() {
							@Override
							public Iterator<InetAddressDescriptor> iterator() {
								return new Iterator<InetAddressDescriptor>() {
									boolean readed = false;
									
									@Override
									public boolean hasNext() {
										return !readed;
									}
	
									@Override
									public InetAddressDescriptor next() {
										readed = true;
										return new InetAddressDescriptor(addr, port, timeout);
									}
								};
							}
						};
					}
				}; 
			}
		}
	}

	private final LightWeightListenerList<DiscoveryListener>	listeners = new LightWeightListenerList<>(DiscoveryListener.class); 
	private final DatagramSocket			broadcastSocket;
	private final DatagramSocket			discoverySocket;
	private final int						discoveryPort;
	private final PortBroadcastGenerator	generator;
	private final boolean					emulateBroadcast;
	
	public LightWeightDiscovery(final int discoveryPortNumber, final int maxRecordSize, final PortBroadcastGenerator generator) throws IOException {
		this(discoveryPortNumber, maxRecordSize, generator, false);
	}
	
	public LightWeightDiscovery(final int discoveryPortNumber, final int maxRecordSize, final PortBroadcastGenerator generator, final boolean emulateBroadcast) throws IOException {
		super(createMediaAdapter(), createMediaDescriptor(), maxRecordSize, DEFAULT_DISCOVERY_PERIOD);
		if (generator == null) {
			throw new NullPointerException("Port broadcast generator can't be null");
		}
		else {
			this.discoveryPort = discoveryPortNumber;
			this.generator = generator;
			this.emulateBroadcast = emulateBroadcast;
			if (emulateBroadcast) {
				this.broadcastSocket = null;
			}
			else {
				this.broadcastSocket = new DatagramSocket();
				this.broadcastSocket.setBroadcast(true);
			}
			this.discoverySocket = new DatagramSocket(discoveryPortNumber);
		}
	}

	protected abstract Broadcast getBroadcastInfo();
	protected abstract Query getQueryInfo();

	@Override
	public void close() throws IOException {
		if (isStarted()) {
			stop();
		}

		discoverySocket.close();
		if (!emulateBroadcast) {
			broadcastSocket.close();
		}
	}

	protected void receiveRecord() throws IOException {
		final DatagramPacket		pack = new DatagramPacket(buffer, buffer.length);
		
		System.err.println("Before:");
		discoverySocket.receive(pack);
		System.err.println("After:");
		
		final DiscoveryRecord<?>	rec = deserialize(buffer);
		
		if (rec.type.needCheckRandom()) {
			final long	currentTime = System.currentTimeMillis();
			
			for (int index = cache.size(); index >= 0; index--) {
				final TimeoutCache		tc = cache.get(index);
				
				if (rec.random == tc.random) {
					if (currentTime > tc.timeout) {
						cache.remove(index);
					}
					else {
						processRecord(rec, pack.getAddress(), pack.getPort());
					}
				}
			}
		}
		else {
			processRecord(rec, pack.getAddress(), pack.getPort());
		}
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

	private static MediaAdapter createMediaAdapter() {
		return new MediaAdapter() {
			
			@Override
			public void sendPackage(final MediaItemDescriptor desc, final byte[] content) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public MediaItemDescriptor receivePackage(final MediaItemDescriptor desc, byte[] content) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MediaItemDescriptor getDescriptor() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}
	
	
	private static MediaDescriptor createMediaDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public static class InetAddressDescriptor {
		public InetAddress	address;
		public final int	port;
		public final int	timeout;
		
		public InetAddressDescriptor(final InetAddress address, final int port, final int timeout) {
			if (address == null) {
				throw new NullPointerException("Address can't be null"); 
			}
			else if (port < 0 || port >= Short.MAX_VALUE) {
				throw new IllegalArgumentException("Port number ["+port+"] out of range 0.."+Short.MAX_VALUE); 
			}
			else if (timeout <= 0) {
				throw new IllegalArgumentException("Timeout ["+timeout+"] must be positive"); 
			}
			else {
				this.address = address;
				this.port = port;
				this.timeout = timeout;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((address == null) ? 0 : address.hashCode());
			result = prime * result + port;
			result = prime * result + timeout;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			InetAddressDescriptor other = (InetAddressDescriptor) obj;
			if (address == null) {
				if (other.address != null) return false;
			} else if (!address.equals(other.address)) return false;
			if (port != other.port) return false;
			if (timeout != other.timeout) return false;
			return true;
		}

		@Override
		public String toString() {
			return "InetAddressDescriptor [address=" + address + ", port=" + port + ", timeout=" + timeout + "]";
		}
	}
	
	public static class DiscoveryEvent extends EventObject {
		private static final long serialVersionUID = 5986987465176119261L;

		private final DiscoveryEventType	type;
		private final InetAddress			address;
		private final int					port;
		
		public DiscoveryEvent(final DiscoveryEventType eventType, final InetAddress address, final int port, final Object source) {
			super(source);
			this.type = eventType;
			this.address = address;
			this.port = port;
		}

		public DiscoveryEventType getEventType() {
			return type;
		}
		
		public InetAddress getInetAddress() {
			return address;
		}
		
		public int getPort() {
			return port;
		}

		@Override
		public String toString() {
			return "DiscoveryEvent [type=" + type + ", address=" + address + ", port=" + port + "]";
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
	
	private static class MediaItemDescriptorImpl implements MediaItemDescriptor {
		private final InetAddress	addr;
		private final int			port;
		private final int			timeout;
		
		public MediaItemDescriptorImpl(final InetAddress addr, final int port, final int timeout) {
			this.addr = addr;
			this.port = port;
			this.timeout = timeout;
		}

		public InetAddress getAddress() {
			return addr;
		}
		
		public int getPort() {
			return port;
		}
		
		@Override
		public int getTimeout() {
			return timeout;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((addr == null) ? 0 : addr.hashCode());
			result = prime * result + port;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass())
				return false;
			MediaItemDescriptorImpl other = (MediaItemDescriptorImpl) obj;
			if (addr == null) {
				if (other.addr != null) return false;
			} else if (!addr.equals(other.addr)) return false;
			if (port != other.port) return false;
			return true;
		}

		@Override
		public String toString() {
			return "MediaItemDescriptorImpl [addr=" + addr + ", port=" + port + ", timeout=" + timeout + "]";
		}
	}
	
	private static class MediaDescritorImpl implements MediaDescriptor {

		@Override
		public Iterable<MediaItemDescriptor> forAllItems() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	private static class MediaAdapterImpl implements MediaAdapter {

		@Override
		public MediaItemDescriptor getDescriptor() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void sendPackage(MediaItemDescriptor desc, byte[] content) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public MediaItemDescriptor receivePackage(MediaItemDescriptor desc, byte[] content) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
