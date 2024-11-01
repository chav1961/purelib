package chav1961.purelib.matrix.internal;


import java.io.DataOutput;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.AggregateDirection;
import chav1961.purelib.matrix.interfaces.Matrix.AggregateType;
import chav1961.purelib.matrix.interfaces.Matrix.ApplyFloat;
import chav1961.purelib.matrix.interfaces.Matrix.Piece;
import chav1961.purelib.streams.DataOutputAdapter;

public class FloatRealMatrixTest  {

	@Test
	public void basicTest() throws RuntimeException, CloneNotSupportedException, IOException {
		final double[]	sum = new double[1];
		
		try(final FloatRealMatrix	m = new FloatRealMatrix(2, 3)) {
			Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
			Assert.assertEquals(2, m.numberOfRows());
			Assert.assertEquals(3, m.numberOfColumns());
			
			try{new FloatRealMatrix(0, 2).close();
				Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{new FloatRealMatrix(3, 0).close();
				Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
	
			// assign ints 
			
			m.assign(1,2,3,4,5,6);
			Assert.assertArrayEquals(new float[] {1,2,3,4,5,6}, m.extractFloats(), 0.001f);

			sum[0] = 0;
			m.extractInts(new DataOutputAdapter() {
				@Override
				public void writeInt(int v) throws IOException {
					sum[0] += v;
				}
			});
			Assert.assertEquals(21, sum[0], 0.001);

			try {m.extractDoubles((DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			try {m.assign((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			m.assign(Piece.of(1,1,1,2), 10, 20);
			Assert.assertArrayEquals(new float[] {1,2,3,4,10,20}, m.extractFloats(), 0.001f);
			try {m.assign(null, 10, 20);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(1,1,1,2), (int[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}

			// assign longs
			
			m.assign(6L,5L,4L,3L,2L,1L);
			Assert.assertArrayEquals(new float[] {6,5,4,3,2,1}, m.extractFloats(), 0.001f);
			
			sum[0] = 0;
			m.extractLongs(new DataOutputAdapter() {
				@Override
				public void writeLong(long v) throws IOException {
					sum[0] += v;
				}
			});
			Assert.assertEquals(21, sum[0], 0.001);

			try {m.extractDoubles((DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			try {m.assign((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			m.assign(Piece.of(1,1,1,2), 10L, 20L);
			Assert.assertArrayEquals(new float[] {6,5,4,3,10,20}, m.extractFloats(), 0.001f);
			try {m.assign(null, 10L, 20L);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(1,1,1,2), (long[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			// assign floats
			
			m.assign(1f,3f,5f,7f,9f,11f);
			Assert.assertArrayEquals(new float[] {1,3,5,7,9,11}, m.extractFloats(), 0.001f);
			
			sum[0] = 0;
			m.extractFloats(new DataOutputAdapter() {
				@Override
				public void writeFloat(float v) throws IOException {
					sum[0] += v;
				}
			});
			Assert.assertEquals(36, sum[0], 0.001);

			try {m.extractDoubles((DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			try {m.assign((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			m.assign(Piece.of(1,1,1,2), 10f, 20f);
			Assert.assertArrayEquals(new float[] {1,3,5,7,10,20}, m.extractFloats(), 0.001f);
			try {m.assign(null, 10f, 20f);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(1,1,1,2), (float[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			// assign doubles
			
			m.assign(11d,9d,7d,5d,3d,1d);
			Assert.assertArrayEquals(new float[] {11,9,7,5,3,1}, m.extractFloats(), 0.001f);
			
			sum[0] = 0;
			m.extractDoubles(new DataOutputAdapter() {
				@Override
				public void writeDouble(double v) throws IOException {
					sum[0] += v;
				}
			});
			Assert.assertEquals(36, sum[0], 0.001); 

			try {m.extractDoubles((DataOutput)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			try {m.assign((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			m.assign(Piece.of(1,1,1,2), 10d, 20d);
			Assert.assertArrayEquals(new float[] {11,9,7,5,10,20}, m.extractFloats(), 0.001f);
			try {m.assign(null, 10d, 20d);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(1,1,1,2), (double[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
	
			// extract and convert
			
			m.assign(1,2,3,4,5,6);
			Assert.assertArrayEquals(new int[] {1,2,3,4,5,6}, m.extractInts());
			Assert.assertArrayEquals(new int[] {5,6}, m.extractInts(Piece.of(1,1,1,2)));
			try {m.extractInts((Piece)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			Assert.assertArrayEquals(new long[] {1,2,3,4,5,6}, m.extractLongs());
			Assert.assertArrayEquals(new long[] {5,6}, m.extractLongs(Piece.of(1,1,1,2)));
			try {m.extractLongs((Piece)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			Assert.assertArrayEquals(new float[] {1,2,3,4,5,6}, m.extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {5,6}, m.extractFloats(Piece.of(1,1,1,2)), 0.001f);
			try {m.extractFloats((Piece)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			Assert.assertArrayEquals(new double[] {1,2,3,4,5,6}, m.extractDoubles(), 0.001);
			Assert.assertArrayEquals(new double[] {5,6}, m.extractDoubles(Piece.of(1,1,1,2)), 0.001d);
			try {m.extractDoubles((Piece)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			// deep equals
			
			final FloatRealMatrix	m2 = new FloatRealMatrix(2, 3);
			final FloatRealMatrix	m3 = new FloatRealMatrix(3, 2);
			
			m2.assign(1,2,3,4,5,6);
			Assert.assertTrue(m2.deepEquals(m));
			m2.assign(6,5,4,3,2,1);
			Assert.assertFalse(m2.deepEquals(m));
			m3.assign(1,2,3,4,5,6);
			Assert.assertFalse(m3.deepEquals(m));
			Assert.assertTrue(m.deepEquals(m));
			Assert.assertFalse(m.deepEquals(null));

			// Test clone()
			
			try(final FloatRealMatrix	dcm = (FloatRealMatrix)m.clone()) {
				Assert.assertTrue(m.deepEquals(dcm));
			}
			
			// assign matrix
			
			m.assign(m2);
			Assert.assertArrayEquals(new float[] {6,5,4,3,2,1}, m.extractFloats(), 0.001f);
			
			try {m.assign((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			final FloatRealMatrix	m4 = new FloatRealMatrix(2, 1);
			
			m4.assign(10,20);
			m.assign(Piece.of(1, 1, 1, 2), m4);
			Assert.assertArrayEquals(new float[] {6,5,4,3,10,20}, m.extractFloats(), 0.001f);
			try {m.assign(null, m4);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(1, 1, 1, 3), m4);
				Assert.fail("Mandatory exception was not detected (1-st argument outside matrix)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(1, 1, 2, 2), m4);
				Assert.fail("Mandatory exception was not detected (1-st argument outside matrix)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(3, 1, 1, 2), m4);
				Assert.fail("Mandatory exception was not detected (1-st argument outside matrix)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(1, 3, 1, 2), m4);
				Assert.fail("Mandatory exception was not detected (1-st argument outside matrix)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(1, 1, 1, 2), (Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			// fill content
			
			m.assign(1,2,3,4,5,6);
			m.fill(10);
			Assert.assertArrayEquals(new float[] {10,10,10,10,10,10}, m.extractFloats(), 0.001f);
			m.fill(20L);
			Assert.assertArrayEquals(new float[] {20,20,20,20,20,20}, m.extractFloats(), 0.001f);
			m.fill(30f);
			Assert.assertArrayEquals(new float[] {30,30,30,30,30,30}, m.extractFloats(), 0.001f);
			m.fill(40d);
			Assert.assertArrayEquals(new float[] {40,40,40,40,40,40}, m.extractFloats(), 0.001f);
	
//			m.assign(1,2,3,4,5,6);
//			m.fill(Piece.of(1,1,1,2), 10, 20);
//			Assert.assertArrayEquals(new double[] {1,0,2,0,3,0,4,0,10,20,10,20}, m.extractDoubles(), 0.001f);
//			try {m.fill(null, 10);
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (NullPointerException exc) {
//			}
//	
//			m.fill(Piece.of(1,1,1,2), 20L, 30L);
//			Assert.assertArrayEquals(new double[] {1,0,2,0,3,0,4,0,20,30,20,30}, m.extractDoubles(), 0.001f);
//			try {m.fill(null, 20L);
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (NullPointerException exc) {
//			}
//	
//			m.fill(Piece.of(1,1,1,2), 30f, 40f);
//			Assert.assertArrayEquals(new double[] {1,0,2,0,3,0,4,0,30,40,30,40}, m.extractDoubles(), 0.001f);
//			try {m.fill(null, 30f);
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (NullPointerException exc) {
//			}
//	
//			m.fill(Piece.of(1,1,1,2), 40d, 50d);
//			Assert.assertArrayEquals(new double[] {1,0,2,0,3,0,4,0,40,50,40,50}, m.extractDoubles(), 0.001f);
//			try {m.fill(null, 40d);
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (NullPointerException exc) {
//			}
			
			// Apply test
			
			m.apply((int x, int y, float val)->0f);
			Assert.assertArrayEquals(new float[] {0,0,0,0,0,0}, m.extractFloats(), 0.001f);
			try {m.apply((ApplyFloat)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
		}
	}

	@Test
	public void addTest() {
		try(final FloatRealMatrix	m1 = new FloatRealMatrix(2, 3);
			final FloatRealMatrix	m2 = new FloatRealMatrix(2, 3)) {
			
			m1.assign(1,2,3,4,5,6);

			// add ints
			
			Assert.assertArrayEquals(new double[] {2,4,6,8,10,12}, m1.add(1,2,3,4,5,6).done().extractDoubles(), 0.001f);
			try {
				m1.add((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.add(1,2,3,4,5,6).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new double[] {11,12,13,14,15,16}, m1.addValue(10).done().extractDoubles(), 0.001f);
			try {
				m1.addValue(5d, 5d);
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m1.addValue(10).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// add longs
			
			Assert.assertArrayEquals(new double[] {2,4,6,8,10,12}, m1.add(1L,2L,3L,4L,5L,6L).done().extractDoubles(), 0.001f);
			try {
				m1.add((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.add(1L,2L,3L,4L,5L,6L).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new double[] {11,12,13,14,15,16}, m1.addValue(10L).done().extractDoubles(), 0.001f);
			try {
				m1.addValue(10L).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// add floats
			
			Assert.assertArrayEquals(new double[] {2,4,6,8,10,12}, m1.add(1f,2f,3f,4f,5f,6f).done().extractDoubles(), 0.001f);
			try {
				m1.add((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.add(1f,2f,3f,4f,5f,6f).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new double[] {11,12,13,14,15,16}, m1.addValue(10f).done().extractDoubles(), 0.001f);
			try {
				m1.addValue(10f, 20f);
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m1.addValue(10f).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// add doubles
			
			Assert.assertArrayEquals(new double[] {2,4,6,8,10,12}, m1.add(1d,2d,3d,4d,5d,6d).done().extractDoubles(), 0.001f);
			try {
				m1.add((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.add(1d,2d,3d,4d,5d,6d).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new double[] {11,12,13,14,15,16}, m1.addValue(10d).done().extractDoubles(), 0.001f);
			try {
				m1.addValue(10d, 20d);
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m1.addValue(10d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// add matrix

			m2.assign(1,2,3,4,5,6);
			Assert.assertArrayEquals(new double[] {2,4,6,8,10,12}, m1.add(m2).done().extractDoubles(), 0.001f);

			try {
				m1.add((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.add(m2).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}

	@Test
	public void subtractTest() {
		try(final FloatRealMatrix	m1 = new FloatRealMatrix(2, 3);
			final FloatRealMatrix	m2 = new FloatRealMatrix(2, 3)) {
			
			m1.assign(1,2,3,4,5,6);

			// subtract ints
			
			Assert.assertArrayEquals(new double[] {0,0,0,0,0,6}, m1.subtract(1,2,3,4,5,0).done().extractDoubles(), 0.001f);
			try {
				m1.subtract((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(1,2,3,4,5,0).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new double[] {-9,-8,-7,-6,-5,-4}, m1.subtractValue(10).done().extractDoubles(), 0.001f);
			try {
				m1.subtractValue(10).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// subtract longs
			
			Assert.assertArrayEquals(new double[] {0,0,0,0,0,6}, m1.subtract(1L,2L,3L,4L,5L,0L).done().extractDoubles(), 0.001f);
			try {
				m1.subtract((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(1L,2L,3L,4L,5L,0L).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new double[] {-9,-8,-7,-6,-5,-4}, m1.subtractValue(10L).done().extractDoubles(), 0.001f);
			try {
				m1.subtractValue(10L).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// subtract floats
			
			Assert.assertArrayEquals(new double[] {0,0,0,0,0,6}, m1.subtract(1f,2f,3f,4f,5f,0f).done().extractDoubles(), 0.001f);
			try {
				m1.subtract((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(1f,2f,3f,4f,5f,6f).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new double[] {-9,-8,-7,-6,-5,-4}, m1.subtractValue(10f).done().extractDoubles(), 0.001f);
			try {
				m1.subtractValue(10f, 10f);
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m1.subtractValue(10f).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// subtract doubles
			
			Assert.assertArrayEquals(new double[] {0,0,0,0,0,6}, m1.subtract(1d,2d,3d,4d,5d,0d).done().extractDoubles(), 0.001f);
			try {
				m1.subtract((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(1d,2d,3d,4d,5d,6d).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new double[] {-9,-8,-7,-6,-5,-4}, m1.subtractValue(10d).done().extractDoubles(), 0.001f);
			try {
				m1.subtractValue(10d, 10d);
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m1.subtractValue(10d).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// subtract matrix

			m2.assign(1,2,3,4,5,0);
			Assert.assertArrayEquals(new double[] {0,0,0,0,0,6}, m1.subtract(m2).done().extractDoubles(), 0.001f);

			try {
				m1.subtract((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(m2).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}

	@Test
	public void subtractFromTest() {
		try(final FloatRealMatrix	m1 = new FloatRealMatrix(2, 3);
			final FloatRealMatrix	m2 = new FloatRealMatrix(2, 3)) {
			
			m1.assign(1,2,3,4,5,6);

			// subtract ints
			
			Assert.assertArrayEquals(new double[] {9,8,7,6,5,4}, m1.subtractFrom(10,10,10,10,10,10).done().extractDoubles(), 0.001f);
			try {
				m1.subtractFrom((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(1,2,3,4,5,0).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new double[] {9,8,7,6,5,4}, m1.subtractFromValue(10).done().extractDoubles(), 0.001f);
			try {
				m1.subtractFromValue(10).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// subtract longs
			
			Assert.assertArrayEquals(new double[] {9,8,7,6,5,4}, m1.subtractFrom(10L,10L,10L,10L,10L,10L).done().extractDoubles(), 0.001f);
			try {
				m1.subtractFrom((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(1L,2L,3L,4L,5L,0L).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new double[] {9,8,7,6,5,4}, m1.subtractFromValue(10L).done().extractDoubles(), 0.001f);
			try {
				m1.subtractFromValue(10L).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// subtract floats
			
			Assert.assertArrayEquals(new double[] {9,8,7,6,5,4}, m1.subtractFrom(10f,10f,10f,10f,10f,10f).done().extractDoubles(), 0.001f);
			try {
				m1.subtractFrom((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(1f,2f,3f,4f,5f,6f).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new double[] {9,8,7,6,5,4}, m1.subtractFromValue(10f).done().extractDoubles(), 0.001f);

			try {
				m1.subtractFromValue(10f, 5f);
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m1.subtractFromValue(10f).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// subtract doubles
			
			Assert.assertArrayEquals(new double[] {9,8,7,6,5,4}, m1.subtractFrom(10d,10d,10d,10d,10d,10d).done().extractDoubles(), 0.001f);
			try {
				m1.subtractFrom((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(1d,2d,3d,4d,5d,6d).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new double[] {9,8,7,6,5,4}, m1.subtractFromValue(10d).done().extractDoubles(), 0.001f);
			
			try {
				m1.subtractFromValue(10d, 5d);
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m1.subtractFromValue(10d).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// subtract matrix

			m2.assign(10,10,10,10,10,10);
			Assert.assertArrayEquals(new double[] {9,8,7,6,5,4}, m1.subtractFrom(m2).done().extractDoubles(), 0.001f);
			try {
				m1.subtractFrom((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(m2).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}

	@Test
	public void mulValueTest() {
		try(final FloatRealMatrix	m = new FloatRealMatrix(2, 3)) {
			m.assign(1,2,3,4,5,6);
			
			// int muls
			
			Assert.assertArrayEquals(new float[] {10,20,30,40,50,60}, m.mulValue(10).done().extractFloats(), 0.001f);
			try {
				m.mulValue(10).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {0.5f,1f,1.5f,2f,2.5f,3f}, m.divValue(2).done().extractFloats(), 0.001f);
			try {
				m.divValue(10).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {2f,1f,0.666666f,0.5f,0.4f,0.333333f}, m.divFromValue(2).done().extractFloats(), 0.001f);
			try {
				m.divFromValue(10).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// long muls
			
			Assert.assertArrayEquals(new float[] {10,20,30,40,50,60}, m.mulValue(10L).done().extractFloats(), 0.001f);
			try {
				m.mulValue(10L).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {0.5f,1f,1.5f,2f,2.5f,3f}, m.divValue(2L).done().extractFloats(), 0.001f);
			try {
				m.divValue(10L).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {2f,1f,0.666666f,0.5f,0.4f,0.333333f}, m.divFromValue(2L).done().extractFloats(), 0.001f);
			try {
				m.divFromValue(10L).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// float muls
			
			Assert.assertArrayEquals(new float[] {10,20,30,40,50,60}, m.mulValue(10f).done().extractFloats(), 0.001f);
			try {
				m.mulValue(5f, 5f);
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m.mulValue(10f).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {0.5f,1f,1.5f,2f,2.5f,3f}, m.divValue(2f).done().extractFloats(), 0.001f);
			try {
				m.divValue(2f, 2f);
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m.divValue(10f).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {2f,1f,0.666666f,0.5f,0.4f,0.333333f}, m.divFromValue(2f).done().extractFloats(), 0.001f);
			try {
				m.divFromValue(2d, 2d);
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m.divFromValue(10f).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// double muls			
			
			Assert.assertArrayEquals(new float[] {10,20,30,40,50,60}, m.mulValue(10d).done().extractFloats(), 0.001f);
			try {
				m.mulValue(5d, 5d);
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m.mulValue(10d).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new float[] {0.5f,1f,1.5f,2f,2.5f,3f}, m.divValue(2d).done().extractFloats(), 0.001f);
			try {
				m.divValue(2d, 2d);
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m.divValue(10d).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new float[] {2f,1f,0.666666f,0.5f,0.4f,0.333333f}, m.divFromValue(2d).done().extractFloats(), 0.001f);
			try {
				m.divFromValue(2d, 2d);
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m.divFromValue(10d).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}

	@Test
	public void mulHadamardTest() {
		try(final FloatRealMatrix	m1 = new FloatRealMatrix(2, 3);
			final FloatRealMatrix	m2 = new FloatRealMatrix(2, 3)) {
			
			m1.assign(1,2,3,4,5,6);
			
			// int muls
			
			Assert.assertArrayEquals(new double[] {1,4,9,16,25,36}, m1.mulHadamard(1,2,3,4,5,6).done().extractDoubles(), 0.001f);
			try {
				m1.mulHadamard((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(1,2,3,4,5,6).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new double[] {1,1,1,1,1,1}, m1.mulInvHadamard(1,2,3,4,5,6).done().extractDoubles(), 0.001f);
			try {
				m1.mulInvHadamard((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(1,2,3,4,5,6).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new double[] {10,5,3.333333f,2.5f,2,1.666666f}, m1.mulInvFromHadamard(10,10,10,10,10,10).done().extractDoubles(), 0.001f);
			try {
				m1.mulInvFromHadamard((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(1,2,3,4,5,6).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// long muls
			
			Assert.assertArrayEquals(new double[] {1,4,9,16,25,36}, m1.mulHadamard(1L,2L,3L,4L,5L,6L).done().extractDoubles(), 0.001f);
			try {
				m1.mulHadamard((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(1L,2L,3L,4L,5L,6L).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new double[] {1,1,1,1,1,1}, m1.mulInvHadamard(1L,2L,3L,4L,5L,6L).done().extractDoubles(), 0.001f);
			try {
				m1.mulInvHadamard((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(1L,2L,3L,4L,5L,6L).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new double[] {10,5,3.333333f,2.5f,2,1.666666f}, m1.mulInvFromHadamard(10L,10L,10L,10L,10L,10L).done().extractDoubles(), 0.001f);
			try {
				m1.mulInvFromHadamard((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(1L,2L,3L,4L,5L,6L).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// float muls
			
			Assert.assertArrayEquals(new double[] {1,4,9,16,25,36}, m1.mulHadamard(1f,2f,3f,4f,5f,6f).done().extractDoubles(), 0.001f);
			try {
				m1.mulHadamard((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(1f,2f,3f,4f,5f,6f).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new double[] {1,1,1,1,1,1}, m1.mulInvHadamard(1f,2f,3f,4f,5f,6f).done().extractDoubles(), 0.001f);
			try {
				m1.mulInvHadamard((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(1f,2f,3f,4f,5f,6f).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new double[] {10,5,3.333333f,2.5f,2,1.666666f}, m1.mulInvFromHadamard(10f,10f,10f,10f,10f,10f).done().extractDoubles(), 0.001f);
			try {
				m1.mulInvFromHadamard((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(1f,2f,3f,4f,5f,6f).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// double muls
			
			Assert.assertArrayEquals(new double[] {1,4,9,16,25,36}, m1.mulHadamard(1d,2d,3d,4d,5d,6d).done().extractDoubles(), 0.001f);
			try {
				m1.mulHadamard((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(1d,2d,3d,4d,5d,6d).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new double[] {1,1,1,1,1,1}, m1.mulInvHadamard(1d,2d,3d,4d,5d,6d).done().extractDoubles(), 0.001f);
			try {
				m1.mulInvHadamard((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(1d,2d,3d,4d,5d,6d).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new double[] {10,5,3.333333f,2.5f,2,1.666666f}, m1.mulInvFromHadamard(10d,10d,10d,10d,10d,10d).done().extractDoubles(), 0.001f);
			try {
				m1.mulInvFromHadamard((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(1d,2d,3d,4d,5d,6d).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// matrix muls
			
			m2.assign(1,2,3,4,5,6);
			Assert.assertArrayEquals(new double[] {1,4,9,16,25,36}, m1.mulHadamard(m2).done().extractDoubles(), 0.001f);
			try {
				m1.mulHadamard((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(m2).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new double[] {1,1,1,1,1,1}, m1.mulInvHadamard(m2).done().extractDoubles(), 0.001f);
			try {
				m1.mulInvHadamard((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(m2).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			m2.assign(10,10,10,10,10,10);
			Assert.assertArrayEquals(new double[] {10,5,3.333333f,2.5f,2,1.666666f}, m1.mulInvFromHadamard(m2).done().extractDoubles(), 0.001f);
	 		try {
				m1.mulInvFromHadamard((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(m2).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}
	
	@Test
	public void ordinalMulTest() {
		try(final FloatRealMatrix	m1 = new FloatRealMatrix(2, 3);
			final FloatRealMatrix	m2 = new FloatRealMatrix(3, 2);
			final FloatRealMatrix	mX = new FloatRealMatrix(4, 4);) {
			Matrix	m3;
			
			m1.assign(1,2,3,4,5,6);
			m2.assign(10,20,30,40,50,60);
			m3 = m1.mul(m2).done();
			
			Assert.assertEquals(2, m3.numberOfRows());
			Assert.assertEquals(2, m3.numberOfColumns());
			Assert.assertArrayEquals(new double[] {220,280,490,640}, m3.extractDoubles(), 0.001f);
			
	 		try {
				m1.mul((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mul(m2).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			try {
				m1.mul(mX);
				Assert.fail("Mandatory exception was not detected (incompatible dimensions)");
			} catch (IllegalArgumentException exc) {
			}
			
			m3 = m1.mulFrom(m2).done();
			
			Assert.assertEquals(3, m3.numberOfRows());
			Assert.assertEquals(3, m3.numberOfColumns());
			Assert.assertArrayEquals(new double[] {90,120,150,190,260,330,290,400,510}, m3.extractDoubles(), 0.001f);
			
	 		try {
				m1.mulFrom((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulFrom(m2).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			try {
				m1.mulFrom(mX);
				Assert.fail("Mandatory exception was not detected (incompatible dimensions)");
			} catch (IllegalArgumentException exc) {
			}
			
		}
	}
	
	@Test
	public void tensorMulTest() {
		try(final FloatRealMatrix	m1 = new FloatRealMatrix(3, 3);
			final FloatRealMatrix	m2 = new FloatRealMatrix(2, 2)) {
			Matrix	m3;
			
			m1.assign(1,2,3,4,5,6,7,8,9);
			m2.assign(10,20,30,40);
			
			m3 = m1.tensorMul(m2).done();
			
			Assert.assertEquals(6, m3.numberOfRows());
			Assert.assertEquals(6, m3.numberOfColumns());
			Assert.assertArrayEquals(new double[] {10,20,20,40,30,60,
												  30,40,60,80,90,120,
												  40,80,50,100,60,120,
												  120,160,150,200,180,240,
												  70,140,80,160,90,180,
												  210,280,240,320,270,360},
					                            m3.extractDoubles(), 0.001f);
	 		try {
				m1.tensorMul((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.tensorMul(m2).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			m3 = m1.tensorMulFrom(m2).done();
			
			Assert.assertEquals(6, m3.numberOfRows());
			Assert.assertEquals(6, m3.numberOfColumns());
			Assert.assertArrayEquals(new double[] {10,20,30,20,40,60,
												  40,50,60,80,100,120,
												  70,80,90,140,160,180,
												  30,60,90,40,80,120,
												  120,150,180,160,200,240,
												  210,240,270,280,320,360}, 
					 							m3.extractDoubles(), 0.001f);
	 		try {
				m1.tensorMulFrom((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.tensorMulFrom(m2).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}

	@Test
	public void unaryTest() {
		try(final FloatRealMatrix	m = new FloatRealMatrix(3, 3)) {
				
			m.assign(1,2,3,4,5,6,7,8,10);
			Assert.assertArrayEquals(new double[] {1,4,7,2,5,8,3,6,10}, m.transpose().done().extractDoubles(), 0.001f);
			try {
				m.transpose().extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new double[] {-0.666666f,-1.333333f,1,-0.666666f,3.66666f,-2,1,-2,1}, m.invert().done().extractDoubles(), 0.001f);
			try {
				m.invert().extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			try{
				new FloatRealMatrix(3, 2).invert();
				Assert.fail("Mandatory exception was not detected (non-square matrix)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertEquals(-3.0f, m.det());
			try {
				m.det2();
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			try{
				new FloatRealMatrix(3, 2).det();
				Assert.fail("Mandatory exception was not detected (non-square matrix)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertEquals(16.0f, m.track());
			try {
				m.track2();
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
		}
	}

	@Test
	public void aggregateTest() {
		try(final FloatRealMatrix	m = new FloatRealMatrix(3, 3)) {
			
			m.assign(1,2,3,4,5,6,7,8,9);
			Assert.assertArrayEquals(new float[] {6, 15, 24}, m.aggregate(AggregateDirection.ByColumns, AggregateType.Sum).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {12, 15, 18}, m.aggregate(AggregateDirection.ByRows, AggregateType.Sum).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {45}, m.aggregate(AggregateDirection.Total, AggregateType.Sum).done().extractFloats(), 0.001f);

			Assert.assertArrayEquals(new float[] {1, 4, 7}, m.aggregate(AggregateDirection.ByColumns, AggregateType.Min).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {1, 2, 3}, m.aggregate(AggregateDirection.ByRows, AggregateType.Min).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {1}, m.aggregate(AggregateDirection.Total, AggregateType.Min).done().extractFloats(), 0.001f);

			Assert.assertArrayEquals(new float[] {3, 6, 9}, m.aggregate(AggregateDirection.ByColumns, AggregateType.Max).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {7, 8, 9}, m.aggregate(AggregateDirection.ByRows, AggregateType.Max).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {9}, m.aggregate(AggregateDirection.Total, AggregateType.Max).done().extractFloats(), 0.001f);

			Assert.assertArrayEquals(new float[] {2, 5, 8}, m.aggregate(AggregateDirection.ByColumns, AggregateType.Avg).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {4, 5, 6}, m.aggregate(AggregateDirection.ByRows, AggregateType.Avg).done().extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {5}, m.aggregate(AggregateDirection.Total, AggregateType.Avg).done().extractFloats(), 0.001f);
			
			try{m.aggregate(null, AggregateType.Avg);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{m.aggregate(AggregateDirection.ByColumns, null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{m.aggregate(AggregateDirection.ByColumns, AggregateType.Avg).extractDoubles();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}
}
