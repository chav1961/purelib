package chav1961.purelib.model;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class ContentModelFactoryTest {
	@Test
	public void menuTest() throws IOException {
		try(final InputStream	is = this.getClass().getResourceAsStream("modelTest1.xml")) {
			
			final ContentMetadataInterface 	cmi = ContentModelFactory.forXmlDescription(is);
		
			System.err.println(cmi);
		}
	}
}
