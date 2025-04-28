package chav1961.purelib.basic.growablearrays;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

@Tag("OrdinalTestCategory")
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
	private static final boolean[]	BOOL_31 = new boolean[]{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true};
	private static final boolean[]	BOOL_33 = new boolean[]{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true};
	
	private static final int[]		LENGTHS = {66, 100, 200, 400, 800, 1600, 3200, 6400, 12800};

	@Test 
	public void byteArrayTest() throws IOException {
		GrowableByteArray	array;
		
		byteArrayTest(array = new GrowableByteArray(true,5)); 		
		Assert.assertTrue(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());
		
		byteArrayTest(array = new GrowableByteArray(false,5));
		Assert.assertFalse(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());

		array = new GrowableByteArray(false,4);
		
		byte[]	content = new byte[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19}; 
		array.append(content);
		Assert.assertEquals(content.length,array.length());
		Assert.assertArrayEquals(content,array.extract());
		Assert.assertArrayEquals(content,array.toPlain().extract());
		
		try(final InputStream	is = array.getInputStream()) {
			for (int index = 0; index < array.length(); index++) {
				Assert.assertEquals(index,is.read());
			}
			Assert.assertEquals(-1,is.read());
		}

		try(final InputStream	is = array.getInputStream()) {
			byte[]	readed = new byte[content.length]; 

			Assert.assertEquals(readed.length,is.read(readed));
			Assert.assertArrayEquals(content,readed);
			Assert.assertEquals(-1,is.read(readed));
		}
		
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

		array = new GrowableShortArray(false,4);
		
		short[]	content = new short[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19}; 
		array.append(content);
		Assert.assertEquals(content.length,array.length());
		Assert.assertArrayEquals(content,array.extract());
		Assert.assertArrayEquals(content,array.toPlain().extract());
		
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

		array = new GrowableIntArray(false,4);
		
		int[]	content = new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19}; 
		array.append(content);
		Assert.assertEquals(content.length,array.length());
		Assert.assertArrayEquals(content,array.extract());
		Assert.assertArrayEquals(content,array.toPlain().extract());
		
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

		array = new GrowableLongArray(false,4);
		
		long[]	content = new long[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19}; 
		array.append(content);
		Assert.assertEquals(content.length,array.length());
		Assert.assertArrayEquals(content,array.extract());
		Assert.assertArrayEquals(content,array.toPlain().extract());
		
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

		array = new GrowableFloatArray(false,4);
		
		float[]	content = new float[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19}; 
		array.append(content);
		Assert.assertEquals(content.length,array.length());
		Assert.assertArrayEquals(content,array.extract(),0.001f);
		Assert.assertArrayEquals(content,array.toPlain().extract(),0.001f);
		
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

		array = new GrowableDoubleArray(false,4);
		
		double[]	content = new double[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19}; 
		array.append(content);
		Assert.assertEquals(content.length,array.length());
		Assert.assertArrayEquals(content,array.extract(),0.001f);
		Assert.assertArrayEquals(content,array.toPlain().extract(),0.001f);
		
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
	public void charArrayTest() throws IOException {
		GrowableCharArray<?>	array;
		
		charArrayTest(array = new GrowableCharArray<>(true,5)); 		
		Assert.assertTrue(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());
		
		charArrayTest(array = new GrowableCharArray<>(false,5));
		Assert.assertFalse(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());

		array = new GrowableCharArray<>(false,4);
		
		char[]	content = new char[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19}; 
		array.append(content);
		Assert.assertEquals(content.length,array.length());
		Assert.assertArrayEquals(content,array.extract());
		Assert.assertArrayEquals(content,array.toPlain().extract());
		
		try(final Reader	rdr = array.getReader()) {
			for (int index = 0; index < array.length(); index++) {
				Assert.assertEquals(index,rdr.read());
			}
			Assert.assertEquals(-1,rdr.read());
		}

		try(final Reader	rdr = array.getReader()) {
			char[]	readed = new char[content.length]; 

			Assert.assertEquals(readed.length,rdr.read(readed));
			Assert.assertArrayEquals(content,readed);
			Assert.assertEquals(-1,rdr.read(readed));
		}
		
		try{new GrowableCharArray<>(false,0);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
		try{new GrowableCharArray<>(false,100);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void booleanArrayTest() {
		GrowableBooleanArray	array;
		
		booleanArrayTest(array = new GrowableBooleanArray(true,5)); 		
		Assert.assertTrue(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());
		
		booleanArrayTest(array = new GrowableBooleanArray(false,5));
		Assert.assertFalse(array.toPlain() == array);
		Assert.assertArrayEquals(array.toArray(),array.toPlain().toArray());

		array = new GrowableBooleanArray(false,4);
		
		boolean[]	content = new boolean[] {true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false}; 
		array.append(content);
		Assert.assertEquals(content.length,array.length());
		Assert.assertArrayEquals(content,array.extract());
		Assert.assertArrayEquals(content,array.toPlain().extract());
				
		try{new GrowableBooleanArray(false,0);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
		try{new GrowableBooleanArray(false,100);
			Assert.fail("Mandatory exception was not detected (array size out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void byteIOTest() throws IOException {
		final GrowableByteArray	gba1 = new GrowableByteArray(true);
		
		gba1.append(new ByteArrayInputStream("test string".getBytes()));
		
		try(final InputStream	is = gba1.getInputStream();
			final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			
			Utils.copyStream(is,baos);
			baos.flush();
			Assert.assertEquals(baos.toString(),"test string");
		}

		try{gba1.append((InputStream)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		final GrowableByteArray	gba2 = new GrowableByteArray(true);
		
		gba2.append(new ByteArrayInputStream("test string".getBytes()));
		
		try(final InputStream	is = gba2.getInputStream();
			final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			
			Utils.copyStream(is,baos);
			baos.flush();
			Assert.assertEquals(baos.toString(),"test string");
		}
	}

	@Test
	public void charIOTest() throws IOException {
		final GrowableCharArray<?>	gca1 = new GrowableCharArray<>(true);
		
		gca1.append(new StringReader("test string"));
		
		try(final Reader	is = gca1.getReader();
			final Writer	os = new StringWriter()) {
			
			Utils.copyStream(is,os);
			os.flush();
			Assert.assertEquals(os.toString(),"test string");
		}

		try{gca1.append((Reader)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		final GrowableCharArray<?>	gca2 = new GrowableCharArray<>(true);
		
		gca2.append(new StringReader("test string"));
		
		try(final Reader	is = gca2.getReader();
			final Writer	os = new StringWriter()) { 
			
			Utils.copyStream(is,os);
			os.flush();
			Assert.assertEquals(os.toString(),"test string");
		}
	}

	@Test
	public void charSequenceTest() { 
		final GrowableCharArray<?>	seq = new GrowableCharArray<>(false);
		
		Assert.assertEquals(0,seq.length());
		try{seq.charAt(1);
			Assert.fail("Mandatory exception was not detected (index outside the range)");
		} catch (IndexOutOfBoundsException exc) {
		}
		try{seq.subSequence(0,0);
			Assert.fail("Mandatory exception was not detected (start index outside the range)");
		} catch (IndexOutOfBoundsException exc) {
		}
		try{seq.subSequence(-1,0);
			Assert.fail("Mandatory exception was not detected (start index outside the range)");
		} catch (IndexOutOfBoundsException exc) {
		}
		try{seq.subSequence(100,0);
			Assert.fail("Mandatory exception was not detected (start index outside the range)");
		} catch (IndexOutOfBoundsException exc) {
		}
		
		seq.append("test string".toCharArray());
		Assert.assertEquals(seq.length(),11);
		Assert.assertEquals(seq.charAt(0),'t'); 
		Assert.assertEquals(seq.charAt(10),'g');

		try{seq.subSequence(0,-1);
			Assert.fail("Mandatory exception was not detected (end index outside the range)");
		} catch (IndexOutOfBoundsException exc) {
		}
		try{seq.subSequence(0,100);
			Assert.fail("Mandatory exception was not detected (end index outside the range)");
		} catch (IndexOutOfBoundsException exc) {
		}		
		try{seq.subSequence(10,5);
			Assert.fail("Mandatory exception was not detected (end index outside the range)");
		} catch (IllegalArgumentException exc) {
		}		
		
		
		final CharSequence		newSeq = seq.subSequence(3,6);
		
		Assert.assertEquals(newSeq.length(),3);
		Assert.assertEquals(newSeq.charAt(0),'t');
		Assert.assertEquals(newSeq.charAt(2),'s');

		final CharSequence		newSeq2 = newSeq.subSequence(1,2);

		Assert.assertEquals(newSeq2.length(),1);
		Assert.assertEquals(newSeq2.charAt(0),' ');

		final CharSequence		newSeq3 = newSeq.subSequence(1,1);

		Assert.assertEquals(newSeq3.length(),0);
	}
	
	private void byteArrayTest(final GrowableByteArray	gba) throws IOException {
		Assert.assertEquals(0,gba.length());
		gba.append((byte)1);
		gba.append(BYTE_31);
		gba.append(BYTE_33);
		Assert.assertEquals(65,gba.length());

		for (int size : LENGTHS) {
			Assert.assertEquals(size,gba.length(size).length());
			Assert.assertEquals(65,gba.length(65).length());
		}
		
		Assert.assertEquals(1,gba.read(0));
		Assert.assertEquals(1,gba.read(1));
		Assert.assertEquals(31,gba.read(31));
		Assert.assertEquals(1,gba.read(32));
		Assert.assertEquals(33,gba.read(64));
		try{gba.read(-1);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(65);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		
		final byte[]	result10 = new byte[10], etalon10_1 = new byte[]{1,1,2,3,4,5,6,7,8,9}, etalon10_2 = new byte[]{30,31,1,2,3,4,5,6,7,8}, etalon10_3 = new byte[]{29,30,31,32,33,0,0,0,0,0};
		
		Assert.assertEquals(10,gba.read(0,result10));
		Assert.assertArrayEquals(etalon10_1,result10);
		Assert.assertEquals(10,gba.read(30,result10));
		Assert.assertArrayEquals(etalon10_2,result10);
		Arrays.fill(result10,(byte)0);
		Assert.assertEquals(5,gba.read(60,result10));
		Assert.assertArrayEquals(etalon10_3,result10);
		
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
		} catch (NullPointerException exc) {
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
		
		gba.clear();
		Assert.assertEquals(gba.length(),0);
		
		try{gba.append((byte[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{gba.append((InputStream)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{gba.append((byte[])null,1,1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{gba.append(new byte[] {1},-1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.append(new byte[] {1},1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.append(new byte[] {1},0,2);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.append(new byte[] {1},0,-1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}

		gba.append(new byte[] {1,2,3});
		try{gba.read(-1,new byte[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(100,new byte[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(0,null,1,1);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{gba.read(0,new byte[10],-1,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(0,new byte[10],100,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(0,new byte[10],1,-1);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		} 
		try{gba.read(0,new byte[10],1,0);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(0,new byte[10],1,100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}

	private void shortArrayTest(final GrowableShortArray	gsa) {
		Assert.assertEquals(gsa.length(),0);
		gsa.append((short)1);
		gsa.append(SHORT_31);
		gsa.append(SHORT_33);
		Assert.assertEquals(gsa.length(),65);

		for (int size : LENGTHS) {
			Assert.assertEquals(gsa.length(size).length(),size);
			Assert.assertEquals(gsa.length(65).length(),65);
		}
		
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
		} catch (NullPointerException exc) {
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
		
		gsa.clear();
		Assert.assertEquals(gsa.length(),0);

		try{gsa.append((short[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{gsa.append((short[])null,1,1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{gsa.append(new short[] {1},-1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.append(new short[] {1},1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.append(new short[] {1},0,2);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.append(new short[] {1},0,-1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	
		gsa.append(new short[] {1,2,3});
		try{gsa.read(-1,new short[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.read(100,new short[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.read(0,null,1,1);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{gsa.read(0,new short[10],-1,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.read(0,new short[10],100,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.read(0,new short[10],1,-1);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		} 
		try{gsa.read(0,new short[10],1,0);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gsa.read(0,new short[10],1,100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}
	
	private void intArrayTest(final GrowableIntArray	gia) {
		Assert.assertEquals(gia.length(),0);
		gia.append((int)1);
		gia.append(INT_31);
		gia.append(INT_33);
		Assert.assertEquals(gia.length(),65);
		
		for (int size : LENGTHS) {
			Assert.assertEquals(gia.length(size).length(),size);
			Assert.assertEquals(gia.length(65).length(),65);
		}		
		
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
		} catch (NullPointerException exc) {
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

		gia.clear();
		Assert.assertEquals(gia.length(),0);

		try{gia.append((int[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{gia.append((int[])null,1,1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{gia.append(new int[] {1},-1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.append(new int[] {1},1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.append(new int[] {1},0,2);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.append(new int[] {1},0,-1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	
		gia.append(new int[] {1,2,3});
		try{gia.read(-1,new int[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.read(100,new int[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.read(0,null,1,1);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{gia.read(0,new int[10],-1,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.read(0,new int[10],100,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.read(0,new int[10],1,-1);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		} 
		try{gia.read(0,new int[10],1,0);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gia.read(0,new int[10],1,100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}

	private void longArrayTest(final GrowableLongArray	gla) {
		Assert.assertEquals(gla.length(),0);
		gla.append((long)1);
		gla.append(LONG_31);
		gla.append(LONG_33);
		Assert.assertEquals(gla.length(),65);

		for (int size : LENGTHS) {
			Assert.assertEquals(gla.length(size).length(),size);
			Assert.assertEquals(gla.length(65).length(),65);
		}
		
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
		} catch (NullPointerException exc) {
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
		
		gla.clear();
		Assert.assertEquals(gla.length(),0);

		try{gla.append((long[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{gla.append((long[])null,1,1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{gla.append(new long[] {1},-1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.append(new long[] {1},1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.append(new long[] {1},0,2);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.append(new long[] {1},0,-1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	
		gla.append(new long[] {1,2,3});
		try{gla.read(-1,new long[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.read(100,new long[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.read(0,null,1,1);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{gla.read(0,new long[10],-1,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.read(0,new long[10],100,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.read(0,new long[10],1,-1);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		} 
		try{gla.read(0,new long[10],1,0);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gla.read(0,new long[10],1,100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}

	private void floatArrayTest(final GrowableFloatArray	gfa) {
		Assert.assertEquals(gfa.length(),0);
		gfa.append((float)1);
		gfa.append(FLOAT_31);
		gfa.append(FLOAT_33);
		Assert.assertEquals(gfa.length(),65);

		for (int size : LENGTHS) {
			Assert.assertEquals(gfa.length(size).length(),size);
			Assert.assertEquals(gfa.length(65).length(),65);
		}
		
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
		} catch (NullPointerException exc) {
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
		
		gfa.clear();
		Assert.assertEquals(gfa.length(),0);

		try{gfa.append((float[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{gfa.append((float[])null,1,1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{gfa.append(new float[] {1},-1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.append(new float[] {1},1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.append(new float[] {1},0,2);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.append(new float[] {1},0,-1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	
		gfa.append(new float[] {1,2,3});
		try{gfa.read(-1,new float[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.read(100,new float[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.read(0,null,1,1);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{gfa.read(0,new float[10],-1,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.read(0,new float[10],100,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.read(0,new float[10],1,-1);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		} 
		try{gfa.read(0,new float[10],1,0);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gfa.read(0,new float[10],1,100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}
	
	private void doubleArrayTest(final GrowableDoubleArray	gda) {
		Assert.assertEquals(gda.length(),0);
		gda.append((double)1);
		gda.append(DOUBLE_31);
		gda.append(DOUBLE_33);
		Assert.assertEquals(gda.length(),65);
		
		for (int size : LENGTHS) {
			Assert.assertEquals(gda.length(size).length(),size);
			Assert.assertEquals(gda.length(65).length(),65);
		}
		
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
		} catch (NullPointerException exc) {
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
		
		gda.clear();
		Assert.assertEquals(gda.length(),0);

		try{gda.append((double[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{gda.append((double[])null,1,1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{gda.append(new double[] {1},-1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.append(new double[] {1},1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.append(new double[] {1},0,2);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.append(new double[] {1},0,-1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	
		gda.append(new double[] {1,2,3});
		try{gda.read(-1,new double[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.read(100,new double[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.read(0,null,1,1);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{gda.read(0,new double[10],-1,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.read(0,new double[10],100,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.read(0,new double[10],1,-1);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		} 
		try{gda.read(0,new double[10],1,0);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gda.read(0,new double[10],1,100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}

	private void charArrayTest(final GrowableCharArray<?>	gca) throws IOException {
		Assert.assertEquals(gca.length(),0);
		gca.append((char)1);
		gca.append(CHAR_31);
		gca.append(CHAR_33);
		Assert.assertEquals(gca.length(),65);
		
		for (int size : LENGTHS) {
			Assert.assertEquals(gca.length(size).length(),size);
			Assert.assertEquals(gca.length(65).length(),65);
		}
		
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
		try{gca.read(66,result10);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.read(65,null);
			Assert.fail("Mandatory exception was not detected (null target array)");
		} catch (NullPointerException exc) {
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
		
		gca.clear();
		Assert.assertEquals(gca.length(),0);
		
		gca.append("1234567890123456789012345678901234567890123456789012345678901234567890");
		Assert.assertEquals(70,gca.length());
		
		gca.clear();
		gca.append("1234567890123456789012345678901234567890123456789012345678901234567890",15,30);
		Assert.assertEquals(15,gca.length());
		Assert.assertArrayEquals("678901234567890".toCharArray(),gca.extract());
		
		gca.clear();
		final SyntaxTreeInterface<Object>	sti = new AndOrTree<>();
		
		sti.placeName("test",1,null);
		gca.append(sti,1);
		Assert.assertEquals(4,gca.length());
		Assert.assertArrayEquals("test".toCharArray(),gca.extract());

		try{gca.append((SyntaxTreeInterface<?>)null,0);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{gca.append(sti,666);
			Assert.fail("Mandatory exception was not detected (2-nd argument is missing in the tree)");
		} catch (IllegalArgumentException exc) {
		}
		
		gca.clear();
		try{gca.append((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{gca.append((Reader)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) { 
		}
		try{gca.append((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		
		try{gca.append((char[])null,1,1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{gca.append(new char[] {1},-1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.append(new char[] {1},1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.append(new char[] {1},0,2);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.append(new char[] {1},0,-1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.append(new char[] {1,2,3},1,0);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}

		try{gca.append((String)null,1,1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{gca.append("a",-1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.append("a",1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.append("a",0,2);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.append("a",0,-1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.append("abc",2,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		
		gca.append(new char[] {1,2,3});
		try{gca.read(-1,new char[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.read(100,new char[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.read(0,null,1,1);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{gca.read(0,new char[10],-1,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.read(0,new char[10],100,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.read(0,new char[10],1,-1);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		} 
		try{gca.read(0,new char[10],1,0);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gca.read(0,new char[10],1,100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}

	private void booleanArrayTest(final GrowableBooleanArray	gba) {
		Assert.assertEquals(gba.length(),0);
		gba.append(false);
		gba.append(BOOL_31);
		gba.append(BOOL_33);
		Assert.assertEquals(gba.length(),65);

		for (int size : LENGTHS) {
			Assert.assertEquals(gba.length(size).length(),size);
			Assert.assertEquals(gba.length(65).length(),65);
		}
		
		Assert.assertFalse(gba.read(0));
		Assert.assertTrue(gba.read(1));
		Assert.assertTrue(gba.read(31));
		Assert.assertTrue(gba.read(32));
		Assert.assertTrue(gba.read(64));
		try{gba.read(-1);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(65);
			Assert.fail("Mandatory exception was not detected (array index out of bounds)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		
		final boolean[]	result10 = new boolean[10];
		
		Assert.assertEquals(gba.read(0,result10),10);
		Assert.assertFalse(result10[0]);
		Assert.assertTrue(result10[1]);
		Assert.assertEquals(gba.read(30,result10),10);
		Assert.assertTrue(result10[0]);
		Assert.assertTrue(result10[1]);
		Arrays.fill(result10,false);
		Assert.assertEquals(gba.read(60,result10),5);
		Assert.assertTrue(result10[0]);
		Assert.assertFalse(result10[9]);
		
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
		} catch (NullPointerException exc) {
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
		
		gba.clear();
		Assert.assertEquals(gba.length(),0);

		try{gba.append((boolean[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{gba.append((boolean[])null,1,1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{gba.append(new boolean[] {true},-1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.append(new boolean[] {true},1,1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.append(new boolean[] {true},0,2);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.append(new boolean[] {true},0,-1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	
		gba.append(new boolean[] {true,false,true});
		try{gba.read(-1,new boolean[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(100,new boolean[100],1,1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(0,null,1,1);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{gba.read(0,new boolean[10],-1,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(0,new boolean[10],100,1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(0,new boolean[10],1,-1);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}  
		try{gba.read(0,new boolean[10],1,0);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{gba.read(0,new boolean[10],1,100);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
	}
}
