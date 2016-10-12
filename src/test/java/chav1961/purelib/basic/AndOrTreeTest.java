package chav1961.purelib.basic;


import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class AndOrTreeTest {
	@Test
	public void charArrayLifeCycleTest() throws IOException {
		final SyntaxTreeInterface<String>	tree = new AndOrTree<String>(1);
		final String						ss = "1234567890";
		final long	nameId = tree.placeName("vassya".toCharArray(),0,"vassya".length(),ss);
		
		Assert.assertEquals(tree.seekName("vassya".toCharArray(),0,"vassya".length()),nameId);	// Seek name
		Assert.assertEquals(tree.seekName("unknown".toCharArray(),0,"unknown".length()),-1);	// Seek soundexed names
		Assert.assertEquals(tree.seekName("vassy".toCharArray(),0,"vassy".length()),-1);
		Assert.assertEquals(tree.seekName("vassya1".toCharArray(),0,"vassya1".length()),-1);
		
		Assert.assertEquals(tree.getNameLength(nameId),6);		// Check name properties
		Assert.assertEquals(tree.getName(nameId),"vassya");
		Assert.assertEquals(tree.getCargo(nameId),ss);

		final long	name2Id = tree.placeName("vassya".toCharArray(),0,"vassya".length(),null);	// Add existent name
		Assert.assertEquals(nameId,name2Id);
		Assert.assertEquals(tree.getCargo(name2Id),ss);			// Null cargo not replaces the old one in the existent node!
		Assert.assertEquals(tree.placeName("vassya".toCharArray(),0,"vassya".length(),ss),nameId);
		Assert.assertEquals(tree.getCargo(name2Id),ss);			// Not null cargo replaces existent cargo!
		
		final long	name3Id = tree.placeName("vassy".toCharArray(),0,"vassy".length(),ss);		// Trunc AND-node
		final long	name4Id = tree.placeName("vassya12".toCharArray(),0,"vassya12".length(),ss);// Expand AND-node
		final long	name5Id = tree.placeName("vanya".toCharArray(),0,"vanya".length(),ss);		// Trunc AND-node
		
		Assert.assertEquals(tree.seekName("vassy".toCharArray(),0,"vassy".length()),name3Id);
		Assert.assertEquals(tree.seekName("vassya12".toCharArray(),0,"vassya12".length()),name4Id);
		Assert.assertEquals(tree.seekName("vanya".toCharArray(),0,"vanya".length()),name5Id);
		
		tree.removeName(nameId);
		Assert.assertEquals(tree.seekName("vassya".toCharArray(),0,"vassya".length()),-1);
		
		Assert.assertEquals(tree.size(),4);
		tree.clear();
		Assert.assertEquals(tree.size(),0);
	}
	
	@Test
	public void performanceTest() throws IOException, InterruptedException {
		int	scale = 20;
		
		System.err.println("Start preparation...");
		
		final char[][]		data = new char[(1 << scale)][];
		
		for (int index = 0; index < data.length; index++) {
			final char[]	element = new char[64];
			
			for (int curs = 0; curs < 63; curs++) {
				element[curs] = (char) (Math.random()*65536);
			}
			data[index] = element;
		}		
		final SyntaxTreeInterface<String>	tree = new AndOrTree<String>(1);

		Thread.sleep(1000);		
		System.gc();		
		Thread.sleep(1000);		
		
		System.err.println("Start test...");
		
		final long	startTime1 = System.nanoTime();
		final long	freeMem1 = Runtime.getRuntime().freeMemory();
		
		for (char[] item : data) {
			tree.placeName(item,0,63,null);
		}
		
		final long	startTime2 = System.nanoTime();
		final long	freeMem2 = Runtime.getRuntime().freeMemory();

		Thread.sleep(1000);		
		System.gc();		
		Thread.sleep(1000);		
		
		final long	startTime2A = System.nanoTime();
		final long	freeMem2A = Runtime.getRuntime().freeMemory();
		
		for (char[] item : data) {
			tree.placeName(item,0,63,null);
		}
		
		final long	startTime3 = System.nanoTime();
		final long	freeMem3 = Runtime.getRuntime().freeMemory();

		for (char[] item : data) {
			tree.seekName(item,0,63);
		}

		final long	startTime4 = System.nanoTime();
		final long	freeMem4 = Runtime.getRuntime().freeMemory();
		
		System.err.println("Place new: time="+((startTime2-startTime1)/data.length)+", size="+((freeMem1-freeMem2)/(1 << scale))+", after GC="+((freeMem1-freeMem2A)/(1 << scale)));
		System.err.println("Place existent: time="+((startTime3-startTime2A)/data.length)+", size="+((freeMem2A-freeMem3)/(1 << scale)));
		System.err.println("Seek: time="+((startTime4-startTime3)/data.length)+", size="+((freeMem3-freeMem4)/(1 << scale)));
	}
}

