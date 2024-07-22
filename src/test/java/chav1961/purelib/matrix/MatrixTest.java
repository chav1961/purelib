package chav1961.purelib.matrix;


import java.util.Arrays;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.matrix.interfaces.DoubleMatrix;
import chav1961.purelib.matrix.interfaces.FloatMatrix;
import chav1961.purelib.matrix.interfaces.IntMatrix;
import chav1961.purelib.matrix.interfaces.LongMatrix;
import chav1961.purelib.matrix.interfaces.Matrix;

@Tag("OrdinalTestCategory")
public class MatrixTest {
	@Test
	public void basicTest() throws CalculationException {
		// Integer matrix test
		final IntMatrix	im = new IntMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6);
		
		Assert.assertEquals(int.class, im.getContentType());
		Assert.assertEquals(2, im.getDimensions());
		Assert.assertEquals(2, im.getSize(0));
		Assert.assertEquals(3, im.getSize(1));

		final int[]		intContent = new int[6];
		im.get(0, intContent, 0, intContent.length);
		
		Assert.assertArrayEquals(new int[] {1, 2, 3, 4, 5, 6}, intContent);

		Arrays.fill(intContent, 10);
		im.set(intContent, 0, 0, intContent.length);
		Arrays.fill(intContent, 0);
		im.get(0, intContent, 0, intContent.length);
		
		Assert.assertArrayEquals(new int[] {10, 10, 10, 10, 10, 10}, intContent);
		
