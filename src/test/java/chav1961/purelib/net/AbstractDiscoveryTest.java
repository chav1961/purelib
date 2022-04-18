package chav1961.purelib.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;
import org.junit.Assert;

import chav1961.purelib.net.interfaces.DiscoveryEventType;
import chav1961.purelib.net.interfaces.DiscoveryListener;
import chav1961.purelib.net.interfaces.MediaAdapter;
import chav1961.purelib.net.interfaces.MediaDescriptor;
import chav1961.purelib.net.interfaces.MediaItemDescriptor;

public class AbstractDiscoveryTest {
	private final PseudoAbstractMediaDescriptor mediaDesc = new PseudoAbstractMediaDescriptor(new PseudoAbstractMediaItemDescriptor(0), new PseudoAbstractMediaItemDescriptor(1), new PseudoAbstractMediaItemDescriptor(2));

	@Test
	public void connectedLifeCycleTest() throws IOException, InterruptedException {
		final BlockingQueue<QueueItem>[]	queues = new BlockingQueue[]{new ArrayBlockingQueue<QueueItem>(10), new ArrayBlockingQueue<QueueItem>(10), new ArrayBlockingQueue<QueueItem>(10)};
		final List<DiscoveryEvent>			list1 = new ArrayList<>();
		final List<DiscoveryEvent>			list2 = new ArrayList<>();
		
		try(final PseudoAbstractDiscovery	pad1 = new PseudoAbstractDiscovery(0, queues, mediaDesc);
			final PseudoAbstractDiscovery	pad2 = new PseudoAbstractDiscovery(1, queues, mediaDesc)) {
			final DiscoveryListener			dl1 = (e)->list1.add(e);
			final DiscoveryListener			dl2 = (e)->list2.add(e);
			
			
			pad1.addDiscoveryListener(dl1);
			pad2.addDiscoveryListener(dl2);
			
			try{pad1.addDiscoveryListener(null);
				Assert.fail("Mandatory  exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}

			pad1.start();
			Assert.assertEquals(0, list1.size());
			Assert.assertEquals(0, list2.size());
			pad2.start();
			Thread.sleep(100);
			
			Assert.assertEquals(2, list1.size());
			Assert.assertEquals(DiscoveryEventType.START, list1.get(0).getEventType());
			Assert.assertEquals("broadcast 1", list1.get(0).getSource());
			Assert.assertEquals(DiscoveryEventType.STATE, list1.get(1).getEventType());
			Assert.assertEquals("available=true,suspended=false", list1.get(1).getSource());
			
			Assert.assertEquals(2, list2.size());
			Assert.assertEquals(DiscoveryEventType.START, list2.get(0).getEventType());
			Assert.assertEquals("broadcast 0", list2.get(0).getSource());
			Assert.assertEquals(DiscoveryEventType.STATE, list1.get(1).getEventType());
			Assert.assertEquals("available=true,suspended=false", list1.get(1).getSource());
			
			list1.clear();
			list2.clear();
			pad2.suspend();
			Thread.sleep(100);
			
			Assert.assertEquals(1, list1.size());
			Assert.assertEquals(DiscoveryEventType.SUSPENDED, list1.get(0).getEventType());
			Assert.assertEquals("broadcast 1", list1.get(0).getSource());
			
			list1.clear();
			list2.clear();
			pad2.resume();
			Thread.sleep(100);
			
			Assert.assertEquals(1, list1.size());
			Assert.assertEquals(DiscoveryEventType.RESUMED, list1.get(0).getEventType());
			Assert.assertEquals("broadcast 1", list1.get(0).getSource());

			list1.clear();
			list2.clear();
			pad2.maintenance(pad1.getMediaAdapter().getDescriptor());
			Thread.sleep(100);

			Assert.assertEquals(1, list2.size());
			Assert.assertEquals(DiscoveryEventType.INFO, list2.get(0).getEventType());
			Assert.assertEquals("query 0", list2.get(0).getSource());
			
			list1.clear();
			list2.clear();
			pad2.stop();
			Thread.sleep(100);

			Assert.assertEquals(1, list1.size());
			Assert.assertEquals(DiscoveryEventType.STOP, list1.get(0).getEventType());
			Assert.assertEquals("broadcast 1", list1.get(0).getSource());
			
			Assert.assertEquals(0, list2.size());
			
			pad1.removeDiscoveryListener(dl1);
			pad1.removeDiscoveryListener(dl2);			
			try{pad1.removeDiscoveryListener(null);
				Assert.fail("Mandatory  exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
		}
	}
	
	@Test
	public void lifeCycleTest() throws IOException {
		final BlockingQueue<QueueItem>[]	queues = new BlockingQueue[]{new ArrayBlockingQueue<QueueItem>(10), new ArrayBlockingQueue<QueueItem>(10), new ArrayBlockingQueue<QueueItem>(10)};
		
		try(final PseudoAbstractDiscovery	pad = new PseudoAbstractDiscovery(0, queues, mediaDesc)) {
			Assert.assertFalse(pad.isStarted());
			Assert.assertFalse(pad.isSuspended());
			
			try{pad.suspend();
				Assert.fail("Mandatory exception was not detected (not started yet)");
			} catch (IllegalStateException exc) {
			}
			try{pad.resume();
				Assert.fail("Mandatory exception was not detected (not started yet)");
			} catch (IllegalStateException exc) {
			}
			try{pad.stop();
				Assert.fail("Mandatory exception was not detected (not started yet)");
			} catch (IllegalStateException exc) {
			}
			
			pad.start();
			
			try{pad.start();
				Assert.fail("Mandatory exception was not detected (already started)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertTrue(pad.isStarted());
			Assert.assertFalse(pad.isSuspended());

			pad.suspend();

			Assert.assertTrue(pad.isStarted());
			Assert.assertTrue(pad.isSuspended());

			try{pad.suspend();
				Assert.fail("Mandatory exception was not detected (already suspended)");
			} catch (IllegalStateException exc) {
			}
		
			pad.resume();

			Assert.assertTrue(pad.isStarted());
			Assert.assertFalse(pad.isSuspended());

			try{pad.resume();
				Assert.fail("Mandatory exception was not detected (already resumed)");
			} catch (IllegalStateException exc) {
			}
	
			pad.stop();
			Assert.assertFalse(pad.isStarted());
			Assert.assertFalse(pad.isSuspended());

			try{pad.stop();
				Assert.fail("Mandatory exception was not detected (don't started)");
			} catch (IllegalStateException exc) {
			}
		}
	}

}

class PseudoAbstractDiscovery extends AbstractDiscovery<String, String> {
	private final int	id;
	
	public PseudoAbstractDiscovery(final int id, final BlockingQueue<QueueItem>[] queues, final PseudoAbstractMediaDescriptor mediaDesc) throws IOException {
		super(new PseudoAbstractMediaAdapter(id, queues), mediaDesc);
		this.id = id;
	}

	@Override
	public void maintenance(Object content) {
		try{sendPackage(DiscoveryEventType.QUERY_INFO, (MediaItemDescriptor)content, "test "+id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String getBroadcastInfo() {
		return "broadcast "+id;
	}

	@Override
	protected String getQueryInfo(final String request) {
		return "query "+id;
	}
}

class QueueItem {
	public final PseudoAbstractMediaItemDescriptor	desc;
	public final byte[]	content;
	
	public QueueItem(PseudoAbstractMediaItemDescriptor desc, byte[] content) {
		this.desc = desc;
		this.content = content;
	}
}

class PseudoAbstractMediaItemDescriptor implements MediaItemDescriptor {
	private static final long serialVersionUID = 261259700027692725L;
	
	private final int	id;
	
	PseudoAbstractMediaItemDescriptor(final int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	public int getTimeout() {
		return 1;
	}

	@Override
	public String toString() {
		return "PseudoAbstractMediaItemDescriptor [id=" + id + ", getTimeout()=" + getTimeout() + "]";
	}
}

class PseudoAbstractMediaDescriptor implements MediaDescriptor {
	private final PseudoAbstractMediaItemDescriptor[]	desc;
	
	PseudoAbstractMediaDescriptor(final PseudoAbstractMediaItemDescriptor... desc) {
		this.desc = desc;
	}

	@Override
	public Iterable<MediaItemDescriptor> forAllItems() {
		return Arrays.asList(desc);
	}
}

class PseudoAbstractMediaAdapter implements MediaAdapter {
	private final PseudoAbstractMediaItemDescriptor	id;
	private final BlockingQueue<QueueItem>[] 		queues;

	PseudoAbstractMediaAdapter(final int id, final BlockingQueue<QueueItem>[] queues) {
		this.id = new PseudoAbstractMediaItemDescriptor(id);
		this.queues = queues;
	}
	
	@Override
	public MediaItemDescriptor getDescriptor() {
		return id;
	}

	@Override
	public void sendPackage(MediaItemDescriptor desc, byte[] content, boolean broadcast) throws IOException {
		final QueueItem	item = new QueueItem((PseudoAbstractMediaItemDescriptor)getDescriptor(), content);
		
		try {
			if (broadcast) {
				if (((PseudoAbstractMediaItemDescriptor)getDescriptor()).getId() != ((PseudoAbstractMediaItemDescriptor)desc).getId()) {
					queues[((PseudoAbstractMediaItemDescriptor)desc).getId()].put(item);
				}
			}
			else {
				queues[((PseudoAbstractMediaItemDescriptor)desc).getId()].put(item);
			}
		} catch (InterruptedException e) {
			throw new IOException(e); 
		}
	}

	@Override
	public MediaItemDescriptor receivePackage(final MediaItemDescriptor desc, byte[] content) throws IOException {
		try{final QueueItem	item = queues[((PseudoAbstractMediaItemDescriptor)getDescriptor()).getId()].take();

			System.arraycopy(item.content, 0, content, 0, Math.min(item.content.length, content.length));
			return item.desc;
		} catch (InterruptedException e) {
			throw new IOException(e); 
		}
	}
}
