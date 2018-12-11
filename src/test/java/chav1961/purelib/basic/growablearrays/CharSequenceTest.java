package chav1961.purelib.basic.growablearrays;

import org.junit.Assert;
import org.junit.Test;

public class CharSequenceTest {
	@Test
	public void basicTest() {
		final GrowableCharArray	seq = new GrowableCharArray(false);
		
		Assert.assertEquals(seq.length(),0);
		try{seq.charAt(1);
			Assert.fail("Mandatory exception was not detected (index outside the range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{seq.subSequence(0,0);
			Assert.fail("Mandatory exception was not detected (index outside the range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		
		seq.append("test string".toCharArray());
		Assert.assertEquals(seq.length(),11);
		Assert.assertEquals(seq.charAt(0),'t');
		Assert.assertEquals(seq.charAt(10),'g');
		
		final CharSequence		newSeq = seq.subSequence(3,6);
		
		Assert.assertEquals(newSeq.length(),3);
		Assert.assertEquals(newSeq.charAt(0),'t');
		Assert.assertEquals(newSeq.charAt(2),'s');

		final CharSequence		newSeq2 = newSeq.subSequence(1,2);

		Assert.assertEquals(newSeq2.length(),1);
		Assert.assertEquals(newSeq2.charAt(0),' ');

		final CharSequence		newSeq3 = newSeq.subSequence(1,1);

		Assert.assertEquals(newSeq3.length(),0);
	}
}
