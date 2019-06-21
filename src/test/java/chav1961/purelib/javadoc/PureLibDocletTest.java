package chav1961.purelib.javadoc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.javadoc.PureLibDoclet;

public class PureLibDocletTest {
	@Test
	public void walkTest() throws ParserConfigurationException {
		final DocumentBuilderFactory 	docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder 			docBuilder = docFactory.newDocumentBuilder();
		final Document 					doc = docBuilder.newDocument();
	    final Element 	rootElement = doc.createElementNS("app","root");
	    final Element 	childElement1 = doc.createElementNS("app","child1"), childElement2 = doc.createElementNS("app","child2");
	    final Element 	childElement11 = doc.createElementNS("app","child11"), childElement12 = doc.createElementNS("app","child12");
	    final Element 	childElement21 = doc.createElementNS("app","child21"), childElement22 = doc.createElementNS("app","child22");
	    final int[]						count = new int[]{0};
	    
	    childElement1.appendChild(childElement11);	childElement1.appendChild(childElement12);
	    childElement2.appendChild(childElement21);	childElement2.appendChild(childElement22);
	    rootElement.appendChild(childElement1);		rootElement.appendChild(childElement2);
	    doc.appendChild(rootElement);
	    
	    PureLibDoclet.walk(rootElement,(mode,node)->{
	    	if (mode == NodeEnterMode.ENTER) {
	    		count[0]++;
	    	}
	    	return ContinueMode.CONTINUE;
	    });
	    Assert.assertEquals(7,count[0]);
	    
	    try{PureLibDoclet.walk(null,(mode,node)->{return ContinueMode.CONTINUE;});
	    	Assert.fail("Mandatory exception was not detected (null 1-st argument)");
	    } catch (NullPointerException exc) {
	    }
	    try{PureLibDoclet.walk(rootElement,null);
	    	Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
	    } catch (NullPointerException exc) {
	    }
	}

	@Test
	public void buildAndSeekPackageTreeTest() throws ParserConfigurationException {
		final DocumentBuilderFactory 	docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder 			docBuilder = docFactory.newDocumentBuilder();
		final Document 					doc = docBuilder.newDocument();
	    final Element 	rootElement = doc.createElementNS("app","root");
	    final int[]						count = new int[]{0};
	    final Set<String>				set = new HashSet<>();
	    
	    doc.appendChild(rootElement);
	    set.addAll(Arrays.asList("p1.p2.p31","p1","p1.p2","p1.p2.p32"));
	    PureLibDoclet.buildPackageTree(rootElement,doc,set);
	    
	    PureLibDoclet.walk((Element)rootElement.getFirstChild(),(mode,node)->{
	    	if (mode == NodeEnterMode.ENTER) {
	    		count[0]++;
	    	}
	    	return ContinueMode.CONTINUE;
	    });
	    Assert.assertEquals(4,count[0]);
	    
	    Assert.assertEquals("p32",PureLibDoclet.seekPackage((Element)rootElement,"p1.p2.p32").getAttribute("name"));

	    try{PureLibDoclet.buildPackageTree(null,doc,set);
	    	Assert.fail("Mandatory exception was not detected (null 1-st argument)");
	    } catch (NullPointerException exc) {
	    }
	    try{PureLibDoclet.buildPackageTree(rootElement,null,set);
	    	Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
	    } catch (NullPointerException exc) {
	    }
	    try{PureLibDoclet.buildPackageTree(rootElement,doc,null);
	    	Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
	    } catch (NullPointerException exc) {
	    }
	    
	    try{PureLibDoclet.seekPackage(null,"p1.p2.p32");
	    	Assert.fail("Mandatory exception was not detected (null 1-st argument)");
	    } catch (NullPointerException exc) {
	    }
	    try{PureLibDoclet.seekPackage((Element)rootElement,null);
	    	Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
	    } catch (IllegalArgumentException exc) {
	    }
	    try{PureLibDoclet.seekPackage((Element)rootElement,"");
	    	Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
	    } catch (IllegalArgumentException exc) {
	    }
	}
}
