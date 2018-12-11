package chav1961.purelib.fsys;


import java.io.IOException;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class FileSystemFactoryTest {

	@Test
	public void test() throws IOException {
		Assert.assertNotNull(FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:./")));
		
		try{FileSystemFactory.createFileSystem(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":./"));			
			Assert.fail("Mandatory exception was not detected (non-absolute URI argument)");
		} catch (IOException exc) {			
		}
		try{FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:./"),null);			
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {			
		}
		try{FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":unknown:./"));			
			Assert.fail("Mandatory exception was not detected (undeployed scheme)");
		} catch (IOException exc) {			
		}
	}
}
