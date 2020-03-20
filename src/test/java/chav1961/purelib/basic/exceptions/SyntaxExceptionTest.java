package chav1961.purelib.basic.exceptions;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class SyntaxExceptionTest {
	@Test
	public void staticToRowColTest() {
		String		test = "line1\nline2\nline3";
		
		Assert.assertEquals(0,SyntaxException.toRow(test,0));
		Assert.assertEquals(1,SyntaxException.toRow(test,11));
		Assert.assertEquals(2,SyntaxException.toRow(test,16));

		try{SyntaxException.toRow((String)null,0);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SyntaxException.toRow(test,-1);
			Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{SyntaxException.toRow(test,test.length()+1);
			Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertEquals(0,SyntaxException.toCol(test,0));
		Assert.assertEquals(5,SyntaxException.toCol(test,11));
		Assert.assertEquals(4,SyntaxException.toCol(test,16));
		
		try{SyntaxException.toCol((String)null,0);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SyntaxException.toCol(test,-1);
			Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{SyntaxException.toCol(test,test.length()+1);
			Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		char[]	testArr = test.toCharArray();
		
		Assert.assertEquals(0,SyntaxException.toRow(testArr,0));
		Assert.assertEquals(1,SyntaxException.toRow(testArr,11));
		Assert.assertEquals(2,SyntaxException.toRow(testArr,16));
		
		try{SyntaxException.toRow((char[])null,0);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SyntaxException.toRow(testArr,-1);
			Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{SyntaxException.toRow(testArr,testArr.length+1);
			Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertEquals(0,SyntaxException.toCol(testArr,0));
		Assert.assertEquals(5,SyntaxException.toCol(testArr,11));
		Assert.assertEquals(4,SyntaxException.toCol(testArr,16));
		
		try{SyntaxException.toCol((char[])null,0);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SyntaxException.toCol(testArr,-1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{SyntaxException.toCol(testArr,testArr.length+1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void staticExtractFragmentTest() {
		String		test = "line1\nline2\nline3";
		
		Assert.assertEquals("ine2",SyntaxException.extractFragment(test,1,3,2));
		Assert.assertEquals("lin",SyntaxException.extractFragment(test,0,1,2));
		Assert.assertEquals("ne3",SyntaxException.extractFragment(test,2,4,2));
		Assert.assertEquals("ne3",SyntaxException.extractFragment(test,100,100,2));
		
		try{SyntaxException.extractFragment((String)null,1,3,2);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{SyntaxException.extractFragment(test,-1,3,2);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{SyntaxException.extractFragment(test,1,-1,2);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{SyntaxException.extractFragment(test,1,3,0);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		char[]		testArr = test.toCharArray();

		Assert.assertEquals("ine2",SyntaxException.extractFragment(testArr,1,3,2));
		Assert.assertEquals("lin",SyntaxException.extractFragment(testArr,0,1,2));
		Assert.assertEquals("ne3",SyntaxException.extractFragment(testArr,2,4,2));
		
		try{SyntaxException.extractFragment((char[])null,1,3,2);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{SyntaxException.extractFragment(testArr,-1,3,2);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{SyntaxException.extractFragment(testArr,1,-1,2);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{SyntaxException.extractFragment(testArr,1,3,0);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{SyntaxException.extractFragment(testArr,100,100,2);
			Assert.fail("Mandatory exception was not detected (no locaiton in the source)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
