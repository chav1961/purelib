package chav1961.purelib.basic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class XMLUtilsTest {
	//	
	//	Walking and attributes test
	//
	@Test
	@Ignore
	public void walkingXMLTest() throws ParserConfigurationException, SAXException, IOException, ContentException {
		final DocumentBuilderFactory 	factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder 			builder = factory.newDocumentBuilder();
		final Document 					document = builder.parse(this.getClass().getResourceAsStream("walkingXML.xml"));
		final Set<String>				content = new HashSet<>(), toCompare = new HashSet<>();
		
		document.getDocumentElement().normalize();
		XMLUtils.walkDownXML(document.getDocumentElement(), (mode,node)->{
			if (mode == NodeEnterMode.ENTER) {
				content.add(node.getTagName());
				if (node.getChildNodes().getLength() == 1) {
					content.add(node.getTextContent().trim());
				}
			}
			return ContinueMode.CONTINUE;
		});
		toCompare.addAll(Arrays.asList("content21","content11","content22","content12","root","level21","level1","level11","level12","level2","level22"));
		Assert.assertEquals(toCompare,content);
		
		try{XMLUtils.walkDownXML(null,(mode,node)->{return ContinueMode.CONTINUE;});
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{XMLUtils.walkDownXML(document.getDocumentElement(), null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		} 
	}	

	@Test
	@Ignore
	public void attributesXMLTest() throws SyntaxException, ParserConfigurationException, SAXException, IOException {
		final DocumentBuilderFactory 	factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder 			builder = factory.newDocumentBuilder();
		final Document 					document = builder.parse(this.getClass().getResourceAsStream("walkingXML.xml"));
		Properties						props, newProps; 
		
		document.normalize();

		final Element 					node = document.getElementById("id11");	// See walkingXML.dtd
		
		props = XMLUtils.getAttributes(node);
		
		Assert.assertEquals("value11",props.getProperty("key11"));
		
		try{XMLUtils.getAttributes(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}		
		
		newProps = XMLUtils.joinAttributes(node,Utils.mkProps("key11","new","key12","value12"),false,false);
		Assert.assertEquals("new",newProps.getProperty("key11"));
		Assert.assertEquals("value12",newProps.getProperty("key12"));
		Assert.assertEquals("value11",XMLUtils.getAttributes(node).getProperty("key11"));
		Assert.assertFalse(XMLUtils.getAttributes(node).containsKey("key12"));

		newProps = XMLUtils.joinAttributes(node,Utils.mkProps("key11","new","key12","value12"),true,false);
		Assert.assertEquals("value11",newProps.getProperty("key11"));
		Assert.assertEquals("value12",newProps.getProperty("key12"));
		Assert.assertEquals("value11",XMLUtils.getAttributes(node).getProperty("key11"));
		Assert.assertFalse(XMLUtils.getAttributes(node).containsKey("key12"));

		newProps = XMLUtils.joinAttributes(node,Utils.mkProps("key11","new","key12","value12"),true,true);
		Assert.assertEquals("value11",newProps.getProperty("key11"));
		Assert.assertEquals("value12",newProps.getProperty("key12"));
		Assert.assertEquals("value11",XMLUtils.getAttributes(node).getProperty("key11"));
		Assert.assertEquals("value12",XMLUtils.getAttributes(node).getProperty("key12"));

		newProps = XMLUtils.joinAttributes(node,Utils.mkProps("key11","new","key12","value12"),false,true);
		Assert.assertEquals("new",newProps.getProperty("key11"));
		Assert.assertEquals("value12",newProps.getProperty("key12"));
		Assert.assertEquals("new",XMLUtils.getAttributes(node).getProperty("key11"));
		Assert.assertEquals("value12",XMLUtils.getAttributes(node).getProperty("key12"));

		
		try{XMLUtils.joinAttributes(null,Utils.mkProps("key11","new","key12","value12"),false,true);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}		
		try{XMLUtils.joinAttributes(node,null,false,true);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {			
		}		
		
		Assert.assertEquals("new",XMLUtils.getAttribute(node,"key11",String.class));
		Assert.assertEquals("value12",XMLUtils.getAttribute(node,"key12",String.class));

		try{XMLUtils.getAttribute(null,"key11",String.class); 
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}		
		try{XMLUtils.getAttribute(node,null,String.class);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}		
		try{XMLUtils.getAttribute(node,"",String.class);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}		
		try{XMLUtils.getAttribute(node,"key11",null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {			
		}		
	}

	@Test
	public void loadHTMLTest() throws IOException, NullPointerException, ContentException {
		try(final InputStream	is = new ByteArrayInputStream("<html id=\"100\"><body><p id=\"aa\">test <font color=red>test</font> test</p></body></html>".getBytes())) {
			final Document		doc = XMLUtils.loadHtml(is, PureLibSettings.CURRENT_LOGGER);
		
			Element item = doc.getDocumentElement();
			Assert.assertEquals("html", item.getNodeName());
			Assert.assertEquals("100", item.getAttribute("id"));
			Assert.assertEquals(2, item.getChildNodes().getLength());
		}
	}
}