		try{im.getSize(3);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{im.get(100, intContent, 0, intContent.length);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{im.get(0, null, 0, intContent.length);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{im.get(0, intContent, 100, intContent.length);
			Assert.fail("Mandatory exception was not detected (2-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{im.get(0, intContent, 0, 100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{im.set(null, 0, 0, intContent.length);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{im.set(intContent, 100, 0, intContent.length);
		Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{im.set(intContent, 0, 100, intContent.length);
			Assert.fail("Mandatory exception was not detected (2-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{im.set(intContent, 0, 0, 100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		// Long matrix test
		final LongMatrix	lm = new LongMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6);
		
		Assert.assertEquals(long.class, lm.getContentType());
		Assert.assertEquals(2, lm.getDimensions());
		Assert.assertEquals(2, lm.getSize(0));
		Assert.assertEquals(3, lm.getSize(1));

		final long[]		longContent = new long[6];
		lm.get(0, longContent, 0, longContent.length);
		
		Assert.assertArrayEquals(new long[] {1, 2, 3, 4, 5, 6}, longContent);

		Arrays.fill(longContent, 10);
		lm.set(longContent, 0, 0, longContent.length);
		Arrays.fill(longContent, 0);
		lm.get(0, longContent, 0, longContent.length);
		
		Assert.assertArrayEquals(new long[] {10, 10, 10, 10, 10, 10}, longContent);
		
		try{lm.getSize(3);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{lm.get(100, longContent, 0, longContent.length);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{lm.get(0, null, 0, longContent.length);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{lm.get(0, longContent, 100, longContent.length);
			Assert.fail("Mandatory exception was not detected (2-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{lm.get(0, longContent, 0, 100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{lm.set(null, 0, 0, longContent.length);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{lm.set(longContent, 100, 0, longContent.length);
		Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{lm.set(longContent, 0, 100, longContent.length);
			Assert.fail("Mandatory exception was not detected (2-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{lm.set(longContent, 0, 0, 100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		// Float matrix test
		final FloatMatrix	fm = new FloatMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6);
		
		Assert.assertEquals(float.class, fm.getContentType());
		Assert.assertEquals(2, fm.getDimensions());
		Assert.assertEquals(2, fm.getSize(0));
		Assert.assertEquals(3, fm.getSize(1));

		final float[]		floatContent = new float[6];
		fm.get(0, floatContent, 0, floatContent.length);
		
		Assert.assertArrayEquals(new float[] {1, 2, 3, 4, 5, 6}, floatContent, 0.001f);

		Arrays.fill(floatContent, 10);
		fm.set(floatContent, 0, 0, floatContent.length);
		Arrays.fill(floatContent, 0);
		fm.get(0, floatContent, 0, floatContent.length);
		
		Assert.assertArrayEquals(new float[] {10, 10, 10, 10, 10, 10}, floatContent, 0.001f);
		
		try{fm.getSize(3);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{fm.get(100, floatContent, 0, floatContent.length);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{fm.get(0, null, 0, floatContent.length);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{fm.get(0, floatContent, 100, floatContent.length);
			Assert.fail("Mandatory exception was not detected (2-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{fm.get(0, floatContent, 0, 100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{fm.set(null, 0, 0, floatContent.length);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{fm.set(floatContent, 100, 0, floatContent.length);
		Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{fm.set(floatContent, 0, 100, floatContent.length);
			Assert.fail("Mandatory exception was not detected (2-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{fm.set(floatContent, 0, 0, 100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		// Double matrix test
		final DoubleMatrix	dm = new DoubleMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6);
		
		Assert.assertEquals(double.class, dm.getContentType());
		Assert.assertEquals(2, dm.getDimensions());
		Assert.assertEquals(2, dm.getSize(0));
		Assert.assertEquals(3, dm.getSize(1));

		final double[]		doubleContent = new double[6];
		dm.get(0, doubleContent, 0, doubleContent.length);
		
		Assert.assertArrayEquals(new double[] {1, 2, 3, 4, 5, 6}, doubleContent, 0.001);

		Arrays.fill(doubleContent, 10);
		dm.set(doubleContent, 0, 0, doubleContent.length);
		Arrays.fill(doubleContent, 0);
		dm.get(0, doubleContent, 0, doubleContent.length);
		
		Assert.assertArrayEquals(new double[] {10, 10, 10, 10, 10, 10}, doubleContent, 0.001);
		
		try{dm.getSize(3);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{dm.get(100, doubleContent, 0, doubleContent.length);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{dm.get(0, null, 0, doubleContent.length);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{dm.get(0, doubleContent, 100, doubleContent.length);
			Assert.fail("Mandatory exception was not detected (2-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{dm.get(0, doubleContent, 0, 100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{dm.set(null, 0, 0, doubleContent.length);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		} 
		try{dm.set(doubleContent, 100, 0, doubleContent.length);
		Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{dm.set(doubleContent, 0, 100, doubleContent.length);
			Assert.fail("Mandatory exception was not detected (2-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{dm.set(doubleContent, 0, 0, 100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
	}	
	
	@Test
	public void additionMatrixTest() throws CalculationException {
		// Integer matrix test
		final IntMatrix	im1 = new IntMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), im2 = new IntMatrixImpl(2, 3, 10, 20, 30, 40, 50, 60);
		
		Assert.assertEquals(new IntMatrixImpl(2, 3, 11, 22, 33, 44, 55, 66), im1.add(im2));
		Assert.assertEquals(new IntMatrixImpl(2, 3, 9, 18, 27, 36, 45, 54), im2.sub(im1));
		Assert.assertEquals(new IntMatrixImpl(2, 3, 11, 12, 13, 14, 15, 16), im1.add(10));
		
		try{im1.add((Matrix<?>)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{im1.add(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}
		try{im1.add((Number)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{im1.sub((Matrix<?>)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{im1.sub(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}

		// Long matrix test
		final LongMatrix	lm1 = new LongMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), lm2 = new LongMatrixImpl(2, 3, 10, 20, 30, 40, 50, 60);
		
		Assert.assertEquals(new LongMatrixImpl(2, 3, 11, 22, 33, 44, 55, 66), lm1.add(lm2));
		Assert.assertEquals(new LongMatrixImpl(2, 3, 9, 18, 27, 36, 45, 54), lm2.sub(lm1));
		Assert.assertEquals(new LongMatrixImpl(2, 3, 11, 12, 13, 14, 15, 16), lm1.add(10));
		
		try{lm1.add((Matrix<?>)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{lm1.add(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}
		try{lm1.add((Number)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{lm1.sub((Matrix<?>)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{lm1.sub(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}

		// Float matrix test
		final FloatMatrix	fm1 = new FloatMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), fm2 = new FloatMatrixImpl(2, 3, 10, 20, 30, 40, 50, 60);
		
		Assert.assertEquals(new FloatMatrixImpl(2, 3, 11, 22, 33, 44, 55, 66), fm1.add(fm2));
		Assert.assertEquals(new FloatMatrixImpl(2, 3, 9, 18, 27, 36, 45, 54), fm2.sub(fm1));
		Assert.assertEquals(new FloatMatrixImpl(2, 3, 11, 12, 13, 14, 15, 16), fm1.add(10));
		
		try{fm1.add((Matrix<?>)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{fm1.add(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}
		try{fm1.add((Number)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{fm1.sub((Matrix<?>)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{fm1.sub(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}

		// Double matrix test
		final DoubleMatrix	dm1 = new DoubleMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), dm2 = new DoubleMatrixImpl(2, 3, 10, 20, 30, 40, 50, 60);
		
		Assert.assertEquals(new DoubleMatrixImpl(2, 3, 11, 22, 33, 44, 55, 66), dm1.add(dm2));
		Assert.assertEquals(new DoubleMatrixImpl(2, 3, 9, 18, 27, 36, 45, 54), dm2.sub(dm1));
		Assert.assertEquals(new DoubleMatrixImpl(2, 3, 11, 12, 13, 14, 15, 16), dm1.add(10));
		
		try{dm1.add((Matrix<?>)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{dm1.add(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}
		try{dm1.add((Number)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{dm1.sub((Matrix<?>)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{dm1.sub(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void multiplicationMatrixTest() throws CalculationException {
		// Integer matrix test
		final IntMatrix	im1 = new IntMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), im2 = new IntMatrixImpl(3, 2, 10, 20, 30, 40, 50, 60);
	
		Assert.assertEquals(new IntMatrixImpl(2, 2, 220, 280, 490, 640), im1.mul(im2));
		Assert.assertEquals(new IntMatrixImpl(2, 3, 1, 4, 9, 16, 25, 36), im1.h_mul(im1));
		Assert.assertEquals(new IntMatrixImpl(2, 3, 10, 20, 30, 40, 50, 60), im1.mul(10));

		try{im1.mul((Matrix<?>)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{im1.mul(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}
		try{im1.h_mul(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{im1.h_mul(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}
		try{im1.mul((Number)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		// Long matrix test
		final LongMatrix	lm1 = new LongMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), lm2 = new LongMatrixImpl(3, 2, 10, 20, 30, 40, 50, 60);
	
		Assert.assertEquals(new LongMatrixImpl(2, 2, 220, 280, 490, 640), lm1.mul(lm2));
		Assert.assertEquals(new LongMatrixImpl(2, 3, 1, 4, 9, 16, 25, 36), lm1.h_mul(lm1));
		Assert.assertEquals(new LongMatrixImpl(2, 3, 10, 20, 30, 40, 50, 60), lm1.mul(10));

		try{lm1.mul((Matrix<?>)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{lm1.mul(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}
		try{lm1.h_mul(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{lm1.h_mul(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}
		try{lm1.mul((Number)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		// Float matrix test
		final FloatMatrix	fm1 = new FloatMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), fm2 = new FloatMatrixImpl(3, 2, 10, 20, 30, 40, 50, 60);
	
		Assert.assertEquals(new FloatMatrixImpl(2, 2, 220, 280, 490, 640), fm1.mul(fm2));
		Assert.assertEquals(new FloatMatrixImpl(2, 3, 1, 4, 9, 16, 25, 36), fm1.h_mul(fm1));
		Assert.assertEquals(new FloatMatrixImpl(2, 3, 10, 20, 30, 40, 50, 60), fm1.mul(10));

		try{fm1.mul((Matrix<?>)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{fm1.mul(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}
		try{fm1.h_mul(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{fm1.h_mul(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}
		try{fm1.mul((Number)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		// Double matrix test
		final DoubleMatrix	dm1 = new DoubleMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6), dm2 = new DoubleMatrixImpl(3, 2, 10, 20, 30, 40, 50, 60);
	
		Assert.assertEquals(new DoubleMatrixImpl(2, 2, 220, 280, 490, 640), dm1.mul(dm2));
		Assert.assertEquals(new DoubleMatrixImpl(2, 3, 1, 4, 9, 16, 25, 36), dm1.h_mul(dm1));
		Assert.assertEquals(new DoubleMatrixImpl(2, 3, 10, 20, 30, 40, 50, 60), dm1.mul(10));

		try{dm1.mul((Matrix<?>)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{dm1.mul(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}
		try{dm1.h_mul(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{dm1.h_mul(new IntMatrixImpl(1, 1));
			Assert.fail("Mandatory exception was not detected (uncompatible dimensions)");
		} catch (IllegalArgumentException exc) {
		}
		try{dm1.mul((Number)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void transpositionMatrixTest() throws CalculationException {
		// Integer matrix test
		final IntMatrix	im1 = new IntMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6);

		Assert.assertEquals(new IntMatrixImpl(3, 2, 1, 4, 2, 5, 3, 6), im1.transp());
		
		// Long matrix test
		final LongMatrix	lm1 = new LongMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6);

		Assert.assertEquals(new LongMatrixImpl(3, 2, 1, 4, 2, 5, 3, 6), lm1.transp());

		// Float matrix test
		final FloatMatrix	fm1 = new FloatMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6);

		Assert.assertEquals(new FloatMatrixImpl(3, 2, 1, 4, 2, 5, 3, 6), fm1.transp());

		// Double matrix test
		final DoubleMatrix	dm1 = new DoubleMatrixImpl(2, 3, 1, 2, 3, 4, 5, 6);

		Assert.assertEquals(new DoubleMatrixImpl(3, 2, 1, 4, 2, 5, 3, 6), dm1.transp());
	}
	
	@Test
	public void inversionMatrixTest() throws CalculationException {
		// Float matrix test
		final FloatMatrix	fm1 = new FloatMatrixImpl(3, 3, 1, 2, 3, 4, 5, 6, 7, 8, 0), fm2 = new FloatMatrixImpl(3, 3, 1, 2, 3, 4, 5, 6, 7, 8, 9);

		Assert.assertEquals(new FloatMatrixImpl(3, 3, -16/9f, 8/9f, -1/9f, 14/9f, -7/9f, 2/9f, -1/9f, 2/9f, -1/9f), fm1.inv());
		
		try{fm2.inv();
			Assert.fail("Mandatory exception was not detected (null determinant)");
		} catch (CalculationException exc) {
		}
		
		// Double matrix test
		final DoubleMatrix	dm1 = new DoubleMatrixImpl(3, 3, 1, 2, 3, 4, 5, 6, 7, 8, 0), dm2 = new DoubleMatrixImpl(3, 3, 1, 2, 3, 4, 5, 6, 7, 8, 9);

		Assert.assertEquals(new DoubleMatrixImpl(3, 3, -16/9d, 8/9d, -1/9d, 14/9d, -7/9d, 2/9d, -1/9d, 2/9d, -1/9d), dm1.inv());

		try{dm2.inv();
			Assert.fail("Mandatory exception was not detected (null determinant)");
		} catch (CalculationException exc) {
		}
	}
}
