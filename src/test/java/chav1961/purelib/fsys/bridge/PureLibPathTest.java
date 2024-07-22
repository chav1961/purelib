package chav1961.purelib.fsys.bridge;

import org.junit.Assert;

import org.junit.Before;

import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
public class PureLibPathTest {
	final PureLibFileSystemProvider p = new PureLibFileSystemProvider();
	PureLibFileSystem				fs;
	
	@Before
	public void startUp() throws Exception {
		fs = (PureLibFileSystem) p.getFileSystem(URI.create("fsys:/"));
	}

	@After
	public void shutDown() throws Exception {
		fs.close();
	}

	@Test
	public void relativeBasicTest() throws IOException {
		String[]	toTest;
		Path		p = new PureLibPath(fs,"/root/dir/file.ext");
		
		Assert.assertEquals(fs,p.getFileSystem());
		Assert.assertFalse(p.isAbsolute());
		Assert.assertNull(p.getRoot());
		
		Assert.assertEquals(URI.create("file.ext"),p.getFileName().toUri());
		Assert.assertEquals(URI.create("/root/dir/"),p.getParent().toUri());
		
		Assert.assertEquals(3,p.getNameCount());
		toTest = new String[]{"/root","/dir","/file.ext"};
		for (int index = 0; index < toTest.length; index++) {
			Assert.assertEquals(toTest[index],p.getName(index).toUri().toString());
		}
		
		Assert.assertEquals(URI.create("/dir/"),p.subpath(1,1).toUri());

		Assert.assertTrue(p.startsWith(p.getParent()));
		Assert.assertFalse(p.getParent().startsWith(p));

		Assert.assertTrue(p.endsWith(p.getFileName()));
		Assert.assertFalse(p.endsWith(p.getParent()));

		Assert.assertEquals(URI.create("/root/dir/"),p.resolve(p.getParent()).toUri());
		Assert.assertEquals(URI.create("/root/dir/file.ext"),p.getParent().resolve(p).toUri());

		Assert.assertEquals(URI.create("/root/dir/"),p.relativize(p.getParent()).toUri());
		Assert.assertEquals(URI.create("file.ext"),p.getParent().relativize(p).toUri());
		
		Assert.assertEquals(p,new PureLibPath(fs,"/root/dir/file.ext/x/../../file.ext").normalize());
		
		Assert.assertTrue(p.compareTo(p) == 0);
		Assert.assertTrue(p.compareTo(p.getParent()) > 0);
		Assert.assertTrue(p.getParent().compareTo(p) < 0);
	}

	@Test
	public void absoluteBasicTest() throws IOException {
		String[]	toTest;
		Path		p = new PureLibPath(fs,"file","/root/dir/file.ext"); 

		Assert.assertEquals(fs,p.getFileSystem());
		Assert.assertTrue(p.isAbsolute());
		Assert.assertEquals(URI.create("file:/"),p.getRoot().toUri());
		
		Assert.assertEquals(URI.create("file.ext"),p.getFileName().toUri());
		Assert.assertEquals(URI.create("file:/root/dir/"),p.getParent().toUri());
		
		Assert.assertEquals(4,p.getNameCount());
		toTest = new String[]{"file:/","/root","/dir","/file.ext"};
		for (int index = 0; index < toTest.length; index++) {
			Assert.assertEquals(toTest[index],p.getName(index).toUri().toString());
		}
		
		Assert.assertEquals(URI.create("file:/root/"),p.subpath(1,1).toUri());

		Assert.assertTrue(p.startsWith(p.getParent()));
		Assert.assertFalse(p.getParent().startsWith(p));

		Assert.assertTrue(p.endsWith(new PureLibPath(fs,"file","/file.ext")));
		Assert.assertFalse(p.endsWith(p.getParent()));

		Assert.assertEquals(URI.create("file:/root/dir/"),p.resolve(p.getParent()).toUri());
		Assert.assertEquals(URI.create("file:/root/dir/file.ext"),p.getParent().resolve(p).toUri());

		Assert.assertEquals(URI.create("file:/root/dir/"),p.relativize(p.getParent()).toUri());
		Assert.assertEquals(URI.create("file.ext"),p.getParent().relativize(p).toUri());

		Assert.assertEquals(p,p.toAbsolutePath());
		Assert.assertEquals(p,p.toRealPath());
		
		Assert.assertTrue(p.compareTo(p) == 0);
		Assert.assertTrue(p.compareTo(p.getParent()) > 0);
		Assert.assertTrue(p.getParent().compareTo(p) < 0);
	}

	@Test
	public void exceptionsTest() throws IOException {
		Path		p = new PureLibPath(fs,"file","/root/dir/file.ext");
		
		try{p.getName(-1);
			Assert.fail("Mandatory exception was not detected (name index out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{p.getName(999);
			Assert.fail("Mandatory exception was not detected (name index out of range)");
		} catch (IllegalArgumentException exc) {
		}
	
		try{p.subpath(-1,1);
			Assert.fail("Mandatory exception was not detected (start index out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{p.subpath(999,1);
			Assert.fail("Mandatory exception was not detected (start index out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{p.subpath(1,-1);
			Assert.fail("Mandatory exception was not detected (end index out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{p.subpath(1,999);
			Assert.fail("Mandatory exception was not detected (end index out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{p.subpath(2,1);
			Assert.fail("Mandatory exception was not detected (end index less than start)");
		} catch (IllegalArgumentException exc) {
		}

		try{p.startsWith((Path)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{p.startsWith(Paths.get("."));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		} 

		try{p.endsWith((Path)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{p.endsWith(Paths.get("."));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{p.resolve((Path)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{p.resolve(Paths.get("."));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{p.relativize((Path)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{p.relativize(Paths.get("."));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{p.compareTo((Path)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{p.compareTo(Paths.get("."));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (ClassCastException exc) {
		}
		
		Path		pr = new PureLibPath(fs,"/root/dir/file.ext");
	
		try{pr.toAbsolutePath();
			Assert.fail("Mandatory exception was not detected (relative paths can't be converted to absolute)");
		} catch (IOError exc) {
		}

		try{pr.toRealPath();
			Assert.fail("Mandatory exception was not detected (relative paths can't be converted to absolute)");
		} catch (IOException exc) {
		}

		try{pr.register(null,null,null);
			Assert.fail("Mandatory exception was not detected (unsupported call)");
		} catch (UnsupportedOperationException exc) {
		}
	}
}
