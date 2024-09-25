package chav1961.purelib.matrix.internal;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.AggregateDirection;
import chav1961.purelib.matrix.interfaces.Matrix.AggregateType;
import chav1961.purelib.matrix.interfaces.Matrix.Piece;

public class IntRealMatrixTest {

	@Test
	public void basicTest() {
		try(final IntRealMatrix	m = new IntRealMatrix(2, 3)) {
			Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
			Assert.assertEquals(2, m.numberOfRows());
			Assert.assertEquals(3, m.numberOfColumns());
			
			try{new IntRealMatrix(0, 2);
				Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{new IntRealMatrix(3, 0);
				Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
	
			// assign ints 
			
			m.assign(1,2,3,4,5,6);
			Assert.assertArrayEquals(new float[] {1,2,3,4,5,6}, m.extractFloats(), 0.001f);
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
			try {m.extractInts(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			Assert.assertArrayEquals(new long[] {1,2,3,4,5,6}, m.extractLongs());
			Assert.assertArrayEquals(new long[] {5,6}, m.extractLongs(Piece.of(1,1,1,2)));
			try {m.extractLongs(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			Assert.assertArrayEquals(new float[] {1,2,3,4,5,6}, m.extractFloats(), 0.001f);
			Assert.assertArrayEquals(new float[] {5,6}, m.extractFloats(Piece.of(1,1,1,2)), 0.001f);
			try {m.extractFloats(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			Assert.assertArrayEquals(new double[] {1,2,3,4,5,6}, m.extractDoubles(), 0.001);
			Assert.assertArrayEquals(new double[] {5,6}, m.extractDoubles(Piece.of(1,1,1,2)), 0.001d);
			try {m.extractDoubles(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			// deep equals
			
			final IntRealMatrix	m2 = new IntRealMatrix(2, 3);
			final IntRealMatrix	m3 = new IntRealMatrix(3, 2);
			
			m2.assign(1,2,3,4,5,6);
			Assert.assertTrue(m2.deepEquals(m));
			m2.assign(6,5,4,3,2,1);
			Assert.assertFalse(m2.deepEquals(m));
			m3.assign(1,2,3,4,5,6);
			Assert.assertFalse(m3.deepEquals(m));
			Assert.assertTrue(m.deepEquals(m));
			Assert.assertFalse(m.deepEquals(null));
			
			// assign matrix
			
			m.assign(m2);
			Assert.assertArrayEquals(new float[] {6,5,4,3,2,1}, m.extractFloats(), 0.001f);
			
			try {m.assign((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			final IntRealMatrix	m4 = new IntRealMatrix(2, 1);
			
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
	
			m.assign(1,2,3,4,5,6);
			m.fill(Piece.of(1,1,1,2), 10);
			Assert.assertArrayEquals(new float[] {1,2,3,4,10,10}, m.extractFloats(), 0.001f);
			try {m.fill(null, 10);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			m.fill(Piece.of(1,1,1,2), 20L);
			Assert.assertArrayEquals(new float[] {1,2,3,4,20,20}, m.extractFloats(), 0.001f);
			try {m.fill(null, 20L);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			m.fill(Piece.of(1,1,1,2), 30f);
			Assert.assertArrayEquals(new float[] {1,2,3,4,30,30}, m.extractFloats(), 0.001f);
			try {m.fill(null, 30f);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			m.fill(Piece.of(1,1,1,2), 40d);
			Assert.assertArrayEquals(new float[] {1,2,3,4,40,40}, m.extractFloats(), 0.001f);
			try {m.fill(null, 40d);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
	
			try {m.fill(0f,0f);
				Assert.fail("Mandatory exception was not detected (unsupported fill)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(null, 0f,0f);
				Assert.fail("Mandatory exception was not detected (unsupported fill)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(1, 1, 1, 2), 0f, 0f);
				Assert.fail("Mandatory exception was not detected (unsupported fill)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(0d,0d);
				Assert.fail("Mandatory exception was not detected (unsupported fill)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(null, 0d,0d);
				Assert.fail("Mandatory exception was not detected (unsupported fill)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(1, 1, 1, 2), 0d, 0d);
				Assert.fail("Mandatory exception was not detected (unsupported fill)");
			} catch (UnsupportedOperationException exc) {
			}
		}
	}

	@Test
	public void addTest() {
		try(final IntRealMatrix	m1 = new IntRealMatrix(2, 3);
			final IntRealMatrix	m2 = new IntRealMatrix(2, 3)) {
			
			m1.assign(1,2,3,4,5,6);

			// add ints
			
			Assert.assertArrayEquals(new int[] {2,4,6,8,10,12}, m1.add(1,2,3,4,5,6).done().extractInts());
			try {
				m1.add((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.add(1,2,3,4,5,6).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new int[] {11,12,13,14,15,16}, m1.addValue(10).done().extractInts());
			try {
				m1.addValue(10).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// add longs
			
			Assert.assertArrayEquals(new int[] {2,4,6,8,10,12}, m1.add(1L,2L,3L,4L,5L,6L).done().extractInts());
			try {
				m1.add((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.add(1L,2L,3L,4L,5L,6L).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new int[] {11,12,13,14,15,16}, m1.addValue(10L).done().extractInts());
			try {
				m1.addValue(10L).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// add floats
			
			Assert.assertArrayEquals(new int[] {2,4,6,8,10,12}, m1.add(1f,2f,3f,4f,5f,6f).done().extractInts());
			try {
				m1.add((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.add(1f,2f,3f,4f,5f,6f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {11,12,13,14,15,16}, m1.addValue(10f).done().extractInts());
			try {
				m1.addValue(10f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// add doubles
			
			Assert.assertArrayEquals(new int[] {2,4,6,8,10,12}, m1.add(1d,2d,3d,4d,5d,6d).done().extractInts());
			try {
				m1.add((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.add(1d,2d,3d,4d,5d,6d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {11,12,13,14,15,16}, m1.addValue(10d).done().extractInts());
			try {
				m1.addValue(10d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// add matrix

			m2.assign(1,2,3,4,5,6);
			Assert.assertArrayEquals(new int[] {2,4,6,8,10,12}, m1.add(m2).done().extractInts());

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
			
			// unsupported ops
			
			try {
				m1.addValue(1f, 1f);
				Assert.fail("Mandatory exception was not detected (unsupported add)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m1.addValue(1d, 1d);
				Assert.fail("Mandatory exception was not detected (unsupported add)");
			} catch (UnsupportedOperationException exc) {
			}
		}
	}

	@Test
	public void subtractTest() {
		try(final IntRealMatrix	m1 = new IntRealMatrix(2, 3);
			final IntRealMatrix	m2 = new IntRealMatrix(2, 3)) {
			
			m1.assign(1,2,3,4,5,6);

			// subtract ints
			
			Assert.assertArrayEquals(new int[] {0,0,0,0,0,6}, m1.subtract(1,2,3,4,5,0).done().extractInts());
			try {
				m1.subtract((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(1,2,3,4,5,0).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new int[] {-9,-8,-7,-6,-5,-4}, m1.subtractValue(10).done().extractInts());
			try {
				m1.subtractValue(10).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// subtract longs
			
			Assert.assertArrayEquals(new int[] {0,0,0,0,0,6}, m1.subtract(1L,2L,3L,4L,5L,0L).done().extractInts());
			try {
				m1.subtract((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(1L,2L,3L,4L,5L,0L).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new int[] {-9,-8,-7,-6,-5,-4}, m1.subtractValue(10L).done().extractInts());
			try {
				m1.subtractValue(10L).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// subtract floats
			
			Assert.assertArrayEquals(new int[] {0,0,0,0,0,6}, m1.subtract(1f,2f,3f,4f,5f,0f).done().extractInts());
			try {
				m1.subtract((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(1f,2f,3f,4f,5f,6f).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {-9,-8,-7,-6,-5,-4}, m1.subtractValue(10f).done().extractInts());
			try {
				m1.subtractValue(10f).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// subtract doubles
			
			Assert.assertArrayEquals(new int[] {0,0,0,0,0,6}, m1.subtract(1d,2d,3d,4d,5d,0d).done().extractInts());
			try {
				m1.subtract((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(1d,2d,3d,4d,5d,6d).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {-9,-8,-7,-6,-5,-4}, m1.subtractValue(10d).done().extractInts());
			try {
				m1.subtractValue(10d).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// subtract matrix

			m2.assign(1,2,3,4,5,0);
			Assert.assertArrayEquals(new int[] {0,0,0,0,0,6}, m1.subtract(m2).done().extractInts());

			try {
				m1.subtract((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtract(m2).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// unsupported ops
			
			try {
				m1.subtractValue(1f, 1f);
				Assert.fail("Mandatory exception was not detected (unsupported subtract)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m1.subtractValue(1d, 1d);
				Assert.fail("Mandatory exception was not detected (unsupported subtract)");
			} catch (UnsupportedOperationException exc) {
			}
		}
	}

	@Test
	public void subtractFromTest() {
		try(final IntRealMatrix	m1 = new IntRealMatrix(2, 3);
			final IntRealMatrix	m2 = new IntRealMatrix(2, 3)) {
			
			m1.assign(1,2,3,4,5,6);

			// subtract ints
			
			Assert.assertArrayEquals(new int[] {9,8,7,6,5,4}, m1.subtractFrom(10,10,10,10,10,10).done().extractInts());
			try {
				m1.subtractFrom((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(1,2,3,4,5,0).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new int[] {9,8,7,6,5,4}, m1.subtractFromValue(10).done().extractInts());
			try {
				m1.subtractFromValue(10).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// subtract longs
			
			Assert.assertArrayEquals(new int[] {9,8,7,6,5,4}, m1.subtractFrom(10L,10L,10L,10L,10L,10L).done().extractInts());
			try {
				m1.subtractFrom((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(1L,2L,3L,4L,5L,0L).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new int[] {9,8,7,6,5,4}, m1.subtractFromValue(10L).done().extractInts());
			try {
				m1.subtractFromValue(10L).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// subtract floats
			
			Assert.assertArrayEquals(new int[] {9,8,7,6,5,4}, m1.subtractFrom(10f,10f,10f,10f,10f,10f).done().extractInts());
			try {
				m1.subtractFrom((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(1f,2f,3f,4f,5f,6f).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {9,8,7,6,5,4}, m1.subtractFromValue(10f).done().extractInts());
			try {
				m1.subtractFromValue(10f).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// subtract doubles
			
			Assert.assertArrayEquals(new int[] {9,8,7,6,5,4}, m1.subtractFrom(10d,10d,10d,10d,10d,10d).done().extractInts());
			try {
				m1.subtractFrom((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(1d,2d,3d,4d,5d,6d).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {9,8,7,6,5,4}, m1.subtractFromValue(10d).done().extractInts());
			try {
				m1.subtractFromValue(10d).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// subtract matrix

			m2.assign(10,10,10,10,10,10);
			Assert.assertArrayEquals(new int[] {9,8,7,6,5,4}, m1.subtractFrom(m2).done().extractInts());
			try {
				m1.subtractFrom((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.subtractFrom(m2).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// unsupported ops
			
			try {
				m1.subtractFromValue(1f, 1f);
				Assert.fail("Mandatory exception was not detected (unsupported subtract)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m1.subtractFromValue(1d, 1d);
				Assert.fail("Mandatory exception was not detected (unsupported subtract)");
			} catch (UnsupportedOperationException exc) {
			}
		}
	}

	@Test
	public void mulValueTest() {
		try(final IntRealMatrix	m = new IntRealMatrix(2, 3)) {
			m.assign(1,2,3,4,5,6);
			
			// int muls
			
			Assert.assertArrayEquals(new int[] {10,20,30,40,50,60}, m.mulValue(10).done().extractInts());
			try {
				m.mulValue(10).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {0,1,1,2,2,3}, m.divValue(2).done().extractInts());
			try {
				m.divValue(10).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new int[] {100,50,33,25,20,16}, m.divFromValue(100).done().extractInts());
			try {
				m.divFromValue(10).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// long muls
			
			Assert.assertArrayEquals(new int[] {10,20,30,40,50,60}, m.mulValue(10L).done().extractInts());
			try {
				m.mulValue(10L).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {0,1,1,2,2,3}, m.divValue(2L).done().extractInts());
			try {
				m.divValue(10L).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new int[] {100,50,33,25,20,16}, m.divFromValue(100L).done().extractInts());
			try {
				m.divFromValue(10L).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// float muls
			
			Assert.assertArrayEquals(new int[] {10,20,30,40,50,60}, m.mulValue(10f).done().extractInts());
			try {
				m.mulValue(10f).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {0,1,1,2,2,3}, m.divValue(2f).done().extractInts());
			try {
				m.divValue(10f).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new int[] {100,50,33,25,20,16}, m.divFromValue(100f).done().extractInts());
			try {
				m.divFromValue(10f).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// double muls			
			
			Assert.assertArrayEquals(new float[] {10,20,30,40,50,60}, m.mulValue(10d).done().extractFloats(), 0.001f);
			try {
				m.mulValue(10d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {0,1,1,2,2,3}, m.divValue(2d).done().extractInts());
			try {
				m.divValue(10d).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new int[] {100,50,33,25,20,16}, m.divFromValue(100d).done().extractInts());
			try {
				m.divFromValue(10d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// unsupported ops
			
			try {
				m.mulValue(10f, 10f).extractInts();
				Assert.fail("Mandatory exception was not detected (unsupported mul)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m.divValue(10f, 10f).extractInts();
				Assert.fail("Mandatory exception was not detected (unsupported mul)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m.divFromValue(10f, 10f).extractInts();
				Assert.fail("Mandatory exception was not detected (unsupported mul)");
			} catch (UnsupportedOperationException exc) {
			}
			
			try {
				m.mulValue(10d, 10d).extractInts();
				Assert.fail("Mandatory exception was not detected (unsupported mul)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m.divValue(10d, 10d).extractInts();
				Assert.fail("Mandatory exception was not detected (unsupported mul)");
			} catch (UnsupportedOperationException exc) {
			}
			try {
				m.divFromValue(10d, 10d).extractInts();
				Assert.fail("Mandatory exception was not detected (unsupported mul)");
			} catch (UnsupportedOperationException exc) {
			}
		}
	}

	@Test
	public void mulHadamardTest() {
		try(final IntRealMatrix	m1 = new IntRealMatrix(2, 3);
			final IntRealMatrix	m2 = new IntRealMatrix(2, 3)) {
			m1.assign(1,2,3,4,5,6);
			
			// int muls
			
			Assert.assertArrayEquals(new int[] {1,4,9,16,25,36}, m1.mulHadamard(1,2,3,4,5,6).done().extractInts());
			try {
				m1.mulHadamard((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(1,2,3,4,5,6).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {1,1,1,1,1,1}, m1.mulInvHadamard(1,2,3,4,5,6).done().extractInts());
			try {
				m1.mulInvHadamard((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(1,2,3,4,5,6).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {10,10,10,10,10,10}, m1.mulInvFromHadamard(10,20,30,40,50,60).done().extractInts());
			try {
				m1.mulInvFromHadamard((int[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(1,2,3,4,5,6).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// long muls
			
			Assert.assertArrayEquals(new int[] {1,4,9,16,25,36}, m1.mulHadamard(1L,2L,3L,4L,5L,6L).done().extractInts());
			try {
				m1.mulHadamard((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(1L,2L,3L,4L,5L,6L).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {1,1,1,1,1,1}, m1.mulInvHadamard(1L,2L,3L,4L,5L,6L).done().extractInts());
			try {
				m1.mulInvHadamard((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(1L,2L,3L,4L,5L,6L).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {10,10,10,10,10,10}, m1.mulInvFromHadamard(10L,20L,30L,40L,50L,60L).done().extractInts());
			try {
				m1.mulInvFromHadamard((long[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(1L,2L,3L,4L,5L,6L).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// float muls
			
			Assert.assertArrayEquals(new int[] {1,4,9,16,25,36}, m1.mulHadamard(1f,2f,3f,4f,5f,6f).done().extractInts());
			try {
				m1.mulHadamard((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(1f,2f,3f,4f,5f,6f).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {1,1,1,1,1,1}, m1.mulInvHadamard(1f,2f,3f,4f,5f,6f).done().extractInts());
			try {
				m1.mulInvHadamard((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(1f,2f,3f,4f,5f,6f).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {10,10,10,10,10,10}, m1.mulInvFromHadamard(10f,20f,30f,40f,50f,60f).done().extractInts());
			try {
				m1.mulInvFromHadamard((float[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(1f,2f,3f,4f,5f,6f).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			// double muls
			
			Assert.assertArrayEquals(new int[] {1,4,9,16,25,36}, m1.mulHadamard(1d,2d,3d,4d,5d,6d).done().extractInts());
			try {
				m1.mulHadamard((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(1d,2d,3d,4d,5d,6d).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {1,1,1,1,1,1}, m1.mulInvHadamard(1d,2d,3d,4d,5d,6d).done().extractInts());
			try {
				m1.mulInvHadamard((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(1d,2d,3d,4d,5d,6d).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			Assert.assertArrayEquals(new int[] {10,10,10,10,10,10}, m1.mulInvFromHadamard(10d,20d,30d,40d,50d,60d).done().extractInts());
			try {
				m1.mulInvFromHadamard((double[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(1d,2d,3d,4d,5d,6d).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			// matrix muls
			
			m2.assign(1,2,3,4,5,6);
			Assert.assertArrayEquals(new int[] {1,4,9,16,25,36}, m1.mulHadamard(m2).done().extractInts());
			try {
				m1.mulHadamard((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulHadamard(m2).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			Assert.assertArrayEquals(new int[] {1,1,1,1,1,1}, m1.mulInvHadamard(m2).done().extractInts());
			try {
				m1.mulInvHadamard((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvHadamard(m2).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			m2.assign(10,20,30,40,50,60);
			Assert.assertArrayEquals(new int[] {10,10,10,10,10,10}, m1.mulInvFromHadamard(m2).done().extractInts());
	 		try {
				m1.mulInvFromHadamard((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulInvFromHadamard(m2).extractInts();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}
	
	@Test
	public void ordinalMulTest() {
		try(final IntRealMatrix	m1 = new IntRealMatrix(2, 3);
			final IntRealMatrix	m2 = new IntRealMatrix(3, 2);
			final IntRealMatrix	mX = new IntRealMatrix(4, 4);) {
			Matrix	m3;
			
			m1.assign(1,2,3,4,5,6);
			m2.assign(10,20,30,40,50,60);
			m3 = m1.mul(m2).done();
			
			Assert.assertEquals(2, m3.numberOfRows());
			Assert.assertEquals(2, m3.numberOfColumns());
			Assert.assertArrayEquals(new int[] {220,280,490,640}, m3.extractInts());
			
	 		try {
				m1.mul((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mul(m2).extractFloats();
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
			Assert.assertArrayEquals(new int[] {90,120,150,190,260,330,290,400,510}, m3.extractInts());
			
	 		try {
				m1.mulFrom((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.mulFrom(m2).extractFloats();
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
		try(final IntRealMatrix	m1 = new IntRealMatrix(3, 3);
			final IntRealMatrix	m2 = new IntRealMatrix(2, 2)) {
			Matrix	m3;
			
			m1.assign(1,2,3,4,5,6,7,8,9);
			m2.assign(10,20,30,40);
			
			m3 = m1.tensorMul(m2).done();
			
			Assert.assertEquals(6, m3.numberOfRows());
			Assert.assertEquals(6, m3.numberOfColumns());
			Assert.assertArrayEquals(new int[] {10,20,20,40,30,60,
												  30,40,60,80,90,120,
												  40,80,50,100,60,120,
												  120,160,150,200,180,240,
												  70,140,80,160,90,180,
												  210,280,240,320,270,360}, m3.extractInts());
	 		try {
				m1.tensorMul((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.tensorMul(m2).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
			
			m3 = m1.tensorMulFrom(m2).done();
			
			Assert.assertEquals(6, m3.numberOfRows());
			Assert.assertEquals(6, m3.numberOfColumns());
			Assert.assertArrayEquals(new int[] {10,20,30,20,40,60,
												  40,50,60,80,100,120,
												  70,80,90,140,160,180,
												  30,60,90,40,80,120,
												  120,150,180,160,200,240,
												  210,240,270,280,320,360}, m3.extractInts());
	 		try {
				m1.tensorMulFrom((Matrix)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {
				m1.tensorMulFrom(m2).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}

	@Test
	public void unaryTest() {
		try(final IntRealMatrix	m = new IntRealMatrix(3, 3)) {
				
			m.assign(1,2,3,4,5,6,7,8,10);
			Assert.assertArrayEquals(new int[] {1,4,7,2,5,8,3,6,10}, m.transpose().done().extractInts());
			try {
				m.transpose().extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}

			try {
				m.invert();
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}
			
			try{
				m.det();
				Assert.fail("Mandatory exception was not detected (unsupported operation)");
			} catch (UnsupportedOperationException exc) {
			}

			Assert.assertEquals(16, m.track().intValue());
		}
	}

	@Test
	public void aggregateTest() {
		try(final IntRealMatrix	m = new IntRealMatrix(3, 3)) {
			Matrix	res;
			
			m.assign(1,2,3,4,5,6,7,8,9);
			Assert.assertArrayEquals(new int[] {6, 15, 24}, m.aggregate(AggregateDirection.ByColumns, AggregateType.Sum).done().extractInts());
			Assert.assertArrayEquals(new int[] {12, 15, 18}, m.aggregate(AggregateDirection.ByRows, AggregateType.Sum).done().extractInts());
			Assert.assertArrayEquals(new int[] {45}, m.aggregate(AggregateDirection.Total, AggregateType.Sum).done().extractInts());

			Assert.assertArrayEquals(new int[] {1, 4, 7}, m.aggregate(AggregateDirection.ByColumns, AggregateType.Min).done().extractInts());
			Assert.assertArrayEquals(new int[] {1, 2, 3}, m.aggregate(AggregateDirection.ByRows, AggregateType.Min).done().extractInts());
			Assert.assertArrayEquals(new int[] {1}, m.aggregate(AggregateDirection.Total, AggregateType.Min).done().extractInts());

			Assert.assertArrayEquals(new int[] {3, 6, 9}, m.aggregate(AggregateDirection.ByColumns, AggregateType.Max).done().extractInts());
			Assert.assertArrayEquals(new int[] {7, 8, 9}, m.aggregate(AggregateDirection.ByRows, AggregateType.Max).done().extractInts());
			Assert.assertArrayEquals(new int[] {9}, m.aggregate(AggregateDirection.Total, AggregateType.Max).done().extractInts());

			Assert.assertArrayEquals(new int[] {2, 5, 8}, m.aggregate(AggregateDirection.ByColumns, AggregateType.Avg).done().extractInts());
			Assert.assertArrayEquals(new int[] {4, 5, 6}, m.aggregate(AggregateDirection.ByRows, AggregateType.Avg).done().extractInts());
			Assert.assertArrayEquals(new int[] {5}, m.aggregate(AggregateDirection.Total, AggregateType.Avg).done().extractInts());
			
			try{m.aggregate(null, AggregateType.Avg);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{m.aggregate(AggregateDirection.ByColumns, null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{m.aggregate(AggregateDirection.ByColumns, AggregateType.Avg).extractFloats();
				Assert.fail("Mandatory exception was not detected (done() call is missing)");
			} catch (IllegalStateException exc) {
			}
		}
	}
}
