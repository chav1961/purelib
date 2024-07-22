package chav1961.purelib.basic.subscribable;

import org.junit.Assert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
public class SubscribableTest {
	@Test
	public void basicTest() {
		final Subscribable<String>	subs = new Subscribable<String>(String.class) {
										@Override public void refresh() {}
									};
		final boolean[]				flags = new boolean[1];
		final int[]					count = new int[1];
		
		flags[0] = false;
		subs.fireChange((listener)->{flags[0] = true;});
		Assert.assertFalse(flags[0]);
		
		subs.addListener("test");
		flags[0] = false;
		subs.fireChange((listener)->{
			Assert.assertEquals("test",listener);
			flags[0] = true;
		});
		Assert.assertTrue(flags[0]);
 
		subs.removeListener("test");
		flags[0] = false;
		subs.fireChange((listener)->{flags[0] = true;});
		Assert.assertFalse(flags[0]);
		
		subs.addListener("test1");
		subs.addListener("test2");
		subs.addListener("test3");
		count[0] = 0;
		subs.fireChange((listener)->{
			Assert.assertTrue("test1".equals(listener) || "test2".equals(listener) || "test3".equals(listener));
			count[0]++;
		});
		Assert.assertEquals(3,count[0]);

		subs.removeListener("test2");
		count[0] = 0;
		subs.fireChange((listener)->{
			Assert.assertTrue("test1".equals(listener) || "test3".equals(listener));
			count[0]++;
		});
		Assert.assertEquals(2,count[0]);
		
		subs.removeListener("test3");
		count[0] = 0;
		subs.fireChange((listener)->{
			Assert.assertTrue("test1".equals(listener));
			count[0]++;
		});
		Assert.assertEquals(1,count[0]);
		
		try{new Subscribable<String>(null) {
				@Override public void refresh() {}
			};
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{subs.addListener(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{subs.removeListener(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Test 
	public void booleanTest() {
		final SubscribableBoolean	sb = new SubscribableBoolean(true);
		final boolean[]				flag = new boolean[1];
		
		Assert.assertEquals(false,sb.get());
		Assert.assertEquals(new SubscribableBoolean(),sb);
		Assert.assertEquals(new SubscribableBoolean().toString(),sb.toString());
		Assert.assertEquals(new SubscribableBoolean().hashCode(),sb.hashCode());
		
		flag[0] = false;
		sb.addListener((oldVal,newVal)->{flag[0] = true;});
		sb.set(true);
		Assert.assertTrue(flag[0]);
		Assert.assertEquals(true,sb.get());

		flag[0] = false;
		sb.refresh();
		Assert.assertTrue(flag[0]);
	}

	@Test 
	public void intTest() {
		final SubscribableInt		si = new SubscribableInt(true);
		final boolean[]				flag = new boolean[1];
		
		Assert.assertEquals(0,si.get());
		Assert.assertEquals(new SubscribableInt(),si);
		Assert.assertEquals(new SubscribableInt().toString(),si.toString());
		Assert.assertEquals(new SubscribableInt().hashCode(),si.hashCode());
		
		flag[0] = false;
		si.addListener((oldVal,newVal)->{flag[0] = true;});
		si.set(10);
		Assert.assertTrue(flag[0]);
		Assert.assertEquals(10,si.get());

		flag[0] = false;
		si.refresh();
		Assert.assertTrue(flag[0]);
	}

	@Test 
	public void longTest() {
		final SubscribableLong		sl = new SubscribableLong(true);
		final boolean[]				flag = new boolean[1];
		
		Assert.assertEquals(0,sl.get());
		Assert.assertEquals(new SubscribableLong(),sl);
		Assert.assertEquals(new SubscribableLong().toString(),sl.toString());
		Assert.assertEquals(new SubscribableLong().hashCode(),sl.hashCode());
		
		flag[0] = false;
		sl.addListener((oldVal,newVal)->{flag[0] = true;});
		sl.set(10);
		Assert.assertTrue(flag[0]);
		Assert.assertEquals(10,sl.get());

		flag[0] = false;
		sl.refresh();
		Assert.assertTrue(flag[0]);
	}

	@Test 
	public void floatTest() {
		final SubscribableFloat		sf = new SubscribableFloat(true);
		final boolean[]				flag = new boolean[1];
		
		Assert.assertEquals(0,sf.get(),0.001);
		Assert.assertEquals(new SubscribableFloat(),sf);
		Assert.assertEquals(new SubscribableFloat().toString(),sf.toString());
		Assert.assertEquals(new SubscribableFloat().hashCode(),sf.hashCode());
		
		flag[0] = false;
		sf.addListener((oldVal,newVal)->{flag[0] = true;});
		sf.set(10);
		Assert.assertTrue(flag[0]);
		Assert.assertEquals(10,sf.get(),0.001);

		flag[0] = false;
		sf.refresh();
		Assert.assertTrue(flag[0]);
	}

	@Test 
	public void doubleTest() {
		final SubscribableDouble	sd = new SubscribableDouble(true);
		final boolean[]				flag = new boolean[1];
		
		Assert.assertEquals(0,sd.get(),0.001);
		Assert.assertEquals(new SubscribableDouble(),sd);
		Assert.assertEquals(new SubscribableDouble().toString(),sd.toString());
		Assert.assertEquals(new SubscribableDouble().hashCode(),sd.hashCode());
		
		flag[0] = false;
		sd.addListener((oldVal,newVal)->{flag[0] = true;});
		sd.set(10);
		Assert.assertTrue(flag[0]);
		Assert.assertEquals(10,sd.get(),0.001);

		flag[0] = false;
		sd.refresh();
		Assert.assertTrue(flag[0]); 
	}

	@Test 
	public void objectTest() {
		final SubscribableObject<String>	so = new SubscribableObject<String>(true);
		final boolean[]						flag = new boolean[1];
		
		Assert.assertNull(so.get());
		Assert.assertEquals(new SubscribableObject<String>(),so);
		Assert.assertEquals(new SubscribableObject<String>().toString(),so.toString());
		Assert.assertEquals(new SubscribableObject<String>().hashCode(),so.hashCode());
		
		flag[0] = false;
		so.addListener((oldVal,newVal)->{flag[0] = true;});
		so.set("test");
		Assert.assertTrue(flag[0]);
		Assert.assertEquals("test",so.get());

		flag[0] = false;
		so.refresh();
		Assert.assertTrue(flag[0]);
	}
	
	@Test 
	public void stringTest() {
		final SubscribableString	ss = new SubscribableString(true);
		final boolean[]				flag = new boolean[1];
		
		Assert.assertNull(ss.get());
		Assert.assertEquals(new SubscribableString(),ss);
		Assert.assertEquals(new SubscribableString().toString(),ss.toString());
		Assert.assertEquals(new SubscribableString().hashCode(),ss.hashCode());
		
		flag[0] = false;
		ss.addListener((oldVal,newVal)->{flag[0] = true;});
		ss.set("test");
		Assert.assertTrue(flag[0]);
		Assert.assertEquals("test",ss.get());

		flag[0] = false;
		ss.refresh();
		Assert.assertTrue(flag[0]);
	}
}
