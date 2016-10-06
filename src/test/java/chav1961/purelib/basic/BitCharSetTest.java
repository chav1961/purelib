package chav1961.purelib.basic;


import org.junit.Assert;
import org.junit.Test;

public class BitCharSetTest {

	@Test
	public void bitCharSetTest() {
		final BitCharSet	bcs = new BitCharSet();
		
		Assert.assertEquals(bcs.size(),0);
		
		try{bcs.add('\uffff');
			Assert.fail("Mandatory exception was not detected (character out of range)");
		} catch (IllegalArgumentException exc) {			
		}
		try{bcs.add((char[])null);
			Assert.fail("Mandatory exception was not detected (null character list)");
		} catch (IllegalArgumentException exc) {			
		}
		try{bcs.addRange((char)10,(char)9);
			Assert.fail("Mandatory exception was not detected (low bound greater than high)");
		} catch (IllegalArgumentException exc) {			
		}
		
		bcs.add('\0');
		Assert.assertEquals(bcs.size(),1);
		bcs.add('\0');
		Assert.assertEquals(bcs.size(),1);
		
		bcs.remove('\0');
		Assert.assertEquals(bcs.size(),0);
		bcs.remove('\0');
		Assert.assertEquals(bcs.size(),0);

		bcs.add((char)63,(char)64);
		Assert.assertEquals(bcs.size(),2);
		bcs.addRange((char)65,(char)127);
		Assert.assertEquals(bcs.size(),65);
		
		bcs.remove((char)63,(char)127);
		Assert.assertEquals(bcs.size(),63);
		bcs.removeRange((char)0,(char)127);
		Assert.assertEquals(bcs.size(),0);

		try{bcs.remove('\uffff');
			Assert.fail("Mandatory exception was not detected (character out of range)");
		} catch (IllegalArgumentException exc) {			
		}
		try{bcs.remove((char[])null);
			Assert.fail("Mandatory exception was not detected (null character list)");
		} catch (IllegalArgumentException exc) {			
		}
		try{bcs.removeRange((char)10,(char)9);
			Assert.fail("Mandatory exception was not detected (low bound greater than high)");
		} catch (IllegalArgumentException exc) {			
		}
		
		bcs.add('a');
		Assert.assertTrue(bcs.contains('a'));
		Assert.assertFalse(bcs.contains('A'));
		Assert.assertFalse(bcs.contains('\uFFFF'));
		
		Assert.assertArrayEquals(bcs.toArray(),new char[]{'a'});
		
		System.err.println(bcs);
	}

	@Test
	public void extendedBitCharSetTest() {
		final BitCharSet	bcs = new ExtendedBitCharSet('a');
		
		Assert.assertEquals(bcs.size(),1);
		Assert.assertTrue(bcs.contains('a'));
		Assert.assertFalse(bcs.contains('\uFFFF'));
		
		bcs.remove('\uFFFF');
		Assert.assertEquals(bcs.size(),1);
		
		bcs.add('\uFFFF');
		Assert.assertEquals(bcs.size(),2);
		Assert.assertTrue(bcs.contains('\uFFFF'));
		
		bcs.remove('\uFFFF');
		Assert.assertEquals(bcs.size(),1);
		
		System.err.println(bcs);		
	}
}
