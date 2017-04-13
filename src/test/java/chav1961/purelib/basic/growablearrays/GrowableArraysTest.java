package chav1961.purelib.basic.growablearrays;


import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class GrowableArraysTest {
	private static final byte[]		BYTE_31 = new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
	private static final byte[]		BYTE_33 = new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33};
	private static final short[]	SHORT_31 = new short[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
	private static final short[]	SHORT_33 = new short[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33};
	private static final int[]		INT_31 = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
	private static final int[]		INT_33 = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33};
	private static final long[]		LONG_31 = new long[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
	private static final long[]		LONG_33 = new long[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33};
	private static final float[]	FLOAT_31 = new float[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
	private static final float[]	FLOAT_33 = new float[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33};
	private static final double[]	DOUBLE_31 = new double[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
	private static final double[]	DOUBLE_33 = new double[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33};
	private static final char[]		CHAR_31 = new char[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
	private static final char[]		CHAR_33 = new char[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33};
	
	@Test
	public void byteArrayTest() {
		GrowableByteArray	array;
		
		byteArrayTest(array = new GrowableByteArray(true,5)); 		
		Assert.assertTrue(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());
		
		byteArrayTest(array = new GrowableByteArray(false,5));
		Assert.assertFalse(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());

		try{new GrowableByteArray(false,0);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
		try{new GrowableByteArray(false,100);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void shortArrayTest() {
		GrowableShortArray	array;
		
		shortArrayTest(array = new GrowableShortArray(true,5)); 		
		Assert.assertTrue(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());
		
		shortArrayTest(array = new GrowableShortArray(false,5));
		Assert.assertFalse(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());

		try{new GrowableShortArray(false,0);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
		try{new GrowableShortArray(false,100);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void intArrayTest() {
		GrowableIntArray	array;
		
		intArrayTest(array = new GrowableIntArray(true,5)); 		
		Assert.assertTrue(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());
		
		intArrayTest(array = new GrowableIntArray(false,5));
		Assert.assertFalse(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());

		try{new GrowableIntArray(false,0);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
		try{new GrowableIntArray(false,100);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void longArrayTest() {
		GrowableLongArray	array;
		
		longArrayTest(array = new GrowableLongArray(true,5)); 		
		Assert.assertTrue(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());
		
		longArrayTest(array = new GrowableLongArray(false,5));
		Assert.assertFalse(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());

		try{new GrowableLongArray(false,0);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
		try{new GrowableLongArray(false,100);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void floatArrayTest() {
		GrowableFloatArray	array;
		
		floatArrayTest(array = new GrowableFloatArray(true,5)); 		
		Assert.assertTrue(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray(),0.0001f);
		
		floatArrayTest(array = new GrowableFloatArray(false,5));
		Assert.assertFalse(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray(),0.0001f);

		try{new GrowableFloatArray(false,0);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
		try{new GrowableFloatArray(false,100);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void doubleArrayTest() {
		GrowableDoubleArray	array;
		
		doubleArrayTest(array = new GrowableDoubleArray(true,5)); 		
		Assert.assertTrue(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray(),0.0001);
		
		doubleArrayTest(array = new GrowableDoubleArray(false,5));
		Assert.assertFalse(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray(),0.0001);

		try{new GrowableDoubleArray(false,0);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
		try{new GrowableDoubleArray(false,100);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	@Test
	public void charArrayTest() {
		GrowableCharArray	array;
		
		charArrayTest(array = new GrowableCharArray(true,5)); 		
		Assert.assertTrue(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());
		
		charArrayTest(array = new GrowableCharArray(false,5));
		Assert.assertFalse(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());

		try{new GrowableCharArray(false,0);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
		try{new GrowableCharArray(false,100);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
	}

	
	private void byteArrayTest(final GrowableByteArray	gba) {
		Assert.assertEquals(gba.length(),0);
		gba.append((byte)1);
		gba.append(BYTE_31);
		gba.append(BYTE_33);
		Assert.assertEquals(gba.length(),65);
		
		Assert.assertEquals(gba.read(0),(byte)1);
		Assert.assertEquals(gba.read(1),(byte)1);
		Assert.assertEquals(gba.read(31),(byte)31);
		Assert.assertEquals(gba.read(32),(byte)1);
		Assert.assertEquals(gba.read(64),(byte)33);
		try{gba.read(-1);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(65);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		
		final byte[]	result10 = new byte[10], etalon10_1 = new byte[]{1,1,2,3,4,5,6,7,8,9}, etalon10_2 = new byte[]{30,31,1,2,3,4,5,6,7,8}, etalon10_3 = new byte[]{29,30,31,32,33,0,0,0,0,0};
		
		Assert.assertEquals(gba.read(0,result10),10);
		Assert.assertArrayEquals(result10,etalon10_1);
		Assert.assertEquals(gba.read(30,result10),10);
		Assert.assertArrayEquals(result10,etalon10_2);
		Arrays.fill(result10,(byte)0);
		Assert.assertEquals(gba.read(60,result10),5);
		Assert.assertArrayEquals(result10,etalon10_3);
		
		try{gba.read(-1,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(65,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(65,null);
			Assert.fail("Mandatory exception was not detected (null target array)");
		} catch (IllegalArgumentException exc) {
		}
		try{gba.read(65,result10,-1,10);
			Assert.fail("Mandatory exception was not detected (target array start index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(65,result10,65,10);
			Assert.fail("Mandatory exception was not detected (target array start index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(65,result10,0,-1);
			Assert.fail("Mandatory exception was not detected (target array end index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(65,result10,0,20);
			Assert.fail("Mandatory exception was not detected (target array end index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(65,result10,10,5);
			Assert.fail("Mandatory exception was not detected (target array end index less than target array strat index)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}

	private void shortArrayTest(final GrowableShortArray	gsa) {
		Assert.assertEquals(gsa.length(),0);
		gsa.append((short)1);
		gsa.append(SHORT_31);
		gsa.append(SHORT_33);
		Assert.assertEquals(gsa.length(),65);
		
		Assert.assertEquals(gsa.read(0),(short)1);
		Assert.assertEquals(gsa.read(1),(short)1);
		Assert.assertEquals(gsa.read(31),(short)31);
		Assert.assertEquals(gsa.read(32),(short)1);
		Assert.assertEquals(gsa.read(64),(short)33);
		try{gsa.read(-1);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.read(65);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		
		final short[]	result10 = new short[10], etalon10_1 = new short[]{1,1,2,3,4,5,6,7,8,9}, etalon10_2 = new short[]{30,31,1,2,3,4,5,6,7,8}, etalon10_3 = new short[]{29,30,31,32,33,0,0,0,0,0};
		
		Assert.assertEquals(gsa.read(0,result10),10);
		Assert.assertArrayEquals(result10,etalon10_1);
		Assert.assertEquals(gsa.read(30,result10),10);
		Assert.assertArrayEquals(result10,etalon10_2);
		Arrays.fill(result10,(short)0);
		Assert.assertEquals(gsa.read(60,result10),5);
		Assert.assertArrayEquals(result10,etalon10_3);
		
		try{gsa.read(-1,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.read(65,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.read(65,null);
			Assert.fail("Mandatory exception was not detected (null target array)");
		} catch (IllegalArgumentException exc) {
		}
		try{gsa.read(65,result10,-1,10);
			Assert.fail("Mandatory exception was not detected (target array start index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.read(65,result10,65,10);
			Assert.fail("Mandatory exception was not detected (target array start index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.read(65,result10,0,-1);
			Assert.fail("Mandatory exception was not detected (target array end index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.read(65,result10,0,20);
			Assert.fail("Mandatory exception was not detected (target array end index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.read(65,result10,10,5);
			Assert.fail("Mandatory exception was not detected (target array end index less than target array strat index)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}
	
	private void intArrayTest(final GrowableIntArray	gia) {
		Assert.assertEquals(gia.length(),0);
		gia.append((int)1);
		gia.append(INT_31);
		gia.append(INT_33);
		Assert.assertEquals(gia.length(),65);
		
		Assert.assertEquals(gia.read(0),(int)1);
		Assert.assertEquals(gia.read(1),(int)1);
		Assert.assertEquals(gia.read(31),(int)31);
		Assert.assertEquals(gia.read(32),(int)1);
		Assert.assertEquals(gia.read(64),(int)33);
		try{gia.read(-1);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.read(65);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		
		final int[]	result10 = new int[10], etalon10_1 = new int[]{1,1,2,3,4,5,6,7,8,9}, etalon10_2 = new int[]{30,31,1,2,3,4,5,6,7,8}, etalon10_3 = new int[]{29,30,31,32,33,0,0,0,0,0};
		
		Assert.assertEquals(gia.read(0,result10),10);
		Assert.assertArrayEquals(result10,etalon10_1);
		Assert.assertEquals(gia.read(30,result10),10);
		Assert.assertArrayEquals(result10,etalon10_2);
		Arrays.fill(result10,(int)0);
		Assert.assertEquals(gia.read(60,result10),5);
		Assert.assertArrayEquals(result10,etalon10_3);
		
		try{gia.read(-1,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.read(65,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.read(65,null);
			Assert.fail("Mandatory exception was not detected (null target array)");
		} catch (IllegalArgumentException exc) {
		}
		try{gia.read(65,result10,-1,10);
			Assert.fail("Mandatory exception was not detected (target array start index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.read(65,result10,65,10);
			Assert.fail("Mandatory exception was not detected (target array start index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.read(65,result10,0,-1);
			Assert.fail("Mandatory exception was not detected (target array end index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.read(65,result10,0,20);
			Assert.fail("Mandatory exception was not detected (target array end index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.read(65,result10,10,5);
			Assert.fail("Mandatory exception was not detected (target array end index less than target array strat index)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}

	private void longArrayTest(final GrowableLongArray	gla) {
		Assert.assertEquals(gla.length(),0);
		gla.append((long)1);
		gla.append(LONG_31);
		gla.append(LONG_33);
		Assert.assertEquals(gla.length(),65);
		
		Assert.assertEquals(gla.read(0),(long)1);
		Assert.assertEquals(gla.read(1),(long)1);
		Assert.assertEquals(gla.read(31),(long)31);
		Assert.assertEquals(gla.read(32),(long)1);
		Assert.assertEquals(gla.read(64),(long)33);
		try{gla.read(-1);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.read(65);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		
		final long[]	result10 = new long[10], etalon10_1 = new long[]{1,1,2,3,4,5,6,7,8,9}, etalon10_2 = new long[]{30,31,1,2,3,4,5,6,7,8}, etalon10_3 = new long[]{29,30,31,32,33,0,0,0,0,0};
		
		Assert.assertEquals(gla.read(0,result10),10);
		Assert.assertArrayEquals(result10,etalon10_1);
		Assert.assertEquals(gla.read(30,result10),10);
		Assert.assertArrayEquals(result10,etalon10_2);
		Arrays.fill(result10,(long)0);
		Assert.assertEquals(gla.read(60,result10),5);
		Assert.assertArrayEquals(result10,etalon10_3);
		
		try{gla.read(-1,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.read(65,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.read(65,null);
			Assert.fail("Mandatory exception was not detected (null target array)");
		} catch (IllegalArgumentException exc) {
		}
		try{gla.read(65,result10,-1,10);
			Assert.fail("Mandatory exception was not detected (target array start index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.read(65,result10,65,10);
			Assert.fail("Mandatory exception was not detected (target array start index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.read(65,result10,0,-1);
			Assert.fail("Mandatory exception was not detected (target array end index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.read(65,result10,0,20);
			Assert.fail("Mandatory exception was not detected (target array end index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.read(65,result10,10,5);
			Assert.fail("Mandatory exception was not detected (target array end index less than target array strat index)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}

	private void floatArrayTest(final GrowableFloatArray	gfa) {
		Assert.assertEquals(gfa.length(),0);
		gfa.append((float)1);
		gfa.append(FLOAT_31);
		gfa.append(FLOAT_33);
		Assert.assertEquals(gfa.length(),65);
		
		Assert.assertEquals(gfa.read(0),(float)1,0.0001f);
		Assert.assertEquals(gfa.read(1),(float)1,0.0001f);
		Assert.assertEquals(gfa.read(31),(float)31,0.0001f);
		Assert.assertEquals(gfa.read(32),(float)1,0.0001f);
		Assert.assertEquals(gfa.read(64),(float)33,0.0001f);
		try{gfa.read(-1);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.read(65);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		
		final float[]	result10 = new float[10], etalon10_1 = new float[]{1,1,2,3,4,5,6,7,8,9}, etalon10_2 = new float[]{30,31,1,2,3,4,5,6,7,8}, etalon10_3 = new float[]{29,30,31,32,33,0,0,0,0,0};
		
		Assert.assertEquals(gfa.read(0,result10),10);
		Assert.assertArrayEquals(result10,etalon10_1,0.0001f);
		Assert.assertEquals(gfa.read(30,result10),10);
		Assert.assertArrayEquals(result10,etalon10_2,0.0001f);
		Arrays.fill(result10,(float)0);
		Assert.assertEquals(gfa.read(60,result10),5);
		Assert.assertArrayEquals(result10,etalon10_3,0.0001f);
		
		try{gfa.read(-1,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.read(65,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.read(65,null);
			Assert.fail("Mandatory exception was not detected (null target array)");
		} catch (IllegalArgumentException exc) {
		}
		try{gfa.read(65,result10,-1,10);
			Assert.fail("Mandatory exception was not detected (target array start index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.read(65,result10,65,10);
			Assert.fail("Mandatory exception was not detected (target array start index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.read(65,result10,0,-1);
			Assert.fail("Mandatory exception was not detected (target array end index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.read(65,result10,0,20);
			Assert.fail("Mandatory exception was not detected (target array end index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.read(65,result10,10,5);
			Assert.fail("Mandatory exception was not detected (target array end index less than target array strat index)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}
	
	private void doubleArrayTest(final GrowableDoubleArray	gda) {
		Assert.assertEquals(gda.length(),0);
		gda.append((double)1);
		gda.append(DOUBLE_31);
		gda.append(DOUBLE_33);
		Assert.assertEquals(gda.length(),65);
		
		Assert.assertEquals(gda.read(0),(double)1,0.0001);
		Assert.assertEquals(gda.read(1),(double)1,0.0001);
		Assert.assertEquals(gda.read(31),(double)31,0.0001);
		Assert.assertEquals(gda.read(32),(double)1,0.0001);
		Assert.assertEquals(gda.read(64),(double)33,0.0001);
		try{gda.read(-1);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.read(65);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		
		final double[]	result10 = new double[10], etalon10_1 = new double[]{1,1,2,3,4,5,6,7,8,9}, etalon10_2 = new double[]{30,31,1,2,3,4,5,6,7,8}, etalon10_3 = new double[]{29,30,31,32,33,0,0,0,0,0};
		
		Assert.assertEquals(gda.read(0,result10),10);
		Assert.assertArrayEquals(result10,etalon10_1,0.0001);
		Assert.assertEquals(gda.read(30,result10),10);
		Assert.assertArrayEquals(result10,etalon10_2,0.0001);
		Arrays.fill(result10,(double)0);
		Assert.assertEquals(gda.read(60,result10),5);
		Assert.assertArrayEquals(result10,etalon10_3,0.0001);
		
		try{gda.read(-1,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.read(65,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.read(65,null);
			Assert.fail("Mandatory exception was not detected (null target array)");
		} catch (IllegalArgumentException exc) {
		}
		try{gda.read(65,result10,-1,10);
			Assert.fail("Mandatory exception was not detected (target array start index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.read(65,result10,65,10);
			Assert.fail("Mandatory exception was not detected (target array start index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.read(65,result10,0,-1);
			Assert.fail("Mandatory exception was not detected (target array end index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.read(65,result10,0,20);
			Assert.fail("Mandatory exception was not detected (target array end index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.read(65,result10,10,5);
			Assert.fail("Mandatory exception was not detected (target array end index less than target array strat index)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}

	private void charArrayTest(final GrowableCharArray	gca) {
		Assert.assertEquals(gca.length(),0);
		gca.append((char)1);
		gca.append(CHAR_31);
		gca.append(CHAR_33);
		Assert.assertEquals(gca.length(),65);
		
		Assert.assertEquals(gca.read(0),(char)1);
		Assert.assertEquals(gca.read(1),(char)1);
		Assert.assertEquals(gca.read(31),(char)31);
		Assert.assertEquals(gca.read(32),(char)1);
		Assert.assertEquals(gca.read(64),(char)33);
		try{gca.read(-1);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.read(65);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		
		final char[]	result10 = new char[10], etalon10_1 = new char[]{1,1,2,3,4,5,6,7,8,9}, etalon10_2 = new char[]{30,31,1,2,3,4,5,6,7,8}, etalon10_3 = new char[]{29,30,31,32,33,0,0,0,0,0};
		
		Assert.assertEquals(gca.read(0,result10),10);
		Assert.assertArrayEquals(result10,etalon10_1);
		Assert.assertEquals(gca.read(30,result10),10);
		Assert.assertArrayEquals(result10,etalon10_2);
		Arrays.fill(result10,(char)0);
		Assert.assertEquals(gca.read(60,result10),5);
		Assert.assertArrayEquals(result10,etalon10_3);
		
		try{gca.read(-1,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.read(65,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.read(65,null);
			Assert.fail("Mandatory exception was not detected (null target array)");
		} catch (IllegalArgumentException exc) {
		}
		try{gca.read(65,result10,-1,10);
			Assert.fail("Mandatory exception was not detected (target array start index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.read(65,result10,65,10);
			Assert.fail("Mandatory exception was not detected (target array start index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.read(65,result10,0,-1);
			Assert.fail("Mandatory exception was not detected (target array end index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.read(65,result10,0,20);
			Assert.fail("Mandatory exception was not detected (target array end index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.read(65,result10,10,5);
			Assert.fail("Mandatory exception was not detected (target array end index less than target array strat index)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}
}
