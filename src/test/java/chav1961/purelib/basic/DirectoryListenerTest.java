package chav1961.purelib.basic;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;import org.hamcrest.core.IsSame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import chav1961.purelib.basic.DirectoryListener.DirectoryWatchDescriptor;

public class DirectoryListenerTest {
	private final File	parent = new File("./src/test/resources/chav1961/purelib/basic/");
	private final File	current = new File(parent,"dwd");
	
	@Before
	public void prepare() {
		if (current.exists()) {
			Utils.deleteDir(current);
		}
		current.mkdirs();
	}
	
	@Test
	public void watcherTest() throws IOException, InterruptedException {
		final Set<File>	collector = new HashSet<>();
		final File		target = new File(current,"test.jar");
		final File		newDir = new File(current,"newDir");
		
		try(final DirectoryWatchDescriptor	dwd = new DirectoryWatchDescriptor(current)) {
			
			Assert.assertFalse(dwd.maintenance(false, (t,f)->collector.add(f)));

			try{dwd.maintenance(false, null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			Utils.copyStream(new File(parent,"test.jar"), target);
			newDir.mkdirs();

			collector.clear();
			Assert.assertTrue(dwd.maintenance(false, (t,f)->collector.add(f)));
			Assert.assertEquals(2, collector.size());

			collector.clear();
			Assert.assertFalse(dwd.maintenance(false, (t,f)->collector.add(f)));
			Assert.assertEquals(0, collector.size());
			
			newDir.delete();
			target.delete();

			collector.clear();
			Assert.assertTrue(dwd.maintenance(false, (t,f)->collector.add(f)));
			Assert.assertEquals(2, collector.size());

			collector.clear();
			Assert.assertFalse(dwd.maintenance(false, (t,f)->collector.add(f)));
			Assert.assertEquals(0, collector.size());
		}
	}

	
	@Test
	public void lsitenerTest() throws IOException, InterruptedException {
		final Set<File>	collector = new HashSet<>();
		final File		target = new File(current,"test.jar");
		final File		newDir = new File(current,"newDir");
		
		try(final DirectoryListener 	dl = new DirectoryListener((t,f)->collector.add(f), true, true, 100, current)) {
			Assert.assertFalse(dl.isStarted());
			dl.start();
			Assert.assertTrue(dl.isStarted());

			Thread.sleep(300);
			
			collector.clear();
			Utils.copyStream(new File(parent,"test.jar"), target);
			newDir.mkdirs();
			
			Thread.sleep(300);
			Assert.assertEquals(2, collector.size());

			collector.clear();
			newDir.delete();
			target.delete();

			Thread.sleep(300);
			Assert.assertEquals(2, collector.size());
			
			collector.clear();
			Thread.sleep(300);
			Assert.assertEquals(0, collector.size());
		}
		
		try{new DirectoryListener(null, true, true, 100, current);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new DirectoryListener((t,f)->collector.add(f), true, true, 0, current);
			Assert.fail("Mandatory exception was not detected (pop-positive 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new DirectoryListener((t,f)->collector.add(f), true, true, 100, (File[])null);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new DirectoryListener((t,f)->collector.add(f), true, true, 100);
			Assert.fail("Mandatory exception was not detected (empty 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new DirectoryListener((t,f)->collector.add(f), true, true, 100, null);
			Assert.fail("Mandatory exception was not detected (nulls inside 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new DirectoryListener((t,f)->collector.add(f), true, true, 100, target);
			Assert.fail("Mandatory exception was not detected (not a directory inside 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	
	@After
	public void unprepare() {
		if (current.exists()) {
			Utils.deleteDir(current);
		}
	}
	
}
