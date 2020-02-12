package chav1961.purelib.fsys.bridge;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;

public class PureLibFileSystemProviderTest {
	@Test
	public void basicAndSPITest() throws IOException {
		final PureLibFileSystemProvider	p = new PureLibFileSystemProvider();
		final Set<URI>	schemes = new HashSet<>();
		final Set<URI>	providers = new HashSet<>(); 
		
		Assert.assertEquals(FileSystemInterface.FILESYSTEM_URI_SCHEME,p.getScheme());

		for (FileSystemInterfaceDescriptor item : ServiceLoader.load(FileSystemInterfaceDescriptor.class)) {
			providers.add(URI.create(item.getUriTemplate().getSchemeSpecificPart()));
		}
		
		try(final PureLibFileSystem		fs = (PureLibFileSystem) p.newFileSystem(URI.create("fsys:/"),Utils.mkMap())) {
			for (Path item : fs.getRootDirectories()) {
				schemes.add(item.toUri());
			}
		}
		Assert.assertEquals(providers,schemes);

		schemes.clear();
		try(final PureLibFileSystem		fs = (PureLibFileSystem) p.getFileSystem(URI.create("fsys:/"))) {
			for (Path item : fs.getRootDirectories()) {
				schemes.add(item.toUri());
			}
		}
		Assert.assertEquals(providers,schemes);
		
		try {p.newFileSystem((URI)null,Utils.mkMap());
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {p.newFileSystem(URI.create("unknown:/"),Utils.mkMap());
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument scheme)");
		} catch (IOException exc) {
		}
		try {p.newFileSystem(URI.create("fsys:/"),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}

		try {p.getFileSystem((URI)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {p.getFileSystem(URI.create("unknown:/"));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument scheme)");
		} catch (FileSystemNotFoundException exc) {
		}
	}
}
