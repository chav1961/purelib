package chav1961.purelib.matrix.internal;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.matrix.interfaces.Matrix.Type;

public class MatricesCastTest {
	@Test
	public void intCastTest() {
		try(final IntRealMatrix	irm = new IntRealMatrix(2, 3)) {
			
			irm.assign(1,2,3,4,5,6);
			
			try(final LongRealMatrix	temp = (LongRealMatrix) irm.cast(Type.REAL_LONG)) {
				
				Assert.assertArrayEquals(new long[] {1,2,3,4,5,6}, temp.extractLongs());
			}

			try(final FloatRealMatrix	temp = (FloatRealMatrix) irm.cast(Type.REAL_FLOAT)) {
				
				Assert.assertArrayEquals(new float[] {1,2,3,4,5,6}, temp.extractFloats(), 0.001f);
			}
			
			try(final DoubleRealMatrix	temp = (DoubleRealMatrix) irm.cast(Type.REAL_DOUBLE)) {
				
				Assert.assertArrayEquals(new double[] {1,2,3,4,5,6}, temp.extractDoubles(), 0.001f);
			}

			try(final FloatComplexMatrix	temp = (FloatComplexMatrix) irm.cast(Type.COMPLEX_FLOAT)) {
				
				Assert.assertArrayEquals(new float[] {1,0,2,0,3,0,4,0,5,0,6,0}, temp.extractFloats(), 0.001f);
			}

			try(final DoubleComplexMatrix	temp = (DoubleComplexMatrix) irm.cast(Type.COMPLEX_DOUBLE)) {
				
				Assert.assertArrayEquals(new double[] {1,0,2,0,3,0,4,0,5,0,6,0}, temp.extractDoubles(), 0.001f);
			}
			
			try {
				irm.cast(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exv) {
			}
		}
	}

	@Test
	public void longCastTest() {
		try(final LongRealMatrix	lrm = new LongRealMatrix(2, 3)) {
			
			lrm.assign(1,2,3,4,5,6);
			
			try(final IntRealMatrix	temp = (IntRealMatrix) lrm.cast(Type.REAL_INT)) {
				
				Assert.assertArrayEquals(new int[] {1,2,3,4,5,6}, temp.extractInts());
			}

			try(final FloatRealMatrix	temp = (FloatRealMatrix) lrm.cast(Type.REAL_FLOAT)) {
				
				Assert.assertArrayEquals(new float[] {1,2,3,4,5,6}, temp.extractFloats(), 0.001f);
			}
			
			try(final DoubleRealMatrix	temp = (DoubleRealMatrix) lrm.cast(Type.REAL_DOUBLE)) {
				
				Assert.assertArrayEquals(new double[] {1,2,3,4,5,6}, temp.extractDoubles(), 0.001f);
			}

			try(final FloatComplexMatrix	temp = (FloatComplexMatrix) lrm.cast(Type.COMPLEX_FLOAT)) {
				
				Assert.assertArrayEquals(new float[] {1,0,2,0,3,0,4,0,5,0,6,0}, temp.extractFloats(), 0.001f);
			}

			try(final DoubleComplexMatrix	temp = (DoubleComplexMatrix) lrm.cast(Type.COMPLEX_DOUBLE)) {
				
				Assert.assertArrayEquals(new double[] {1,0,2,0,3,0,4,0,5,0,6,0}, temp.extractDoubles(), 0.001f);
			}
			
			try {
				lrm.cast(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exv) {
			}
		}
	}

	@Test
	public void floatCastTest() {
		try(final FloatRealMatrix	frm = new FloatRealMatrix(2, 3)) {
			
			frm.assign(1,2,3,4,5,6);
			
			try(final IntRealMatrix	temp = (IntRealMatrix) frm.cast(Type.REAL_INT)) {
				
				Assert.assertArrayEquals(new int[] {1,2,3,4,5,6}, temp.extractInts());
			}

			try(final LongRealMatrix	temp = (LongRealMatrix) frm.cast(Type.REAL_LONG)) {
				
				Assert.assertArrayEquals(new long[] {1,2,3,4,5,6}, temp.extractLongs());
			}
			
			try(final DoubleRealMatrix	temp = (DoubleRealMatrix) frm.cast(Type.REAL_DOUBLE)) {
				
				Assert.assertArrayEquals(new double[] {1,2,3,4,5,6}, temp.extractDoubles(), 0.001f);
			}

			try(final FloatComplexMatrix	temp = (FloatComplexMatrix) frm.cast(Type.COMPLEX_FLOAT)) {
				
				Assert.assertArrayEquals(new float[] {1,0,2,0,3,0,4,0,5,0,6,0}, temp.extractFloats(), 0.001f);
			}

			try(final DoubleComplexMatrix	temp = (DoubleComplexMatrix) frm.cast(Type.COMPLEX_DOUBLE)) {
				
				Assert.assertArrayEquals(new double[] {1,0,2,0,3,0,4,0,5,0,6,0}, temp.extractDoubles(), 0.001f);
			}
			
			try {
				frm.cast(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exv) {
			}
		}
	}

	@Test
	public void doubleCastTest() {
		try(final DoubleRealMatrix	drm = new DoubleRealMatrix(2, 3)) {
			
			drm.assign(1,2,3,4,5,6);
			
			try(final IntRealMatrix	temp = (IntRealMatrix) drm.cast(Type.REAL_INT)) {
				
				Assert.assertArrayEquals(new int[] {1,2,3,4,5,6}, temp.extractInts());
			}

			try(final LongRealMatrix	temp = (LongRealMatrix) drm.cast(Type.REAL_LONG)) {
				
				Assert.assertArrayEquals(new long[] {1,2,3,4,5,6}, temp.extractLongs());
			}
			
			try(final FloatRealMatrix	temp = (FloatRealMatrix) drm.cast(Type.REAL_FLOAT)) {
				
				Assert.assertArrayEquals(new float[] {1,2,3,4,5,6}, temp.extractFloats(), 0.001f);
			}

			try(final FloatComplexMatrix	temp = (FloatComplexMatrix) drm.cast(Type.COMPLEX_FLOAT)) {
				
				Assert.assertArrayEquals(new float[] {1,0,2,0,3,0,4,0,5,0,6,0}, temp.extractFloats(), 0.001f);
			}

			try(final DoubleComplexMatrix	temp = (DoubleComplexMatrix) drm.cast(Type.COMPLEX_DOUBLE)) {
				
				Assert.assertArrayEquals(new double[] {1,0,2,0,3,0,4,0,5,0,6,0}, temp.extractDoubles(), 0.001f);
			}
			
			try {
				drm.cast(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exv) {
			}
		}
	}

	@Test
	public void complexFloatCastTest() {
		try(final FloatComplexMatrix	fcm = new FloatComplexMatrix(2, 3)) {
			
			fcm.assign(1,1,2,1,3,1,4,1,5,1,6,1);
			
			try(final IntRealMatrix	temp = (IntRealMatrix) fcm.cast(Type.REAL_INT)) {
				
				Assert.assertArrayEquals(new int[] {1,2,3,4,5,6}, temp.extractInts());
			}

			try(final LongRealMatrix	temp = (LongRealMatrix) fcm.cast(Type.REAL_LONG)) {
				
				Assert.assertArrayEquals(new long[] {1,2,3,4,5,6}, temp.extractLongs());
			}
			
			try(final FloatRealMatrix	temp = (FloatRealMatrix) fcm.cast(Type.REAL_FLOAT)) {
				
				Assert.assertArrayEquals(new float[] {1,2,3,4,5,6}, temp.extractFloats(), 0.001f);
			}

			try(final DoubleRealMatrix	temp = (DoubleRealMatrix) fcm.cast(Type.REAL_DOUBLE)) {
				
				Assert.assertArrayEquals(new double[] {1,2,3,4,5,6}, temp.extractDoubles(), 0.001f);
			}

			try(final DoubleComplexMatrix	temp = (DoubleComplexMatrix) fcm.cast(Type.COMPLEX_DOUBLE)) {
				
				Assert.assertArrayEquals(new double[] {1,1,2,1,3,1,4,1,5,1,6,1}, temp.extractDoubles(), 0.001f);
			}
			
			try {
				fcm.cast(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exv) {
			}
		}
	}

	@Test
	public void complexDoubleCastTest() {
		try(final DoubleComplexMatrix	dcm = new DoubleComplexMatrix(2, 3)) {
			
			dcm.assign(1,1,2,1,3,1,4,1,5,1,6,1);
			
			try(final IntRealMatrix	temp = (IntRealMatrix) dcm.cast(Type.REAL_INT)) {
				
				Assert.assertArrayEquals(new int[] {1,2,3,4,5,6}, temp.extractInts());
			}

			try(final LongRealMatrix	temp = (LongRealMatrix) dcm.cast(Type.REAL_LONG)) {
				
				Assert.assertArrayEquals(new long[] {1,2,3,4,5,6}, temp.extractLongs());
			}
			
			try(final FloatRealMatrix	temp = (FloatRealMatrix) dcm.cast(Type.REAL_FLOAT)) {
				
				Assert.assertArrayEquals(new float[] {1,2,3,4,5,6}, temp.extractFloats(), 0.001f);
			}

			try(final DoubleRealMatrix	temp = (DoubleRealMatrix) dcm.cast(Type.REAL_DOUBLE)) {
				
				Assert.assertArrayEquals(new double[] {1,2,3,4,5,6}, temp.extractDoubles(), 0.001f);
			}

			try(final FloatComplexMatrix	temp = (FloatComplexMatrix) dcm.cast(Type.COMPLEX_FLOAT)) {
				
				Assert.assertArrayEquals(new float[] {1,1,2,1,3,1,4,1,5,1,6,1}, temp.extractFloats(), 0.001f);
			}
			
			try {
				dcm.cast(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exv) {
			}
		}
	}
}
