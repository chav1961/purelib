package chav1961.purelib.matrix;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.matrix.interfaces.DoubleMatrix;
import chav1961.purelib.matrix.interfaces.FloatMatrix;
import chav1961.purelib.matrix.interfaces.IntMatrix;
import chav1961.purelib.matrix.interfaces.LongMatrix;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class MatrixTest {
	@Test
	public void doubleMatrixTest() {
		final DoubleMatrix	dm1 = new DoubleMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), dm2 = new DoubleMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), dm3 = new DoubleMatrixImpl(2, 3, 6, 5, 4, 3, 2, 1);
		
		Assert.assertEquals(dm2, dm1);
		Assert.assertEquals(dm2.hashCode(), dm1.hashCode());
		Assert.assertEquals(dm2.toString(), dm1.toString());
		
		Assert.assertEquals(new DoubleMatrixImpl(2, 3, 7, 7, 7, 7, 7, 7), dm1.add(dm3));
		Assert.assertEquals(new DoubleMatrixImpl(2, 3, 0, 0, 0, 0, 0, 0), dm1.sub(dm2));
		
		final DoubleMatrix	dm4 = new DoubleMatrixImpl(2, 3, 2, 1, -3, 0, 4, -1), dm5 = new DoubleMatrixImpl(3, 2, 5, -1, 6, -3, 0, 7);
		final DoubleMatrix	dmm = new DoubleMatrixImpl(3, 3, 7, -2, 19, -15, 3, -18, 23, -4, 17);
		 
		Assert.assertEquals(dmm, dm4.mul(dm5));
		
		final DoubleMatrix	dm6 = new DoubleMatrixImpl(2, 2, 7, 4, 5, 3);
		final DoubleMatrix	dmi = new DoubleMatrixImpl(2, 2, 3, -4, -5, 7);
		
		Assert.assertEquals(dmi, dm6.inv());
		
		final DoubleMatrix	dm7 = new DoubleMatrixImpl(2, 3, 2, 1, -3, 0, 4, -1);
		final DoubleMatrix	dmt = new DoubleMatrixImpl(3, 2, 2, -3, 4, 1, 0, -1);
		
		Assert.assertEquals(dmt, dm7.transp());
		
		try{new DoubleMatrixImpl(0, 3, 1, 2, 3, 4, 5, 6);
			Assert.fail("Mandatory exception was not detected (non-positive X-dimension)");
		} catch (IllegalArgumentException exc) {
		}
		try{new DoubleMatrixImpl(2, 0, 1, 2, 3, 4, 5, 6);
			Assert.fail("Mandatory exception was not detected (non-positive Y-dimension)");
		} catch (IllegalArgumentException exc) {
		}
		try{new DoubleMatrixImpl(2, 3, null);
			Assert.fail("Mandatory exception was not detected (null initials)");
		} catch (NullPointerException exc) {
		}
		try{new DoubleMatrixImpl(2, 0, 1, 2, 3, 4, 5);
			Assert.fail("Mandatory exception was not detected (initials less or more than size)");
		} catch (IllegalArgumentException exc) {
		}

		try{dm4.add(null);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (NullPointerException exc) {
		}
		try{dm4.add(dm6);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{dm4.sub(null);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (NullPointerException exc) {
		}
		try{dm4.sub(dm6);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{dm1.mul(null);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (NullPointerException exc) {
		}
		try{dm1.mul(dm2);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (IllegalArgumentException exc) {
		}

		try{dm1.inv();
			Assert.fail("Mandatory exception was not detected (not square matrix)");
		} catch (IllegalStateException exc) {
		}
		
		try{new DoubleMatrixImpl(1, 1, 0).inv();
			Assert.fail("Mandatory exception was not detected (0 at main diagonal)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void floatMatrixTest() {
		final FloatMatrix	fm1 = new FloatMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), fm2 = new FloatMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), fm3 = new FloatMatrixImpl(2, 3, 6, 5, 4, 3, 2, 1);
		
		Assert.assertEquals(fm2, fm1);
		Assert.assertEquals(fm2.hashCode(), fm1.hashCode());
		Assert.assertEquals(fm2.toString(), fm1.toString());
		
		Assert.assertEquals(new FloatMatrixImpl(2, 3, 7, 7, 7, 7, 7, 7), fm1.add(fm3));
		Assert.assertEquals(new FloatMatrixImpl(2, 3, 0, 0, 0, 0, 0, 0), fm1.sub(fm2));
		
		final FloatMatrix	fm4 = new FloatMatrixImpl(2, 3, 2, 1, -3, 0, 4, -1), fm5 = new FloatMatrixImpl(3, 2, 5, -1, 6, -3, 0, 7);
		final FloatMatrix	fmm = new FloatMatrixImpl(3, 3, 7, -2, 19, -15, 3, -18, 23, -4, 17);
		 
		Assert.assertEquals(fmm, fm4.mul(fm5));
		
		final FloatMatrix	fm6 = new FloatMatrixImpl(2, 2, 7, 4, 5, 3);
		final FloatMatrix	fmi = new FloatMatrixImpl(2, 2, 3, -4, -5, 7);
		
		Assert.assertEquals(fmi, fm6.inv());
		
		final FloatMatrix	fm7 = new FloatMatrixImpl(2, 3, 2, 1, -3, 0, 4, -1);
		final FloatMatrix	fmt = new FloatMatrixImpl(3, 2, 2, -3, 4, 1, 0, -1);
		
		Assert.assertEquals(fmt, fm7.transp());
		
		try{new FloatMatrixImpl(0, 3, 1, 2, 3, 4, 5, 6);
			Assert.fail("Mandatory exception was not detected (non-positive X-dimension)");
		} catch (IllegalArgumentException exc) {
		}
		try{new FloatMatrixImpl(2, 0, 1, 2, 3, 4, 5, 6);
			Assert.fail("Mandatory exception was not detected (non-positive Y-dimension)");
		} catch (IllegalArgumentException exc) {
		}
		try{new FloatMatrixImpl(2, 3, null);
			Assert.fail("Mandatory exception was not detected (null initials)");
		} catch (NullPointerException exc) {
		}
		try{new FloatMatrixImpl(2, 0, 1, 2, 3, 4, 5);
			Assert.fail("Mandatory exception was not detected (initials less or more than size)");
		} catch (IllegalArgumentException exc) {
		}

		try{fm4.add(null);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (NullPointerException exc) {
		}
		try{fm4.add(fm6);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{fm4.sub(null);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (NullPointerException exc) {
		}
		try{fm4.sub(fm6);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{fm1.mul(null);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (NullPointerException exc) {
		}
		try{fm1.mul(fm2);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (IllegalArgumentException exc) {
		}

		try{fm1.inv();
			Assert.fail("Mandatory exception was not detected (not square matrix)");
		} catch (IllegalStateException exc) {
		}
		
		try{new FloatMatrixImpl(1, 1, 0).inv();
			Assert.fail("Mandatory exception was not detected (0 at main diagonal)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void longMatrixTest() {
		final LongMatrix	lm1 = new LongMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), lm2 = new LongMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), lm3 = new LongMatrixImpl(2, 3, 6, 5, 4, 3, 2, 1);
		
		Assert.assertEquals(lm2, lm1);
		Assert.assertEquals(lm2.hashCode(), lm1.hashCode());
		Assert.assertEquals(lm2.toString(), lm1.toString());
		
		Assert.assertEquals(new LongMatrixImpl(2, 3, 7, 7, 7, 7, 7, 7), lm1.add(lm3));
		Assert.assertEquals(new LongMatrixImpl(2, 3, 0, 0, 0, 0, 0, 0), lm1.sub(lm2));
		
		final LongMatrix	lm4 = new LongMatrixImpl(2, 3, 2, 1, -3, 0, 4, -1), lm5 = new LongMatrixImpl(3, 2, 5, -1, 6, -3, 0, 7);
		final LongMatrix	lmm = new LongMatrixImpl(3, 3, 7, -2, 19, -15, 3, -18, 23, -4, 17);
		 
		Assert.assertEquals(lmm, lm4.mul(lm5));
		
		final LongMatrix	lm6 = new LongMatrixImpl(2, 2, 7, 4, 5, 3);
		final DoubleMatrix	lmi = new DoubleMatrixImpl(2, 2, 3, -4, -5, 7);
		
		Assert.assertEquals(lmi, lm6.inv());
		
		final LongMatrix	lm7 = new LongMatrixImpl(2, 3, 2, 1, -3, 0, 4, -1);
		final LongMatrix	lmt = new LongMatrixImpl(3, 2, 2, -3, 4, 1, 0, -1);
		
		Assert.assertEquals(lmt, lm7.transp());
		
		try{new LongMatrixImpl(0, 3, 1, 2, 3, 4, 5, 6);
			Assert.fail("Mandatory exception was not detected (non-positive X-dimension)");
		} catch (IllegalArgumentException exc) {
		}
		try{new LongMatrixImpl(2, 0, 1, 2, 3, 4, 5, 6);
			Assert.fail("Mandatory exception was not detected (non-positive Y-dimension)");
		} catch (IllegalArgumentException exc) {
		}
		try{new LongMatrixImpl(2, 3, null);
			Assert.fail("Mandatory exception was not detected (null initials)");
		} catch (NullPointerException exc) {
		}
		try{new LongMatrixImpl(2, 0, 1, 2, 3, 4, 5);
			Assert.fail("Mandatory exception was not detected (initials less or more than size)");
		} catch (IllegalArgumentException exc) {
		}

		try{lm4.add(null);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (NullPointerException exc) {
		}
		try{lm4.add(lm6);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{lm4.sub(null);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (NullPointerException exc) {
		}
		try{lm4.sub(lm6);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{lm1.mul(null);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (NullPointerException exc) {
		}
		try{lm1.mul(lm2);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (IllegalArgumentException exc) {
		}

		try{lm1.inv();
			Assert.fail("Mandatory exception was not detected (not square matrix)");
		} catch (IllegalStateException exc) {
		}
		
		try{new LongMatrixImpl(1, 1, 0).inv();
			Assert.fail("Mandatory exception was not detected (0 at main diagonal)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void intMatrixTest() {
		final IntMatrix	im1 = new IntMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), im2 = new IntMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), im3 = new IntMatrixImpl(2, 3, 6, 5, 4, 3, 2, 1);
		
		Assert.assertEquals(im2, im1);
		Assert.assertEquals(im2.hashCode(), im1.hashCode());
		Assert.assertEquals(im2.toString(), im1.toString());
		
		Assert.assertEquals(new IntMatrixImpl(2, 3, 7, 7, 7, 7, 7, 7), im1.add(im3));
		Assert.assertEquals(new IntMatrixImpl(2, 3, 0, 0, 0, 0, 0, 0), im1.sub(im2));
		
		final IntMatrix	im4 = new IntMatrixImpl(2, 3, 2, 1, -3, 0, 4, -1), im5 = new IntMatrixImpl(3, 2, 5, -1, 6, -3, 0, 7);
		final IntMatrix	imm = new IntMatrixImpl(3, 3, 7, -2, 19, -15, 3, -18, 23, -4, 17);
		 
		Assert.assertEquals(imm, im4.mul(im5));
		
		final IntMatrix		im6 = new IntMatrixImpl(2, 2, 7, 4, 5, 3);
		final FloatMatrix	imi = new FloatMatrixImpl(2, 2, 3, -4, -5, 7);
		
		Assert.assertEquals(imi, im6.inv());
		
		final IntMatrix	im7 = new IntMatrixImpl(2, 3, 2, 1, -3, 0, 4, -1);
		final IntMatrix	imt = new IntMatrixImpl(3, 2, 2, -3, 4, 1, 0, -1);
		
		Assert.assertEquals(imt, im7.transp());
		
		try{new IntMatrixImpl(0, 3, 1, 2, 3, 4, 5, 6);
			Assert.fail("Mandatory exception was not detected (non-positive X-dimension)");
		} catch (IllegalArgumentException exc) {
		}
		try{new IntMatrixImpl(2, 0, 1, 2, 3, 4, 5, 6);
			Assert.fail("Mandatory exception was not detected (non-positive Y-dimension)");
		} catch (IllegalArgumentException exc) {
		}
		try{new IntMatrixImpl(2, 3, null);
			Assert.fail("Mandatory exception was not detected (null initials)");
		} catch (NullPointerException exc) {
		}
		try{new IntMatrixImpl(2, 0, 1, 2, 3, 4, 5);
			Assert.fail("Mandatory exception was not detected (initials less or more than size)");
		} catch (IllegalArgumentException exc) {
		}

		try{im4.add(null);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (NullPointerException exc) {
		}
		try{im4.add(im6);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{im4.sub(null);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (NullPointerException exc) {
		}
		try{im4.sub(im6);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{im1.mul(null);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (NullPointerException exc) {
		}
		try{im1.mul(im2);
			Assert.fail("Mandatory exception was not detected (different size of operands)");
		} catch (IllegalArgumentException exc) {
		}

		try{im1.inv();
			Assert.fail("Mandatory exception was not detected (not square matrix)");
		} catch (IllegalStateException exc) {
		}
		
		try{new IntMatrixImpl(1, 1, 0).inv();
			Assert.fail("Mandatory exception was not detected (0 at main diagonal)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
