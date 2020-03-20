package chav1961.purelib.basic;

import java.awt.Color;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.CharUtils.ArgumentType;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class CharUtilsTest {
	public static final float		EPSILON_FLOAT = 0.00001f;
	public static final double		EPSILON = 0.000000001;
	
	@Test
	public void intConversionTest() throws SyntaxException {
		final int[]		value = new int[1];
		
		Assert.assertEquals(CharUtils.parseInt("0".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],0);
		Assert.assertEquals(CharUtils.parseInt("1".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharUtils.parseInt("1 ".toCharArray(),0,value,false),1);		Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharUtils.parseInt("123456".toCharArray(),0,value,false),6);	Assert.assertEquals(value[0],123456);
		
		Assert.assertEquals(6,CharUtils.parseSignedInt("123456".toCharArray(),0,value,false));	
		Assert.assertEquals(123456,value[0]);
		Assert.assertEquals(7,CharUtils.parseSignedInt("-123456".toCharArray(),0,value,false));	
		Assert.assertEquals(-123456,value[0]);
		
		try{CharUtils.parseInt(null,0,value,false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseInt("".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseInt("0".toCharArray(),1,value,false);
			Assert.fail("Mandatory exception was not detected (1-nd argument outside the bound)");
 		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseInt("0".toCharArray(),0,null,false);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseInt("0".toCharArray(),0,new int[0],false);
			Assert.fail("Mandatory exception was not detected (zero length 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseInt("1234567890123".toCharArray(),0,value,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (SyntaxException exc) {
		}

		Assert.assertEquals(CharUtils.parseIntExtended("0".toCharArray(),0,value,false),1);				Assert.assertEquals(value[0],0);
		Assert.assertEquals(CharUtils.parseIntExtended("1".toCharArray(),0,value,false),1);				Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharUtils.parseIntExtended("1 ".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharUtils.parseIntExtended("123456".toCharArray(),0,value,false),6);		Assert.assertEquals(value[0],123456);
		Assert.assertEquals(CharUtils.parseIntExtended("0123456".toCharArray(),0,value,false),7);		Assert.assertEquals(value[0],0123456);
		Assert.assertEquals(CharUtils.parseIntExtended("0x123456".toCharArray(),0,value,false),8);		Assert.assertEquals(value[0],0x123456);
		Assert.assertEquals(CharUtils.parseIntExtended("0xcafeCAFE".toCharArray(),0,value,false),10);	Assert.assertEquals(value[0],0xcafeCAFE);
		Assert.assertEquals(CharUtils.parseIntExtended("0b10101010".toCharArray(),0,value,false),10);	Assert.assertEquals(value[0],0b10101010);

		try{CharUtils.parseIntExtended(null,0,value,false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseIntExtended("".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseIntExtended("0".toCharArray(),1,value,false);
			Assert.fail("Mandatory exception was not detected (1-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseIntExtended("0".toCharArray(),0,null,false);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseIntExtended("0".toCharArray(),0,new int[0],false);
			Assert.fail("Mandatory exception was not detected (zero length 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseIntExtended("1234567890123".toCharArray(),0,value,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void longConversionTest() throws SyntaxException {
		final long[]		value = new long[1];
		
		Assert.assertEquals(CharUtils.parseLong("0".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],0);
		Assert.assertEquals(CharUtils.parseLong("1".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharUtils.parseLong("1 ".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharUtils.parseLong("123456".toCharArray(),0,value,false),6);		Assert.assertEquals(value[0],123456);

		Assert.assertEquals(6,CharUtils.parseSignedLong("123456".toCharArray(),0,value,false));
		Assert.assertEquals(123456,value[0]);
		Assert.assertEquals(7,CharUtils.parseSignedLong("-123456".toCharArray(),0,value,false));
		Assert.assertEquals(-123456,value[0]);
		
		try{CharUtils.parseLong(null,0,value,false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseLong("".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseLong("0".toCharArray(),1,value,false);
			Assert.fail("Mandatory exception was not detected (1-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseLong("0".toCharArray(),0,null,false);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseLong("0".toCharArray(),0,new long[0],false);
			Assert.fail("Mandatory exception was not detected (zero length 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseLong("123456789012345678901234567890".toCharArray(),0,value,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (SyntaxException exc) {
		}

		Assert.assertEquals(CharUtils.parseLongExtended("0".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],0);
		Assert.assertEquals(CharUtils.parseLongExtended("1".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharUtils.parseLongExtended("1 ".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharUtils.parseLongExtended("123456".toCharArray(),0,value,false),6);		Assert.assertEquals(value[0],123456);
		Assert.assertEquals(CharUtils.parseLongExtended("0123456".toCharArray(),0,value,false),7);		Assert.assertEquals(value[0],0123456);
		Assert.assertEquals(CharUtils.parseLongExtended("0x123456".toCharArray(),0,value,false),8);		Assert.assertEquals(value[0],0x123456);
		Assert.assertEquals(CharUtils.parseLongExtended("0xcafeCAFE".toCharArray(),0,value,false),10);	Assert.assertEquals(value[0],0xcafeCAFEL);
		Assert.assertEquals(CharUtils.parseLongExtended("0b10101010".toCharArray(),0,value,false),10);	Assert.assertEquals(value[0],0b10101010);

		try{CharUtils.parseLongExtended(null,0,value,false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseLongExtended("".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseLongExtended("0".toCharArray(),1,value,false);
			Assert.fail("Mandatory exception was not detected (1-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseLongExtended("0".toCharArray(),0,null,false);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseLongExtended("0".toCharArray(),0,new long[0],false);
			Assert.fail("Mandatory exception was not detected (zero length 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseLongExtended("123456789012345678901234567890".toCharArray(),0,value,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void floatConversionTest() throws SyntaxException {
		final float[]		value = new float[1];
		
		Assert.assertEquals(CharUtils.parseFloat("0".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],0,EPSILON_FLOAT);
		Assert.assertEquals(CharUtils.parseFloat("1".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1,EPSILON_FLOAT);
		Assert.assertEquals(CharUtils.parseFloat("1 ".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1,EPSILON_FLOAT);
		Assert.assertEquals(CharUtils.parseFloat("123456".toCharArray(),0,value,false),6);		Assert.assertEquals(value[0],123456,EPSILON_FLOAT);
		Assert.assertEquals(CharUtils.parseFloat("1234567".toCharArray(),0,value,false),7);		Assert.assertEquals(value[0],1234567.0,EPSILON_FLOAT);

		Assert.assertEquals(7,CharUtils.parseSignedFloat("1234567".toCharArray(),0,value,false));
		Assert.assertEquals(1234567.0,value[0],EPSILON_FLOAT);
		Assert.assertEquals(8,CharUtils.parseSignedFloat("-1234567".toCharArray(),0,value,false));
		Assert.assertEquals(-1234567.0,value[0],EPSILON_FLOAT);
		
		Assert.assertEquals(CharUtils.parseFloat("0.1".toCharArray(),0,value,false),3);			Assert.assertEquals(value[0],0.1,EPSILON_FLOAT);
		Assert.assertEquals(CharUtils.parseFloat("0.0000000000000000001".toCharArray(),0,value,false),21);			Assert.assertEquals(value[0],0.0000000000000000001,EPSILON_FLOAT);
		Assert.assertEquals(CharUtils.parseFloat("2E10".toCharArray(),0,value,false),4);		Assert.assertEquals(value[0],2E10,EPSILON_FLOAT);
		Assert.assertEquals(CharUtils.parseFloat("1.2E10".toCharArray(),0,value,false),6);		Assert.assertEquals(value[0],1.2E10,EPSILON_FLOAT);
		Assert.assertEquals(CharUtils.parseFloat("1.2E+10".toCharArray(),0,value,false),7);		Assert.assertEquals(value[0],1.2E10,EPSILON_FLOAT);
		Assert.assertEquals(CharUtils.parseFloat("1.2E-10".toCharArray(),0,value,false),7);		Assert.assertEquals(value[0],1.2E-10,EPSILON_FLOAT);

		try{CharUtils.parseFloat(null,0,value,false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseFloat("".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseFloat("0".toCharArray(),1,value,false);
			Assert.fail("Mandatory exception was not detected (1-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseFloat("0".toCharArray(),0,null,false);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseFloat("0".toCharArray(),0,new float[0],false);
			Assert.fail("Mandatory exception was not detected (zero length 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseFloat("1.".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (missing fractional)");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.parseFloat("1E".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.parseFloat("1E-".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.parseFloat("1E+".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.parseFloat(("1E"+(Float.MAX_EXPONENT+10)).toCharArray(),0,value,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void doubleConversionTest() throws SyntaxException {
		final double[]		value = new double[1];
		
		Assert.assertEquals(CharUtils.parseDouble("0".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],0,EPSILON);
		Assert.assertEquals(CharUtils.parseDouble("1".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1,EPSILON);
		Assert.assertEquals(CharUtils.parseDouble("1 ".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1,EPSILON);
		Assert.assertEquals(CharUtils.parseDouble("123456".toCharArray(),0,value,false),6);		Assert.assertEquals(value[0],123456,EPSILON);
		Assert.assertEquals(CharUtils.parseDouble("12345678901234567890".toCharArray(),0,value,false),20);		Assert.assertEquals(value[0],12345678901234567890.0,EPSILON);

		Assert.assertEquals(20,CharUtils.parseSignedDouble("12345678901234567890".toCharArray(),0,value,false));
		Assert.assertEquals(12345678901234567890.0,value[0],EPSILON);
		Assert.assertEquals(21,CharUtils.parseSignedDouble("-12345678901234567890".toCharArray(),0,value,false));
		Assert.assertEquals(-12345678901234567890.0,value[0],EPSILON);
		
		Assert.assertEquals(CharUtils.parseDouble("0.1".toCharArray(),0,value,false),3);		Assert.assertEquals(value[0],0.1,EPSILON);
		Assert.assertEquals(CharUtils.parseDouble("0.0000000000000000001".toCharArray(),0,value,false),21);			Assert.assertEquals(value[0],0.0000000000000000001,EPSILON);
		Assert.assertEquals(CharUtils.parseDouble("2E10".toCharArray(),0,value,false),4);		Assert.assertEquals(value[0],2E10,EPSILON);
		Assert.assertEquals(CharUtils.parseDouble("1.2E10".toCharArray(),0,value,false),6);		Assert.assertEquals(value[0],1.2E10,EPSILON);
		Assert.assertEquals(CharUtils.parseDouble("1.2E+10".toCharArray(),0,value,false),7);	Assert.assertEquals(value[0],1.2E10,EPSILON);
		Assert.assertEquals(CharUtils.parseDouble("1.2E-10".toCharArray(),0,value,false),7);	Assert.assertEquals(value[0],1.2E-10,EPSILON);

		try{CharUtils.parseDouble(null,0,value,false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseDouble("".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseDouble("0".toCharArray(),1,value,false);
			Assert.fail("Mandatory exception was not detected (1-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseDouble("0".toCharArray(),0,null,false);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseDouble("0".toCharArray(),0,new double[0],false);
			Assert.fail("Mandatory exception was not detected (zero length 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseDouble("1.".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (missing fractional)");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.parseDouble("1E".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.parseDouble("1E-".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.parseDouble("1E+".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.parseDouble(("1E"+(Double.MAX_EXPONENT+10)).toCharArray(),0,value,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (SyntaxException exc) {
		}
	}
	
	@Test
	public void numberConversionTest() throws SyntaxException {
		final long[]		value = new long[2];
		
		Assert.assertEquals(CharUtils.parseNumber("0".toCharArray(),0,value,CharUtils.PREF_ANY,false),1);			Assert.assertEquals(value[0],0,EPSILON);
		Assert.assertEquals(CharUtils.parseNumber("1".toCharArray(),0,value,CharUtils.PREF_ANY,false),1);			Assert.assertEquals(value[0],1,EPSILON);
		Assert.assertEquals(CharUtils.parseNumber("1 ".toCharArray(),0,value,CharUtils.PREF_ANY,false),1);			Assert.assertEquals(value[0],1,EPSILON);
		Assert.assertEquals(CharUtils.parseNumber("123456".toCharArray(),0,value,CharUtils.PREF_ANY,false),6);		Assert.assertEquals(value[0],123456,EPSILON);
		Assert.assertEquals(CharUtils.parseNumber("12345678901234567890".toCharArray(),0,value,CharUtils.PREF_ANY,false),20);		Assert.assertEquals(Double.longBitsToDouble(value[0]),12345678901234567890.0,EPSILON);
		
		Assert.assertEquals(CharUtils.parseNumber("0.1".toCharArray(),0,value,CharUtils.PREF_ANY,false),3);			Assert.assertEquals(Double.longBitsToDouble(value[0]),0.1,EPSILON);
		Assert.assertEquals(CharUtils.parseNumber("0.0000000000000000001".toCharArray(),0,value,CharUtils.PREF_ANY,false),21);			Assert.assertEquals(Double.longBitsToDouble(value[0]),0.0000000000000000001,EPSILON);
		Assert.assertEquals(CharUtils.parseNumber("2E10".toCharArray(),0,value,CharUtils.PREF_ANY,false),4);		Assert.assertEquals(Double.longBitsToDouble(value[0]),2E10,EPSILON);
		Assert.assertEquals(CharUtils.parseNumber("1.2E10".toCharArray(),0,value,CharUtils.PREF_ANY,false),6);		Assert.assertEquals(Double.longBitsToDouble(value[0]),1.2E10,EPSILON);
		Assert.assertEquals(CharUtils.parseNumber("1.2E+10".toCharArray(),0,value,CharUtils.PREF_ANY,false),7);		Assert.assertEquals(Double.longBitsToDouble(value[0]),1.2E10,EPSILON);
		Assert.assertEquals(CharUtils.parseNumber("1.2E-10".toCharArray(),0,value,CharUtils.PREF_ANY,false),7);		Assert.assertEquals(Double.longBitsToDouble(value[0]),1.2E-10,EPSILON);
		
		try{CharUtils.parseNumber(null,0,value,CharUtils.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseNumber("".toCharArray(),0,value,CharUtils.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseNumber("0".toCharArray(),1,value,CharUtils.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (1-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseNumber("0".toCharArray(),0,null,CharUtils.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseNumber("0".toCharArray(),0,new long[0],CharUtils.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (zero length 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseNumber("1.".toCharArray(),0,value,CharUtils.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (missing fractional)");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.parseNumber("1E".toCharArray(),0,value,CharUtils.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.parseNumber("1E-".toCharArray(),0,value,CharUtils.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.parseNumber("1E+".toCharArray(),0,value,CharUtils.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.parseNumber(("1E"+(Double.MAX_EXPONENT+10)).toCharArray(),0,value,CharUtils.PREF_ANY,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.parseNumber("1234567890123456789012345".toCharArray(),0,value,CharUtils.PREF_INT|CharUtils.PREF_LONG,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.parseNumber("12345678901".toCharArray(),0,value,CharUtils.PREF_INT,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (SyntaxException exc) {
		}
		
	}

	@Test
	public void numberValidationTest() throws SyntaxException {
		Assert.assertEquals(8,CharUtils.validateNumber("1.2E-300".toCharArray(),0,CharUtils.PREF_DOUBLE,false));
		Assert.assertEquals(8,CharUtils.validateNumber("1.2E-30F".toCharArray(),0,CharUtils.PREF_FLOAT,false));
		Assert.assertEquals(19,CharUtils.validateNumber("1234567890123456789".toCharArray(),0,CharUtils.PREF_LONG,false));
		Assert.assertEquals(9,CharUtils.validateNumber("123456789".toCharArray(),0,CharUtils.PREF_INT,false));

		Assert.assertTrue(CharUtils.validateNumber("1.2E-300".toCharArray(),0,CharUtils.PREF_INT,false) < 0);
		Assert.assertTrue(CharUtils.validateNumber("1.2E-300".toCharArray(),0,CharUtils.PREF_LONG,false) < 0);
		Assert.assertTrue(CharUtils.validateNumber("1.2E-300".toCharArray(),0,CharUtils.PREF_FLOAT,false) < 0);
	}	
	
	@Test
	public void numberPrintingTest() {
		final char[]	content = new char[100], emptyContent = new char[1];
		
		Assert.assertEquals(Long.valueOf(new String(content,0,CharUtils.printLong(content,0,0,true))).longValue(),0L);
		Assert.assertEquals(Long.valueOf(new String(content,0,CharUtils.printLong(content,0,100,true))).longValue(),100L);
		Assert.assertEquals(Long.valueOf(new String(content,0,CharUtils.printLong(content,0,-100,true))).longValue(),-100L);
		Assert.assertEquals(Long.valueOf(new String(content,0,CharUtils.printLong(content,0,Long.MAX_VALUE,true))).longValue(),Long.MAX_VALUE);
		Assert.assertEquals(Long.valueOf(new String(content,0,CharUtils.printLong(content,0,Long.MIN_VALUE,true))).longValue(),Long.MIN_VALUE);
		
		Assert.assertEquals(CharUtils.printLong(emptyContent,0,0,true),1);
		Assert.assertEquals(CharUtils.printLong(emptyContent,0,100,true),-3);
		Assert.assertEquals(CharUtils.printLong(emptyContent,0,-100,true),-4);
		
		try{CharUtils.printLong(null,0,0,true);
			Assert.fail("mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.printLong(new char[0],0,0,true);
			Assert.fail("mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.printLong(content,-1,0,true);
			Assert.fail("mandatory exception was not detected (2-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(Double.valueOf(new String(content,0,CharUtils.printDouble(content,0,0,true))).doubleValue(),0,0.0001);
		Assert.assertEquals(Double.valueOf(new String(content,0,CharUtils.printDouble(content,0,100,true))).doubleValue(),100,0.0001);
		Assert.assertEquals(Double.valueOf(new String(content,0,CharUtils.printDouble(content,0,-100,true))).doubleValue(),-100,0.0001);
		Assert.assertEquals(Double.valueOf(new String(content,0,CharUtils.printDouble(content,0,0.001,true))).doubleValue(),0.001,0.0000001);
		Assert.assertEquals(Double.valueOf(new String(content,0,CharUtils.printDouble(content,0,-0.001,true))).doubleValue(),-0.001,0.0000001);
		Assert.assertEquals(Double.valueOf(new String(content,0,CharUtils.printDouble(content,0,Double.MAX_VALUE,true))).doubleValue(),Double.MAX_VALUE,0.0001E308);
		Assert.assertEquals(Double.valueOf(new String(content,0,CharUtils.printDouble(content,0,Double.MIN_VALUE,true))).doubleValue(),Double.MIN_VALUE,10000E-308);

		Assert.assertEquals(CharUtils.printDouble(emptyContent,0,0,true),1);
		Assert.assertEquals(CharUtils.printDouble(emptyContent,0,100,true),-3);
		Assert.assertEquals(CharUtils.printDouble(emptyContent,0,-100,true),-4);
		
		try{CharUtils.printDouble(null,0,0,true);
			Assert.fail("mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.printDouble(new char[0],0,0,true);
			Assert.fail("mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.printDouble(content,-1,0,true);
			Assert.fail("mandatory exception was not detected (2-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
	}
 
	@Test
	public void charConversionTest() {
		Assert.assertFalse(CharUtils.symbolNeedsEscaping(' ',false));
		Assert.assertTrue(CharUtils.symbolNeedsEscaping('\n',false));
		Assert.assertTrue(CharUtils.symbolNeedsEscaping('\u2020',true));
		Assert.assertFalse(CharUtils.symbolNeedsEscaping('\u2020',false));
		
		final char[]	result = new char[1];
		
		Assert.assertEquals(1,CharUtils.parseEscapedChar(" ".toCharArray(),0,result));
		Assert.assertEquals(' ',result[0]);
		Assert.assertEquals(2,CharUtils.parseEscapedChar("\\n".toCharArray(),0,result));
		Assert.assertEquals('\n',result[0]);
		Assert.assertEquals(2,CharUtils.parseEscapedChar("\\b".toCharArray(),0,result));
		Assert.assertEquals('\b',result[0]);
		Assert.assertEquals(2,CharUtils.parseEscapedChar("\\f".toCharArray(),0,result));
		Assert.assertEquals('\f',result[0]);
		Assert.assertEquals(2,CharUtils.parseEscapedChar("\\r".toCharArray(),0,result));
		Assert.assertEquals('\r',result[0]);
		Assert.assertEquals(2,CharUtils.parseEscapedChar("\\t".toCharArray(),0,result));
		Assert.assertEquals('\t',result[0]);
		Assert.assertEquals(2,CharUtils.parseEscapedChar("\\\"".toCharArray(),0,result));
		Assert.assertEquals('\"',result[0]);
		Assert.assertEquals(2,CharUtils.parseEscapedChar("\\'".toCharArray(),0,result));
		Assert.assertEquals('\'',result[0]);
		Assert.assertEquals(2,CharUtils.parseEscapedChar("\\\\".toCharArray(),0,result));
		Assert.assertEquals('\\',result[0]);
		Assert.assertEquals(4,CharUtils.parseEscapedChar("\\030".toCharArray(),0,result));
		Assert.assertEquals('\030',result[0]);
		Assert.assertEquals(6,CharUtils.parseEscapedChar("\\u20Fe".toCharArray(),0,result));
		Assert.assertEquals('\u20Fe',result[0]);

		try{CharUtils.parseEscapedChar(null,0,result);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseEscapedChar(new char[0],0,result);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseEscapedChar(" ".toCharArray(),-1,result);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseEscapedChar(" ".toCharArray(),100,result);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseEscapedChar(" ".toCharArray(),0,null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{CharUtils.parseEscapedChar("\\".toCharArray(),0,result);
			Assert.fail("Mandatory exception was not detected (truncated escape)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseEscapedChar("\\Z".toCharArray(),0,result);
			Assert.fail("Mandatory exception was not detected (unknown escape)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseEscapedChar("\\03".toCharArray(),0,result);
			Assert.fail("Mandatory exception was not detected (truncated octal code)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseEscapedChar("\\038".toCharArray(),0,result);
			Assert.fail("Mandatory exception was not detected (illegal octal digit)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseEscapedChar("\\u200Z".toCharArray(),0,result);
			Assert.fail("Mandatory exception was not detected (illegal unicode digit)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	@Test
	public void stringConversionTest() {
		final int[]			location = new int[2];
		final StringBuilder	sb = new StringBuilder();
		
		Assert.assertEquals(CharUtils.parseUnescapedString("'source'".toCharArray(),1,'\'',true,location),8);	Assert.assertArrayEquals(location, new int[]{1,6});
		Assert.assertEquals(CharUtils.parseUnescapedString("''".toCharArray(),1,'\'',true,location),2);			Assert.assertArrayEquals(location, new int[]{1,0});
		Assert.assertEquals(CharUtils.parseUnescapedString("'12\\n'".toCharArray(),1,'\'',true,location),-3);	Assert.assertArrayEquals(location, new int[]{1,2});

		try{CharUtils.parseUnescapedString(null,0,'\'',false,location);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseUnescapedString("".toCharArray(),0,'\'',false,location);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseUnescapedString("''".toCharArray(),10,'\'',false,location);
			Assert.fail("Mandatory exception was not detected (2-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseUnescapedString("''".toCharArray(),0,'\'',false,null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseUnescapedString("''".toCharArray(),0,'\'',false,new int[0]);
			Assert.fail("Mandatory exception was not detected (empty 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseUnescapedString("'".toCharArray(),1,'\'',false,location);
			Assert.fail("Mandatory exception was not detected (unclosed quota)");
		} catch (IllegalArgumentException exc) {
		}
		
		sb.setLength(0);	Assert.assertEquals(CharUtils.parseString("'source'".toCharArray(),1,'\'',sb),8);		Assert.assertEquals(sb.toString(),"source");
		sb.setLength(0);	Assert.assertEquals(CharUtils.parseString("''".toCharArray(),1,'\'',sb),2);				Assert.assertEquals(sb.toString(),"");
		sb.setLength(0);	Assert.assertEquals(CharUtils.parseString("'sou\\nrce'".toCharArray(),1,'\'',sb),10);	Assert.assertEquals(sb.toString(),"sou\nrce");
		sb.setLength(0);	Assert.assertEquals(CharUtils.parseString("'\\\"\\\\\\/\\b\\f\\n\\r\\t\\ucAf0'".toCharArray(),1,'\'',sb),24);	Assert.assertEquals(sb.toString(),"\"\\/\b\f\n\r\t\ucAf0");
			
		try{CharUtils.parseString(null,0,'\'',sb);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseString("".toCharArray(),0,'\'',sb);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseString("''".toCharArray(),10,'\'',sb);
			Assert.fail("Mandatory exception was not detected (2-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseString("''".toCharArray(),0,'\'',null);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (NullPointerException exc) {
		}
		try{CharUtils.parseString("'".toCharArray(),1,'\'',sb);
			Assert.fail("Mandatory exception was not detected (unclosed quota)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseString("'\\z'".toCharArray(),1,'\'',sb);
			Assert.fail("Mandatory exception was not detected (unsupported escaping)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseString("'\\uMZINANA'".toCharArray(),1,'\'',sb);
			Assert.fail("Mandatory exception was not detected (illegal escaping chars)");
		} catch (IllegalArgumentException exc) {
		}

		sb.setLength(0);	Assert.assertEquals(CharUtils.parseStringExtended("'source'".toCharArray(),1,'\'',sb),8);		Assert.assertEquals(sb.toString(),"source");
		sb.setLength(0);	Assert.assertEquals(CharUtils.parseStringExtended("''".toCharArray(),1,'\'',sb),2);				Assert.assertEquals(sb.toString(),"");
		sb.setLength(0);	Assert.assertEquals(CharUtils.parseStringExtended("'sou\\nrce'".toCharArray(),1,'\'',sb),10);	Assert.assertEquals(sb.toString(),"sou\nrce");
		sb.setLength(0);	Assert.assertEquals(CharUtils.parseStringExtended("'\\\"\\\\\\/\\b\\f\\n\\r\\t\\ucAf0'".toCharArray(),1,'\'',sb),24);	Assert.assertEquals(sb.toString(),"\"\\/\b\f\n\r\t\ucAf0");
		sb.setLength(0);	Assert.assertEquals(CharUtils.parseStringExtended("'\\076\\0x4d'".toCharArray(),1,'\'',sb),11);	Assert.assertEquals(sb.toString(),">M");
			
		try{CharUtils.parseStringExtended(null,0,'\'',sb);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseStringExtended("".toCharArray(),0,'\'',sb);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseStringExtended("''".toCharArray(),10,'\'',sb);
			Assert.fail("Mandatory exception was not detected (2-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseStringExtended("''".toCharArray(),0,'\'',null);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (NullPointerException exc) {
		}
		try{CharUtils.parseStringExtended("'".toCharArray(),1,'\'',sb);
			Assert.fail("Mandatory exception was not detected (unclosed quota)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseStringExtended("'\\z'".toCharArray(),1,'\'',sb);
			Assert.fail("Mandatory exception was not detected (unsupported escaping)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseStringExtended("'\\uMZINANA'".toCharArray(),1,'\'',sb);
			Assert.fail("Mandatory exception was not detected (illegal escaping chars)");
		} catch (IllegalArgumentException exc) {
		}
		
		final char[]	target = new char[10];
		
		Arrays.fill(target,' ');
		Assert.assertEquals(1,CharUtils.printEscapedChar(target,0,'0',true,true));
		Assert.assertArrayEquals("0         ".toCharArray(),target);

		Arrays.fill(target,' ');
		Assert.assertEquals(2,CharUtils.printEscapedChar(target,0,'\n',true,true));
		Assert.assertArrayEquals("\\n        ".toCharArray(),target);

		Arrays.fill(target,' ');
		Assert.assertEquals(6,CharUtils.printEscapedChar(target,0,'\u2020',true,true));
		Assert.assertArrayEquals("\\u2020    ".toCharArray(),target);
		
		try{CharUtils.printEscapedChar(null,0,'\u2020',true,true);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.printEscapedChar(new char[0],0,'\u2020',true,true);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.printEscapedChar(target,-1,'\u2020',true,true);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.printEscapedChar(target,100,'\u2020',true,true);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		Arrays.fill(target,' ');
		Assert.assertEquals(1,CharUtils.printEscapedString(target,0,"0",true,true));
		Assert.assertArrayEquals("0         ".toCharArray(),target);

		Arrays.fill(target,' ');
		Assert.assertEquals(2,CharUtils.printEscapedString(target,0,"\n",true,true));
		Assert.assertArrayEquals("\\n        ".toCharArray(),target);

		Arrays.fill(target,' ');
		Assert.assertEquals(6,CharUtils.printEscapedString(target,0,"\u2020",true,true));
		Assert.assertArrayEquals("\\u2020    ".toCharArray(),target);

		try{CharUtils.printEscapedString(null,0," ",true,true);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.printEscapedString(new char[0],0," ",true,true);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.printEscapedString(target,-1," ",true,true);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.printEscapedString(target,100," ",true,true);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.printEscapedString(target,0,null,true,true);
			Assert.fail("Mandatory exception was not detected (null 3-dr argument)");
		} catch (IllegalArgumentException exc) {
		}
	
	}

	@Test
	public void enumConversionTest() {
		final PseudoConversionEnum[]	data = new PseudoConversionEnum[1]; 
		
		Assert.assertEquals(CharUtils.parseEnum("enum1".toCharArray(),0,PseudoConversionEnum.class,data),5);	Assert.assertEquals(data[0],PseudoConversionEnum.enum1);
		Assert.assertEquals(CharUtils.parseEnum("enum2".toCharArray(),0,PseudoConversionEnum.class,data),5);	Assert.assertEquals(data[0],PseudoConversionEnum.enum2);

		try{CharUtils.parseEnum(null,0,PseudoConversionEnum.class,data);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseEnum("".toCharArray(),0,PseudoConversionEnum.class,data);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseEnum("enum1".toCharArray(),10,PseudoConversionEnum.class,data);
			Assert.fail("Mandatory exception was not detected (2-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseEnum("enum1".toCharArray(),0,null,data);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{CharUtils.parseEnum("enum1".toCharArray(),0,PseudoConversionEnum.class,null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseEnum("enum1".toCharArray(),0,PseudoConversionEnum.class,new PseudoConversionEnum[0]);
			Assert.fail("Mandatory exception was not detected (empty 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseEnum("enum3".toCharArray(),0,PseudoConversionEnum.class,data);
			Assert.fail("Mandatory exception was not detected (enum constant not exists)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void nameConversionTest() {
		final int[]		location = new int[2];
		
		Assert.assertEquals(CharUtils.parseName("name_123".toCharArray(),0,location),8);
		Assert.assertArrayEquals(location,new int[]{0,7});

		try{CharUtils.parseName(null,0,location);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseName("".toCharArray(),0,location);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseName("enum1".toCharArray(),10,location);
			Assert.fail("Mandatory exception was not detected (2-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseName("enum1".toCharArray(),0,null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.parseName("enum1".toCharArray(),0,new int[1]);
			Assert.fail("Mandatory exception was not detected (illegal size of 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	@Test
	public void skipBlankTest() {
		Assert.assertEquals(0,CharUtils.skipBlank("test".toCharArray(),0,false));
		Assert.assertEquals(1,CharUtils.skipBlank(" test".toCharArray(),0,false));
		Assert.assertEquals(1,CharUtils.skipBlank("\ntest".toCharArray(),0,false));
		Assert.assertEquals(0,CharUtils.skipBlank("\ntest".toCharArray(),0,true));
		Assert.assertEquals(2,CharUtils.skipBlank(" \n".toCharArray(),0,false));

		try{CharUtils.skipBlank(null,0,true);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}		
		try{CharUtils.skipBlank("".toCharArray(),0,true);
			Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CharUtils.skipBlank(" ".toCharArray(),-1,true);
			Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CharUtils.skipBlank("".toCharArray(),0,true);
			Assert.fail("Mandatory exception was not detected (2-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}		
	}
	
	@Test
	public void compareTest() {
		Assert.assertTrue(CharUtils.compare("test string".toCharArray(), 5, "str".toCharArray()));
		Assert.assertTrue(CharUtils.compare("test string".toCharArray(), 5, "string".toCharArray()));
		Assert.assertTrue(CharUtils.compare("test string".toCharArray(), 5, "".toCharArray()));
		Assert.assertFalse(CharUtils.compare("test string".toCharArray(), 5, "str1".toCharArray()));
	
		try{CharUtils.compare(null, 5, "str".toCharArray());
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CharUtils.compare("".toCharArray(), 5, "str".toCharArray());
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CharUtils.compare("test string".toCharArray(), 11, "str".toCharArray());
			Assert.fail("Mandatory exception was not detected (2-nd argument out of bounds)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CharUtils.compare("test string".toCharArray(), 5, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}		

		Assert.assertTrue(CharUtils.compare("test string".toCharArray(), 5, "str".toCharArray(), 0, 3));
		Assert.assertTrue(CharUtils.compare("test string".toCharArray(), 5, "string".toCharArray(), 0, 6));
		Assert.assertTrue(CharUtils.compare("test string".toCharArray(), 5, "".toCharArray(), 0, 0));
		Assert.assertFalse(CharUtils.compare("test string".toCharArray(), 5, "str1".toCharArray(), 0, 4));

		try{CharUtils.compare(null, 5, "str".toCharArray(), 0, 3);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CharUtils.compare("".toCharArray(), 5, "str".toCharArray(), 0, 3);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CharUtils.compare("test string".toCharArray(), 11, "str".toCharArray(), 0, 3);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of bounds)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CharUtils.compare("test string".toCharArray(), 5, null, 0, 3);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}		
		try{CharUtils.compare("test string".toCharArray(), 5, "str".toCharArray(), 3, 3);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CharUtils.compare("test string".toCharArray(), 5, "str".toCharArray(), 0, 4);
			Assert.fail("Mandatory exception was not detected (5-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}		
	}

	@Test
	public void extractTest() throws SyntaxException {
		final Object[]	result= new Object[10];
		
		// Constants (extract)
		Assert.assertEquals(3,CharUtils.extract("text".toCharArray(),0,result,'t','e','x'));	
		Assert.assertEquals(4,CharUtils.extract("text".toCharArray(),0,result,"text".toCharArray()));
		
		try{CharUtils.extract("text".toCharArray(),0,result,'z');
			Assert.fail("Mandatory exception was not detected (missing 'z')");
		} catch (SyntaxException exc) {
		}
		try{CharUtils.extract("text".toCharArray(),0,result,"z".toCharArray());
			Assert.fail("Mandatory exception was not detected (missing 'z')");
		} catch (SyntaxException exc) {
		}

		// Constants (tyExtract)
		Assert.assertEquals(3,CharUtils.tryExtract("text".toCharArray(),0,'t','e','x'));	
		Assert.assertEquals(4,CharUtils.tryExtract("text".toCharArray(),0,"text".toCharArray()));
		Assert.assertEquals(-2,CharUtils.tryExtract("try".toCharArray(),0,'t','e','x'));
		Assert.assertEquals(-1,CharUtils.tryExtract("try".toCharArray(),0,"text".toCharArray()));
		
		// Lexemas (extract)
		Assert.assertEquals(3,CharUtils.extract("120".toCharArray(),0,result,ArgumentType.ordinalInt));	
		Assert.assertEquals(120,((Integer)result[0]).intValue());
		
		Assert.assertEquals(2,CharUtils.extract("CC".toCharArray(),0,result,ArgumentType.hexInt));
		Assert.assertEquals(204,((Integer)result[0]).intValue());

		Assert.assertEquals(3,CharUtils.extract("120".toCharArray(),0,result,ArgumentType.ordinalLong));
		Assert.assertEquals(120,((Long)result[0]).longValue());
		
		Assert.assertEquals(2,CharUtils.extract("CC".toCharArray(),0,result,ArgumentType.hexLong));
		Assert.assertEquals(204,((Long)result[0]).longValue());

		Assert.assertEquals(4,CharUtils.extract("12.5".toCharArray(),0,result,ArgumentType.ordinalFloat));
		Assert.assertEquals(12.5f,((Float)result[0]).floatValue(),0.0001f);

		Assert.assertEquals(4,CharUtils.extract("test".toCharArray(),0,result,ArgumentType.name));
		Assert.assertEquals("test",result[0].toString());

		Assert.assertEquals(9,CharUtils.extract("test-test".toCharArray(),0,result,ArgumentType.hyphenedName));
		Assert.assertEquals("test-test",result[0].toString());

		Assert.assertEquals(4,CharUtils.extract("test".toCharArray(),0,result,ArgumentType.simpleTerminatedString));
		Assert.assertEquals("test",result[0].toString());
		Assert.assertEquals(6,CharUtils.extract("'test'".toCharArray(),0,result,ArgumentType.simpleTerminatedString));
		Assert.assertEquals("test",result[0].toString());
		Assert.assertEquals(6,CharUtils.extract("\"test\"".toCharArray(),0,result,ArgumentType.simpleTerminatedString));
		Assert.assertEquals("test",result[0].toString());

		Assert.assertEquals(4,CharUtils.extract("test".toCharArray(),0,result,ArgumentType.specialTerminatedString));
		Assert.assertEquals("test",result[0].toString());
		Assert.assertEquals(6,CharUtils.extract("'test'".toCharArray(),0,result,ArgumentType.specialTerminatedString));
		Assert.assertEquals("test",result[0].toString());
		Assert.assertEquals(6,CharUtils.extract("\"test\"".toCharArray(),0,result,ArgumentType.specialTerminatedString));
		Assert.assertEquals("test",result[0].toString());

		Assert.assertEquals(5,CharUtils.extract("black".toCharArray(),0,result,ArgumentType.colorRepresentation));
		Assert.assertEquals(Color.BLACK,result[0]);
		Assert.assertEquals(7,CharUtils.extract("#FFFFFF".toCharArray(),0,result,ArgumentType.colorRepresentation));
		Assert.assertEquals(Color.WHITE,result[0]);
		
		// Lexemas (tryExtract)
		Assert.assertEquals(3,CharUtils.tryExtract("120".toCharArray(),0,ArgumentType.ordinalInt));	
		
		Assert.assertEquals(2,CharUtils.tryExtract("CC".toCharArray(),0,ArgumentType.hexInt));

		Assert.assertEquals(3,CharUtils.tryExtract("120".toCharArray(),0,ArgumentType.ordinalLong));
		
		Assert.assertEquals(2,CharUtils.tryExtract("CC".toCharArray(),0,ArgumentType.hexLong));

		Assert.assertEquals(4,CharUtils.tryExtract("12.5".toCharArray(),0,ArgumentType.ordinalFloat));

		Assert.assertEquals(4,CharUtils.tryExtract("test".toCharArray(),0,ArgumentType.name));

		Assert.assertEquals(9,CharUtils.tryExtract("test-test".toCharArray(),0,ArgumentType.hyphenedName));

		Assert.assertEquals(4,CharUtils.tryExtract("test".toCharArray(),0,ArgumentType.simpleTerminatedString));
		Assert.assertEquals(6,CharUtils.tryExtract("'test'".toCharArray(),0,ArgumentType.simpleTerminatedString));
		Assert.assertEquals(6,CharUtils.tryExtract("\"test\"".toCharArray(),0,ArgumentType.simpleTerminatedString));

		Assert.assertEquals(4,CharUtils.tryExtract("test".toCharArray(),0,ArgumentType.specialTerminatedString));
		Assert.assertEquals(6,CharUtils.tryExtract("'test'".toCharArray(),0,ArgumentType.specialTerminatedString));
		Assert.assertEquals(6,CharUtils.tryExtract("\"test\"".toCharArray(),0,ArgumentType.specialTerminatedString));

		Assert.assertEquals(5,CharUtils.tryExtract("black".toCharArray(),0,ArgumentType.colorRepresentation));
		Assert.assertEquals(7,CharUtils.tryExtract("#FFFFFF".toCharArray(),0,ArgumentType.colorRepresentation));
		
		// Complex test
		Assert.assertEquals(13,CharUtils.extract("test 120 test".toCharArray(),0,result,"test".toCharArray(),ArgumentType.ordinalInt,"test".toCharArray()));	// Complex
		Assert.assertEquals(120,((Integer)result[0]).intValue());

		Assert.assertEquals(13,CharUtils.tryExtract("test 120 test".toCharArray(),0,"test".toCharArray(),ArgumentType.ordinalInt,"test".toCharArray()));	// Complex
		
		// Invalid arguments (extract)
		try{CharUtils.extract(null,0,result,"z".toCharArray());		
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.extract("".toCharArray(),0,result,"z".toCharArray());
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{CharUtils.extract("test".toCharArray(),-1,result,"z".toCharArray());
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.extract("test".toCharArray(),10,result,"z".toCharArray());
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{CharUtils.extract("test".toCharArray(),0,null,"z".toCharArray());
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}

		try{CharUtils.extract("test".toCharArray(),0,result,(Object[])null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.extract("test".toCharArray(),0,result);
			Assert.fail("Mandatory exception was not detected (empty 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{CharUtils.extract("test".toCharArray(),0,new Object[0],ArgumentType.hexLong);
			Assert.fail("Mandatory exception was not detected (3-rd argument is too small)");
		} catch (IllegalArgumentException exc) {
		}

		// Invalid arguments (tryExtract)
		try{CharUtils.tryExtract(null,0,"z".toCharArray());		
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.tryExtract("".toCharArray(),0,"z".toCharArray());
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{CharUtils.tryExtract("test".toCharArray(),-1,"z".toCharArray());
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.tryExtract("test".toCharArray(),10,"z".toCharArray());
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{CharUtils.tryExtract("test".toCharArray(),0,(Object[])null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.tryExtract("test".toCharArray(),0);
			Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
	}	
	
	
	@Test
	public void substitutionTest() {
		Assert.assertNull(CharUtils.substitute("key",null,(key)->{return "";}));
		Assert.assertEquals(CharUtils.substitute("key","value",(key)->{return "";}),"value");
		Assert.assertEquals(CharUtils.substitute("key","before${key2}",(key)->{return "value2";}),"beforevalue2");
		Assert.assertEquals(CharUtils.substitute("key","${key2}after",(key)->{return "value2";}),"value2after");
		Assert.assertEquals(CharUtils.substitute("key","before${key2}after",(key)->{return "value2";}),"beforevalue2after");
		Assert.assertEquals(CharUtils.substitute("key","before${key2}inside${key2}after",(key)->{return "value2";}),"beforevalue2insidevalue2after");
		Assert.assertEquals(CharUtils.substitute("key","before${key2}inside${key2}after",(key)->{return null;}),"before${key2}inside${key2}after");
		Assert.assertEquals(CharUtils.substitute("key","before${${key1}}inside${${key1}}after",(key)->{return "key1".equals(key) ? "key2" : "value2";}),"beforevalue2insidevalue2after");

		Assert.assertArrayEquals(CharUtils.substitute("key","before${${key1}}inside${${key1}}after".toCharArray(),0,"before${${key1}}inside${${key1}}after".length(),
					(key,from,to)->{return "key1".equals(new String(key,from,to-from)) ? "key2".toCharArray() : "value2".toCharArray();})
					,"beforevalue2insidevalue2after".toCharArray());
		
		
		try{CharUtils.substitute("key","before${key2}after",null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}		
		try{CharUtils.substitute("key","before${key2after",(key)->{return "value2";});
			Assert.fail("Mandatory exception was not detected (missing '}' in the value)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CharUtils.substitute("key","before${}after",(key)->{return "value2";});
			Assert.fail("Mandatory exception was not detected (empty '${}' in the value)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CharUtils.substitute("key","before${key2after",(key)->{return "${key2}";});
			Assert.fail("Mandatory exception was not detected (recursive substitution)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{CharUtils.substitute("key","before${key2}after".toCharArray(),0,1,null);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (NullPointerException exc) {
		}		
		try{CharUtils.substitute("key","before${key2}after".toCharArray(),100,1,(key,from,to)->{return "value2".toCharArray();});
			Assert.fail("Mandatory exception was not detected (from out of range)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CharUtils.substitute("key","before${key2}after".toCharArray(),0,100,(key,from,to)->{return "value2".toCharArray();});
			Assert.fail("Mandatory exception was not detected (from+length out of range)");
		} catch (IllegalArgumentException exc) {
		}		
	}

	@Test
	public void splitTest() {
		String[]	content;
		
		Assert.assertArrayEquals(new String[]{"first","second"},CharUtils.split("first,second",','));
		
		Assert.assertArrayEquals(new String[]{"first","","second"},CharUtils.split("first,,second",','));
		Assert.assertArrayEquals(new String[]{"first","second"},CharUtils.split("first,,second",',',false,true));
		Assert.assertArrayEquals(new String[]{"first","","second"},CharUtils.split("first,,second",',',true,false));
		
		Assert.assertArrayEquals(new String[]{"","second"},CharUtils.split(",second",','));
		Assert.assertArrayEquals(new String[]{"second"},CharUtils.split(",second",',',false,true));
		Assert.assertArrayEquals(new String[]{"second"},CharUtils.split(",second",',',true,false));
		
		Assert.assertArrayEquals(new String[]{"first",""},CharUtils.split("first,",','));
		Assert.assertArrayEquals(new String[]{"first"},CharUtils.split("first,",',',false,true));
		Assert.assertArrayEquals(new String[]{"first"},CharUtils.split("first,",',',true,false));
		
		Assert.assertArrayEquals(new String[]{"first"},CharUtils.split("first",','));
		
		try{CharUtils.split(null,',');
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals(-2,CharUtils.split("first,second",',',new String[0]));
		Assert.assertEquals(2,CharUtils.split("first,second",',',content = new String[2]));
		Assert.assertArrayEquals(new String[]{"first","second"},content);

		try{CharUtils.split(null,',', new String[1]);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CharUtils.split("test",',',null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertArrayEquals(new String[]{"first","second"},CharUtils.split("first::second","::"));
		
		Assert.assertArrayEquals(new String[]{"first","","second"},CharUtils.split("first::::second","::"));
		Assert.assertArrayEquals(new String[]{"first","second"},CharUtils.split("first::::second","::",false,true));
		Assert.assertArrayEquals(new String[]{"first","","second"},CharUtils.split("first::::second","::",true,false));
		
		Assert.assertArrayEquals(new String[]{"","second"},CharUtils.split("::second","::"));
		Assert.assertArrayEquals(new String[]{"second"},CharUtils.split("::second","::",false,true));
		Assert.assertArrayEquals(new String[]{"second"},CharUtils.split("::second","::",true,false));
		
		Assert.assertArrayEquals(new String[]{"first",""},CharUtils.split("first::","::"));
		Assert.assertArrayEquals(new String[]{"first"},CharUtils.split("first::","::",false,true));
		Assert.assertArrayEquals(new String[]{"first"},CharUtils.split("first::","::",true,false));
		
		Assert.assertArrayEquals(new String[]{"first"},CharUtils.split("first","::"));

		Assert.assertEquals(-2,CharUtils.split("first::second","::",new String[0]));
		Assert.assertEquals(2,CharUtils.split("first::second","::",content = new String[2]));
		Assert.assertArrayEquals(new String[]{"first","second"},content);
		
		try{CharUtils.split(null,"::");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CharUtils.split("test",null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.split("test","");
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.split(null,"::",new String[0]);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CharUtils.split("test",null,new String[0]);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.split("test","",new String[0]);
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.split("test","splitter",null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void joinTest() {
		Assert.assertArrayEquals("test1,test2".toCharArray(),CharUtils.join(",".toCharArray(),"test1".toCharArray(),"test2".toCharArray()));
		Assert.assertArrayEquals("test1".toCharArray(),CharUtils.join(",".toCharArray(),"test1".toCharArray()));
		
		try{CharUtils.join(null,"test1".toCharArray());
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.join("".toCharArray(),"test1".toCharArray());
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.join(",".toCharArray(),(char[][])null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{CharUtils.join(",".toCharArray(),(char[])null);
			Assert.fail("Mandatory exception was not detected (nulls inside 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals("test1,test2",CharUtils.join(",","test1","test2"));
		Assert.assertEquals("test1",CharUtils.join(",","test1"));
		
		try{CharUtils.join(null,"test1");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.join("","test1");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.join(",",(String[])null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{CharUtils.join(",",(String)null);
			Assert.fail("Mandatory exception was not detected (nulls inside 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
	}
	
	@Test
	public void likeTest() {
		Assert.assertEquals(5,CharUtils.like("first".toCharArray(),"first".toCharArray(),0));
		Assert.assertEquals(5,CharUtils.like("first".toCharArray(),"fi?st".toCharArray(),0));
		Assert.assertEquals(5,CharUtils.like("first".toCharArray(),"?irst".toCharArray(),0));
		Assert.assertEquals(5,CharUtils.like("first".toCharArray(),"firs?".toCharArray(),0));
		Assert.assertTrue(CharUtils.like("first".toCharArray(),"irst".toCharArray(),1) >= 0);
		Assert.assertTrue(CharUtils.like("first".toCharArray(),"firs".toCharArray(),0) < 0);
		Assert.assertTrue(CharUtils.like("firs".toCharArray(),"first".toCharArray(),0) < 0);
		
		Assert.assertEquals(5,CharUtils.like("first".toCharArray(),"*".toCharArray(),0));
		Assert.assertEquals(5,CharUtils.like("first".toCharArray(),"f*".toCharArray(),0));
		Assert.assertEquals(5,CharUtils.like("first".toCharArray(),"f*t".toCharArray(),0));
		Assert.assertEquals(5,CharUtils.like("first".toCharArray(),"f*s*".toCharArray(),0));
		Assert.assertEquals(5,CharUtils.like("first".toCharArray(),"f*r*t".toCharArray(),0));
		Assert.assertEquals(5,CharUtils.like("first".toCharArray(),"*r*t".toCharArray(),0));
		Assert.assertEquals(5,CharUtils.like("first".toCharArray(),"*r*t".toCharArray(),1));
		Assert.assertTrue(CharUtils.like("first".toCharArray(),"*rs".toCharArray(),0) < 0);

		try{CharUtils.like(null,"first".toCharArray(),0);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CharUtils.like("first".toCharArray(),null,0);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{CharUtils.like("first".toCharArray(),"first".toCharArray(),-1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.like("first".toCharArray(),"first".toCharArray(),100);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void terminateAndConvertTest() {
		Assert.assertArrayEquals("test\n".toCharArray(),CharUtils.terminateAndConvert2CharArray("test",'\n'));
		Assert.assertArrayEquals("\n".toCharArray(),CharUtils.terminateAndConvert2CharArray("",'\n'));
		
		try{CharUtils.terminateAndConvert2CharArray(null,'\n');
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void escapeStringTest() {
		Assert.assertEquals("test",CharUtils.unescapeStringContent(CharUtils.escapeStringContent("test")));
		Assert.assertEquals("test\b\f\n\r\t\\\"\'\013\uACCAtest",CharUtils.unescapeStringContent(CharUtils.escapeStringContent("test\b\f\n\r\t\\\"\'\013\uACCAtest")));
		
		try{CharUtils.escapeStringContent(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CharUtils.unescapeStringContent(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Test
	public void charSequenceWrappersTest() {
		final char[]		content = "test string".toCharArray();
		
		innerTest(CharUtils.toCharSequence(content,0,content.length-1));
		innerTest(CharUtils.toWeakCharSequence(content,0,content.length-1));
		
		try{CharUtils.toCharSequence(null,0,content.length-1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CharUtils.toCharSequence(content,-1,content.length-1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.toCharSequence(content,content.length,content.length-1);
		Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.toCharSequence(content,0,-1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.toCharSequence(content,0,content.length);
		Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.toCharSequence(content,1,0);
			Assert.fail("Mandatory exception was not detected (3-rd argument less than 2-nd one)");
		} catch (IllegalArgumentException exc) {
		}

		try{CharUtils.toWeakCharSequence(null,0,content.length-1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CharUtils.toWeakCharSequence(content,-1,content.length-1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.toWeakCharSequence(content,content.length,content.length-1);
		Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.toWeakCharSequence(content,0,-1);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.toWeakCharSequence(content,0,content.length);
		Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharUtils.toWeakCharSequence(content,1,0);
			Assert.fail("Mandatory exception was not detected (3-rd argument less than 2-nd one)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	private static void innerTest(final CharSequence seq) {
		Assert.assertEquals(11,seq.length());
		Assert.assertEquals('t',seq.charAt(0));
		Assert.assertEquals('g',seq.charAt(10));
		Assert.assertEquals(seq,seq.subSequence(0,seq.length()-1));
		
		try{seq.charAt(-1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (StringIndexOutOfBoundsException exc) {
		}
		try{seq.charAt(666);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (StringIndexOutOfBoundsException exc) {
		}
		try{seq.subSequence(-1,seq.length()-1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (StringIndexOutOfBoundsException exc) {
		}
		try{seq.subSequence(666,seq.length()-1);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (StringIndexOutOfBoundsException exc) {
		}
		
		try{seq.subSequence(0,-1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (StringIndexOutOfBoundsException exc) {
		}
		try{seq.subSequence(1,666);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (StringIndexOutOfBoundsException exc) {
		}
		
		try{seq.subSequence(1,0);
			Assert.fail("Mandatory exception was not detected (3-rd argument less than 2-nd)");
		} catch (StringIndexOutOfBoundsException exc) {
		}
	}
}

enum PseudoConversionEnum {
	enum1, enum2
}
