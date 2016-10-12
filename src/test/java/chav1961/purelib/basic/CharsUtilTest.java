package chav1961.purelib.basic;

import org.junit.Assert;
import org.junit.Test;

public class CharsUtilTest {
	public static final double		EPSILON = 0.000000001;
	
	@Test
	public void intConversionTest() {
		final int[]		value = new int[1];
		
		Assert.assertEquals(CharsUtil.parseInt("0".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],0);
		Assert.assertEquals(CharsUtil.parseInt("1".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharsUtil.parseInt("1 ".toCharArray(),0,value,false),1);		Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharsUtil.parseInt("123456".toCharArray(),0,value,false),6);	Assert.assertEquals(value[0],123456);
		
		try{CharsUtil.parseInt(null,0,value,false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseInt("".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseInt("0".toCharArray(),1,value,false);
			Assert.fail("Mandatory exception was not detected (1-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseInt("0".toCharArray(),0,null,false);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseInt("0".toCharArray(),0,new int[0],false);
			Assert.fail("Mandatory exception was not detected (zero length 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseInt("1234567890123".toCharArray(),0,value,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertEquals(CharsUtil.parseIntExtended("0".toCharArray(),0,value,false),1);				Assert.assertEquals(value[0],0);
		Assert.assertEquals(CharsUtil.parseIntExtended("1".toCharArray(),0,value,false),1);				Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharsUtil.parseIntExtended("1 ".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharsUtil.parseIntExtended("123456".toCharArray(),0,value,false),6);		Assert.assertEquals(value[0],123456);
		Assert.assertEquals(CharsUtil.parseIntExtended("0123456".toCharArray(),0,value,false),7);		Assert.assertEquals(value[0],0123456);
		Assert.assertEquals(CharsUtil.parseIntExtended("0x123456".toCharArray(),0,value,false),8);		Assert.assertEquals(value[0],0x123456);
		Assert.assertEquals(CharsUtil.parseIntExtended("0xcafeCAFE".toCharArray(),0,value,false),10);	Assert.assertEquals(value[0],0xcafeCAFE);
		Assert.assertEquals(CharsUtil.parseIntExtended("0b10101010".toCharArray(),0,value,false),10);	Assert.assertEquals(value[0],0b10101010);

		try{CharsUtil.parseIntExtended(null,0,value,false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseIntExtended("".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseIntExtended("0".toCharArray(),1,value,false);
			Assert.fail("Mandatory exception was not detected (1-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseIntExtended("0".toCharArray(),0,null,false);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseIntExtended("0".toCharArray(),0,new int[0],false);
			Assert.fail("Mandatory exception was not detected (zero length 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseIntExtended("1234567890123".toCharArray(),0,value,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void longConversionTest() {
		final long[]		value = new long[1];
		
		Assert.assertEquals(CharsUtil.parseLong("0".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],0);
		Assert.assertEquals(CharsUtil.parseLong("1".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharsUtil.parseLong("1 ".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharsUtil.parseLong("123456".toCharArray(),0,value,false),6);		Assert.assertEquals(value[0],123456);
		
		try{CharsUtil.parseLong(null,0,value,false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseLong("".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseLong("0".toCharArray(),1,value,false);
			Assert.fail("Mandatory exception was not detected (1-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseLong("0".toCharArray(),0,null,false);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseLong("0".toCharArray(),0,new long[0],false);
			Assert.fail("Mandatory exception was not detected (zero length 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseLong("123456789012345678901234567890".toCharArray(),0,value,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertEquals(CharsUtil.parseLongExtended("0".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],0);
		Assert.assertEquals(CharsUtil.parseLongExtended("1".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharsUtil.parseLongExtended("1 ".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1);
		Assert.assertEquals(CharsUtil.parseLongExtended("123456".toCharArray(),0,value,false),6);		Assert.assertEquals(value[0],123456);
		Assert.assertEquals(CharsUtil.parseLongExtended("0123456".toCharArray(),0,value,false),7);		Assert.assertEquals(value[0],0123456);
		Assert.assertEquals(CharsUtil.parseLongExtended("0x123456".toCharArray(),0,value,false),8);		Assert.assertEquals(value[0],0x123456);
		Assert.assertEquals(CharsUtil.parseLongExtended("0xcafeCAFE".toCharArray(),0,value,false),10);	Assert.assertEquals(value[0],0xcafeCAFEL);
		Assert.assertEquals(CharsUtil.parseLongExtended("0b10101010".toCharArray(),0,value,false),10);	Assert.assertEquals(value[0],0b10101010);

		try{CharsUtil.parseLongExtended(null,0,value,false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseLongExtended("".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseLongExtended("0".toCharArray(),1,value,false);
			Assert.fail("Mandatory exception was not detected (1-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseLongExtended("0".toCharArray(),0,null,false);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseLongExtended("0".toCharArray(),0,new long[0],false);
			Assert.fail("Mandatory exception was not detected (zero length 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseLongExtended("123456789012345678901234567890".toCharArray(),0,value,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void doubleConversionTest() {
		final double[]		value = new double[1];
		
		Assert.assertEquals(CharsUtil.parseDouble("0".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],0,EPSILON);
		Assert.assertEquals(CharsUtil.parseDouble("1".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1,EPSILON);
		Assert.assertEquals(CharsUtil.parseDouble("1 ".toCharArray(),0,value,false),1);			Assert.assertEquals(value[0],1,EPSILON);
		Assert.assertEquals(CharsUtil.parseDouble("123456".toCharArray(),0,value,false),6);		Assert.assertEquals(value[0],123456,EPSILON);
		Assert.assertEquals(CharsUtil.parseDouble("12345678901234567890".toCharArray(),0,value,false),20);		Assert.assertEquals(value[0],12345678901234567890.0,EPSILON);
		
		Assert.assertEquals(CharsUtil.parseDouble("0.1".toCharArray(),0,value,false),3);		Assert.assertEquals(value[0],0.1,EPSILON);
		Assert.assertEquals(CharsUtil.parseDouble("0.0000000000000000001".toCharArray(),0,value,false),21);			Assert.assertEquals(value[0],0.0000000000000000001,EPSILON);
		Assert.assertEquals(CharsUtil.parseDouble("2E10".toCharArray(),0,value,false),4);		Assert.assertEquals(value[0],2E10,EPSILON);
		Assert.assertEquals(CharsUtil.parseDouble("1.2E10".toCharArray(),0,value,false),6);		Assert.assertEquals(value[0],1.2E10,EPSILON);
		Assert.assertEquals(CharsUtil.parseDouble("1.2E+10".toCharArray(),0,value,false),7);	Assert.assertEquals(value[0],1.2E10,EPSILON);
		Assert.assertEquals(CharsUtil.parseDouble("1.2E-10".toCharArray(),0,value,false),7);	Assert.assertEquals(value[0],1.2E-10,EPSILON);

		try{CharsUtil.parseDouble(null,0,value,false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseDouble("".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseDouble("0".toCharArray(),1,value,false);
			Assert.fail("Mandatory exception was not detected (1-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseDouble("0".toCharArray(),0,null,false);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseDouble("0".toCharArray(),0,new double[0],false);
			Assert.fail("Mandatory exception was not detected (zero length 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseDouble("1.".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (missing fractional)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseDouble("1E".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseDouble("1E-".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseDouble("1E+".toCharArray(),0,value,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseDouble("1E400".toCharArray(),0,value,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void numberConversionTest() {
		final long[]		value = new long[2];
		
		Assert.assertEquals(CharsUtil.parseNumber("0".toCharArray(),0,value,CharsUtil.PREF_ANY,false),1);			Assert.assertEquals(value[0],0,EPSILON);
		Assert.assertEquals(CharsUtil.parseNumber("1".toCharArray(),0,value,CharsUtil.PREF_ANY,false),1);			Assert.assertEquals(value[0],1,EPSILON);
		Assert.assertEquals(CharsUtil.parseNumber("1 ".toCharArray(),0,value,CharsUtil.PREF_ANY,false),1);			Assert.assertEquals(value[0],1,EPSILON);
		Assert.assertEquals(CharsUtil.parseNumber("123456".toCharArray(),0,value,CharsUtil.PREF_ANY,false),6);		Assert.assertEquals(value[0],123456,EPSILON);
		Assert.assertEquals(CharsUtil.parseNumber("12345678901234567890".toCharArray(),0,value,CharsUtil.PREF_ANY,false),20);		Assert.assertEquals(Double.longBitsToDouble(value[0]),12345678901234567890.0,EPSILON);
		
		Assert.assertEquals(CharsUtil.parseNumber("0.1".toCharArray(),0,value,CharsUtil.PREF_ANY,false),3);			Assert.assertEquals(Double.longBitsToDouble(value[0]),0.1,EPSILON);
		Assert.assertEquals(CharsUtil.parseNumber("0.0000000000000000001".toCharArray(),0,value,CharsUtil.PREF_ANY,false),21);			Assert.assertEquals(Double.longBitsToDouble(value[0]),0.0000000000000000001,EPSILON);
		Assert.assertEquals(CharsUtil.parseNumber("2E10".toCharArray(),0,value,CharsUtil.PREF_ANY,false),4);		Assert.assertEquals(Double.longBitsToDouble(value[0]),2E10,EPSILON);
		Assert.assertEquals(CharsUtil.parseNumber("1.2E10".toCharArray(),0,value,CharsUtil.PREF_ANY,false),6);		Assert.assertEquals(Double.longBitsToDouble(value[0]),1.2E10,EPSILON);
		Assert.assertEquals(CharsUtil.parseNumber("1.2E+10".toCharArray(),0,value,CharsUtil.PREF_ANY,false),7);		Assert.assertEquals(Double.longBitsToDouble(value[0]),1.2E10,EPSILON);
		Assert.assertEquals(CharsUtil.parseNumber("1.2E-10".toCharArray(),0,value,CharsUtil.PREF_ANY,false),7);		Assert.assertEquals(Double.longBitsToDouble(value[0]),1.2E-10,EPSILON);

		try{CharsUtil.parseNumber(null,0,value,CharsUtil.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseNumber("".toCharArray(),0,value,CharsUtil.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseNumber("0".toCharArray(),1,value,CharsUtil.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (1-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseNumber("0".toCharArray(),0,null,CharsUtil.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseNumber("0".toCharArray(),0,new long[0],CharsUtil.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (zero length 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseNumber("1.".toCharArray(),0,value,CharsUtil.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (missing fractional)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseNumber("1E".toCharArray(),0,value,CharsUtil.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseNumber("1E-".toCharArray(),0,value,CharsUtil.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseNumber("1E+".toCharArray(),0,value,CharsUtil.PREF_ANY,false);
			Assert.fail("Mandatory exception was not detected (missing exponent)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseNumber("1E400".toCharArray(),0,value,CharsUtil.PREF_ANY,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseNumber("1234567890123456789012345".toCharArray(),0,value,CharsUtil.PREF_INT|CharsUtil.PREF_LONG,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseNumber("12345678901".toCharArray(),0,value,CharsUtil.PREF_INT,true);
			Assert.fail("Mandatory exception was not detected (overflow on conversion)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void stringConversionTest() {
		final int[]			location = new int[2];
		final StringBuilder	sb = new StringBuilder();
		
		Assert.assertEquals(CharsUtil.parseUnescapedString("'source'".toCharArray(),1,'\'',true,location),8);	Assert.assertArrayEquals(location, new int[]{1,6});
		Assert.assertEquals(CharsUtil.parseUnescapedString("''".toCharArray(),1,'\'',true,location),2);			Assert.assertArrayEquals(location, new int[]{1,0});
		Assert.assertEquals(CharsUtil.parseUnescapedString("'12\\n'".toCharArray(),1,'\'',true,location),-3);	Assert.assertArrayEquals(location, new int[]{1,2});

		try{CharsUtil.parseUnescapedString(null,0,'\'',false,location);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseUnescapedString("".toCharArray(),0,'\'',false,location);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseUnescapedString("''".toCharArray(),10,'\'',false,location);
			Assert.fail("Mandatory exception was not detected (2-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseUnescapedString("''".toCharArray(),0,'\'',false,null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseUnescapedString("''".toCharArray(),0,'\'',false,new int[0]);
			Assert.fail("Mandatory exception was not detected (empty 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseUnescapedString("'".toCharArray(),1,'\'',false,location);
			Assert.fail("Mandatory exception was not detected (unclosed quota)");
		} catch (IllegalArgumentException exc) {
		}
		
		sb.setLength(0);	Assert.assertEquals(CharsUtil.parseString("'source'".toCharArray(),1,'\'',sb),8);		Assert.assertEquals(sb.toString(),"source");
		sb.setLength(0);	Assert.assertEquals(CharsUtil.parseString("''".toCharArray(),1,'\'',sb),2);				Assert.assertEquals(sb.toString(),"");
		sb.setLength(0);	Assert.assertEquals(CharsUtil.parseString("'sou\\nrce'".toCharArray(),1,'\'',sb),10);	Assert.assertEquals(sb.toString(),"sou\nrce");
		sb.setLength(0);	Assert.assertEquals(CharsUtil.parseString("'\\\"\\\\\\/\\b\\f\\n\\r\\t\\ucAf0'".toCharArray(),1,'\'',sb),24);	Assert.assertEquals(sb.toString(),"\"\\/\b\f\n\r\t\ucAf0");
			
		try{CharsUtil.parseString(null,0,'\'',sb);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseString("".toCharArray(),0,'\'',sb);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseString("''".toCharArray(),10,'\'',sb);
			Assert.fail("Mandatory exception was not detected (2-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseString("''".toCharArray(),0,'\'',null);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseString("'".toCharArray(),1,'\'',sb);
			Assert.fail("Mandatory exception was not detected (unclosed quota)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseString("'\\z'".toCharArray(),1,'\'',sb);
			Assert.fail("Mandatory exception was not detected (unsupported escaping)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseString("'\\uMZINANA'".toCharArray(),1,'\'',sb);
			Assert.fail("Mandatory exception was not detected (illegal escaping chars)");
		} catch (IllegalArgumentException exc) {
		}

		sb.setLength(0);	Assert.assertEquals(CharsUtil.parseStringExtended("'source'".toCharArray(),1,'\'',sb),8);		Assert.assertEquals(sb.toString(),"source");
		sb.setLength(0);	Assert.assertEquals(CharsUtil.parseStringExtended("''".toCharArray(),1,'\'',sb),2);				Assert.assertEquals(sb.toString(),"");
		sb.setLength(0);	Assert.assertEquals(CharsUtil.parseStringExtended("'sou\\nrce'".toCharArray(),1,'\'',sb),10);	Assert.assertEquals(sb.toString(),"sou\nrce");
		sb.setLength(0);	Assert.assertEquals(CharsUtil.parseStringExtended("'\\\"\\\\\\/\\b\\f\\n\\r\\t\\ucAf0'".toCharArray(),1,'\'',sb),24);	Assert.assertEquals(sb.toString(),"\"\\/\b\f\n\r\t\ucAf0");
		sb.setLength(0);	Assert.assertEquals(CharsUtil.parseStringExtended("'\\076\\0x4d'".toCharArray(),1,'\'',sb),11);	Assert.assertEquals(sb.toString(),">M");
			
		try{CharsUtil.parseStringExtended(null,0,'\'',sb);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseStringExtended("".toCharArray(),0,'\'',sb);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseStringExtended("''".toCharArray(),10,'\'',sb);
			Assert.fail("Mandatory exception was not detected (2-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseStringExtended("''".toCharArray(),0,'\'',null);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseStringExtended("'".toCharArray(),1,'\'',sb);
			Assert.fail("Mandatory exception was not detected (unclosed quota)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseStringExtended("'\\z'".toCharArray(),1,'\'',sb);
			Assert.fail("Mandatory exception was not detected (unsupported escaping)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseStringExtended("'\\uMZINANA'".toCharArray(),1,'\'',sb);
			Assert.fail("Mandatory exception was not detected (illegal escaping chars)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void enumConversionTest() {
		final PseudoConversionEnum[]	data = new PseudoConversionEnum[1]; 
		
		Assert.assertEquals(CharsUtil.parseEnum("enum1".toCharArray(),0,PseudoConversionEnum.class,data),5);	Assert.assertEquals(data[0],PseudoConversionEnum.enum1);
		Assert.assertEquals(CharsUtil.parseEnum("enum2".toCharArray(),0,PseudoConversionEnum.class,data),5);	Assert.assertEquals(data[0],PseudoConversionEnum.enum2);

		try{CharsUtil.parseEnum(null,0,PseudoConversionEnum.class,data);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseEnum("".toCharArray(),0,PseudoConversionEnum.class,data);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseEnum("enum1".toCharArray(),10,PseudoConversionEnum.class,data);
			Assert.fail("Mandatory exception was not detected (2-nd argument outside the bound)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseEnum("enum1".toCharArray(),0,null,data);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseEnum("enum1".toCharArray(),0,PseudoConversionEnum.class,null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseEnum("enum1".toCharArray(),0,PseudoConversionEnum.class,new PseudoConversionEnum[0]);
			Assert.fail("Mandatory exception was not detected (empty 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CharsUtil.parseEnum("enum3".toCharArray(),0,PseudoConversionEnum.class,data);
			Assert.fail("Mandatory exception was not detected (enum constant not exists)");
		} catch (IllegalArgumentException exc) {
		}
	}
}

enum PseudoConversionEnum {
	enum1, enum2
}
