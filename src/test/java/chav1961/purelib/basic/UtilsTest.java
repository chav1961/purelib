package chav1961.purelib.basic;


import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.Utils.IndicesComparator;
import chav1961.purelib.basic.Utils.IndicesMover;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.streams.charsource.StringCharSource;
import chav1961.purelib.streams.chartarget.StringBuilderCharTarget;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

@Tag("OrdinalTestCategory")
public class UtilsTest {
	@Test
	public void copyStreamTest() throws IOException, PrintingException, ContentException {
		try(final ByteArrayInputStream	bais = new ByteArrayInputStream("test string".getBytes());
			final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
		
			Assert.assertEquals(Utils.copyStream(bais,baos),"test string".length());
			Assert.assertEquals(baos.toString(),"test string");
			
			try{Utils.copyStream(null,baos);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {				
			}
			try{Utils.copyStream(bais,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {				 
			}
		}

		try(final Reader		rdr = new StringReader("test string");
			final StringWriter	wr = new StringWriter()) {
		
			Assert.assertEquals(Utils.copyStream(rdr,wr),"test string".length());
			Assert.assertEquals(wr.toString(),"test string");
			
			try{Utils.copyStream(null,wr);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {				
			}
			try{Utils.copyStream(rdr,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {				
			}
		}
		
		final StringBuilder		sb = new StringBuilder();
		final CharacterSource	cs = new StringCharSource("test");
		final CharacterTarget	ct = new StringBuilderCharTarget(sb);
		
		Utils.copyStream(cs,ct);
		Assert.assertEquals("test",sb.toString());
		try{Utils.copyStream(null,ct);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {				
		}
		try{Utils.copyStream(cs,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {				
		}
	}

	@Test
	public void mkMapSetAndPropertiesTest() throws IOException {
		final Map<String,Object>	etalon = new HashMap<String,Object>(){private static final long serialVersionUID = 1L; {put("key1","value1"); put("key2","value2");}};
		
		Assert.assertEquals(Utils.mkMap("key1","value1","key2","value2"),etalon);
		
		try{Utils.mkMap((Object[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {				
		}
		try{Utils.mkMap("key1");
			Assert.fail("Mandatory exception was not detected (odd amount of parameters)");
		} catch (IllegalArgumentException exc) {				
		}
		try{Utils.mkMap(null,"value1");
			Assert.fail("Mandatory exception was not detected (key is null)");
		} catch (IllegalArgumentException exc) {				
		}
		
		final Set<String>		etalonSet = new HashSet<String>(){private static final long serialVersionUID = 1L; {add("key1"); add("key2");}};

		Assert.assertEquals(Utils.mkSet(String.class,"key1","key2"),etalonSet);

		try{Utils.mkSet(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {				
		}
		try{Utils.mkSet(String.class,(String[])null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {				
		}

		final Properties		etalonProps = new Properties(){private static final long serialVersionUID = 1L; {setProperty("key1","value1"); setProperty("key2","value2");}};

		Assert.assertEquals(Utils.mkProps("key1","value1","key2","value2"),etalonProps);

		try{Utils.mkProps((String[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {				
		}
		try{Utils.mkProps("key1");
			Assert.fail("Mandatory exception was not detected (odd amount of parameters)");
		} catch (IllegalArgumentException exc) {				
		}
		try{Utils.mkProps(null,"value1");
			Assert.fail("Mandatory exception was not detected (key is null)");
		} catch (IllegalArgumentException exc) {				
		}
	}

	@Test
	public void fromResourceTest() throws IOException {
		Assert.assertEquals(Utils.fromResource(new StringReader("test string")),"test string");
		Assert.assertEquals(Utils.fromResource(this.getClass().getResource("resourcefile.txt")),"test string");
		
		try{Utils.fromResource((Reader)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{Utils.fromResource((URL)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{Utils.fromResource(this.getClass().getResource("resourcefile.txt"),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
	}

	@Test
	public void wrappingTest() throws IOException {
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new boolean[]{true,false})),new boolean[]{true,false});
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new byte[]{1,2,3})),new byte[]{1,2,3});
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new short[]{1,2,3})),new short[]{1,2,3});
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new int[]{1,2,3})),new int[]{1,2,3});
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new long[]{1,2,3})),new long[]{1,2,3});
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new float[]{1,2,3})),new float[]{1,2,3},0.0001f);
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new double[]{1,2,3})),new double[]{1,2,3},0.0001);
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new char[]{'1','2','3'})),new char[]{'1','2','3'});
		
		for (Class<?> clazz : new Class[]{boolean.class,byte.class,char.class,double.class,float.class,int.class,long.class,short.class,void.class}) {
			Assert.assertEquals(clazz,Utils.wrapper2Primitive(Utils.primitive2Wrapper(clazz)));
		}
		
		try{Utils.wrapper2Primitive(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{Utils.wrapper2Primitive(String.class);
			Assert.fail("Mandatory exception was not detected (1-st argument is not primitive class)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void fillArrayTest() {
		// test byte
		
		byte[]	byteContent = new byte[0];
		Utils.fillArray(byteContent, (byte)1);
		
		byteContent = new byte[32];
		Utils.fillArray(byteContent, (byte)1);
		Assert.assertEquals((byte)1, byteContent[31]);

		byteContent = new byte[33];
		Utils.fillArray(byteContent, (byte)1);
		Assert.assertEquals((byte)1, byteContent[32]);

		byteContent = new byte[63];
		Utils.fillArray(byteContent, (byte)1);
		Assert.assertEquals((byte)1, byteContent[62]);
		
		byteContent = new byte[64];
		Utils.fillArray(byteContent, (byte)1);
		Assert.assertEquals((byte)1, byteContent[63]);

		byteContent = new byte[65];
		Utils.fillArray(byteContent, (byte)1);
		Assert.assertEquals((byte)1, byteContent[64]);

		byteContent = new byte[127];
		Utils.fillArray(byteContent, (byte)1);
		Assert.assertEquals((byte)1, byteContent[126]);
		
		byteContent = new byte[129];
		Utils.fillArray(byteContent, (byte)1);
		Assert.assertEquals((byte)1, byteContent[128]);
		
		try{Utils.fillArray((byte[])null, (byte)1);
			Assert.fail("MAndatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		// test short
		
		short[]	shortContent = new short[0];
		Utils.fillArray(shortContent, (short)1);
		
		shortContent = new short[32];
		Utils.fillArray(shortContent, (short)1);
		Assert.assertEquals((short)1, shortContent[31]);

		shortContent = new short[33];
		Utils.fillArray(shortContent, (short)1);
		Assert.assertEquals((short)1, shortContent[32]);

		shortContent = new short[63];
		Utils.fillArray(shortContent, (short)1);
		Assert.assertEquals((short)1, shortContent[62]);
		
		shortContent = new short[64];
		Utils.fillArray(shortContent, (short)1);
		Assert.assertEquals((short)1, shortContent[63]);

		shortContent = new short[65];
		Utils.fillArray(shortContent, (short)1);
		Assert.assertEquals((short)1, shortContent[64]);

		shortContent = new short[127];
		Utils.fillArray(shortContent, (short)1);
		Assert.assertEquals((short)1, shortContent[126]);
		
		shortContent = new short[129];
		Utils.fillArray(shortContent, (short)1);
		Assert.assertEquals((short)1, shortContent[128]);
		
		try{Utils.fillArray((short[])null, (short)1);
			Assert.fail("MAndatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		// test int
		
		int[]	intContent = new int[0];
		Utils.fillArray(intContent, 1);
		
		intContent = new int[32];
		Utils.fillArray(intContent, 1);
		Assert.assertEquals(1, intContent[31]);

		intContent = new int[33];
		Utils.fillArray(intContent, 1);
		Assert.assertEquals(1, intContent[32]);

		intContent = new int[63];
		Utils.fillArray(intContent, 1);
		Assert.assertEquals(1, intContent[62]);
		
		intContent = new int[64];
		Utils.fillArray(intContent, 1);
		Assert.assertEquals(1, intContent[63]);

		intContent = new int[65];
		Utils.fillArray(intContent, 1);
		Assert.assertEquals(1, intContent[64]);

		intContent = new int[127];
		Utils.fillArray(intContent, 1);
		Assert.assertEquals(1, intContent[126]);
		
		intContent = new int[129];
		Utils.fillArray(intContent, 1);
		Assert.assertEquals(1, intContent[128]);
		
		try{Utils.fillArray((int[])null, 1);
			Assert.fail("MAndatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		// test long
		
		long[]	longContent = new long[0];
		Utils.fillArray(longContent, 1);
		
		longContent = new long[32];
		Utils.fillArray(longContent, 1);
		Assert.assertEquals(1, longContent[31]);

		longContent = new long[33];
		Utils.fillArray(longContent, 1);
		Assert.assertEquals(1, longContent[32]);

		longContent = new long[63];
		Utils.fillArray(longContent, 1);
		Assert.assertEquals(1, longContent[62]);
		
		longContent = new long[64];
		Utils.fillArray(longContent, 1);
		Assert.assertEquals(1, longContent[63]);

		longContent = new long[65];
		Utils.fillArray(longContent, 1);
		Assert.assertEquals(1, longContent[64]);

		longContent = new long[127];
		Utils.fillArray(longContent, 1);
		Assert.assertEquals(1, longContent[126]);
		
		longContent = new long[129];
		Utils.fillArray(longContent, 1);
		Assert.assertEquals(1, longContent[128]);
		
		try{Utils.fillArray((long[])null, 1);
			Assert.fail("MAndatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		// test float
		
		float[]	floatContent = new float[0];
		Utils.fillArray(floatContent, 1);
		
		floatContent = new float[32];
		Utils.fillArray(floatContent, 1f);
		Assert.assertEquals(1f, floatContent[31], 0.0001f);

		floatContent = new float[33];
		Utils.fillArray(floatContent, 1f);
		Assert.assertEquals(1f, floatContent[32], 0.0001f);

		floatContent = new float[63];
		Utils.fillArray(floatContent, 1f);
		Assert.assertEquals(1f, floatContent[62], 0.0001f);
		
		floatContent = new float[64];
		Utils.fillArray(floatContent, 1f);
		Assert.assertEquals(1f, floatContent[63], 0.0001f);

		floatContent = new float[65];
		Utils.fillArray(floatContent, 1f);
		Assert.assertEquals(1f, floatContent[64], 0.0001f);

		floatContent = new float[127];
		Utils.fillArray(floatContent, 1f);
		Assert.assertEquals(1f, floatContent[126], 0.0001f);
		
		floatContent = new float[129];
		Utils.fillArray(floatContent, 1f);
		Assert.assertEquals(1f, floatContent[128], 0.0001f);
		
		try{Utils.fillArray((float[])null, 1f);
			Assert.fail("MAndatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		// test double
		
		double[]	doubleContent = new double[0];
		Utils.fillArray(doubleContent, 1);
		
		doubleContent = new double[32];
		Utils.fillArray(doubleContent, 1);
		Assert.assertEquals(1, doubleContent[31], 0.0001);

		doubleContent = new double[33];
		Utils.fillArray(doubleContent, 1);
		Assert.assertEquals(1, doubleContent[32], 0.0001);

		doubleContent = new double[63];
		Utils.fillArray(doubleContent, 1);
		Assert.assertEquals(1, doubleContent[62], 0.0001);
		
		doubleContent = new double[64];
		Utils.fillArray(doubleContent, 1);
		Assert.assertEquals(1, doubleContent[63], 0.0001);

		doubleContent = new double[65];
		Utils.fillArray(doubleContent, 1);
		Assert.assertEquals(1, doubleContent[64], 0.0001);

		doubleContent = new double[127];
		Utils.fillArray(doubleContent, 1);
		Assert.assertEquals(1, doubleContent[126], 0.0001);
		
		doubleContent = new double[129];
		Utils.fillArray(doubleContent, 1);
		Assert.assertEquals(1, doubleContent[128], 0.0001);
		
		try{Utils.fillArray((double[])null, 1f);
			Assert.fail("MAndatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		// test char
		
		char[]	charContent = new char[0];
		Utils.fillArray(charContent, '1');
		
		charContent = new char[32];
		Utils.fillArray(charContent, '1');
		Assert.assertEquals('1', charContent[31]);

		charContent = new char[33];
		Utils.fillArray(charContent, '1');
		Assert.assertEquals('1', charContent[32]);

		charContent = new char[63];
		Utils.fillArray(charContent, '1');
		Assert.assertEquals('1', charContent[62]);
		
		charContent = new char[64];
		Utils.fillArray(charContent, '1');
		Assert.assertEquals('1', charContent[63]);

		charContent = new char[65];
		Utils.fillArray(charContent, '1');
		Assert.assertEquals('1', charContent[64]);

		charContent = new char[127];
		Utils.fillArray(charContent, '1');
		Assert.assertEquals('1', charContent[126]);
		
		charContent = new char[129];
		Utils.fillArray(charContent, '1');
		Assert.assertEquals('1', charContent[128]);
		
		try{Utils.fillArray((char[])null, '1');
			Assert.fail("MAndatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		// test boolean
		
		boolean[]	booleanContent = new boolean[0];
		Utils.fillArray(booleanContent, true);
		
		booleanContent = new boolean[32];
		Utils.fillArray(booleanContent, true);
		Assert.assertEquals(true, booleanContent[31]);

		booleanContent = new boolean[33];
		Utils.fillArray(booleanContent, true);
		Assert.assertEquals(true, booleanContent[32]);

		booleanContent = new boolean[63];
		Utils.fillArray(booleanContent, true);
		Assert.assertEquals(true, booleanContent[62]);
		
		booleanContent = new boolean[64];
		Utils.fillArray(booleanContent, true);
		Assert.assertEquals(true, booleanContent[63]);

		booleanContent = new boolean[65];
		Utils.fillArray(booleanContent, true);
		Assert.assertEquals(true, booleanContent[64]);

		booleanContent = new boolean[127];
		Utils.fillArray(booleanContent, true);
		Assert.assertEquals(true, booleanContent[126]);
		
		booleanContent = new boolean[129];
		Utils.fillArray(booleanContent, true);
		Assert.assertEquals(true, booleanContent[128]);
		
		try{Utils.fillArray((boolean[])null, true);
			Assert.fail("MAndatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		// test T
		
		String[]	stringContent = new String[0];
		Utils.fillArray(stringContent, "1");
		
		stringContent = new String[32];
		Utils.fillArray(stringContent, "1");
		Assert.assertEquals("1", stringContent[31]);

		stringContent = new String[33];
		Utils.fillArray(stringContent, "1");
		Assert.assertEquals("1", stringContent[32]);

		stringContent = new String[63];
		Utils.fillArray(stringContent, "1");
		Assert.assertEquals("1", stringContent[62]);
		
		stringContent = new String[64];
		Utils.fillArray(stringContent, "1");
		Assert.assertEquals("1", stringContent[63]);

		stringContent = new String[65];
		Utils.fillArray(stringContent, "1");
		Assert.assertEquals("1", stringContent[64]);

		stringContent = new String[127];
		Utils.fillArray(stringContent, "1");
		Assert.assertEquals("1", stringContent[126]);
		
		stringContent = new String[129];
		Utils.fillArray(stringContent, "1");
		Assert.assertEquals("1", stringContent[128]);
		
		try{Utils.fillArray((String[])null, "1");
			Assert.fail("MAndatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
	}
	
	@Test
	public void extractValuesTest() throws IOException {
		Assert.assertEquals(100L,Utils.extractLongValue(Long.valueOf(100)));
		
		try {Utils.extractLongValue(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {Utils.extractLongValue("");
			Assert.fail("Mandatory exception was not detected (1-st argument is not a number)");
		} catch (IllegalArgumentException exc) {
		}		
		
		Assert.assertEquals(100.0,Utils.extractDoubleValue(Double.valueOf(100)),0.001);
		
		try {Utils.extractDoubleValue(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {Utils.extractDoubleValue("");
			Assert.fail("Mandatory exception was not detected (1-st argument is not a number)");
		} catch (IllegalArgumentException exc) {
		}		
	}
	
	@Test
	public void fileMask2RegexTest() throws IOException, NullPointerException, URISyntaxException {
		Assert.assertEquals("q1\\_",Utils.fileMask2Regex("q1_"));
		Assert.assertEquals("q1\\_\\ q2",Utils.fileMask2Regex("q1_ q2"));
		Assert.assertEquals(".*",Utils.fileMask2Regex("*"));
		Assert.assertEquals("..*",Utils.fileMask2Regex("?*"));
		Assert.assertEquals(".*\\.txt",Utils.fileMask2Regex("*.txt"));
		
		try{Utils.fileMask2Regex(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{Utils.fileMask2Regex("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	@Test
	public void checkArrayAndCollectionContentTest() {
		Assert.assertEquals(-1,Utils.checkArrayContent4Nulls(new Object[]{"","",""}));
		Assert.assertEquals(2,Utils.checkArrayContent4Nulls(new Object[]{"","",null}));
		
		try{Utils.checkArrayContent4Nulls(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals(-1,Utils.checkCollectionContent4Nulls(Arrays.asList(new Object[]{"","",""})));
		Assert.assertEquals(2,Utils.checkCollectionContent4Nulls(Arrays.asList(new Object[]{"","",null})));
		
		try{Utils.checkCollectionContent4Nulls(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void walkingTest() throws IOException, ParserConfigurationException, SAXException, ContentException {
		final List<String>	collection = new ArrayList<>();
		
		try (final Reader	rdr = new StringReader("<?xml version=\"1.0\"?><root><level1><level11/><level12 id=\"x\"/></level1><level2><level21/><level22/></level2></root>")){
			final DocumentBuilderFactory	factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder 			builder = factory.newDocumentBuilder();
			final Document 					document = builder.parse(new InputSource(rdr));

			collection.clear();
			Utils.walkDownEverywhere((Element)document.getDocumentElement()
					,(type,node)->{
						final NodeList	list = ((Element)node).getChildNodes();
						final Element[]	result = new Element[list.getLength()];
						
						for (int index = 0; index < result.length; index++) {
							result[index] = (Element)list.item(index);
						}
						return result;
					  }
					,(mode,node)->{
						if (mode == NodeEnterMode.ENTER) {
							collection.add(node.getNodeName());
						}
						return ContinueMode.CONTINUE;
					}
			);
			Assert.assertEquals(Arrays.asList("root","level1","level11","level12","level2","level21","level22"),collection);

			collection.clear();
			Utils.walkDownEverywhere((Element)document.getDocumentElement()
					,(type,node)->{
						final NodeList	list = ((Element)node).getChildNodes();
						final Element[]	result = new Element[list.getLength()];
						
						for (int index = 0; index < result.length; index++) {
							result[index] = (Element)list.item(index);
						}
						return result;
					  }
					,(mode,node)->{
						if (mode == NodeEnterMode.ENTER) {
							collection.add(node.getNodeName());
						}
						return node.getNodeName().length() > 6 ? ContinueMode.SKIP_CHILDREN : ContinueMode.CONTINUE;
					}
			);
			Assert.assertEquals(Arrays.asList("root","level1","level11","level2","level21"),collection);
			
			try{Utils.walkDownEverywhere((Element)null
						,(type,node)->{
							final NodeList	list = ((Element)node).getChildNodes();
							final Element[]	result = new Element[list.getLength()];
							
							for (int index = 0; index < result.length; index++) {
								result[index] = (Element)list.item(index);
							}
							return result;
						  }
						,(mode,node)->{
							if (mode == NodeEnterMode.ENTER) {
								collection.add(node.getNodeName());
							}
							return node.getNodeName().length() > 6 ? ContinueMode.SKIP_CHILDREN : ContinueMode.CONTINUE;
						}
				);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			try{Utils.walkDownEverywhere((Element)document.getDocumentElement()
						,null
						,(mode,node)->{
							if (mode == NodeEnterMode.ENTER) {
								collection.add(node.getNodeName());
							}
							return node.getNodeName().length() > 6 ? ContinueMode.SKIP_CHILDREN : ContinueMode.CONTINUE;
						}
				);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{Utils.walkDownEverywhere((Element)document.getDocumentElement()
						,(type,node)->{
							final NodeList	list = ((Element)node).getChildNodes();
							final Element[]	result = new Element[list.getLength()];
							
							for (int index = 0; index < result.length; index++) {
								result[index] = (Element)list.item(index);
							}
							return result;
						  }
						,null
				);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
			
			collection.clear();
			Utils.walkUpEverywhere(document.getDocumentElement().getElementsByTagName("level11").item(0)
					,(type,node)->{
						switch (type) {
							case PARENT:
								if (node.getParentNode() instanceof Element) {
									return new Element[]{(Element)node.getParentNode()};
								}
							case SIBLINGS:
								return new Element[0];
							default :
								return null;
						}
					  }
					,(mode,node)->{
						if (mode == NodeEnterMode.ENTER) {
							collection.add(node.getNodeName());
						}
						return ContinueMode.CONTINUE;
					}
			);
			Assert.assertEquals(Arrays.asList("level11","level1","root"),collection);
			
			try{Utils.walkUpEverywhere((Element)null
						,(type,node)->{
							switch (type) {
								case PARENT:
									if (node.getParentNode() instanceof Element) {
										return new Element[]{(Element)node.getParentNode()};
									}
								case SIBLINGS:
									return new Element[0];
								default :
									return null;
							}
						  }
						,(mode,node)->{
							if (mode == NodeEnterMode.ENTER) {
								collection.add(node.getNodeName());
							}
							return ContinueMode.CONTINUE;
						}
				);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{Utils.walkUpEverywhere(document.getDocumentElement().getElementsByTagName("level11").item(0)
						,null
						,(mode,node)->{
							if (mode == NodeEnterMode.ENTER) {
								collection.add(node.getNodeName());
							}
							return ContinueMode.CONTINUE;
						}
				);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{Utils.walkUpEverywhere(document.getDocumentElement().getElementsByTagName("level11").item(0)
						,(type,node)->{
							switch (type) {
								case PARENT:
									if (node.getParentNode() instanceof Element) {
										return new Element[]{(Element)node.getParentNode()};
									}
								case SIBLINGS:
									return new Element[0];
								default :
									return null;
							}
						  }
						,null
				);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
		}
	}
	
//	@Test
	public void buildProxyTest() throws IOException, ParserConfigurationException, SAXException, ContentException, NoSuchMethodException {
		final Class<Connection>	connClass = Connection.class;
		final Set<Method>		methods = new HashSet<>();
		final Connection		inst = (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] {connClass}, (a,b,c)->{return null;});
		
		methods.add(connClass.getMethod("createStatement"));
		Utils.buildProxy(connClass, inst, methods, (a,b,c,d)->{return null;});
	}
	
	@Test
	public void parallelArraysSeekTest() {
		final int[]	array1 = {1,2,3,4,5,6,7,8,9,10};
		
		Assert.assertEquals(4, Utils.parallelArraysBinarySearch(0, array1.length-1, (i)->5-array1[i]));
		Assert.assertEquals(-1, Utils.parallelArraysBinarySearch(0, array1.length-1, (i)->0-array1[i]));
		Assert.assertEquals(-10, Utils.parallelArraysBinarySearch(0, array1.length-1, (i)->100-array1[i]));
	}

	@Test
	public void parallelArraysSortTest() {
		final int[]	array1 = {1,2,3,4,5,6,7,8,9,10}, array2 = array1.clone();
		
		final IndicesComparator	ic = new IndicesComparator() {
									@Override
									public int compareTo(int index1, int index2) {
										return array1[index2] - array1[index1];
									}
								};
		final IndicesMover		im = new IndicesMover() {
									int	temp;
									@Override
									public void move(int from, int to, int length) {
										if (to < 0) {
											temp = array1[from]; 
										}
										else if (from < 0) {
											array1[to] = temp; 
										}
										else {
											System.arraycopy(array1, from, array1, to, length);
										}
									}
								};
		Utils.parallelArraysQSort(0, array1.length-1, ic, im, 1);
		Assert.assertArrayEquals(array2, array1);
		
		setArrayValue(array1,new int[]{1,3,5,7,9,2,4,6,8,10});
		Utils.parallelArraysQSort(0, array1.length-1, ic, im, 1);
		Assert.assertArrayEquals(array2, array1);

		setArrayValue(array1,new int[]{2,4,6,8,10,9,7,5,3,1});
		Utils.parallelArraysQSort(0, array1.length-1, ic, im, 1);
		Assert.assertArrayEquals(array2, array1);
		
		final int[]	source = new int[10000], target = source.clone();
		final int[]	temp = new int[10];
 		long 		total = 0;
		
		for(int count = 0; count < 1000; count++) {
			fillRandomArray(source);
			System.arraycopy(source, 0, target, 0, source.length);
			
			Arrays.sort(target);
//			System.err.print('.');
//			if (count % 100 == 0) {
//				System.err.println();
//			}
			long	start = System.nanoTime();
			Utils.parallelArraysQSort(0, source.length-1, 
					(i1,i2)->source[i2]-source[i1], 
					(f,t,len)->{
						if (len == 1) {
							if (t < 0) {
								temp[-1-t] = source[f];
							}
							else if (f < 0) {
								source[t] = temp[-1-f];
							}
							else {
								source[t] = source[f];
							}
						}
						else {
							if (t < 0) {
								System.arraycopy(source, f, temp, -1-t, len);
							}
							else if (f < 0) {
								System.arraycopy(temp, -1-f, source, t, len);
							}
							else {
								System.arraycopy(source, f, source, t, len);
							}
						}
						if (t < 0) {
							switch (len) {
								case 5 :
									temp[-1-t+4] = source[f+4];
								case 4 :
									temp[-1-t+3] = source[f+3];
								case 3 :
									temp[-1-t+2] = source[f+2];
								case 2 :
									temp[-1-t+1] = source[f+1];
								case 1 :
									temp[-1-t] = source[f];
									break;
								default :
									System.arraycopy(source, f, temp, -1-t, len);
							}
						}
						else if (f < 0) {
							if (len == 1) {
								source[t] = temp[-1-f];
							}
							else {
								System.arraycopy(temp, -1-f, source, t, len);
							}
						}
						else if (len == 1) {
							source[t] = source[f];
						}
						else {
							System.arraycopy(source, f, source, t, len);
						}
					}, temp.length);
			total += System.nanoTime() - start;
			Assert.assertArrayEquals(target, source);
		}
//		System.err.println("T="+total/1000000+"msec");
	}
	
	private static int[] fillRandomArray(final int[] result) {
		for(int index = 0; index < result.length; index++) {
			result[index] = (int) (1000 * Math.random() - 500);
		}
		return result;
	}

	private static void setArrayValue(final int[] target, final int[] source) {
		System.arraycopy(source, 0, target, 0, target.length);
	}
}
