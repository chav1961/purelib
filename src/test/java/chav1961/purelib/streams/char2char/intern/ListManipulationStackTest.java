package chav1961.purelib.streams.char2char.intern;


import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.streams.char2char.intern.ListManipulationStack;

@Tag("OrdinalTestCategory")
public class ListManipulationStackTest {
	@Test
	public void lifeCycleTest() {
		final ListManipulationStack	lms = new ListManipulationStack();
		
		Assert.assertEquals(lms.size(),0);
		
		lms.push(ListManipulationStack.ListType.TYPE_UL);
		Assert.assertEquals(lms.size(),1);
		Assert.assertEquals(lms.getTopType(),ListManipulationStack.ListType.TYPE_UL);
		Assert.assertEquals(lms.count(),0);

		lms.inc();
		Assert.assertEquals(lms.count(),1);

		for (int index = 0; index < 10; index++) {
			lms.push(ListManipulationStack.ListType.TYPE_UL);
		}
		Assert.assertEquals(lms.size(),11);
		for (int index = 0; index < 10; index++) {
			Assert.assertEquals(lms.pop(),ListManipulationStack.ListType.TYPE_UL);
		}
		Assert.assertEquals(lms.size(),1);
		Assert.assertEquals(lms.count(),1);
		
		Assert.assertEquals(lms.pop(),ListManipulationStack.ListType.TYPE_UL);
		Assert.assertEquals(lms.size(),0);
	}
}
