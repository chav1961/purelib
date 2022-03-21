package chav1961.purelib.net;

import java.io.IOException;
import java.net.InetAddress;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.net.LightWeightDiscovery.PortBroadcastGenerator;

public class LightWeightDiscoveryTest {
	@Test
	public void basicTest() throws IOException {
		try(final PseudoLightWeightDiscovery	lwd = new PseudoLightWeightDiscovery(21000, PortBroadcastGenerator.of(InetAddress.getLoopbackAddress(), 21001, 30))) {
			Assert.assertFalse(lwd.isStarted());
			
			lwd.start();
			Assert.assertTrue(lwd.isStarted());

			lwd.stop();
			Assert.assertFalse(lwd.isStarted());
		}
	}

	@Test
	public void connectionTest() throws IOException, InterruptedException {
		try(final PseudoLightWeightDiscovery	lwdFirst = new PseudoLightWeightDiscovery(21000, PortBroadcastGenerator.of(InetAddress.getLoopbackAddress(), 21001, 30));
			final PseudoLightWeightDiscovery	lwdSecond = new PseudoLightWeightDiscovery(21001, PortBroadcastGenerator.of(InetAddress.getLoopbackAddress(), 21000, 30))) {
			lwdFirst.addDiscoveryListener((l)->{
				System.err.println("First="+l);
			});
			lwdSecond.addDiscoveryListener((l)->{
				System.err.println("Second="+l);
			});
			lwdFirst.start();
			lwdSecond.start();
			Thread.sleep(1000);
			lwdSecond.stop();
			lwdFirst.stop();
		}
	}
	
}


class PseudoLightWeightDiscovery extends LightWeightDiscovery<String, String> {
	boolean		maintenanceCalled = false;
	
	public PseudoLightWeightDiscovery(final int discoveryPortNumber, final PortBroadcastGenerator generator) throws IOException {
		super(discoveryPortNumber, LightWeightDiscovery.DEFAULT_RECORD_SIZE, generator, true);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void maintenance(final Object content) {
		maintenanceCalled = true;
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