package chav1961.purelib.ui;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;

public class FormFieldFormatTest {
	@Test
	public void basicTest() throws SyntaxException {
		final FormFieldFormat	fff1 = new FormFieldFormat();
		
		Assert.assertEquals(fff1.getLen(),0);
		Assert.assertEquals(fff1.getFrac(),0);
		Assert.assertFalse(fff1.isMandatory());
		Assert.assertFalse(fff1.isNegativeMarked());
		Assert.assertFalse(fff1.isPositiveMarked());
		Assert.assertFalse(fff1.isReadOnly());
		Assert.assertFalse(fff1.isReadOnlyOnExistent());
		Assert.assertFalse(fff1.isUsedInList());
		Assert.assertFalse(fff1.isUsedInListAnchored());
		Assert.assertFalse(fff1.isZeroMarked());

		final FormFieldFormat	fff2 = new FormFieldFormat("20.2mrRnzplL".toCharArray()), fff3 = new FormFieldFormat("20.2mrRnzplL".toCharArray());

		Assert.assertEquals(fff2.getLen(),20);
		Assert.assertEquals(fff2.getFrac(),2);
		Assert.assertTrue(fff2.isMandatory());
		Assert.assertTrue(fff2.isNegativeMarked());
		Assert.assertTrue(fff2.isPositiveMarked());
		Assert.assertTrue(fff2.isReadOnly());
		Assert.assertTrue(fff2.isReadOnlyOnExistent());
		Assert.assertTrue(fff2.isUsedInList());
		Assert.assertTrue(fff2.isUsedInListAnchored());
		Assert.assertTrue(fff2.isZeroMarked());
		Assert.assertEquals(fff2,fff3);
		Assert.assertEquals(fff2.hashCode(),fff3.hashCode());
		Assert.assertEquals(fff2.toString(),fff3.toString());

		try{new FormFieldFormat((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{new FormFieldFormat((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		try{new FormFieldFormat(new char[] {'?'});
			Assert.fail("Mandatory exception was not detected (unsupported char in the 1-st argument)");
		} catch (SyntaxException exc) {
		}
		try{new FormFieldFormat("20.30".toCharArray());
			Assert.fail("Mandatory exception was not detected (fractional part greater than total length)");
		} catch (SyntaxException exc) {
		}
		
		try{new FormFieldFormat(null,0,0);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new FormFieldFormat("mrRnzplL".toCharArray(),-1,10);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{new FormFieldFormat("mrRnzplL".toCharArray(),100,10);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{new FormFieldFormat("mrRnzplL".toCharArray(),0,-1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{new FormFieldFormat("mrRnzplL".toCharArray(),0,"mrRnzplL".length()+1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
