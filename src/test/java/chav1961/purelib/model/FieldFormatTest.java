package chav1961.purelib.model;


import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.model.FieldFormat.Alignment;
import chav1961.purelib.model.FieldFormat.ContentType;

@Tag("OrdinalTestCategory")
public class FieldFormatTest {
	@Test
	public void staticTest() throws SyntaxException {
		Assert.assertEquals(ContentType.BooleanContent,FieldFormat.defineContentType(boolean.class,null));
		Assert.assertEquals(ContentType.BooleanContent,FieldFormat.defineContentType(Boolean.class,null));

		Assert.assertEquals(ContentType.IntegerContent,FieldFormat.defineContentType(byte.class,null));
		Assert.assertEquals(ContentType.IntegerContent,FieldFormat.defineContentType(short.class,null));
		Assert.assertEquals(ContentType.IntegerContent,FieldFormat.defineContentType(int.class,null));
		Assert.assertEquals(ContentType.IntegerContent,FieldFormat.defineContentType(long.class,null));
		Assert.assertEquals(ContentType.IntegerContent,FieldFormat.defineContentType(BigInteger.class,null));
		
		Assert.assertEquals(ContentType.NumericContent,FieldFormat.defineContentType(float.class,null));
		Assert.assertEquals(ContentType.NumericContent,FieldFormat.defineContentType(double.class,null));
		Assert.assertEquals(ContentType.NumericContent,FieldFormat.defineContentType(BigDecimal.class,null));
		
		Assert.assertEquals(ContentType.StringContent,FieldFormat.defineContentType(char.class,null));
		Assert.assertEquals(ContentType.StringContent,FieldFormat.defineContentType(Character.class,null));
		Assert.assertEquals(ContentType.StringContent,FieldFormat.defineContentType(String.class,null));
		Assert.assertEquals(ContentType.FormattedStringContent,FieldFormat.defineContentType(String.class,""));

	 	Assert.assertEquals(ContentType.PasswordContent,FieldFormat.defineContentType(char[].class,null));
		
		Assert.assertEquals(ContentType.DateContent,FieldFormat.defineContentType(Date.class,null));
		Assert.assertEquals(ContentType.DateContent,FieldFormat.defineContentType(Calendar.class,null));

		Assert.assertEquals(ContentType.TimestampContent,FieldFormat.defineContentType(Timestamp.class,null));
		
		Assert.assertEquals(ContentType.FileContent,FieldFormat.defineContentType(File.class,null));
		Assert.assertEquals(ContentType.FileContent,FieldFormat.defineContentType(FileSystemInterface.class,null));
		
		Assert.assertEquals(ContentType.URIContent,FieldFormat.defineContentType(URI.class,null));

		Assert.assertEquals(ContentType.EnumContent,FieldFormat.defineContentType(ContentType.class,null));

		Assert.assertEquals(ContentType.ArrayContent,FieldFormat.defineContentType(int[].class,null));

		Assert.assertEquals(ContentType.Unclassified,FieldFormat.defineContentType(Object.class,null));
	}

	@Test
	public void basicTest() throws SyntaxException {
		Assert.assertNull(new FieldFormat(String.class,"").getFormatMask());
		
		Assert.assertFalse(new FieldFormat(String.class,"").isReadOnly(false));
		Assert.assertFalse(new FieldFormat(String.class,"").isReadOnly(true));
		Assert.assertTrue(new FieldFormat(String.class,"r").isReadOnly(false));
		Assert.assertTrue(new FieldFormat(String.class,"R").isReadOnly(true));
		
		try{new FieldFormat(String.class,"Rr");
			Assert.fail("Mandatory exception was not detected (mutually exclusive parameters)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertFalse(new FieldFormat(String.class,"").isUsedInList());
		Assert.assertFalse(new FieldFormat(String.class,"").isAnchored());
		Assert.assertTrue(new FieldFormat(String.class,"l").isUsedInList());
		Assert.assertTrue(new FieldFormat(String.class,"L").isAnchored());
		
		try{new FieldFormat(String.class,"Ll");
			Assert.fail("Mandatory exception was not detected (mutually exclusive parameters)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertFalse(new FieldFormat(String.class,"").isMandatory());
		Assert.assertTrue(new FieldFormat(String.class,"m").isMandatory());

		Assert.assertFalse(new FieldFormat(String.class,"").isOutput());
		Assert.assertTrue(new FieldFormat(String.class,"o").isOutput());
		
		Assert.assertFalse(new FieldFormat(String.class,"").isHighlighted(-1));
		Assert.assertTrue(new FieldFormat(String.class,"n").isHighlighted(-1));
		
		Assert.assertFalse(new FieldFormat(String.class,"").isHighlighted(0));
		Assert.assertTrue(new FieldFormat(String.class,"z").isHighlighted(0));
		
		Assert.assertFalse(new FieldFormat(String.class,"").isHighlighted(+1));
		Assert.assertTrue(new FieldFormat(String.class,"p").isHighlighted(+1));

		Assert.assertFalse(new FieldFormat(String.class,"").needSelectOnFocus());
		Assert.assertTrue(new FieldFormat(String.class,"s").needSelectOnFocus());

		Assert.assertEquals(Alignment.NoMatter,new FieldFormat(String.class,"").getAlignment());
		Assert.assertEquals(Alignment.LeftAlignment,new FieldFormat(String.class,"<").getAlignment());
		Assert.assertEquals(Alignment.RightAlignment,new FieldFormat(String.class,">").getAlignment());
		Assert.assertEquals(Alignment.CenterAlignment,new FieldFormat(String.class,"><").getAlignment());
		Assert.assertEquals(Alignment.Ajusted,new FieldFormat(String.class,"<>").getAlignment());
		
		try{new FieldFormat(String.class,"<<");
			Assert.fail("Mandatory exception was not detected (duplicate alignment signs)");
		} catch (IllegalArgumentException exc) {
		}
		try{new FieldFormat(String.class,">>");
			Assert.fail("Mandatory exception was not detected (duplicate alignment signs)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertEquals(10,new FieldFormat(String.class,"10").getLength());
		Assert.assertEquals(10,new FieldFormat(String.class,"10.2").getLength());
		Assert.assertEquals(2,new FieldFormat(String.class,"10.2").getPrecision());

		try{new FieldFormat(String.class,"10.9");
			Assert.fail("Mandatory exception was not detected (frac too long)");
		} catch (IllegalArgumentException exc) {
		}
		try{new FieldFormat(String.class,"10m10");
			Assert.fail("Mandatory exception was not detected (duplicate length)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
