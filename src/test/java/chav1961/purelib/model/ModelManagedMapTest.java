package chav1961.purelib.model;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfaces.Action;
import chav1961.purelib.ui.interfaces.Format;

public class ModelManagedMapTest {
	@Test
	public void basicTest() throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException {
		final ContentMetadataInterface 	cmi = ContentModelFactory.forAnnotatedClass(AnnotatedForTest.class);	// see ContentModelFactoryTest
		final ModelManagedMap			mmm = new ModelManagedMap(cmi);
		
		Assert.assertEquals(cmi,mmm.getModel());
		Assert.assertArrayEquals(new String[] {"testSet1"},mmm.names());
		Assert.assertEquals(1,mmm.nameIds().length);
		Assert.assertEquals("testSet1",mmm.getMetadata("testSet1").getName());
		
		Assert.assertEquals("testSet1",mmm.nameById(mmm.idByName("testSet1")));

		try{mmm.nameById(-1);
			Assert.fail("Mandatory exception was not detected (unknown 1-st argument)");
		} catch (ContentException exc) {
		}
		try{mmm.idByName(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{mmm.idByName("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{mmm.idByName("unknown");
			Assert.fail("Mandatory exception was not detected (unknown 1-st argument)");
		} catch (ContentException exc) {
		}
		
		Assert.assertEquals(0f,mmm.getFloat("testSet1"),0.001f);
		Assert.assertEquals(mmm,mmm.setFloat("testSet1",1f));
		Assert.assertEquals(1f,mmm.getFloat("testSet1"),0.001f);
		
		System.err.println(mmm.toString());
		
		try{new ModelManagedMap(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void complexTest() throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException {
		final ContentMetadataInterface 	cmi = ContentModelFactory.forAnnotatedClass(PseudoManagerForTest.class);
		final ModelManagedMap			mmm = new ModelManagedMap(cmi);
		
		Assert.assertEquals(cmi,mmm.getModel());
		Assert.assertEquals(9,mmm.nameIds().length);

		Assert.assertEquals(0,mmm.getByte("byteField"));
		Assert.assertEquals(mmm,mmm.setByte("byteField",(byte)1));
		Assert.assertEquals(1,mmm.getByte("byteField"));
		try{mmm.get("byteField",String.class);
			Assert.fail("Mandatory exception was not detected (incompatible types)");
		} catch (ContentException exc) {
		}
		
		Assert.assertEquals(0,mmm.getShort("shortField"));
		Assert.assertEquals(mmm,mmm.setShort("shortField",(short)1));
		Assert.assertEquals(1,mmm.getShort("shortField"));
		try{mmm.get("shortField",String.class);
			Assert.fail("Mandatory exception was not detected (incompatible types)");
		} catch (ContentException exc) {
		}

		Assert.assertEquals(0,mmm.getInt("intField"));
		Assert.assertEquals(mmm,mmm.setInt("intField",(int)1));
		Assert.assertEquals(1,mmm.getInt("intField"));
		try{mmm.get("intField",String.class);
			Assert.fail("Mandatory exception was not detected (incompatible types)");
		} catch (ContentException exc) {
		}
		
		Assert.assertEquals(0,mmm.getLong("longField"));
		Assert.assertEquals(mmm,mmm.setLong("longField",(long)1));
		Assert.assertEquals(1,mmm.getLong("longField"));
		try{mmm.get("longField",String.class);
			Assert.fail("Mandatory exception was not detected (incompatible types)");
		} catch (ContentException exc) {
		}

		Assert.assertEquals(0,mmm.getFloat("floatField"),0.001f);
		Assert.assertEquals(mmm,mmm.setFloat("floatField",(float)1));
		Assert.assertEquals(1,mmm.getFloat("floatField"),0.001f);
		try{mmm.get("floatField",String.class);
			Assert.fail("Mandatory exception was not detected (incompatible types)");
		} catch (ContentException exc) {
		}

		Assert.assertEquals(0,mmm.getDouble("doubleField"),0.001);
		Assert.assertEquals(mmm,mmm.setDouble("doubleField",(double)1));
		Assert.assertEquals(1,mmm.getDouble("doubleField"),0.001);
		try{mmm.get("doubleField",String.class);
			Assert.fail("Mandatory exception was not detected (incompatible types)");
		} catch (ContentException exc) {
		}

		Assert.assertEquals(0,mmm.getChar("charField"));
		Assert.assertEquals(mmm,mmm.setChar("charField",(char)1));
		Assert.assertEquals(1,mmm.getChar("charField"));
		try{mmm.get("charField",String.class);
			Assert.fail("Mandatory exception was not detected (incompatible types)");
		} catch (ContentException exc) {
		}

		Assert.assertEquals(false,mmm.getBoolean("booleanField"));
		Assert.assertEquals(mmm,mmm.setBoolean("booleanField",true));
		Assert.assertEquals(true,mmm.getBoolean("booleanField"));
		try{mmm.get("booleanField",String.class);
			Assert.fail("Mandatory exception was not detected (incompatible types)");
		} catch (ContentException exc) {
		}

		Assert.assertNull(mmm.get("stringField",String.class));
		Assert.assertEquals(mmm,mmm.set("stringField",String.class,"test"));
		Assert.assertEquals("test",mmm.get("stringField",String.class));
		try{mmm.getBoolean("stringField");
			Assert.fail("Mandatory exception was not detected (incompatible types)");
		} catch (ContentException exc) {
		}
		
		try{mmm.get("unknown",String.class);
			Assert.fail("Mandatory exception was not detected (unknown name)");
		} catch (ContentException exc) {
		}
	}
}

@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/localization")
@LocaleResource(value="testSet1",tooltip="testSet2",help="testSet1")
class PseudoManagerForTest {
	@LocaleResource(value="testSet1",tooltip="testSet2")
	@Format("10.3mpzn")
		private byte 	byteField = 0;
	
	@LocaleResource(value="testSet1",tooltip="testSet2")
	@Format("10.3mpzn")
		private short 	shortField = 0;

	@LocaleResource(value="testSet1",tooltip="testSet2")
	@Format("10.3mpzn")
		private int		intField = 0;

	@LocaleResource(value="testSet1",tooltip="testSet2")
	@Format("10.3mpzn")
		private long	longField = 0L;

	@LocaleResource(value="testSet1",tooltip="testSet2")
	@Format("10.3mpzn")
		private float	floatField = 0.0f;

	@LocaleResource(value="testSet1",tooltip="testSet2")
	@Format("10.3mpzn")
		private double	doubleField = 0.0;

	@LocaleResource(value="testSet1",tooltip="testSet2")
	@Format("10.3mpzn")
		private char	charField = ' ';

	@LocaleResource(value="testSet1",tooltip="testSet2")
	@Format("10.3mpzn")
		private boolean	booleanField = false;

	@LocaleResource(value="testSet1",tooltip="testSet2")
	@Format("10.3mpzn")
		private String	stringField = "";
}

