package chav1961.purelib.basic;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.streams.charsource.StringCharSource;
import chav1961.purelib.streams.chartarget.StringBuilderCharTarget;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

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
		
		try{Utils.primitive2Wrapper(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{Utils.primitive2Wrapper(String.class);
			Assert.fail("Mandatory exception was not detected (1-st argument is not primitive class)");
		} catch (IllegalArgumentException exc) {
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
	public void checkArrayContentTest() {
		Assert.assertEquals(-1,Utils.checkArrayContent4Nulls(new Object[]{"","",""}));
		Assert.assertEquals(2,Utils.checkArrayContent4Nulls(new Object[]{"","",null}));
		
		try{Utils.checkArrayContent4Nulls(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{Utils.checkArrayContent4Nulls("");
			Assert.fail("Mandatory exception was not detected (1-st argument is not array)");
		} catch (IllegalArgumentException exc) {
		}
		try{Utils.checkArrayContent4Nulls(new int[]{1,2,3});
			Assert.fail("Mandatory exception was not detected (1-st argument is not referenced array)");
		} catch (IllegalArgumentException exc) {
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
}
