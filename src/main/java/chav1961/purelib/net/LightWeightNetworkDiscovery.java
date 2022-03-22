package chav1961.purelib.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.net.interfaces.DiscoveryListener;
import chav1961.purelib.net.interfaces.MediaAdapter;
import chav1961.purelib.net.interfaces.MediaDescriptor;
import chav1961.purelib.net.interfaces.MediaItemDescriptor;

public abstract class LightWeightNetworkDiscovery<Broadcast extends Serializable, Query extends Serializable> extends AbstractDiscovery<Broadcast, Query> {
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
	
	public LightWeightNetworkDiscovery(final int discoveryPortNumber, final int maxRecordSize, final PortBroadcastGenerator generator) throws IOException {
		this(discoveryPortNumber, maxRecordSize, generator, false);
	}
	
	public LightWeightNetworkDiscovery(final int discoveryPortNumber, final int maxRecordSize, final PortBroadcastGenerator generator, final boolean emulateBroadcast) throws IOException {
		super(createMediaAdapter(discoveryPortNumber, DEFAULT_DISCOVERY_PERIOD, emulateBroadcast)
			, createMediaDescriptor(generator)
			, maxRecordSize, DEFAULT_DISCOVERY_PERIOD);
		
		if (generator == null) {
			throw new NullPointerException("Port broadcast generator can't be null");
		}
		else {
		}
	}

	protected abstract Broadcast getBroadcastInfo();
	protected abstract Query getQueryInfo();

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

	private static MediaAdapter createMediaAdapter(final int discoveryPort, final int timeout, final boolean emulateBroadcast) throws IOException {
		return new MediaAdapterImpl(discoveryPort, timeout, emulateBroadcast);
	}
	
	private static MediaDescriptor createMediaDescriptor(final PortBroadcastGenerator generator) {
		return new MediaDescritorImpl(generator);
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
	
	static class MediaItemDescriptorImpl implements MediaItemDescriptor {
		private static final long serialVersionUID = -5341870551839274512L;
		
		private final InetAddress	addr;
		private final int			port;
		private final int			timeout;
		
		MediaItemDescriptorImpl(final InetAddress addr, final int port, final int timeout) {
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
	
	static class MediaDescritorImpl implements MediaDescriptor {
		private final PortBroadcastGenerator	generator;
		private final List<MediaItemDescriptor>	items = new ArrayList<>();
		
		MediaDescritorImpl(final PortBroadcastGenerator generator) {

			this.generator = generator;
			
			for (InetAddressDescriptor item : generator.enumPorts()) {
				items.add(new MediaItemDescriptorImpl(item.address, item.port, item.timeout));
			}
		}
		
		@Override
		public Iterable<MediaItemDescriptor> forAllItems() {
			return items;
		}
	}
	
	//https://www.baeldung.com/java-broadcast-multicast
	static class MediaAdapterImpl implements MediaAdapter, Closeable {
		private final DatagramSocket		broadcastSocket;
		private final DatagramSocket		socket;
		private final int 					discoveryPort;
		private final int 					discoveryTimeout;
		private final boolean				emulateBroadcast;
		private final MediaItemDescriptor	desc;
		
		MediaAdapterImpl(final int discoveryPort, final int discoveryTimeout, final boolean emulateBroadcast) throws SocketException {
			if (discoveryPort <= 0 || discoveryPort > Short.MAX_VALUE) {
				throw new IllegalArgumentException("Discovery port ["+discoveryPort+"] out of range 1.."+Short.MAX_VALUE);
			}
			else if (discoveryTimeout <= 0) {
				throw new IllegalArgumentException("Discovery timeout ["+discoveryTimeout+"] must be positive");
			}
			else {
				this.discoveryPort = discoveryPort;
				this.discoveryTimeout = discoveryTimeout;
				this.emulateBroadcast = emulateBroadcast;
				if (emulateBroadcast) {
					this.broadcastSocket = null;
				}
				else {
					this.broadcastSocket = new DatagramSocket();
					this.broadcastSocket.setBroadcast(true);
				}
				this.socket = new DatagramSocket(discoveryPort);
				this.desc = new MediaItemDescriptorImpl(socket.getLocalAddress(), discoveryPort, discoveryTimeout);
			}
		}

		@Override
		public MediaItemDescriptor getDescriptor() {
			return desc;
		}

		@Override
		public void sendPackage(final MediaItemDescriptor desc, final byte[] content, final boolean broadcast) throws IOException {
			final DatagramPacket	packet = new DatagramPacket(content, content.length, ((MediaItemDescriptorImpl)desc).getAddress(), ((MediaItemDescriptorImpl)desc).getPort());
			
			if (broadcast && !emulateBroadcast) {
				broadcastSocket.send(packet);
			}
			else {
				socket.send(packet);
			}
		}

		@Override
		public MediaItemDescriptor receivePackage(MediaItemDescriptor desc, byte[] content) throws IOException {
			final DatagramPacket	packet = new DatagramPacket(content, content.length);
			
			socket.receive(packet);
			return new MediaItemDescriptorImpl(packet.getAddress(), packet.getPort(), DEFAULT_DISCOVERY_PERIOD);
		}

		@Override
		public void close() throws IOException {
			socket.close();
			if (!emulateBroadcast) {
				broadcastSocket.close();
			}
		}
	}
}
