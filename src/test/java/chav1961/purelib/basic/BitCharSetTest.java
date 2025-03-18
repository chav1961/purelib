package chav1961.purelib.basic;


import org.junit.Assert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
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
		} catch (NullPointerException exc) {			
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
		} catch (NullPointerException exc) {			
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

		bcs.addRange('e','g');
		
		Assert.assertArrayEquals(bcs.toArrayPairs(),new char[][] {new char[] {'a'}, new char[] {'e','g'}});
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
	}

	@Test
	public void setOperationsTest() {
		Assert.assertArrayEquals(new BitCharSet('0','1','2').union(new BitCharSet('2','3','4')).toArray(),"01234".toCharArray());
		Assert.assertArrayEquals(new BitCharSet('0','1','2').intersect(new BitCharSet('2','3','4')).toArray(),"2".toCharArray());
		Assert.assertArrayEquals(new BitCharSet('0','1','2').minus(new BitCharSet('2','3','4')).toArray(),"01".toCharArray());
		Assert.assertEquals(new BitCharSet('0','1','2').invert().toArray().length,125);
		
		final BitCharSet	bcs = new BitCharSet();
		
		try{bcs.union(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{bcs.intersect(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{bcs.minus(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}		

	@Test
	public void staticTest() {
		Assert.assertArrayEquals(BitCharSet.buildCharSet((symbol)->{return symbol >= '0' && symbol <= '9';}).toArray(),"0123456789".toCharArray());
		Assert.assertArrayEquals(ExtendedBitCharSet.buildCharSet((symbol)->{return symbol >= '\u1000' && symbol <= '\u1001';}).toArray(),"\u1000\u1001".toCharArray());

		try{BitCharSet.buildCharSet(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{ExtendedBitCharSet.buildCharSet(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
