package chav1961.purelib.model;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.model.ModelUtils.ModelComparisonCallback;
import chav1961.purelib.model.ModelUtils.ModelComparisonCallback.DifferenceLocalization;
import chav1961.purelib.model.ModelUtils.ModelComparisonCallback.DifferenceType;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class ModelUtilsTest {
	@Test
	public void buildURIsTest() throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException {
		final TestClass					tc = new TestClass();

		Assert.assertEquals(URI.create("app:field:/chav1961.purelib.model.TestClass/testByte"), ModelUtils.buildUriByClassAndField(tc.getClass(),"testByte"));

		try{ModelUtils.buildUriByClassAndField(null,"testByte");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{ModelUtils.buildUriByClassAndField(tc.getClass(),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{ModelUtils.buildUriByClassAndField(tc.getClass(),"");
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{ModelUtils.buildUriByClassAndField(tc.getClass(),"unknown");
			Assert.fail("Mandatory exception was not detected (field name not found)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(URI.create("app:action:/TestClass.testMethod"), ModelUtils.buildUriByClassAndMethod(tc.getClass(),"testMethod"));

		try{ModelUtils.buildUriByClassAndMethod(null,"testMethod");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{ModelUtils.buildUriByClassAndMethod(tc.getClass(),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{ModelUtils.buildUriByClassAndMethod(tc.getClass(),"");
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{ModelUtils.buildUriByClassAndMethod(tc.getClass(),"unknown");
			Assert.fail("Mandatory exception was not detected (method name not found)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	@Test
	public void getAndSetValueTest() throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException {
		final TestClass					tc = new TestClass();
		final ContentMetadataInterface	meta = ContentModelFactory.forAnnotatedClass(tc.getClass());

		final URI						byteUri = ModelUtils.buildUriByClassAndField(tc.getClass(),"testByte"); 
		
		Assert.assertEquals((byte)10,ModelUtils.getValueByGetter(tc,byteUri,meta.byApplicationPath(byteUri)[0]));
		ModelUtils.setValueBySetter(tc,(byte)20,byteUri,meta.byApplicationPath(byteUri)[0]);
		Assert.assertEquals((byte)20,tc.testByte);
		
		final URI						shortUri = ModelUtils.buildUriByClassAndField(tc.getClass(),"testShort"); 
		
		Assert.assertEquals((short)10,ModelUtils.getValueByGetter(tc,shortUri,meta.byApplicationPath(shortUri)[0]));
		ModelUtils.setValueBySetter(tc,(short)20,shortUri,meta.byApplicationPath(shortUri)[0]);
		Assert.assertEquals((short)20,tc.testShort);
		
		final URI						intUri = ModelUtils.buildUriByClassAndField(tc.getClass(),"testInt"); 
		
		Assert.assertEquals(10,ModelUtils.getValueByGetter(tc,intUri,meta.byApplicationPath(intUri)[0]));
		ModelUtils.setValueBySetter(tc,20,intUri,meta.byApplicationPath(intUri)[0]);
		Assert.assertEquals(20,tc.testInt);

		final URI						longUri = ModelUtils.buildUriByClassAndField(tc.getClass(),"testLong"); 
		
		Assert.assertEquals(10L,ModelUtils.getValueByGetter(tc,longUri,meta.byApplicationPath(longUri)[0]));
		ModelUtils.setValueBySetter(tc,20L,longUri,meta.byApplicationPath(longUri)[0]);
		Assert.assertEquals(20L,tc.testLong);
		
		final URI						floatUri = ModelUtils.buildUriByClassAndField(tc.getClass(),"testFloat"); 
		
		Assert.assertEquals(10f,ModelUtils.getValueByGetter(tc,floatUri,meta.byApplicationPath(floatUri)[0]));
		ModelUtils.setValueBySetter(tc,20f,floatUri,meta.byApplicationPath(floatUri)[0]);
		Assert.assertEquals(20f,tc.testFloat,0.001f);
		
		final URI						doubleUri = ModelUtils.buildUriByClassAndField(tc.getClass(),"testDouble"); 
		
		Assert.assertEquals(10.0,ModelUtils.getValueByGetter(tc,doubleUri,meta.byApplicationPath(doubleUri)[0]));
		ModelUtils.setValueBySetter(tc,20.0,doubleUri,meta.byApplicationPath(doubleUri)[0]);
		Assert.assertEquals(20.0,tc.testDouble,0.001f);
		
		final URI						charUri = ModelUtils.buildUriByClassAndField(tc.getClass(),"testChar"); 
		
		Assert.assertEquals((char)10,ModelUtils.getValueByGetter(tc,charUri,meta.byApplicationPath(charUri)[0]));
		ModelUtils.setValueBySetter(tc,(char)20,charUri,meta.byApplicationPath(charUri)[0]);
		Assert.assertEquals((char)20,tc.testChar);

		final URI						booleanUri = ModelUtils.buildUriByClassAndField(tc.getClass(),"testBoolean"); 
		
		Assert.assertEquals(true,ModelUtils.getValueByGetter(tc,booleanUri,meta.byApplicationPath(booleanUri)[0]));
		ModelUtils.setValueBySetter(tc,false,booleanUri,meta.byApplicationPath(booleanUri)[0]);
		Assert.assertEquals(false,tc.testBoolean);
		
		try{ModelUtils.getValueByGetter(null,intUri,meta.byApplicationPath(intUri)[0]);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{ModelUtils.getValueByGetter(tc,(URI)null,meta.byApplicationPath(intUri)[0]);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{ModelUtils.getValueByGetter(tc,intUri,null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}

		try{ModelUtils.setValueBySetter(null,10,intUri,meta.byApplicationPath(intUri)[0]);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{ModelUtils.setValueBySetter(tc,10,(URI)null,meta.byApplicationPath(intUri)[0]);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{ModelUtils.setValueBySetter(tc,10,intUri,null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Test
	public void nodeComparisonTest() {
		final Set<DifferenceLocalization>	diff = new HashSet<>();
		final ModelComparisonCallback		mcc = new ModelComparisonCallback() {
												@Override
												public ContinueMode difference(final ContentNodeMetadata left, final ContentNodeMetadata right, final DifferenceType diffType, final Set<DifferenceLocalization> details) {
													diff.addAll(details);
													return ContinueMode.CONTINUE;
												}
											}; 
		final MutableContentNodeMetadata	md1 = new MutableContentNodeMetadata("name",String.class,"./name",null,"labelId","tooltipId",null,new FieldFormat(String.class,"m"),URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/test"),null);
		final MutableContentNodeMetadata	md2 = new MutableContentNodeMetadata("name",String.class,"./name",null,"labelId","tooltipId",null,new FieldFormat(String.class,"m"),URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/test"),null);
		final MutableContentNodeMetadata	md3 = new MutableContentNodeMetadata("name",String.class,"./name2",null,"labelId",null,"helpId",new FieldFormat(String.class,"m"),URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/another"),null);

		Assert.assertTrue(ModelUtils.compare(md1,md2,mcc));
		Assert.assertEquals(Set.of(),diff);
		Assert.assertTrue(ModelUtils.compare(md1,md3,mcc));
		Assert.assertEquals(Set.of(DifferenceLocalization.IN_UI_PATH,DifferenceLocalization.IN_HELP,DifferenceLocalization.IN_APP_PATH,DifferenceLocalization.IN_TOOLTIP),diff);

		try{ModelUtils.compare(null,md3,mcc);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{ModelUtils.compare(md1,null,mcc);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{ModelUtils.compare(md1,md3,null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void nodeSerializationTest() throws IOException {
		final MutableContentNodeMetadata	md = new MutableContentNodeMetadata("name",String.class,"./name",null,"labelId","tooltipId",null,new FieldFormat(String.class,"m"),URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/test"),null);
		
		try(final StringWriter			wr = new StringWriter()) {
			try(final JsonStaxPrinter	prn = new JsonStaxPrinter(wr)) {
				ModelUtils.serializeToJson(md,prn);
				
				try{ModelUtils.serializeToJson(null,prn);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try{ModelUtils.serializeToJson(md,null);
					Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
				} catch (NullPointerException exc) {
				}
			}
			
			try(final Reader			rdr = new StringReader(wr.toString());
				final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
				
				parser.next();
				Assert.assertTrue(ModelUtils.compare(md, ModelUtils.deserializeFromJson(parser), (a,b,c,d)->ContinueMode.CONTINUE));

				try{ModelUtils.deserializeFromJson(null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
			}
			
			try{testErroneousJsonDeserialization(wr.toString().replace("\"version\":\"1.0\",",""));
				Assert.fail("Mandatory exception was not detected (missing version field)");
			} catch (IOException exc) {
			}
			try{testErroneousJsonDeserialization(wr.toString().replace("\"1.0\"","\"0.0\""));
				Assert.fail("Mandatory exception was not detected (unsupported version)");
			} catch (IOException exc) {
			}
			try{testErroneousJsonDeserialization(wr.toString().replace("\"name\"","\"unknown\""));
				Assert.fail("Mandatory exception was not detected (unsupported field name)");
			} catch (IOException exc) {
			}
			try{testErroneousJsonDeserialization(wr.toString().replace("\"name\":\"name\",",""));
				Assert.fail("Mandatory exception was not detected (mandatory field missing)");
			} catch (IOException exc) {
			}
			try{testErroneousJsonDeserialization(wr.toString().replace("\"type\":\"java.lang.String\"","\"type\":\"unknown\""));
				Assert.fail("Mandatory exception was not detected (unknown class in the class loader)");
			} catch (IOException exc) {
			}
			try{testErroneousJsonDeserialization(wr.toString().replace("\"format\":\"m\"","\"format\":\"?\""));
				Assert.fail("Mandatory exception was not detected (illegal format)");
			} catch (IOException exc) {
			}
			try{testErroneousJsonDeserialization(wr.toString().replace("\"appUri\":\"app:/test\"","\"appUri\":\"a b c d e\""));
				Assert.fail("Mandatory exception was not detected (illegal URI)");
			} catch (IOException exc) {
			}
		}
	}
	
	private void testErroneousJsonDeserialization(final String content) throws IOException {
		try(final Reader			rdr = new StringReader(content);
			final JsonStaxParser	parser= new JsonStaxParser(rdr)) {

			parser.next();
			ModelUtils.deserializeFromJson(parser);
		}
	}
}
