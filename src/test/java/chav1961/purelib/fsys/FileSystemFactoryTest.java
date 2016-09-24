package chav1961.purelib.fsys;


import java.io.IOException;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

public class FileSystemFactoryTest {

	@Test
	public void test() throws IOException {
		Assert.assertNotNull(FileSystemFactory.createFileSystem(URI.create("file:./")));
		
		try{FileSystemFactory.createFileSystem(null);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (IllegalArgumentException exc) {			
		}
		try{FileSystemFactory.createFileSystem(URI.create("./"));			
			Assert.fail("Mandatory exception was not detected (non-absolute URI argument)");
		} catch (IllegalArgumentException exc) {			
		}
		try{FileSystemFactory.createFileSystem(URI.create("file:./"),null);			
			Assert.fail("Mandatory exception was not detected (null s-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
		try{FileSystemFactory.createFileSystem(URI.create("unknown:./"));			
			Assert.fail("Mandatory exception was not detected (undeployed scheme)");
		} catch (IOException exc) {			
		}
	}
}
