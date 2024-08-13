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

	@Test
	public void realIntArithmeticTest() {
		try(final Matrix	m1 = new RIMatrixImpl(2, 4);
			final Matrix	m2 = new RIMatrixImpl(2, 4);
			final Matrix	m3 = new RIMatrixImpl(2, 4);
			final Matrix	m4 = new RIMatrixImpl(4, 2);
			final Matrix	m5 = new RIMatrixImpl(4, 1)) {
			
			m1.assign(1,2,3,4,5,6,7,8);
			m2.assign(1,2,3,4,5,6,7,8);
			m3.assign(-1,-2,-3,-4,-5,-6,-7,-8);
			m4.assign(1,2,3,4,5,6,7,8);
			m5.assign(1,2,3,4);

			// Add
			
			try(final Matrix	m = m1.add(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new int[] {2,4,6,8,10,12,14,16}, m.extractInts());
			}
			
			try {m1.add((int[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.add((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.add(new RLMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(1,2);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.add(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.add(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Add scalar
			
			try(final Matrix	m = m1.addValue(10).done()) {
				Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new int[] {11,12,13,14,15,16,17,18}, m.extractInts());
			}
			
			try {m1.addValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Subtract
			
			try(final Matrix	m = m1.subtract(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new int[] {0,0,0,0,0,0,0,0}, m.extractInts());
			}
			
			try {m1.subtract((int[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtract((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtract(new RLMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(1,2);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtract(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtract(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Subtract scalar
			
			try(final Matrix	m = m1.subtractValue(-10).done()) {
				Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new int[] {11,12,13,14,15,16,17,18}, m.extractInts());
			}
			
			try {m1.subtractValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Inverted subtract
			
			try(final Matrix	m = m1.subtractFrom(m3).done()) {
				Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new int[] {-2,-4,-6,-8,-10,-12,-14,-16}, m.extractInts());
			}
			
			try {m1.subtractFrom((int[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtractFrom((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtractFrom(new RLMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(1,2);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFrom(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFrom(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Inverted subtract scalar
			
			try(final Matrix	m = m1.subtractFromValue(10).done()) {
				Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new int[] {9,8,7,6,5,4,3,2}, m.extractInts());
			}
			
			try {m1.subtractFromValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Hadamard multiplication
			
			try(final Matrix	m = m1.mulHadamard(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new int[] {1,4,9,16,25,36,49,64}, m.extractInts());
			}
			
			try {m1.mulHadamard((int[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulHadamard(new RLMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(1,2);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulHadamard(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulHadamard(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard multiplication scalar
			
			try(final Matrix	m = m1.mulValue(10).done()) {
				Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new int[] {10,20,30,40,50,60,70,80}, m.extractInts());
			}
			
			try {m1.mulValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard "division"
			
			try(final Matrix	m = m1.mulInvHadamard(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new int[] {1,1,1,1,1,1,1,1}, m.extractInts());
			}
			
			try {m1.mulInvHadamard((int[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvHadamard(new RLMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(1,2);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvHadamard(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvHadamard(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard "division" scalar
			
			try(final Matrix	m = m1.divValue(2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new int[] {0,1,1,2,2,3,3,4}, m.extractInts());
			}
			
			try {m1.divValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Invert Hadamard "division"
			
			try(final Matrix	m = m1.mulInvFromHadamard(m3).done()) {
				Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new int[] {-1,-1,-1,-1,-1,-1,-1,-1}, m.extractInts());
			}
			
			try {m1.mulInvFromHadamard((int[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvFromHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvFromHadamard(new RLMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(1,2);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvFromHadamard(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvFromHadamard(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Invert Hadamard "division" scalar
			
			try(final Matrix	m = m1.divFromValue(100).done()) {
				Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new int[] {100,50,33,25,20,16,14,12}, m.extractInts());
			}
			
			try {m1.divFromValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			try(final Matrix	m = m1.transpose().done()) {
				Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
				Assert.assertEquals(4, m.numberOfRows());
				Assert.assertEquals(2, m.numberOfColumns());
				Assert.assertArrayEquals(new int[] {1,5,2,6,3,7,4,8}, m.extractInts());
			}
			
			try(final Matrix	m = m5.transpose().done()) {
				Assert.assertEquals(Matrix.Type.REAL_INT, m.getType());
				Assert.assertEquals(1, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new int[] {1,2,3,4}, m.extractInts());
			}
		}
	}

	@Test
	public void realLongArithmeticTest() {
		try(final Matrix	m1 = new RLMatrixImpl(2, 4);
			final Matrix	m2 = new RLMatrixImpl(2, 4);
			final Matrix	m3 = new RLMatrixImpl(2, 4);
			final Matrix	m4 = new RLMatrixImpl(4, 2);
			final Matrix	m5 = new RLMatrixImpl(4, 1)) {
			
			m1.assign(1L,2L,3L,4L,5L,6L,7L,8L);
			m2.assign(1L,2L,3L,4L,5L,6L,7L,8L);
			m3.assign(-1L,-2L,-3L,-4L,-5L,-6L,-7L,-8L);
			m4.assign(1L,2L,3L,4L,5L,6L,7L,8L);
			m5.assign(1L,2L,3L,4L);

			// Add
			
			try(final Matrix	m = m1.add(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new long[] {2,4,6,8,10,12,14,16}, m.extractLongs());
			}
			
			try {m1.add((long[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.add((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.add(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(1L,2L);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.add(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.add(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Add scalar
			
			try(final Matrix	m = m1.addValue(10L).done()) {
				Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new long[] {11,12,13,14,15,16,17,18}, m.extractLongs());
			}
			
			try {m1.addValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Subtract
			
			try(final Matrix	m = m1.subtract(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new long[] {0,0,0,0,0,0,0,0}, m.extractLongs());
			}
			
			try {m1.subtract((long[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtract((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtract(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(1L,2L);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtract(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtract(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Subtract scalar
			
			try(final Matrix	m = m1.subtractValue(-10L).done()) {
				Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new long[] {11,12,13,14,15,16,17,18}, m.extractLongs());
			}
			
			try {m1.subtractValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Inverted subtract
			
			try(final Matrix	m = m1.subtractFrom(m3).done()) {
				Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new long[] {-2,-4,-6,-8,-10,-12,-14,-16}, m.extractLongs());
			}
			
			try {m1.subtractFrom((long[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtractFrom((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtractFrom(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(1L,2L);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFrom(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFrom(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Inverted subtract scalar
			
			try(final Matrix	m = m1.subtractFromValue(10L).done()) {
				Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new long[] {9,8,7,6,5,4,3,2}, m.extractLongs());
			}
			
			try {m1.subtractFromValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Hadamard multiplication
			
			try(final Matrix	m = m1.mulHadamard(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new long[] {1,4,9,16,25,36,49,64}, m.extractLongs());
			}
			
			try {m1.mulHadamard((long[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(1L,2L);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulHadamard(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulHadamard(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard multiplication scalar
			
			try(final Matrix	m = m1.mulValue(10L).done()) {
				Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new long[] {10,20,30,40,50,60,70,80}, m.extractLongs());
			}
			
			try {m1.mulValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard "division"
			
			try(final Matrix	m = m1.mulInvHadamard(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new long[] {1,1,1,1,1,1,1,1}, m.extractLongs());
			}
			
			try {m1.mulInvHadamard((long[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(1L,2L);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvHadamard(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvHadamard(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard "division" scalar
			
			try(final Matrix	m = m1.divValue(2L).done()) {
				Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new long[] {0,1,1,2,2,3,3,4}, m.extractLongs());
			}
			
			try {m1.divValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Invert Hadamard "division"
			
			try(final Matrix	m = m1.mulInvFromHadamard(m3).done()) {
				Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new long[] {-1,-1,-1,-1,-1,-1,-1,-1}, m.extractLongs());
			}
			
			try {m1.mulInvFromHadamard((long[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvFromHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvFromHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(1L,2L);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvFromHadamard(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvFromHadamard(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Invert Hadamard "division" scalar
			
			try(final Matrix	m = m1.divFromValue(100L).done()) {
				Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new long[] {100,50,33,25,20,16,14,12}, m.extractLongs());
			}
			
			try {m1.divFromValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			try(final Matrix	m = m1.transpose().done()) {
				Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
				Assert.assertEquals(4, m.numberOfRows());
				Assert.assertEquals(2, m.numberOfColumns());
				Assert.assertArrayEquals(new long[] {1,5,2,6,3,7,4,8}, m.extractLongs());
			}
			
			try(final Matrix	m = m5.transpose().done()) {
				Assert.assertEquals(Matrix.Type.REAL_LONG, m.getType());
				Assert.assertEquals(1, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new long[] {1,2,3,4}, m.extractLongs());
			}
		}
	}
	
	@Test
	public void realFloatArithmeticTest() {
		try(final Matrix	m1 = new RFMatrixImpl(2, 4);
			final Matrix	m2 = new RFMatrixImpl(2, 4);
			final Matrix	m3 = new RFMatrixImpl(2, 4);
			final Matrix	m4 = new RFMatrixImpl(4, 2);
			final Matrix	m5 = new RFMatrixImpl(4, 1)) {
			
			m1.assign(1f,2f,3f,4f,5f,6f,7f,8f);
			m2.assign(1f,2f,3f,4f,5f,6f,7f,8f);
			m3.assign(-1f,-2f,-3f,-4f,-5f,-6f,-7f,-8f);
			m4.assign(1f,2f,3f,4f,5f,6f,7f,8f);
			m5.assign(1f,2f,3f,4f);

			// Add
			
			try(final Matrix	m = m1.add(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {2,4,6,8,10,12,14,16}, m.extractFloats(), 0.001f);
			}
			
			try {m1.add((float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.add((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.add(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(1f,2f);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.add(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.add(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Add scalar
			
			try(final Matrix	m = m1.addValue(10f).done()) {
				Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {11,12,13,14,15,16,17,18}, m.extractFloats(), 0.001f);
			}
			
			try {m1.addValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Subtract
			
			try(final Matrix	m = m1.subtract(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {0,0,0,0,0,0,0,0}, m.extractFloats(), 0.001f);
			}
			
			try {m1.subtract((float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtract((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtract(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(1f,2f);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtract(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtract(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Subtract scalar
			
			try(final Matrix	m = m1.subtractValue(-10f).done()) {
				Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {11,12,13,14,15,16,17,18}, m.extractFloats(), 0.001f);
			}
			
			try {m1.subtractValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Inverted subtract
			
			try(final Matrix	m = m1.subtractFrom(m3).done()) {
				Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {-2,-4,-6,-8,-10,-12,-14,-16}, m.extractFloats(), 0.001f);
			}
			
			try {m1.subtractFrom((float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtractFrom((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtractFrom(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(1f,2f);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFrom(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFrom(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Inverted subtract scalar
			
			try(final Matrix	m = m1.subtractFromValue(10f).done()) {
				Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {9,8,7,6,5,4,3,2}, m.extractFloats(), 0.001f);
			}
			
			try {m1.subtractFromValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Hadamard multiplication
			
			try(final Matrix	m = m1.mulHadamard(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {1,4,9,16,25,36,49,64}, m.extractFloats(), 0.001f);
			}
			
			try {m1.mulHadamard((float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(1f,2f);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulHadamard(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard multiplication scalar
			
			try(final Matrix	m = m1.mulValue(10f).done()) {
				Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {10,20,30,40,50,60,70,80}, m.extractFloats(), 0.001f);
			}
			
			try {m1.mulValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard "division"
			
			try(final Matrix	m = m1.mulInvHadamard(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {1,1,1,1,1,1,1,1}, m.extractFloats(), 0.001f);
			}
			
			try {m1.mulInvHadamard((float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(1f,2f);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvHadamard(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard "division" scalar
			
			try(final Matrix	m = m1.divValue(2f).done()) {
				Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {0.5f,1f,1.5f,2f,2.5f,3f,3.5f,4f}, m.extractFloats(), 0.001f);
			}
			
			try {m1.divValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Invert Hadamard "division"
			
			try(final Matrix	m = m1.mulInvFromHadamard(m3).done()) {
				Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {-1,-1,-1,-1,-1,-1,-1,-1}, m.extractFloats(), 0.001f);
			}
			
			try {m1.mulInvFromHadamard((float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvFromHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvFromHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(1f,2f);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvFromHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvFromHadamard(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Invert Hadamard "division" scalar
			
			try(final Matrix	m = m1.divFromValue(100f).done()) {
				Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {100,50,33.3333f,25,20,16.6666f,14.2857f,12.5f}, m.extractFloats(), 0.001f);
			}
			
			try {m1.divFromValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			try(final Matrix	m = m1.transpose().done()) {
				Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
				Assert.assertEquals(4, m.numberOfRows());
				Assert.assertEquals(2, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {1,5,2,6,3,7,4,8}, m.extractFloats(), 0.001f);
			}
			
			try(final Matrix	m = m5.transpose().done()) {
				Assert.assertEquals(Matrix.Type.REAL_FLOAT, m.getType());
				Assert.assertEquals(1, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {1,2,3,4}, m.extractFloats(), 0.001f);
			}
		}
	}

	@Test
	public void realDoubleArithmeticTest() {
		try(final Matrix	m1 = new RDMatrixImpl(2, 4);
			final Matrix	m2 = new RDMatrixImpl(2, 4);
			final Matrix	m3 = new RDMatrixImpl(2, 4);
			final Matrix	m4 = new RDMatrixImpl(4, 2);
			final Matrix	m5 = new RDMatrixImpl(4, 1)) {
			
			m1.assign(1d,2d,3d,4d,5d,6d,7d,8d);
			m2.assign(1d,2d,3d,4d,5d,6d,7d,8d);
			m3.assign(-1d,-2d,-3d,-4d,-5d,-6d,-7d,-8d);
			m4.assign(1d,2d,3d,4d,5d,6d,7d,8d);
			m5.assign(1d,2d,3d,4d);

			// Add
			
			try(final Matrix	m = m1.add(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {2,4,6,8,10,12,14,16}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.add((double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.add((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.add(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(1d,2d);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.add(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.add(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Add scalar
			
			try(final Matrix	m = m1.addValue(10d).done()) {
				Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {11,12,13,14,15,16,17,18}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.addValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Subtract
			
			try(final Matrix	m = m1.subtract(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {0,0,0,0,0,0,0,0}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.subtract((double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtract((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtract(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(1d,2d);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtract(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtract(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Subtract scalar
			
			try(final Matrix	m = m1.subtractValue(-10d).done()) {
				Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {11,12,13,14,15,16,17,18}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.subtractValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Inverted subtract
			
			try(final Matrix	m = m1.subtractFrom(m3).done()) {
				Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {-2,-4,-6,-8,-10,-12,-14,-16}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.subtractFrom((double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtractFrom((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtractFrom(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(1d,2d);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFrom(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFrom(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Inverted subtract scalar
			
			try(final Matrix	m = m1.subtractFromValue(10d).done()) {
				Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {9,8,7,6,5,4,3,2}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.subtractFromValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Hadamard multiplication
			
			try(final Matrix	m = m1.mulHadamard(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {1,4,9,16,25,36,49,64}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.mulHadamard((double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(1d,2d);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulHadamard(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard multiplication scalar
			
			try(final Matrix	m = m1.mulValue(10d).done()) {
				Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {10,20,30,40,50,60,70,80}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.mulValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard "division"
			
			try(final Matrix	m = m1.mulInvHadamard(m2).done()) {
				Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {1,1,1,1,1,1,1,1}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.mulInvHadamard((double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(1d,2d);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvHadamard(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard "division" scalar
			
			try(final Matrix	m = m1.divValue(2d).done()) {
				Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {0.5f,1f,1.5f,2f,2.5f,3f,3.5f,4f}, m.extractDoubles(), 0.001f);
			}
			
			try {m1.divValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Invert Hadamard "division"
			
			try(final Matrix	m = m1.mulInvFromHadamard(m3).done()) {
				Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {-1,-1,-1,-1,-1,-1,-1,-1}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.mulInvFromHadamard((double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvFromHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvFromHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(1d,2d);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvFromHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvFromHadamard(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Invert Hadamard "division" scalar
			
			try(final Matrix	m = m1.divFromValue(100d).done()) {
				Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {100,50,33.3333f,25,20,16.6666f,14.2857f,12.5f}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.divFromValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			try(final Matrix	m = m1.transpose().done()) {
				Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
				Assert.assertEquals(4, m.numberOfRows());
				Assert.assertEquals(2, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {1,5,2,6,3,7,4,8}, m.extractDoubles(), 0.001d);
			}
			
			try(final Matrix	m = m5.transpose().done()) {
				Assert.assertEquals(Matrix.Type.REAL_DOUBLE, m.getType());
				Assert.assertEquals(1, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {1,2,3,4}, m.extractDoubles(), 0.001d);
			}
		}
	}

	@Test
	public void complexFloatArithmeticTest() {
		try(final Matrix	m1 = new CFMatrixImpl(2, 4);
			final Matrix	m2 = new CFMatrixImpl(2, 4);
			final Matrix	m3 = new CFMatrixImpl(2, 4);
			final Matrix	m4 = new CFMatrixImpl(4, 2);
			final Matrix	m5 = new CFMatrixImpl(4, 1)) {
			
			m1.assign(1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f,7f,0f,8f,0f);
			m2.assign(1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f,7f,0f,8f,0f);
			m3.assign(-1f,0f,-2f,0f,-3f,0f,-4f,0f,-5f,0f,-6f,0f,-7f,0f,-8f,0f);
			m4.assign(1f,0f,2f,0f,3f,0f,4f,0f,5f,0f,6f,0f,7f,0f,8f,0f);
			m5.assign(1f,0f,2f,0f,3f,0f,4f,0f);

			// Add
			
			try(final Matrix	m = m1.add(m2).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {2,0,4,0,6,0,8,0,10,0,12,0,14,0,16,0}, m.extractFloats(), 0.001f);
			}
			
			try {m1.add((float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.add((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.add(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(1f,2f);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.add(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.add(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Add scalar
			
			try(final Matrix	m = m1.addValue(10f, 10f).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {11,10,12,10,13,10,14,10,15,10,16,10,17,10,18,10}, m.extractFloats(), 0.001f);
			}
			
			try {m1.addValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Subtract
			
			try(final Matrix	m = m1.subtract(m2).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, m.extractFloats(), 0.001f);
			}
			
			try {m1.subtract((float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtract((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtract(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(1f,2f);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtract(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtract(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Subtract scalar
			
			try(final Matrix	m = m1.subtractValue(-10f, 10f).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {11,-10,12,-10,13,-10,14,-10,15,-10,16,-10,17,-10,18,-10}, m.extractFloats(), 0.001f);
			}
			
			try {m1.subtractValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Inverted subtract
			
			try(final Matrix	m = m1.subtractFrom(m3).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {-2,0,-4,0,-6,0,-8,0,-10,0,-12,0,-14,0,-16,0}, m.extractFloats(), 0.001f);
			}
			
			try {m1.subtractFrom((float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtractFrom((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtractFrom(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(1f,2f);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFrom(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFrom(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Inverted subtract scalar
			
			try(final Matrix	m = m1.subtractFromValue(10f, 10f).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {9,10,8,10,7,10,6,10,5,10,4,10,3,10,2,10}, m.extractFloats(), 0.001f);
			}
			
			try {m1.subtractFromValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Hadamard multiplication
			
			try(final Matrix	m = m1.mulHadamard(m2).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {1,0,4,0,9,0,16,0,25,0,36,0,49,0,64,0}, m.extractFloats(), 0.001f);
			}
			
			try {m1.mulHadamard((float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(1f,2f);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulHadamard(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard multiplication scalar
			
			try(final Matrix	m = m1.mulValue(10f, 10f).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {10,10,20,20,30,30,40,40,50,50,60,60,70,70,80,80}, m.extractFloats(), 0.001f);
			}
			
			try {m1.mulValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard "division"
			
			try(final Matrix	m = m1.mulInvHadamard(m2).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0}, m.extractFloats(), 0.001f);
			}
			
			try {m1.mulInvHadamard((float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(1f,2f);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvHadamard(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard "division" scalar
			
			try(final Matrix	m = m1.divValue(2f, 2f).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {0.25f,-0.25f,0.5f,-0.5f,0.75f,-0.75f,1f,-1f,1.25f,-1.25f,1.5f,-1.5f,1.75f,-1.75f,2f,-2f}, m.extractFloats(), 0.001f);
			}
			
			try {m1.divValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Invert Hadamard "division"
			
			try(final Matrix	m = m1.mulInvFromHadamard(m3).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {-1,0,-1,0,-1,0,-1,0,-1,0,-1,0,-1,0,-1,0}, m.extractFloats(), 0.001f);
			}
			
			try {m1.mulInvFromHadamard((float[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvFromHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvFromHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(1f,2f);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvFromHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvFromHadamard(1d,2d,3d,4d,5d,6d,7d,8d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Invert Hadamard "division" scalar
			
			try(final Matrix	m = m1.divFromValue(100f, 100f).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {100,100,50,50,33.3333f,33.3333f,25,25,20,20,16.6666f,16.6666f,14.2857f,14.2857f,12.5f,12.5f}, m.extractFloats(), 0.001f);
			}
			
			try {m1.divFromValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1d, 0d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			try(final Matrix	m = m1.transpose().done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
				Assert.assertEquals(4, m.numberOfRows());
				Assert.assertEquals(2, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {1,0,5,0,2,0,6,0,3,0,7,0,4,0,8,0}, m.extractFloats(), 0.001f);
			}
			
			try(final Matrix	m = m5.transpose().done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_FLOAT, m.getType());
				Assert.assertEquals(1, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new float[] {1,0,2,0,3,0,4,0}, m.extractFloats(), 0.001f);
			}
		}
	}

	@Test
	public void complexDoubleArithmeticTest() {
		try(final Matrix	m1 = new CDMatrixImpl(2, 4);
			final Matrix	m2 = new CDMatrixImpl(2, 4);
			final Matrix	m3 = new CDMatrixImpl(2, 4);
			final Matrix	m4 = new CDMatrixImpl(4, 2);
			final Matrix	m5 = new CDMatrixImpl(4, 1)) {
			
			m1.assign(1d,0d,2d,0d,3d,0d,4d,0d,5d,0d,6d,0d,7d,0d,8d,0d);
			m2.assign(1d,0d,2d,0d,3d,0d,4d,0d,5d,0d,6d,0d,7d,0d,8d,0d);
			m3.assign(-1d,0d,-2d,0d,-3d,0d,-4d,0d,-5d,0d,-6d,0d,-7d,0d,-8d,0d);
			m4.assign(1d,0d,2d,0d,3d,0d,4d,0d,5d,0d,6d,0d,7d,0d,8d,0d);
			m5.assign(1d,0d,2d,0d,3d,0d,4d,0d);

			// Add
			
			try(final Matrix	m = m1.add(m2).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {2,0,4,0,6,0,8,0,10,0,12,0,14,0,16,0}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.add((double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.add((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.add(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(1d,2d);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.add(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.add(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.add(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Add scalar
			
			try(final Matrix	m = m1.addValue(10d, 10d).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {11,10,12,10,13,10,14,10,15,10,16,10,17,10,18,10}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.addValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.addValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Subtract
			
			try(final Matrix	m = m1.subtract(m2).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.subtract((double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtract((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtract(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(1d,2d);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtract(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtract(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtract(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Subtract scalar
			
			try(final Matrix	m = m1.subtractValue(-10d, 10d).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {11,-10,12,-10,13,-10,14,-10,15,-10,16,-10,17,-10,18,-10}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.subtractValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Inverted subtract
			
			try(final Matrix	m = m1.subtractFrom(m3).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {-2,0,-4,0,-6,0,-8,0,-10,0,-12,0,-14,0,-16,0}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.subtractFrom((double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtractFrom((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.subtractFrom(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(1d,2d);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.subtractFrom(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFrom(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFrom(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Inverted subtract scalar
			
			try(final Matrix	m = m1.subtractFromValue(10d, 10d).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {9,10,8,10,7,10,6,10,5,10,4,10,3,10,2,10}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.subtractFromValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.subtractFromValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			// Hadamard multiplication
			
			try(final Matrix	m = m1.mulHadamard(m2).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {1,0,4,0,9,0,16,0,25,0,36,0,49,0,64,0}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.mulHadamard((double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(1d,2d);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulHadamard(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard multiplication scalar
			
			try(final Matrix	m = m1.mulValue(10d, 10d).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {10,10,20,20,30,30,40,40,50,50,60,60,70,70,80,80}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.mulValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard "division"
			
			try(final Matrix	m = m1.mulInvHadamard(m2).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.mulInvHadamard((double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(1d,2d);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvHadamard(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Hadamard "division" scalar
			
			try(final Matrix	m = m1.divValue(2d, 2d).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {0.25f,-0.25f,0.5f,-0.5f,0.75f,-0.75f,1f,-1f,1.25f,-1.25f,1.5f,-1.5f,1.75f,-1.75f,2f,-2f}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.divValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Invert Hadamard "division"
			
			try(final Matrix	m = m1.mulInvFromHadamard(m3).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {-1,0,-1,0,-1,0,-1,0,-1,0,-1,0,-1,0,-1,0}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.mulInvFromHadamard((double[])null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvFromHadamard((Matrix)null);
				Assert.fail("Mandatory exceptions was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {m1.mulInvFromHadamard(new RIMatrixImpl(2, 4));
				Assert.fail("Mandatory exceptions was not detected (1-st argument different type)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(m4);
				Assert.fail("Mandatory exceptions was not detected (1-st argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(1d,2d);
				Assert.fail("Mandatory exceptions was not detected (1-nd argument different size)");
			} catch (IllegalArgumentException exc) {
			}
			try {m1.mulInvFromHadamard(1,2,3,4,5,6,7,8);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvFromHadamard(1L,2L,3L,4L,5L,6L,7L,8L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.mulInvFromHadamard(1f,2f,3f,4f,5f,6f,7f,8f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}

			// Invert Hadamard "division" scalar
			
			try(final Matrix	m = m1.divFromValue(100d, 100d).done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
				Assert.assertEquals(2, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {100,100,50,50,33.3333f,33.3333f,25,25,20,20,16.6666f,16.6666f,14.2857f,14.2857f,12.5f,12.5f}, m.extractDoubles(), 0.001d);
			}
			
			try {m1.divFromValue(1);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1L);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1f, 0f);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			try {m1.divFromValue(1d);
				Assert.fail("Mandatory exceptions was not detected (unsupported op)");
			} catch (UnsupportedOperationException exc) {
			}
			
			try(final Matrix	m = m1.transpose().done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
				Assert.assertEquals(4, m.numberOfRows());
				Assert.assertEquals(2, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {1,0,5,0,2,0,6,0,3,0,7,0,4,0,8,0}, m.extractDoubles(), 0.001d);
			}
			
			try(final Matrix	m = m5.transpose().done()) {
				Assert.assertEquals(Matrix.Type.COMPLEX_DOUBLE, m.getType());
				Assert.assertEquals(1, m.numberOfRows());
				Assert.assertEquals(4, m.numberOfColumns());
				Assert.assertArrayEquals(new double[] {1,0,2,0,3,0,4,0}, m.extractDoubles(), 0.001f);
			}
		}
	}
}
