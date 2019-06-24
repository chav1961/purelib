package chav1961.purelib.javadoc;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NewPureLibDocletTest {
	@Test
	public void placeAndSeekPackageTest() throws IOException, ParserConfigurationException {
		try{final DocumentBuilderFactory 	docFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder 			docBuilder = docFactory.newDocumentBuilder();
			final Document 					doc = docBuilder.newDocument();
		    final Element 					rootElement = doc.createElementNS(NewPureLibDoclet.TAG_PREFIX,"javadoc");

		    NewPureLibDoclet.placePackage(rootElement,doc,"package.subpackage");
		    Assert.assertEquals("subpackage",NewPureLibDoclet.seekPackage(rootElement,"package.subpackage").getAttribute(NewPureLibDoclet.ATTR_NAME));
		} catch (ParserConfigurationException | TransformerFactoryConfigurationError | DOMException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void complexTest() throws IOException, ParserConfigurationException {
		NewPureLibDoclet.main(new String[0]);
	}	
}
