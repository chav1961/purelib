package chav1961.purelib.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class ContentMetadataFilterTest {
	@Test
	public void basicTest() throws IOException, EnvironmentException {
		try(final InputStream				is = ContentMetadataFilterTest.class.getResourceAsStream("modelTest1.xml")) {
			final ContentMetadataInterface	nested = ContentModelFactory.forXmlDescription(is);
			final ContentMetadataFilter		filter1 = new ContentMetadataFilter(nested, ".*", "item.*");
			final ContentMetadataFilter		filter2 = new ContentMetadataFilter(nested, ".*", "item.*");
			final int[] 					count = new int[1];

			count[0] = 0;
			nested.walkDown((a,b,c,d)->{count[0]++; return ContinueMode.CONTINUE;},nested.getRoot().getUIPath());
			Assert.assertEquals(16,count[0]);
			
			Assert.assertTrue(filter1.isAllowed(URI.create("")));
			Assert.assertFalse(filter1.isAllowed(URI.create("")));
			Assert.assertFalse(filter1.isAllowed(URI.create("")));

			count[0] = 0;
			filter1.walkDown((a,b,c,d)->{count[0]++; return ContinueMode.CONTINUE;},nested.getRoot().getUIPath());
			Assert.assertEquals(0,count[0]);

			Assert.assertTrue(filter2.isAllowed(URI.create("")));
			Assert.assertFalse(filter2.isAllowed(URI.create("")));
			Assert.assertFalse(filter2.isAllowed(URI.create("")));
			
			count[0] = 0;
			filter2.walkDown((a,b,c,d)->{count[0]++; return ContinueMode.CONTINUE;},nested.getRoot().getUIPath());
			Assert.assertEquals(0,count[0]);
		}
	}

	@Test
	public void exceptionsTest() throws IOException, EnvironmentException {
		try(final InputStream				is = ContentMetadataFilterTest.class.getResourceAsStream("")) {
			final ContentMetadataInterface	nested = ContentModelFactory.forXmlDescription(is);

			try{new ContentMetadataFilter(null, "*");
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ContentMetadataFilter(null, "*", "*");
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ContentMetadataFilter(null, new URI[0]);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ContentMetadataFilter(null, new URI[0], new URI[0]);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
	
			try{new ContentMetadataFilter(nested, (String)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ContentMetadataFilter(nested, "");
				Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ContentMetadataFilter(nested, (URI[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ContentMetadataFilter(nested, new URI[0]);
				Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ContentMetadataFilter(nested, new URI[]{null});
				Assert.fail("Mandatory exception was not detected (null inside 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
	
			try{new ContentMetadataFilter(nested, "*", (String)null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ContentMetadataFilter(nested, "*", "");
				Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ContentMetadataFilter(nested, new URI[]{URI.create("unknown")}, (URI[])null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ContentMetadataFilter(nested, new URI[]{URI.create("unknown")}, new URI[0]);
				Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ContentMetadataFilter(nested, new URI[]{URI.create("unknown")}, new URI[]{null});
				Assert.fail("Mandatory exception was not detected (null inside 3-rd argument)");
			} catch (IllegalArgumentException exc) {
			}
		}
	}
}
