package chav1961.purelib.i18n;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;

public class I18nTest {
	@Test
	public void multilangStringTest() {
        Locale.setDefault(Locale.ENGLISH);
        
		final MultilangString	ms = new MultilangString();
		
		Assert.assertEquals(ms.get(),"<No data for the locale ["+Locale.getDefault()+"]>");
		Assert.assertEquals(ms.add(Locale.GERMAN,"help").get(),"<No data for the locale ["+Locale.getDefault()+"]>");
		Assert.assertEquals(ms.get(Locale.GERMAN),"help");
		Assert.assertEquals(ms.add(Locale.FRANCE,"help G").add(Locale.getDefault(),"URA!!!").get(),"URA!!!");
		
		try{ms.add(null,"text");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{ms.add(Locale.getDefault(),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{ms.get(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void multilangStringRepoTest() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, ContentException {
		final DocumentBuilderFactory 	dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder 			dBuilder = dbFactory.newDocumentBuilder();
        final Reader					rdr = new StringReader(Utils.fromResource(this.getClass().getResource("test.xml")));
        final Document 					doc = dBuilder.parse(new InputSource(rdr));
          
        doc.getDocumentElement().normalize();
        
        final XPath 					xpath = XPathFactory.newInstance().newXPath();
        final String 					expression = "/repo";	        
        final Node						node = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);  
        
        Locale.setDefault(Locale.ENGLISH);

        final MultilangStringRepo		repo1 = new MultilangStringRepo(), repo2 = new MultilangStringRepo();
        
        Assert.assertEquals(repo1.size(),0);
        
        repo1.importData((Element) node);
        
        Assert.assertEquals(repo1.size(),3);
        Assert.assertTrue(repo1.contains("item1"));
        Assert.assertFalse(repo1.contains("unknown"));
        
        repo1.add("item4",new MultilangString().add(Locale.getDefault(),"assa"));
        Assert.assertEquals(repo1.size(),4);
        Assert.assertEquals(repo1.get("item4").get(),"assa");
        
        Assert.assertEquals(repo2.add(repo1).size(),4);
        Assert.assertEquals(repo2.remove("item4").get(),"assa");
        
        Assert.assertEquals(repo1.substitute("123${item1}456"),"123value1456");
        Assert.assertEquals(repo1.substitute("${item1}456"),"value1456");
        Assert.assertEquals(repo1.substitute("123${item1}"),"123value1");
        
        
        try{repo2.importData(null);
        	Assert.fail("Mandatory exception was not detected (null 1-st argument)");
        } catch (IllegalArgumentException exc) {
        }
        
        try{repo2.contains(null);
    		Assert.fail("Mandatory exception was not detected (null 1-st argument)");
        } catch (IllegalArgumentException exc) {
        }
        try{repo2.contains("");
    		Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
        } catch (IllegalArgumentException exc) {
        }

        try{repo2.get(null);
    		Assert.fail("Mandatory exception was not detected (null 1-st argument)");
        } catch (IllegalArgumentException exc) {
        }
        try{repo2.get("");
    		Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
        } catch (IllegalArgumentException exc) {
        }
        try{repo2.get("unknown");
    		Assert.fail("Mandatory exception was not detected (non-existent key)");
        } catch (IllegalArgumentException exc) {
        }

        try{repo2.remove(null);
    		Assert.fail("Mandatory exception was not detected (null 1-st argument)");
        } catch (IllegalArgumentException exc) {
        }
        try{repo2.remove("");
    		Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
        } catch (IllegalArgumentException exc) {
        }
        try{repo2.remove("unknown");
    		Assert.fail("Mandatory exception was not detected (non-existent key)");
        } catch (IllegalArgumentException exc) {
        }
        
        try{repo2.add(null);
    		Assert.fail("Mandatory exception was not detected (null 1-st argument)");
        } catch (IllegalArgumentException exc) {
        }
        try{repo2.add(null,new MultilangString().add(Locale.getDefault(),"assa"));
    		Assert.fail("Mandatory exception was not detected (null 1-st argument)");
        } catch (IllegalArgumentException exc) {
        }
        try{repo2.add("",new MultilangString().add(Locale.getDefault(),"assa"));
    		Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
        } catch (IllegalArgumentException exc) {
        }
        try{repo2.add("item",null);
    		Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
        } catch (IllegalArgumentException exc) {
        }
	}
}
