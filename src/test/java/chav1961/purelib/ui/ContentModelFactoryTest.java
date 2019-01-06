package chav1961.purelib.ui;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class ContentModelFactoryTest {
	@Test
	public void menuTest() throws IOException {
		try(final InputStream	is = this.getClass().getResourceAsStream("modelTest1.xml")) {
			
			//final ContentMetadataInterface 	cmi = 
					ContentModelFactory.forXmlDescription(is);
			
		}
	}
}
