package chav1961.purelib.fsys.bridge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class PureLibFileSystemProviderTest {
	
	final File	ioDir = new File(new File(System.getProperty("java.io.tmpdir")),"testDir");
	
	@Before
	public void prepare() throws IOException {
		new File(ioDir,"fromDir").mkdirs();
		try(final FileOutputStream	fos = new FileOutputStream(new File(ioDir,"fromDir/fromFile.txt"))) {
			fos.write("test string".getBytes());
			fos.flush();
		}
	}

	@After
	public void unprepare() throws IOException { 
		Utils.deleteDir(ioDir);
	}
	
	@Test
	public void basicAndSPITest() throws IOException {
		final PureLibFileSystemProvider	p = new PureLibFileSystemProvider();
		final Set<URI>	schemes = new HashSet<>();
		final Set<URI>	providers = new HashSet<>(); 
		
		Assert.assertEquals(FileSystemInterface.FILESYSTEM_URI_SCHEME,p.getScheme());

		for (FileSystemInterfaceDescriptor item : FileSystemFactory.getAvailableFileSystems()) {
			providers.add(URI.create(item.getUriTemplate().getSchemeSpecificPart()));
		}
		Assert.assertTrue(!providers.isEmpty());
		
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

	@Test
	public void viewsAndAttributesTest() throws IOException {
		final PureLibFileSystemProvider	plfsp = new PureLibFileSystemProvider();
		
		
	}
	
	@Test
	public void pathAndActionsTest() throws IOException {
		final PureLibFileSystemProvider	plfsp = new PureLibFileSystemProvider();
		
		try(final PureLibFileSystem		fs = (PureLibFileSystem) plfsp.newFileSystem(URI.create("fsys:/"),Utils.mkMap())) {
			final Path					p = fs.getPath("file",ioDir.getAbsoluteFile().toURI().getSchemeSpecificPart());
			final Set<String>			dirNames = new HashSet<>();
			
			Assert.assertTrue(p.isAbsolute());
			Assert.assertEquals(p,plfsp.getPath(p.toUri()));

			try{fs.getPath(null,ioDir.getAbsoluteFile().toURI().getSchemeSpecificPart());
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{fs.getPath("",ioDir.getAbsoluteFile().toURI().getSchemeSpecificPart());
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{fs.getPath("file",(String[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{fs.getPath("file",(String)null);
				Assert.fail("Mandatory exception was not detected (nulls inside 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			try{plfsp.getPath((URI)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{plfsp.getPath(URI.create("abcde"));
				Assert.fail("Mandatory exception was not detected (1-st argument is not absolute URI)");
			} catch (IllegalArgumentException exc) {
			}
			try{plfsp.getPath(URI.create("unknown:/abcde"));
				Assert.fail("Mandatory exception was not detected (1-st argument refers to unknown file system)");
			} catch (FileSystemNotFoundException exc) {
			}
			
			final Path					pNew = fs.getPath("file",ioDir.getAbsoluteFile().toURI().getSchemeSpecificPart(),"toDir");
			
			plfsp.createDirectory(pNew);

			try{plfsp.createDirectory(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{plfsp.createDirectory(Paths.get(URI.create("file:/c:/")));
				Assert.fail("Mandatory exception was not detected (1-st argument is not a Pure Library path)");
			} catch (IllegalArgumentException exc) {
			}
			
			try(DirectoryStream<Path>	stream = plfsp.newDirectoryStream(p,PureLibFileSystemProvider.ALL_CONTENT)) {
				for (Path item : stream) {
					dirNames.add(item.getFileName().toUri().toString());
				}
			}
			Assert.assertEquals(Set.of("fromDir","toDir"),dirNames);

			try{plfsp.newDirectoryStream(null,PureLibFileSystemProvider.ALL_CONTENT);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{plfsp.newDirectoryStream(Paths.get(URI.create("file:/c:/")),PureLibFileSystemProvider.ALL_CONTENT);
				Assert.fail("Mandatory exception was not detected (1-st argument is not a Pure Library path)");
			} catch (IllegalArgumentException exc) {
			}
			try{plfsp.newDirectoryStream(fs.getPath("file",ioDir.getAbsoluteFile().toURI().getSchemeSpecificPart(),"unknown"),PureLibFileSystemProvider.ALL_CONTENT);
				Assert.fail("Mandatory exception was not detected (1-st argument refers to invalid object)");
			} catch (NotDirectoryException exc) {
			}
			try{plfsp.newDirectoryStream(p,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			final Path					pOld = fs.getPath("file",ioDir.getAbsoluteFile().toURI().getSchemeSpecificPart(),"fromDir");
			
			plfsp.move(pOld,pNew);
			
			try{plfsp.copy(null,pNew);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{plfsp.copy(Paths.get(URI.create("file:/c:/")),pNew);
				Assert.fail("Mandatory exception was not detected (1-st argument is not a Pure Library path)");
			} catch (IllegalArgumentException exc) {
			}
			try{plfsp.copy(pOld,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{plfsp.copy(pOld,Paths.get(URI.create("file:/c:/")));
				Assert.fail("Mandatory exception was not detected (2-nd argument is not a Pure Library path)");
			} catch (IllegalArgumentException exc) {
			}
			 
			try{plfsp.delete(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{plfsp.delete(Paths.get(URI.create("file:/c:/")));
				Assert.fail("Mandatory exception was not detected (1-st argument is not a Pure Library path)");
			} catch (IllegalArgumentException exc) {
			}

			dirNames.clear();
			try(DirectoryStream<Path>	stream = plfsp.newDirectoryStream(p,PureLibFileSystemProvider.ALL_CONTENT)) {
				for (Path item : stream) {
					dirNames.add(item.getFileName().toUri().toString());
				}
			}
			Assert.assertEquals(Set.of("toDir"),dirNames);
			
			Assert.assertTrue(plfsp.isSameFile(pNew,pNew)); 
			Assert.assertFalse(plfsp.isSameFile(pNew,pOld));

			try{plfsp.isSameFile(null,pNew);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{plfsp.isSameFile(pNew,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}

			Assert.assertFalse(plfsp.isHidden(pNew));

			try{plfsp.isHidden(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
		}
	}
}

