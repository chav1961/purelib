package chav1961.purelib.basic.growablearrays;

import org.junit.Assert;
import org.junit.Test;

public class ManagersTest {
	private int					call;
	
	@Test
	public void plainTest() {
		final AbstractArrayContentManager	aacm = new AbstractPlainContentManager(AbstractArrayContentManager.MINIMAL_ARRAY_SIZE) {
												@Override int truncateArray(int newSize) {call--; return newSize;}
												@Override int expandArray(int newSize) {call++; return newSize;}
												@Override void walk(Walker walker) {}
											};
		
		call = 0;
		for (int index = 4; index < 20; index++) {
			aacm.checkSize(2 << index);
			aacm.checkSize(3 << index);
			aacm.checkSize(2 << index);
			aacm.checkSize(1 << index);
		}
		Assert.assertEquals(call,16);

		call = 0;
		for (int index = 4; index < 20; index++) {
			aacm.checkSize(1 << index);
			aacm.checkSize(4 << index);
			aacm.checkSize(1 << index);
		}
		Assert.assertEquals(call,1);
		
		Assert.assertEquals(aacm.toSliceIndex(100),0);
		Assert.assertEquals(aacm.toRelativeOffset(100),100);
	}

	@Test
	public void slicedTest() {
		final AbstractArrayContentManager	aacm = new AbstractSlicedContentManager(AbstractArrayContentManager.MINIMAL_ARRAY_SIZE) {
												@Override int truncateArray(int newSize) {call--; return newSize;}
												@Override int expandArray(int newSize) {call++; return newSize;}
												@Override void walk(Walker walker) {}
											};
		
		call = 0;
		for (int index = 4; index < 20; index++) {
			aacm.checkSize(2 << index);
			aacm.checkSize(3 << index);
			aacm.checkSize(2 << index);
			aacm.checkSize(1 << index);
		}
		Assert.assertEquals(call,16);

		call = 0;
		for (int index = 4; index < 20; index++) {
			aacm.checkSize(1 << index);
			aacm.checkSize(4 << index);
			aacm.checkSize(1 << index);
		}
		Assert.assertEquals(call,1);
		
		Assert.assertEquals(aacm.toSliceIndex(100),6);
		Assert.assertEquals(aacm.toRelativeOffset(100),4);
	}
}
