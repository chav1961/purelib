package chav1961.purelib.fsys;


import java.io.IOException;

import java.net.URI;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
import chav1961.purelib.fsys.internal.FileSystemInMemory;
import chav1961.purelib.fsys.internal.FileSystemOnFile;
import chav1961.purelib.fsys.internal.FileSystemOnFileSystem;
import chav1961.purelib.fsys.internal.FileSystemOnRMI;
import chav1961.purelib.fsys.internal.FileSystemOnXMLReadOnly;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;

@Tag("OrdinalTestCategory")
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
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void spiTest() throws IOException, EnvironmentException {
		final Set<Class<FileSystemInterface>>	providers = new HashSet<>(); 
		
		for (FileSystemInterface item : ServiceLoader.load(FileSystemInterface.class)) {
			providers.add((Class<FileSystemInterface>)item.getClass());
		}
		Assert.assertEquals(5,providers.size());
		Assert.assertTrue(providers.contains(FileSystemOnFile.class));
		Assert.assertTrue(providers.contains(FileSystemOnFileSystem.class));
		Assert.assertTrue(providers.contains(FileSystemOnXMLReadOnly.class));
		Assert.assertTrue(providers.contains(FileSystemOnRMI.class));
		Assert.assertTrue(providers.contains(FileSystemInMemory.class));
	}
}
