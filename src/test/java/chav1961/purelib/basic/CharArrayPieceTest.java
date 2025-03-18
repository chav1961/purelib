package chav1961.purelib.basic;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class CharArrayPieceTest {
	@Test
	public void copyTest() throws CloneNotSupportedException {
		final char[]			test = "test string".toCharArray();
		final CharArrayPiece	cap1 = new CharArrayPiece("test string"), cap2 = new CharArrayPiece("test string"),
								cap3 = new CharArrayPiece("test string1"), cap4 = new CharArrayPiece("test strinh"); 
		
		Assert.assertEquals(11, cap1.length());
		Assert.assertEquals('t', cap1.charAt(0));
		Assert.assertEquals('g', cap1.charAt(10));
		Assert.assertEquals(new CharArrayPiece("e"), cap1.subSequence(1,2));

		Assert.assertEquals(cap2, cap1);
		Assert.assertFalse(cap3.equals(cap1));
		Assert.assertFalse(cap4.equals(cap1));
		Assert.assertEquals(cap1, (CharArrayPiece)cap1.clone());
		Assert.assertEquals(cap2.hashCode(), cap1.hashCode());
		Assert.assertEquals(cap2.toString(), cap1.toString());
		
		Assert.assertTrue(cap1.compareTo(cap2) == 0);
		Assert.assertTrue(cap1.compareTo(cap3) < 0);
		Assert.assertTrue(cap1.compareTo(cap4) < 0);
		Assert.assertTrue(cap1.compareTo(test, 0, test.length) == 0);
	}

	@Test
	public void directTest() throws CloneNotSupportedException {
		final char[]			test = "test string".toCharArray();
		final String			str1 = "test string", str3 = "test string1", str4 = "test strinh";
		final CharArrayPiece	cap1 = new CharArrayPiece(str1.toCharArray(), 0, str1.length()), 
								cap2 = new CharArrayPiece(str1.toCharArray(), 0, str1.length()),
								cap3 = new CharArrayPiece(str3.toCharArray(), 0, str3.length()),
								cap4 = new CharArrayPiece(str4.toCharArray(), 0, str4.length());
		
		Assert.assertEquals(11, cap1.length());
		Assert.assertEquals('t', cap1.charAt(0));
		Assert.assertEquals('g', cap1.charAt(10));
		Assert.assertEquals(new CharArrayPiece("e"), cap1.subSequence(1,2));
		
		Assert.assertEquals(cap2, cap1);
		Assert.assertFalse(cap3.equals(cap1));
		Assert.assertFalse(cap4.equals(cap1));
		Assert.assertEquals(cap1, (CharArrayPiece)cap1.clone());
		Assert.assertEquals(cap2.hashCode(), cap1.hashCode());
		Assert.assertEquals(cap2.toString(), cap1.toString());
		
		Assert.assertTrue(cap1.compareTo(cap2) == 0);
		Assert.assertTrue(cap1.compareTo(cap3) < 0);
		Assert.assertTrue(cap1.compareTo(cap4) < 0);
		Assert.assertTrue(cap1.compareTo(test, 0, test.length) == 0);
	}
}
