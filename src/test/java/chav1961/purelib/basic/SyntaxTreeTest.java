package chav1961.purelib.basic;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface.Walker;
import chav1961.purelib.testing.TestingUtils;

public class SyntaxTreeTest {
	private final int				PERF_AMOUNT = 20;
	private final PrintStream		ps = TestingUtils.err(); 
	private final int[]				amount = new int[1];
	private final Walker<Object>	PRINT = new Walker<Object>(){
												@Override
												public boolean process(char[] name, int len, long id, Object cargo) {
													amount[0]++;
													return true;
												}
											};  
	
	@Tag("OrdinalTestCategory")
	@Test
	public void basicFunctionalityTest() {
		basicFunctionalityTest(new AndOrTree<Object>());
		
		try{new AndOrTree<Object>(-1,1);
			Assert.fail("Mandatory exception was not detected (negative 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new AndOrTree<Object>(1,0);
			Assert.fail("Mandatory exception was not detected (non-positive 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void specialTest() {
		final SyntaxTreeInterface<String>	sti = new AndOrTree<>();
		final long	id1 = sti.placeOrChangeName((CharSequence)"skip20_1", "skip20_1");
		final long	id2 = sti.placeOrChangeName((CharSequence)"skip20_2", "skip20_2");
		final long	id3 = sti.placeOrChangeName((CharSequence)"skip2", "skip2");
		final long	id4 = sti.placeOrChangeName((CharSequence)"skip2", "skip2");
		
		Assert.assertEquals(id3, id4);
	}	
	
	@Tag("OrdinalTestCategory")
	@Test
	public void extendedFunctionalityTest() {
		extendedFunctionalityTest(new AndOrTree<Object>());
	}

	@Tag("PerformanceTestCategory")
	@Test
	public void performanceTest() throws InterruptedException {
		final SyntaxTreeInterface<Object>	tt = new AndOrTree<>();
		final char[][]						data = new char[(1 << PERF_AMOUNT)][];
		
		ps.println("Start preparation...");
		
		for (int index = 0; index < data.length; index++) {
			
			final char[]	element = new char[64];
			
			for (int curs = 0; curs < 63; curs++) {
				element[curs] = (char) (Math.random()*65536);
			}
			data[index] = element;
		}		

		Thread.sleep(1000);		
		System.gc();		
		Thread.sleep(1000);		
		
		ps.println("Start test...");
		
		final long	startTime1 = System.nanoTime();
		final long	freeMem1 = Runtime.getRuntime().freeMemory();
		
		for (char[] item : data) {
			tt.placeName(item,0,63,1,null);
		}
		
		final long	startTime2 = System.nanoTime();
		final long	freeMem2 = Runtime.getRuntime().freeMemory();

		Thread.sleep(1000);		
		System.gc();		
		Thread.sleep(1000);		
		
		final long	startTime2A = System.nanoTime();
		final long	freeMem2A = Runtime.getRuntime().freeMemory();
		
		for (char[] item : data) {
			tt.placeName(item,0,63,1,null);
		}
		
		final long	startTime3 = System.nanoTime();
		final long	freeMem3 = Runtime.getRuntime().freeMemory();

		for (char[] item : data) {
			tt.seekName(item,0,63);
		}

		final long	startTime4 = System.nanoTime();
		final long	freeMem4 = Runtime.getRuntime().freeMemory();
		
		ps.println("Place new: time="+((startTime2-startTime1)/data.length)+", size="+((freeMem1-freeMem2)/(1 << PERF_AMOUNT))+", after GC="+((freeMem1-freeMem2A)/(1 << PERF_AMOUNT)));
		ps.println("Place existent: time="+((startTime3-startTime2A)/data.length)+", size="+((freeMem2A-freeMem3)/(1 << PERF_AMOUNT)));
		ps.println("Seek: time="+((startTime4-startTime3)/data.length)+", size="+((freeMem3-freeMem4)/(1 << PERF_AMOUNT)));
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void andOrTreeSpecificTest() throws IOException {
		final SyntaxTreeInterface<Object>	src = new AndOrTree<>();
		
		src.placeName((CharSequence)"abcde", null);
		src.placeName((CharSequence)"abcde1", null);
		src.placeName((CharSequence)"bcdea", null);
		
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final DataOutputStream	dos = new DataOutputStream(baos)) {
				AndOrTree.rawUpload((AndOrTree<?>)src, dos);
				
				try{AndOrTree.rawUpload((AndOrTree<?>)null, dos);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try{AndOrTree.rawUpload((AndOrTree<?>)src, null);
					Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
				} catch (NullPointerException exc) {
				}
			}
			try(final ByteArrayInputStream			bais = new ByteArrayInputStream(baos.toByteArray());
				final DataInputStream				dis = new DataInputStream(bais)) {
				final SyntaxTreeInterface<Object>	target = AndOrTree.rawDownload(dis);
				
				Assert.assertTrue(target.seekName((CharSequence)"abcde") >= 0);
				Assert.assertTrue(target.seekName((CharSequence)"abcde1") >= 0);
				Assert.assertTrue(target.seekName((CharSequence)"bcdea") >= 0);
				Assert.assertFalse(target.seekName((CharSequence)"unknown") >= 0);
				
				target.placeName((CharSequence)"cega", null);

				Assert.assertTrue(target.seekName((CharSequence)"abcde") >= 0);
				Assert.assertTrue(target.seekName((CharSequence)"abcde1") >= 0);
				Assert.assertTrue(target.seekName((CharSequence)"bcdea") >= 0);
				Assert.assertTrue(target.seekName((CharSequence)"cega") >= 0);
				Assert.assertFalse(target.seekName((CharSequence)"unknown") >= 0);

				try{AndOrTree.rawDownload(dis);
					Assert.fail("Mandatory exception was not detected (illegal input format)");
				} catch (IOException exc) {
				}
			}
		}
	}	
	
	private void basicFunctionalityTest(final SyntaxTreeInterface<Object> tt) {
		Assert.assertEquals(tt.size(),0);
		
		tt.placeName("abcde".toCharArray(),0,5,1,null);	// Only root node using
		tt.placeName("cdefg".toCharArray(),0,5,2,null);
		tt.placeName("bcdef".toCharArray(),0,5,3,null);
		tt.placeName("defgh".toCharArray(),0,5,4,null);
		tt.placeName("efghi".toCharArray(),0,5,5,null);
		tt.placeName("ghijk".toCharArray(),0,5,6,null);
		tt.placeName("fghij".toCharArray(),0,5,7,null);
		tt.placeName("hijlk".toCharArray(),0,5,8,null);
		tt.placeName("ijklm".toCharArray(),0,5,9,null);	// Expand root node

		amount[0] = 0;
		tt.walk(PRINT);
		Assert.assertEquals(amount[0],9);
		
		Assert.assertEquals(tt.seekName("abcde".toCharArray(),0,5),1);	// Existent nodes
		Assert.assertEquals(tt.seekName("cdefg".toCharArray(),0,5),2);
		Assert.assertEquals(tt.seekName("bcdef".toCharArray(),0,5),3);
		Assert.assertEquals(tt.seekName("defgh".toCharArray(),0,5),4);
		Assert.assertEquals(tt.seekName("efghi".toCharArray(),0,5),5);
		Assert.assertEquals(tt.seekName("ghijk".toCharArray(),0,5),6);
		Assert.assertEquals(tt.seekName("fghij".toCharArray(),0,5),7);
		Assert.assertEquals(tt.seekName("hijlk".toCharArray(),0,5),8);
		Assert.assertEquals(tt.seekName("ijklm".toCharArray(),0,5),9);

		Assert.assertEquals(tt.seekName("j".toCharArray(),0,1),-1);		// Missing nodes
		Assert.assertEquals(tt.seekName("bz".toCharArray(),0,2),-2);
		Assert.assertEquals(tt.seekName("bc".toCharArray(),0,2),-3);
		Assert.assertEquals(tt.seekName("bcdefg".toCharArray(),0,6),-6);
		
		tt.placeName("abcdef".toCharArray(),0,6,10,null);	// Expand TermNode
		
		amount[0] = 0;
		tt.walk(PRINT);
		Assert.assertEquals(amount[0],10);
		
		Assert.assertEquals(tt.seekName("abcde".toCharArray(),0,5),1);
		Assert.assertEquals(tt.seekName("abcdef".toCharArray(),0,6),10);
		
		tt.placeName("acdef".toCharArray(),0,5,11,null);	// Cutting And node
		tt.placeName("abc".toCharArray(),0,3,12,null);

		amount[0] = 0;
		tt.walk(PRINT);
		Assert.assertEquals(amount[0],12);
		
		Assert.assertEquals(tt.seekName("abcde".toCharArray(),0,5),1);
		Assert.assertEquals(tt.seekName("acdef".toCharArray(),0,5),11);
		Assert.assertEquals(tt.seekName("abc".toCharArray(),0,3),12);
		
		tt.placeName("abcdeg".toCharArray(),0,6,13,null);	// Splitting And node
		
		amount[0] = 0;
		tt.walk(PRINT);
		Assert.assertEquals(amount[0],13);
		
		Assert.assertEquals(tt.seekName("abcde".toCharArray(),0,5),1);
		Assert.assertEquals(tt.seekName("abcdef".toCharArray(),0,6),10);
		Assert.assertEquals(tt.seekName("abcdeg".toCharArray(),0,6),13);

		Assert.assertEquals(tt.size(),13);
		tt.placeName("abcdeg".toCharArray(),0,6,14,null);	// Duplicate addition
		Assert.assertEquals(tt.size(),13);
		
		amount[0] = 0;
		tt.walk(PRINT);
		Assert.assertEquals(amount[0],13);

		Assert.assertEquals(tt.getName(tt.seekName((CharSequence)"abcde")),"abcde");	// Restore names by ids
		Assert.assertEquals(tt.getName(tt.seekName((CharSequence)"cdefg")),"cdefg");
		Assert.assertEquals(tt.getName(tt.seekName((CharSequence)"bcdef")),"bcdef");
		Assert.assertEquals(tt.getName(tt.seekName((CharSequence)"defgh")),"defgh");
		Assert.assertEquals(tt.getName(tt.seekName((CharSequence)"efghi")),"efghi");
		Assert.assertEquals(tt.getName(tt.seekName((CharSequence)"ghijk")),"ghijk");
		Assert.assertEquals(tt.getName(tt.seekName((CharSequence)"fghij")),"fghij");
		Assert.assertEquals(tt.getName(tt.seekName((CharSequence)"hijlk")),"hijlk");
		Assert.assertEquals(tt.getName(tt.seekName((CharSequence)"ijklm")),"ijklm");
		Assert.assertEquals(tt.getName(tt.seekName((CharSequence)"abcdef")),"abcdef");
		Assert.assertEquals(tt.getName(tt.seekName((CharSequence)"acdef")),"acdef");
		Assert.assertEquals(tt.getName(tt.seekName((CharSequence)"abc")),"abc");
		Assert.assertEquals(tt.getName(tt.seekName((CharSequence)"abcdeg")),"abcdeg");
		
		tt.clear();
		amount[0] = 0;
		tt.walk(PRINT);
		Assert.assertEquals(amount[0],0);
		
		try{tt.placeName(null,0,1,1,null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{tt.placeName(new char[0],0,1,1,null);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{tt.placeName("12345".toCharArray(),10,1,1,null);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{tt.placeName("12345".toCharArray(),0,10,1,null);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{tt.placeName("12345".toCharArray(),2,1,1,null);
			Assert.fail("Mandatory exception was not detected (3-rd argument < 2-nd one)");
		} catch (IllegalArgumentException exc) {
		}
		try{tt.placeName("12345".toCharArray(),0,6,-1,null);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{tt.seekName(null,0,1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{tt.seekName(new char[0],0,1);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{tt.seekName("12345".toCharArray(),10,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{tt.seekName("12345".toCharArray(),0,10);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{tt.seekName("12345".toCharArray(),3,2);
			Assert.fail("Mandatory exception was not detected (3-rd argument < 2-nd one)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{tt.walk(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}		
	}

	private void extendedFunctionalityTest(final SyntaxTreeInterface<Object> tt) {
		Assert.assertEquals(tt.size(),0);
		
		tt.placeName("abcd".toCharArray(),0,4,1,null);
		tt.placeName("abcde".toCharArray(),0,5,2,null);
		tt.placeName("abcdef".toCharArray(),0,6,3,null);
		tt.placeName("abdef".toCharArray(),0,5,4,null);

		Assert.assertEquals(tt.size(),4);
		
		Assert.assertEquals(tt.seekName("abcd".toCharArray(),0,4),1);
		Assert.assertEquals(tt.seekName("abcde".toCharArray(),0,5),2);
		Assert.assertEquals(tt.seekName("abcdef".toCharArray(),0,6),3);
		Assert.assertEquals(tt.seekName("abdef".toCharArray(),0,5),4);
		
		Assert.assertTrue(tt.compareNames(1, 2) < 0);
		Assert.assertTrue(tt.compareNames(2, 3) < 0);
		Assert.assertTrue(tt.compareNames(3, 4) < 0);
		Assert.assertTrue(tt.compareNames(1, 4) < 0);

		Assert.assertTrue(tt.compareNames(4, 3) > 0);
		Assert.assertTrue(tt.compareNames(3, 2) > 0);
		Assert.assertTrue(tt.compareNames(2, 1) > 0);
		Assert.assertTrue(tt.compareNames(4, 1) > 0);
		
		Assert.assertTrue(tt.contains(1));
		Assert.assertFalse(tt.contains(5));
		Assert.assertEquals(tt.getNameLength(1),4);
		Assert.assertEquals(tt.getNameLength(2),5);
		Assert.assertEquals(tt.getNameLength(3),6);
		Assert.assertEquals(tt.getNameLength(100),-1);
		
		Assert.assertEquals(tt.getName(1),"abcd");
		Assert.assertEquals(tt.getName(2),"abcde");
		Assert.assertEquals(tt.getName(3),"abcdef");
		Assert.assertNull(tt.getName(100));
		
		char[]	result = new char[1];
		
		Assert.assertEquals(tt.getName(3,result,0),-6);
		result = new char[6];
		Assert.assertEquals(tt.getName(3,result,0),6);
		result = new char[7];
		Assert.assertEquals(tt.getName(3,result,1),7);
		
		Assert.assertNull(tt.getCargo(1));
		tt.setCargo(1,"test string");
		Assert.assertEquals(tt.getCargo(1),"test string");
		
		try{tt.getNameLength(-1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{tt.getName(-1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{tt.getName(-1,new char[1],0);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}		
		try{tt.getName(1,null,0);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{tt.getName(1,new char[0],0);
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{tt.getName(1,new char[10],20);
			Assert.fail("Mandatory exception was not detected (2-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}		

		try{tt.getCargo(-1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{tt.setCargo(-1,null);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
