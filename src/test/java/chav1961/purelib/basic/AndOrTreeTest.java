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
}

