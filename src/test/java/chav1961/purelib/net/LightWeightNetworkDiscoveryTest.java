package chav1961.purelib.net;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.net.LightWeightNetworkDiscovery.MediaAdapterImpl;
import chav1961.purelib.net.LightWeightNetworkDiscovery.MediaDescritorImpl;
import chav1961.purelib.net.LightWeightNetworkDiscovery.MediaItemDescriptorImpl;
import chav1961.purelib.net.LightWeightNetworkDiscovery.PortBroadcastGenerator;
import chav1961.purelib.net.interfaces.DiscoveryEventType;
import chav1961.purelib.net.interfaces.DiscoveryListener;
import chav1961.purelib.net.interfaces.MediaItemDescriptor;

public class LightWeightNetworkDiscoveryTest {
	@Test
	public void basicTest() throws IOException {
		final MediaItemDescriptorImpl	mdii = new MediaItemDescriptorImpl(InetAddress.getLocalHost(), 20000, 30);
		
		Assert.assertEquals(InetAddress.getLocalHost(), mdii.getAddress());
		Assert.assertEquals(20000, mdii.getPort());
		Assert.assertEquals(30, mdii.getTimeout());
		
		final MediaDescritorImpl		mdi = new MediaDescritorImpl(PortBroadcastGenerator.of(InetAddress.getLocalHost(), 20000, 30));
		
		for (MediaItemDescriptor item :mdi.forAllItems()) {
			Assert.assertEquals(mdii, item);
		}
	}
	
	@Test
	public void adaptersTest() throws IOException, InterruptedException {
		try(final MediaAdapterImpl	mai1 = new MediaAdapterImpl(20000, 30, true);
			final MediaAdapterImpl	mai2 = new MediaAdapterImpl(20001, 30, true)) {
			final MediaItemDescriptor[]	result = new MediaItemDescriptor[1];

			final Thread	t = new Thread(()->{
										try{final byte[]	content = new byte[3];
											final MediaItemDescriptor	mid = mai2.receivePackage(null, content); 
											
											Assert.assertArrayEquals(new byte[] {1,2,3}, content);
											result[0] = mid; 
										} catch (IOException e) {
										}
									});
			t.setDaemon(true);
			t.start();
			Thread.sleep(200);
			mai1.sendPackage(new MediaItemDescriptorImpl(InetAddress.getLocalHost(), 20001, 30), new byte[] {1,2,3}, false);
			Thread.sleep(200);
			Assert.assertEquals(new MediaItemDescriptorImpl(InetAddress.getLocalHost(), 20000, 30), result[0]);
		}
	}

	@Test
	public void complexTest() throws IOException, InterruptedException {
		final List<DiscoveryEvent>			list1 = new ArrayList<>();
		final List<DiscoveryEvent>			list2 = new ArrayList<>();
		final PortBroadcastGenerator		pg1 = PortBroadcastGenerator.of(InetAddress.getLocalHost(), 20002, 30);
		final PortBroadcastGenerator		pg2 = PortBroadcastGenerator.of(InetAddress.getLocalHost(), 20003, 30);
		
		try(final PseudoLightWeightDiscovery	pad1 = new PseudoLightWeightDiscovery(20002, pg1);
			final PseudoLightWeightDiscovery	pad2 = new PseudoLightWeightDiscovery(20003, pg2)) {
			final DiscoveryListener				dl1 = (e)->list1.add(e);
			final DiscoveryListener				dl2 = (e)->list2.add(e);
			
			pad1.addDiscoveryListener(dl1);
			pad2.addDiscoveryListener(dl2);

			pad1.start();
			pad2.start();
			
			Thread.sleep(200);
			
			Assert.assertEquals(2, list1.size());
			Assert.assertEquals(DiscoveryEventType.START, list1.get(0).getEventType());
			Assert.assertEquals("broadcast", list1.get(0).getSource());
			Assert.assertEquals(DiscoveryEventType.STATE, list1.get(1).getEventType());
			Assert.assertEquals("available=true,suspended=false", list1.get(1).getSource());
			
		}
	}
}


class PseudoLightWeightDiscovery extends LightWeightNetworkDiscovery<String,String> {
	public PseudoLightWeightDiscovery(final int discoveryPortNumber, final PortBroadcastGenerator generator) throws IOException {
		super(discoveryPortNumber, AbstractDiscovery.DEFAULT_RECORD_SIZE, generator, true);
	}

	@Override
	public void maintenance(Object content) {
	}

	@Override
	protected String getBroadcastInfo() {
		return "broadcast";
	}

	@Override
	protected String getQueryInfo() {
		return "query";
	}
}