package chav1961.purelib.fsys;


import java.io.IOException;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;

public class FileSystemFactoryTest {
	@Test
	public void creationTest() throws IOException {
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

	@Test
	public void descriptionTest() throws IOException, EnvironmentException {
		Assert.assertTrue(FileSystemFactory.getAvailableFileSystems().length > 0);

		for (FileSystemInterfaceDescriptor item : FileSystemFactory.getAvailableFileSystems()) {
			final Localizer		localizer = LocalizerFactory.getLocalizer(item.getLocalizerAssociated());
			
			Assert.assertEquals(item.getClass().getSimpleName(),item.getClassName());
			Assert.assertEquals(PureLibSettings.CURRENT_VERSION,item.getVersion());
			Assert.assertTrue(localizer.containsKey(item.getDescriptionId()));
			Assert.assertTrue(localizer.containsKey(item.getVendorId()));
			Assert.assertTrue(localizer.containsKey(item.getLicenseId()));
			Assert.assertTrue(localizer.containsKey(item.getLicenseContentId()));
			Assert.assertTrue(localizer.containsKey(item.getHelpId()));
			Assert.assertEquals(item,item.getInstance());
			Assert.assertNotNull(item.getUriTemplate());
			if (item instanceof FileSystemInMemory) {
				Assert.assertTrue(item.testConnection(item.getUriTemplate(),null));
			}
		}
	}
}
