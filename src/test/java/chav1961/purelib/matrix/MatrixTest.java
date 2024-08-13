package chav1961.purelib.matrix;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.Piece;

public class MatrixTest {
	@Test
	public void basicRealIntTest() {
		try(final Matrix	m = new RIMatrixImpl(2, 4);
			final Matrix	mEq = new RIMatrixImpl(2, 4);
			final Matrix	mNe = new RIMatrixImpl(2, 4);
			final Matrix	mBlock = new RIMatrixImpl(2, 2);) {
			Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
			Assert.assertEquals(2, m.numberOfRows());
			Assert.assertEquals(4, m.numberOfColumns());
			
			Assert.assertEquals(8, m.extractInts().length);
			
			try {m.extractLongs();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractFloats();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractDoubles();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			m.assign(1,2,3,4,5,6,7,8);
			Assert.assertArrayEquals(new int[] {1,2,3,4,5,6,7,8}, m.extractInts());

			mEq.assign(1,2,3,4,5,6,7,8);
			mNe.assign(1,2,3,4,5,6,7,0);
			
			Assert.assertTrue(m.deepEquals(m));
			Assert.assertTrue(m.deepEquals(mEq));
			Assert.assertFalse(m.deepEquals(null));
			Assert.assertFalse(m.deepEquals(mBlock));
			Assert.assertFalse(m.deepEquals(mNe));
			
			mNe.assign(m);
			Assert.assertTrue(m.deepEquals(mNe));

			Assert.assertFalse(Utils.checkEmptyOrNullString(m.toHumanReadableString()));
			
			try {m.assign((int[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(1,2,3,4,5,6,7);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(mBlock);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			Assert.assertArrayEquals(new int[] {1,2,5,6}, m.extractInts(Piece.of(0, 0, 2, 2)));
			
			try {m.extractInts(null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.extractLongs(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractFloats(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractDoubles(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.assign(Piece.of(0, 0, 2, 2), 10, 20, 50, 60);
			Assert.assertArrayEquals(new int[] {10,20,3,4,50,60,7,8}, m.extractInts());

			mBlock.assign(100, 200, 500, 600);
			m.assign(Piece.of(0, 0, 2, 2), mBlock);
			Assert.assertArrayEquals(new int[] {100,200,3,4,500,600,7,8}, m.extractInts());
			
			try {m.assign(null, 1, 2, 5, 6);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), (int[])null);
				Assert.fail("Mandatory exceptions was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), (Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1, 2, 5);
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1L, 2L, 5L, 6L);
			Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1L, 2L, 5L, 6L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1f, 2f, 5f, 6f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1d, 2d, 5d, 6d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.fill(10);
			Assert.assertArrayEquals(new int[] {10,10,10,10,10,10,10,10}, m.extractInts());

			try {m.fill(10L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10f,0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10d,0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.fill(Piece.of(0, 0, 2, 2), 20);
			Assert.assertArrayEquals(new int[] {20,20,10,10,20,20,10,10}, m.extractInts());

			try {m.fill(null, 10);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10f,0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10d,0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
		}
		
		try(final Matrix	m = new RIMatrixImpl(1, 4);
			final Matrix	mEq = new RIMatrixImpl(1, 4);
			final Matrix	mBlock = new RIMatrixImpl(1, 2)) {
			Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
			Assert.assertEquals(1, m.numberOfRows());
			Assert.assertEquals(4, m.numberOfColumns());
			
			Assert.assertEquals(4, m.extractInts().length);
			
			m.assign(1,2,3,4);
			Assert.assertArrayEquals(new int[] {1,2}, m.extractInts(Piece.of(0, 0, 1, 2)));
			
			m.assign(Piece.of(0, 0, 1, 2), 10,20);
			Assert.assertArrayEquals(new int[] {10,20,3,4}, m.extractInts());
			
			m.fill(Piece.of(0, 0, 1, 2), 100);
			Assert.assertArrayEquals(new int[] {100,100,3,4}, m.extractInts());

			mEq.assign(10,20,30,40);
			mBlock.assign(100,200);
			
			m.assign(mEq);
			Assert.assertArrayEquals(new int[] {10,20,30,40}, m.extractInts());
			
			try {m.assign(new RLMatrixImpl(1, 1));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(mBlock);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			
			m.assign(Piece.of(0, 0, 1, 2), mBlock);
			Assert.assertArrayEquals(new int[] {100,200,30,40}, m.extractInts());

			try {m.assign(Piece.of(0, 0, 1, 2), new RLMatrixImpl(1, 1));
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(0, 0, 1, 2), mEq);
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
		}		
	}

	@Test
	public void basicRealLongTest() {
		try(final Matrix	m = new RLMatrixImpl(2, 4);
			final Matrix	mEq = new RLMatrixImpl(2, 4);
			final Matrix	mNe = new RLMatrixImpl(2, 4);
			final Matrix	mBlock = new RLMatrixImpl(2, 2);) {
			Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
			Assert.assertEquals(2, m.numberOfRows());
			Assert.assertEquals(4, m.numberOfColumns());
			
			Assert.assertEquals(8, m.extractLongs().length);
			
			try {m.extractInts();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractFloats();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractDoubles();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			m.assign(1L,2L,3L,4L,5L,6L,7L,8L);
			Assert.assertArrayEquals(new long[] {1,2,3,4,5,6,7,8}, m.extractLongs());

			mEq.assign(1L,2L,3L,4L,5L,6L,7L,8L);
			mNe.assign(1L,2L,3L,4L,5L,6L,7L,0L);
			
			Assert.assertTrue(m.deepEquals(m));
			Assert.assertTrue(m.deepEquals(mEq));
			Assert.assertFalse(m.deepEquals(null));
			Assert.assertFalse(m.deepEquals(mBlock));
			Assert.assertFalse(m.deepEquals(mNe));
			
			mNe.assign(m);
			Assert.assertTrue(m.deepEquals(mNe));

			Assert.assertFalse(Utils.checkEmptyOrNullString(m.toHumanReadableString()));
			
			try {m.assign((long[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(1L,2L,3L,4L,5L,6L,7L);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(mBlock);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			Assert.assertArrayEquals(new long[] {1,2,5,6}, m.extractLongs(Piece.of(0, 0, 2, 2)));
			
			try {m.extractLongs(null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.extractInts(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractFloats(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractDoubles(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.assign(Piece.of(0, 0, 2, 2), 10L, 20L, 50L, 60L);
			Assert.assertArrayEquals(new long[] {10,20,3,4,50,60,7,8}, m.extractLongs());

			mBlock.assign(100L, 200L, 500L, 600L);
			m.assign(Piece.of(0, 0, 2, 2), mBlock);
			Assert.assertArrayEquals(new long[] {100,200,3,4,500,600,7,8}, m.extractLongs());
			
			try {m.assign(null, 1L, 2L, 5L, 6L);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), (long[])null);
				Assert.fail("Mandatory exceptions was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), (Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1L, 2L, 5L);
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1, 2, 5, 6);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1f, 2f, 5f, 6f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1d, 2d, 5d, 6d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.fill(10L);
			Assert.assertArrayEquals(new long[] {10,10,10,10,10,10,10,10}, m.extractLongs());

			try {m.fill(null, 10L);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.fill(10);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10f,0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10d,0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.fill(Piece.of(0, 0, 2, 2), 20L);
			Assert.assertArrayEquals(new long[] {20,20,10,10,20,20,10,10}, m.extractLongs());

			try {m.fill(Piece.of(0, 0, 2, 2), 10);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10f,0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10d,0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
		}

		try(final Matrix	m = new RLMatrixImpl(1, 4);
			final Matrix	mEq = new RLMatrixImpl(1, 4);
			final Matrix	mBlock = new RLMatrixImpl(1, 2)) {
			Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
			Assert.assertEquals(1, m.numberOfRows());
			Assert.assertEquals(4, m.numberOfColumns());
			
			Assert.assertEquals(4, m.extractLongs().length);
			
			m.assign(1L,2L,3L,4L);
			Assert.assertArrayEquals(new long[] {1,2}, m.extractLongs(Piece.of(0, 0, 1, 2)));
			
			m.assign(Piece.of(0, 0, 1, 2), 10L, 20L);
			Assert.assertArrayEquals(new long[] {10,20,3,4}, m.extractLongs());
			
			m.fill(Piece.of(0, 0, 1, 2), 100L);
			Assert.assertArrayEquals(new long[] {100,100,3,4}, m.extractLongs());

			mEq.assign(10L,20L,30L,40L);
			mBlock.assign(100L,200L);
			
			m.assign(mEq);
			Assert.assertArrayEquals(new long[] {10,20,30,40}, m.extractLongs());
			
			try {m.assign(new RIMatrixImpl(1, 1));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(mBlock);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			
			m.assign(Piece.of(0, 0, 1, 2), mBlock);
			Assert.assertArrayEquals(new long[] {100,200,30,40}, m.extractLongs());

			try {m.assign(Piece.of(0, 0, 1, 2), new RIMatrixImpl(1, 1));
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(0, 0, 1, 2), mEq);
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
		}		
	}

	@Test
	public void basicRealFloatTest() {
		try(final Matrix	m = new RFMatrixImpl(2, 4);
			final Matrix	mEq = new RFMatrixImpl(2, 4);
			final Matrix	mNe = new RFMatrixImpl(2, 4);
			final Matrix	mBlock = new RFMatrixImpl(2, 2);) {
			Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
			Assert.assertEquals(2, m.numberOfRows());
			Assert.assertEquals(4, m.numberOfColumns());
			
			Assert.assertEquals(8, m.extractFloats().length);
			
			try {m.extractInts();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractLongs();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractDoubles();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			m.assign(1f,2f,3f,4f,5f,6f,7f,8f);
			Assert.assertArrayEquals(new float[] {1,2,3,4,5,6,7,8}, m.extractFloats(), 0.001f);

			mEq.assign(1f,2f,3f,4f,5f,6f,7f,8f);
			mNe.assign(1f,2f,3f,4f,5f,6f,7f,0f);
			
			Assert.assertTrue(m.deepEquals(m));
			Assert.assertTrue(m.deepEquals(mEq));
			Assert.assertFalse(m.deepEquals(null));
			Assert.assertFalse(m.deepEquals(mBlock));
			Assert.assertFalse(m.deepEquals(mNe));
			
			mNe.assign(m);
			Assert.assertTrue(m.deepEquals(mNe));

			Assert.assertFalse(Utils.checkEmptyOrNullString(m.toHumanReadableString()));
			
			try {m.assign((float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(1f,2f,3f,4f,5f,6f,7f);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(mBlock);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			Assert.assertArrayEquals(new float[] {1,2,5,6}, m.extractFloats(Piece.of(0, 0, 2, 2)), 0.001f);
			
			try {m.extractFloats(null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.extractInts(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractLongs(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractDoubles(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.assign(Piece.of(0, 0, 2, 2), 10f, 20f, 50f, 60f);
			Assert.assertArrayEquals(new float[] {10,20,3,4,50,60,7,8}, m.extractFloats(), 0.001f);

			mBlock.assign(100f, 200f, 500f, 600f);
			m.assign(Piece.of(0, 0, 2, 2), mBlock);
			Assert.assertArrayEquals(new float[] {100,200,3,4,500,600,7,8}, m.extractFloats(), 0.001f);
			
			try {m.assign(null, 1f, 2f, 5f, 6f);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), (float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), (Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1f, 2f, 5f);
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1, 2, 5, 6);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1L, 2L, 5L, 6L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1d, 2d, 5d, 6d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.fill(10f);
			Assert.assertArrayEquals(new float[] {10,10,10,10,10,10,10,10}, m.extractFloats(), 0.001f);

			try {m.fill(null, 10f);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.fill(10);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10f,0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10d,0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.fill(Piece.of(0, 0, 2, 2), 20f);
			Assert.assertArrayEquals(new float[] {20,20,10,10,20,20,10,10}, m.extractFloats(), 0.001f);

			try {m.fill(Piece.of(0, 0, 2, 2), 10);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10f,0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10d,0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
		}

		try(final Matrix	m = new RFMatrixImpl(1, 4);
			final Matrix	mEq = new RFMatrixImpl(1, 4);
			final Matrix	mBlock = new RFMatrixImpl(1, 2)) {
			Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
			Assert.assertEquals(1, m.numberOfRows());
			Assert.assertEquals(4, m.numberOfColumns());
			
			Assert.assertEquals(4, m.extractFloats().length);
			
			m.assign(1f,2f,3f,4f);
			Assert.assertArrayEquals(new float[] {1,2}, m.extractFloats(Piece.of(0, 0, 1, 2)), 0.001f);
			
			m.assign(Piece.of(0, 0, 1, 2), 10f, 20f);
			Assert.assertArrayEquals(new float[] {10,20,3,4}, m.extractFloats(), 0.001f);
			
			m.fill(Piece.of(0, 0, 1, 2), 100f);
			Assert.assertArrayEquals(new float[] {100,100,3,4}, m.extractFloats(), 0.001f);

			mEq.assign(10f,20f,30f,40f);
			mBlock.assign(100f,200f);
			
			m.assign(mEq);
			Assert.assertArrayEquals(new float[] {10,20,30,40}, m.extractFloats(), 0.001f);
			
			try {m.assign(new RIMatrixImpl(1, 1));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(mBlock);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			
			m.assign(Piece.of(0, 0, 1, 2), mBlock);
			Assert.assertArrayEquals(new float[] {100,200,30,40}, m.extractFloats(), 0.001f);

			try {m.assign(Piece.of(0, 0, 1, 2), new RIMatrixImpl(1, 1));
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(0, 0, 1, 2), mEq);
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
		}		
	}

	@Test
	public void basicRealDoubleTest() {
		try(final Matrix	m = new RDMatrixImpl(2, 4);
			final Matrix	mEq = new RDMatrixImpl(2, 4);
			final Matrix	mNe = new RDMatrixImpl(2, 4);
			final Matrix	mBlock = new RDMatrixImpl(2, 2);) {
			Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
			Assert.assertEquals(2, m.numberOfRows());
			Assert.assertEquals(4, m.numberOfColumns());
			
			Assert.assertEquals(8, m.extractDoubles().length);
			
			try {m.extractInts();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractLongs();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractFloats();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			m.assign(1d,2d,3d,4d,5d,6d,7d,8d);
			Assert.assertArrayEquals(new double[] {1,2,3,4,5,6,7,8}, m.extractDoubles(), 0.001d);

			mEq.assign(1d,2d,3d,4d,5d,6d,7d,8d);
			mNe.assign(1d,2d,3d,4d,5d,6d,7d,0d);
			
			Assert.assertTrue(m.deepEquals(m));
			Assert.assertTrue(m.deepEquals(mEq));
			Assert.assertFalse(m.deepEquals(null));
			Assert.assertFalse(m.deepEquals(mBlock));
			Assert.assertFalse(m.deepEquals(mNe));
			
			mNe.assign(m);
			Assert.assertTrue(m.deepEquals(mNe));

			Assert.assertFalse(Utils.checkEmptyOrNullString(m.toHumanReadableString()));
			
			try {m.assign((double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(1d,2d,3d,4d,5d,6d,7d);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(mBlock);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			Assert.assertArrayEquals(new double[] {1,2,5,6}, m.extractDoubles(Piece.of(0, 0, 2, 2)), 0.001d);
			
			try {m.extractDoubles(null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.extractInts(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractLongs(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractFloats(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.assign(Piece.of(0, 0, 2, 2), 10d, 20d, 50d, 60d);
			Assert.assertArrayEquals(new double[] {10,20,3,4,50,60,7,8}, m.extractDoubles(), 0.001d);

			mBlock.assign(100d, 200d, 500d, 600d);
			m.assign(Piece.of(0, 0, 2, 2), mBlock);
			Assert.assertArrayEquals(new double[] {100,200,3,4,500,600,7,8}, m.extractDoubles(), 0.001d);
			
			try {m.assign(null, 1d, 2d, 5d, 6d);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), (double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), (Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1d, 2d, 5d);
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1, 2, 5, 6);
			Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1L, 2L, 5L, 6L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1f, 2f, 5f, 6f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.fill(10d);
			Assert.assertArrayEquals(new double[] {10,10,10,10,10,10,10,10}, m.extractDoubles(), 0.001d);

			try {m.fill(10);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10f,0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10d,0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.fill(Piece.of(0, 0, 2, 2), 20d);
			Assert.assertArrayEquals(new double[] {20,20,10,10,20,20,10,10}, m.extractDoubles(), 0.001d);

			try {m.fill(null, 10d);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10f,0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10d,0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
		}
		
		try(final Matrix	m = new RDMatrixImpl(1, 4);
			final Matrix	mEq = new RDMatrixImpl(1, 4);
			final Matrix	mBlock = new RDMatrixImpl(1, 2)) {
			Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
			Assert.assertEquals(1, m.numberOfRows());
			Assert.assertEquals(4, m.numberOfColumns());
			
			Assert.assertEquals(4, m.extractDoubles().length);
			
			m.assign(1d,2d,3d,4d);
			Assert.assertArrayEquals(new double[] {1,2}, m.extractDoubles(Piece.of(0, 0, 1, 2)), 0.001d);
			
			m.assign(Piece.of(0, 0, 1, 2), 10d, 20d);
			Assert.assertArrayEquals(new double[] {10,20,3,4}, m.extractDoubles(), 0.001d);
			
			m.fill(Piece.of(0, 0, 1, 2), 100d);
			Assert.assertArrayEquals(new double[] {100,100,3,4}, m.extractDoubles(), 0.001d);

			mEq.assign(10d,20d,30d,40d);
			mBlock.assign(100d,200d);
			
			m.assign(mEq);
			Assert.assertArrayEquals(new double[] {10,20,30,40}, m.extractDoubles(), 0.001d);
			
			try {m.assign(new RLMatrixImpl(1, 1));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(mBlock);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			
			m.assign(Piece.of(0, 0, 1, 2), mBlock);
			Assert.assertArrayEquals(new double[] {100,200,30,40}, m.extractDoubles(), 0.001d);

			try {m.assign(Piece.of(0, 0, 1, 2), new RLMatrixImpl(1, 1));
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(0, 0, 1, 2), mEq);
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
		}		
	}

	@Test
	public void basicComplexFloatTest() {
		try(final Matrix	m = new CFMatrixImpl(2, 4);
			final Matrix	mEq = new CFMatrixImpl(2, 4);
			final Matrix	mNe = new CFMatrixImpl(2, 4);
			final Matrix	mBlock = new CFMatrixImpl(2, 2);) {
			Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
			Assert.assertEquals(2, m.numberOfRows());
			Assert.assertEquals(4, m.numberOfColumns());
			
			Assert.assertEquals(16, m.extractFloats().length);
			
			try {m.extractInts();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractLongs();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractDoubles();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			m.assign(1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f,7f,0f,8f,0f);
			Assert.assertArrayEquals(new float[] {1,0,2,0,3,0,4,0,5,0,6,0,7,0,8,0}, m.extractFloats(), 0.001f);

			mEq.assign(1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f,7f,0f,8f,0f);
			mNe.assign(1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f,7f,0f,0f,0f);
			
			Assert.assertTrue(m.deepEquals(m));
			Assert.assertTrue(m.deepEquals(mEq));
			Assert.assertFalse(m.deepEquals(null));
			Assert.assertFalse(m.deepEquals(mBlock));
			Assert.assertFalse(m.deepEquals(mNe));
			
			mNe.assign(m);
			Assert.assertTrue(m.deepEquals(mNe));

			Assert.assertFalse(Utils.checkEmptyOrNullString(m.toHumanReadableString()));
			
			try {m.assign((float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f,7f,0f);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(mBlock);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			Assert.assertArrayEquals(new float[] {1,0,2,0,5,0,6,0}, m.extractFloats(Piece.of(0, 0, 2, 2)), 0.001f);
			
			try {m.extractFloats(null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.extractInts(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractLongs(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractDoubles(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.assign(Piece.of(0, 0, 2, 2), 10f, 0f, 20f, 0f, 50f, 0f, 60f, 0f);
			Assert.assertArrayEquals(new float[] {10, 0, 20, 0, 3, 0, 4, 0, 50, 0, 60, 0, 7, 0, 8, 0}, m.extractFloats(), 0.001f);

			mBlock.assign(100f, 0f, 200f, 0f, 500f, 0f, 600f, 0f);
			m.assign(Piece.of(0, 0, 2, 2), mBlock);
			Assert.assertArrayEquals(new float[] {100,0,200,0,3,0,4,0,500,0,600,0,7,0,8,0}, m.extractFloats(), 0.001f);
			
			try {m.assign(null, 1f, 0f, 2f, 0f, 5f, 0f, 6f, 0f);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), (float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), (Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1f, 0f, 2f, 0f, 5f, 0f);
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1L, 2L, 5L, 6L);
			Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1d, 2d, 5d, 6d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.fill(10f, 0f);
			Assert.assertArrayEquals(new float[] {10,0,10,0,10,0,10,0,10,0,10,0,10,0,10,0}, m.extractFloats(), 0.001f);

			try {m.fill(10);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10d,0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.fill(Piece.of(0, 0, 2, 2), 20f, 0f);
			Assert.assertArrayEquals(new float[] {20,0,20,0,10,0,10,0,20,0,20,0,10,0,10,0}, m.extractFloats(), 0.001f);

			try {m.fill(null, 10f, 0f);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10d,0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
		}
		
		try(final Matrix	m = new CFMatrixImpl(1, 4);
			final Matrix	mEq = new CFMatrixImpl(1, 4);
			final Matrix	mBlock = new CFMatrixImpl(1, 2)) {
			Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
			Assert.assertEquals(1, m.numberOfRows());
			Assert.assertEquals(4, m.numberOfColumns());
			
			Assert.assertEquals(8, m.extractFloats().length);
			
			m.assign(1f,0f,2f,0f,3f,0f,4f,0f);
			Assert.assertArrayEquals(new float[] {1,0,2,0}, m.extractFloats(Piece.of(0, 0, 1, 2)), 0.001f);
			
			m.assign(Piece.of(0, 0, 1, 2), 10f, 0f, 20f, 0f);
			Assert.assertArrayEquals(new float[] {10,0,20,0,3,0,4,0}, m.extractFloats(), 0.001f);
			
			m.fill(Piece.of(0, 0, 1, 2), 100f, 0f);
			Assert.assertArrayEquals(new float[] {100,0,100,0,3,0,4,0}, m.extractFloats(), 0.001f);

			mEq.assign(10f,0f,20f,0f,30f,0f,40f,0f);
			mBlock.assign(100f,0f,200f,0f);
			
			m.assign(mEq);
			Assert.assertArrayEquals(new float[] {10,0,20,0,30,0,40,0}, m.extractFloats(), 0.001f);
			
			try {m.assign(new RLMatrixImpl(1, 1));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(mBlock);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			
			m.assign(Piece.of(0, 0, 1, 2), mBlock);
			Assert.assertArrayEquals(new float[] {100,0,200,0,30,0,40,0}, m.extractFloats(), 0.001f);

			try {m.assign(Piece.of(0, 0, 1, 2), new RLMatrixImpl(1, 1));
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(0, 0, 1, 2), mEq);
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
		}		
	}

	@Test
	public void basicComplexDoubleTest() {
		try(final Matrix	m = new CDMatrixImpl(2, 4);
			final Matrix	mEq = new CDMatrixImpl(2, 4);
			final Matrix	mNe = new CDMatrixImpl(2, 4);
			final Matrix	mBlock = new CDMatrixImpl(2, 2);) {
			Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
			Assert.assertEquals(2, m.numberOfRows());
			Assert.assertEquals(4, m.numberOfColumns());
			
			Assert.assertEquals(16, m.extractDoubles().length);
			
			try {m.extractInts();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractLongs();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractFloats();
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			m.assign(1d,0d,2d,0d,3d,0d,4d,0d,5d,0d,6d,0d,7d,0d,8d,0d);
			Assert.assertArrayEquals(new double[] {1,0,2,0,3,0,4,0,5,0,6,0,7,0,8,0}, m.extractDoubles(), 0.001d);

			mEq.assign(1d,0d,2d,0d,3d,0d,4d,0d,5d,0d,6d,0d,7d,0d,8d,0d);
			mNe.assign(1d,0d,2d,0d,3d,0d,4d,0d,5d,0d,6d,0d,7d,0d,0d,0d);
			
			Assert.assertTrue(m.deepEquals(m));
			Assert.assertTrue(m.deepEquals(mEq));
			Assert.assertFalse(m.deepEquals(null));
			Assert.assertFalse(m.deepEquals(mBlock));
			Assert.assertFalse(m.deepEquals(mNe));
			
			mNe.assign(m);
			Assert.assertTrue(m.deepEquals(mNe));

			Assert.assertFalse(Utils.checkEmptyOrNullString(m.toHumanReadableString()));
			
			try {m.assign((double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(1d,0d,2d,0d,3d,0d,4d,0d,5d,0d,6d,0d,7d,0d);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(mBlock);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			Assert.assertArrayEquals(new double[] {1,0,2,0,5,0,6,0}, m.extractDoubles(Piece.of(0, 0, 2, 2)), 0.001d);
			
			try {m.extractDoubles(null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.extractInts(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractLongs(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.extractFloats(Piece.of(0, 0, 2, 2));
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.assign(Piece.of(0, 0, 2, 2), 10d, 0d, 20d, 0d, 50d, 0d, 60d, 0d);
			Assert.assertArrayEquals(new double[] {10, 0, 20, 0, 3, 0, 4, 0, 50, 0, 60, 0, 7, 0, 8, 0}, m.extractDoubles(), 0.001d);

			mBlock.assign(100d, 0d, 200d, 0d, 500d, 0d, 600d, 0d);
			m.assign(Piece.of(0, 0, 2, 2), mBlock);
			Assert.assertArrayEquals(new double[] {100,0,200,0,3,0,4,0,500,0,600,0,7,0,8,0}, m.extractDoubles(), 0.001d);
			
			try {m.assign(null, 1d, 0d, 2d, 0d, 5d, 0d, 6d, 0d);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), (double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), (Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1d, 0d, 2d, 0d, 5d, 0d);
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1L, 2L, 5L, 6L);
			Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.assign(Piece.of(0, 0, 2, 2), 1f, 2f, 5f, 6f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.fill(10d, 0d);
			Assert.assertArrayEquals(new double[] {10,0,10,0,10,0,10,0,10,0,10,0,10,0,10,0}, m.extractDoubles(), 0.001d);

			try {m.fill(10);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(10d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			m.fill(Piece.of(0, 0, 2, 2), 20d, 0d);
			Assert.assertArrayEquals(new double[] {20,0,20,0,10,0,10,0,20,0,20,0,10,0,10,0}, m.extractDoubles(), 0.001d);

			try {m.fill(null, 10d, 0d);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m.fill(Piece.of(0, 0, 2, 2), 10d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
		}
		
		try(final Matrix	m = new CDMatrixImpl(1, 4);
			final Matrix	mEq = new CDMatrixImpl(1, 4);
			final Matrix	mBlock = new CDMatrixImpl(1, 2)) {
			Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
			Assert.assertEquals(1, m.numberOfRows());
			Assert.assertEquals(4, m.numberOfColumns());
			
			Assert.assertEquals(8, m.extractDoubles().length);
			
			m.assign(1d,0d,2d,0d,3d,0d,4d,0d);
			Assert.assertArrayEquals(new double[] {1,0,2,0}, m.extractDoubles(Piece.of(0, 0, 1, 2)), 0.001d);
			
			m.assign(Piece.of(0, 0, 1, 2), 10d, 0d, 20d, 0d);
			Assert.assertArrayEquals(new double[] {10,0,20,0,3,0,4,0}, m.extractDoubles(), 0.001d);
			
			m.fill(Piece.of(0, 0, 1, 2), 100d, 0d);
			Assert.assertArrayEquals(new double[] {100,0,100,0,3,0,4,0}, m.extractDoubles(), 0.001d);

			mEq.assign(10d,0d,20d,0d,30d,0d,40d,0d);
			mBlock.assign(100d,0d,200d,0d);
			
			m.assign(mEq);
			Assert.assertArrayEquals(new double[] {10,0,20,0,30,0,40,0}, m.extractDoubles(), 0.001d);
			
			try {m.assign(new RLMatrixImpl(1, 1));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(mBlock);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			
			m.assign(Piece.of(0, 0, 1, 2), mBlock);
			Assert.assertArrayEquals(new double[] {100,0,200,0,30,0,40,0}, m.extractDoubles(), 0.001d);

			try {m.assign(Piece.of(0, 0, 1, 2), new RLMatrixImpl(1, 1));
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m.assign(Piece.of(0, 0, 1, 2), mEq);
				Assert.fail("Mandatory exceptions was not detected (2-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
		}		
	}
}
