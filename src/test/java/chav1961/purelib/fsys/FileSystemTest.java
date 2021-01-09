package chav1961.purelib.fsys;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarOutputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.testing.OrdinalTestCategory;


@Category(OrdinalTestCategory.class)
public class FileSystemTest {
	@Before
	public void prepare() throws ClassNotFoundException, IOException {
		new File("./src/test/resources/chav1961/purelib/fsys/fsTest/").listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				pathname.listFiles(this);
				pathname.delete();
				return false;
			}
		});
		new File("./src/test/resources/chav1961/purelib/fsys/fsTestJar/").listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				pathname.listFiles(this);
				pathname.delete();
				return false;
			}
		});
		new File("./src/test/resources/chav1961/purelib/fsys/fsTestJar/").mkdirs();
		try(final OutputStream		os = new FileOutputStream(new File("./src/test/resources/chav1961/purelib/fsys/fsTestJar/fs.jar"));
			final JarOutputStream	jos = new JarOutputStream(os)) {
			jos.finish();
		}
	}

	@After
	public void unprepare() {
		new File("./src/test/resources/chav1961/purelib/fsys/fsTest/").listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				pathname.listFiles(this);
				pathname.delete();
				return false;
			}
		});
		new File("./src/test/resources/chav1961/purelib/fsys/fsTestJar/").listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				pathname.listFiles(this);
				pathname.delete();
				return false;
			}
		});
	}

	@Test
	public void onXMLReadOnlyTest() throws Exception {
		try(final FileSystemInterface	fs = new FileSystemOnXMLReadOnly(URI.create("xmlReadOnly:"+new File("./src/test/resources/chav1961/purelib/fsys/xmlreadonly.xml").toURI().toString()))) {
			
			try(final FileOutputStream	fos = new FileOutputStream("./src/test/resources/chav1961/purelib/fsys/fsTest/innerFile.txt")) {
				
				fos.write("test string".getBytes());
				fos.flush();
			}
			
			Assert.assertTrue(fs.exists());
			Assert.assertEquals(fs.getPath(),"/");
			Assert.assertEquals(fs.getName(),"/");
			
			final String[]	list = fs.list();
			Assert.assertEquals(list.length,1);
			Assert.assertArrayEquals(list,new String[]{"/"});
			
			final String[]	list2 = fs.open("/root").list();
			Assert.assertTrue(fs.exists());
			Assert.assertTrue(fs.isDirectory());
			Assert.assertFalse(fs.isFile());
			Assert.assertTrue(fs.canRead());
			Assert.assertFalse(fs.canWrite());
			Assert.assertEquals(fs.getPath(),"/root");
			Assert.assertEquals(fs.getName(),"root");
			Assert.assertEquals(list2.length,2);
			Assert.assertArrayEquals(list2,new String[]{"directory1","link1"});

			final String[]	list3 = fs.open("./directory1").list();
			Assert.assertTrue(fs.exists());
			Assert.assertTrue(fs.isDirectory());
			Assert.assertFalse(fs.isFile());
			Assert.assertTrue(fs.canRead());
			Assert.assertFalse(fs.canWrite());
			Assert.assertEquals(fs.getPath(),"/root/directory1");
			Assert.assertEquals(fs.getName(),"directory1");
			Assert.assertEquals(list3.length,2);
			Assert.assertArrayEquals(list3,new String[]{"directory2","file1"});
			
			try(final Writer	wr = new StringWriter()) {
				fs.open("./file1").copy(wr);
				Assert.assertEquals(wr.toString(),"file content");
			}

			final String[]	list4 = fs.open("/root/link1").list();
			
			Assert.assertTrue(fs.exists());
			Assert.assertTrue(fs.isDirectory());
			Assert.assertTrue(fs.canRead());
			Assert.assertTrue(fs.canWrite());
			Assert.assertEquals(fs.getPath(),"/root/link1");
			Assert.assertEquals(fs.getName(),"link1");
			Assert.assertEquals(list4.length,1);
			Assert.assertArrayEquals(list4,new String[]{"innerFile.txt"});
		}
	}

//	@Test
	public void onClassLoaderReadOnlyTest() throws Exception {
		try(final FileSystemInterface	fs = new FileSystemOnClassLoader(URI.create("classloader:/chav1961/purelib/fsys/classloader"))) {
			
			try(final FileOutputStream	fos = new FileOutputStream("./src/test/resources/chav1961/purelib/fsys/fsTest/innerFile.txt")) {
				
				fos.write("test string".getBytes());
				fos.flush();
			}
			
			Assert.assertTrue(fs.exists());
			Assert.assertEquals(fs.getPath(),"/");
			Assert.assertEquals(fs.getName(),"/");
			
			final String[]	list = fs.list();
			Assert.assertEquals(list.length,2);
			Assert.assertArrayEquals(list,new String[]{"dir","file.txt"});
			
			final String[]	list2 = fs.open("/dir").list();
			Assert.assertTrue(fs.exists());
			Assert.assertTrue(fs.isDirectory());
			Assert.assertFalse(fs.isFile());
			Assert.assertTrue(fs.canRead());
			Assert.assertFalse(fs.canWrite());
			Assert.assertEquals(fs.getPath(),"/dir");
			Assert.assertEquals(fs.getName(),"dir");
			Assert.assertEquals(list2.length,1);
			Assert.assertArrayEquals(list2,new String[]{"innerFile.txt"});

			try(final Writer	wr = new StringWriter()) {
				fs.open("./innerFile.txt").copy(wr);
				Assert.assertEquals(wr.toString(),"test string");
			}
		}
	}
	
	@Test
	public void basicTest() throws Exception {
		// test usual file system
		try(final FileSystemInterface	fs = new FileSystemOnFile(new File("./src/test/resources/chav1961/purelib/fsys/fsTest/").toURI())) {
			test(fs,false);
		}

		// test file system on file system
		final File	f = new File("./src/test/resources/chav1961/purelib/fsys/fsTestJar/fs.jar");
		try(final FileSystemInterface	fs = new FileSystemOnFileSystem(URI.create("fsys:jar:"+f.toURI()))) {
			test(fs,false);
		}
		
		// test file system in memory
		try(final FileSystemInterface	fs = new FileSystemInMemory(URI.create("/"))) {
			test(fs,false);
		}
		
		// Test RMI connection to the file system
		try{java.rmi.registry.LocateRegistry.createRegistry(Registry.REGISTRY_PORT);	// Start RMI registry
		} catch (ExportException ex) {
			java.rmi.registry.LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
		}
		try(final FileSystemInterface	fsNest = new FileSystemOnFile(new File("./src/test/resources/chav1961/purelib/fsys/fsTest/").toURI());
			final RMIFileSystemServer	fss = new RMIFileSystemServer(URI.create("rmi://localhost:"+Registry.REGISTRY_PORT+"/testRMI"),fsNest);  
			final FileSystemInterface	fs = new FileSystemOnRMI(URI.create("rmi://localhost:"+Registry.REGISTRY_PORT+"/testRMI"))) {
			test(fs,false);
		}
		
	}

	@Test
	public void joinTest() throws Exception {
		try(final FileSystemInterface	fs = new FileSystemOnFile(new File("./src/test/resources/chav1961/purelib/fsys/fsTest/").toURI())) {
			joinTest(fs,false);
		}

		// test file system on file system
		final File	f = new File("./src/test/resources/chav1961/purelib/fsys/fsTestJar/fs.jar");
		try(final FileSystemInterface	fs = new FileSystemOnFileSystem(URI.create("fsys:jar:"+f.toURI()))) {
			joinTest(fs,false);
		}
		
		// test file system in memory
		try(final FileSystemInterface	fs = new FileSystemInMemory(URI.create("/"))) {
			joinTest(fs,false);
		}
		
		// Test RMI connection to the file system
		try{java.rmi.registry.LocateRegistry.createRegistry(Registry.REGISTRY_PORT);	// Start RMI registry
		} catch (ExportException ex) {
			java.rmi.registry.LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
		}
		try(final FileSystemInterface	fsNest = new FileSystemOnFile(new File("./src/test/resources/chav1961/purelib/fsys/fsTest/").toURI());
			final RMIFileSystemServer	fss = new RMIFileSystemServer(URI.create("rmi://localhost:"+Registry.REGISTRY_PORT+"/testRMI"),fsNest);  
			final FileSystemInterface	fs = new FileSystemOnRMI(URI.create("rmi://localhost:"+Registry.REGISTRY_PORT+"/testRMI"))) {
			joinTest(fs,false);
		}
	}
	
	
	private void test(final FileSystemInterface fs, boolean testMetadata) throws Exception {
		int	count;
		
		Assert.assertEquals(fs.getPath(),"/");		// Test root
		Assert.assertEquals(fs.getName(),"/");
		Assert.assertTrue(fs.exists());
		Assert.assertTrue(fs.isDirectory());
		Assert.assertFalse(fs.isFile());
		Assert.assertTrue(fs.canRead());
		Assert.assertTrue(fs.canWrite());
		Assert.assertTrue(fs.lastModified() >= 0);
		Assert.assertEquals(fs.size(),0); 
		
		try{fs.open(null);
			Assert.fail("Mandatory exceptin was not detected (null argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{fs.open("");
			Assert.fail("Mandatory exceptin was not detected (empty argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{fs.open("../");
			Assert.fail("Mandatory exceptin was not detected (attempt to jump higher than root)");
		} catch (IllegalArgumentException exc) {
		}
		
		fs.open("./newDir");						// Creation of the directory
		Assert.assertEquals(fs.getPath(),"/newDir");
		Assert.assertEquals(fs.getName(),"newDir");
		Assert.assertFalse(fs.exists());
		
		fs.mkDir();
		Assert.assertTrue(fs.exists());
		Assert.assertTrue(fs.isDirectory());
		Assert.assertFalse(fs.isFile());

		try{fs.create();
			Assert.fail("Mandatory exceptin was not detected (there is a directory with the same name here)");
		} catch (IOException exc) {
		}
		
		fs.open("./nestedDir").mkDir().open("../newFile.txt").create();	// Creation of the directory, changing path and creation of the file 
		
		Assert.assertEquals(fs.getPath(),"/newDir/newFile.txt");
		Assert.assertTrue(fs.exists());
		Assert.assertFalse(fs.isDirectory());
		Assert.assertTrue(fs.isFile());

		try{fs.mkDir();
			Assert.fail("Mandatory exceptin was not detected (there is a file with the same name here)");
		} catch (IOException exc) {
		}
		
													// Get directory content
		final Set<String> content = new HashSet<String>(){private static final long serialVersionUID = 1L; {addAll(Arrays.asList(fs.open("../").list()));}}
						, awaited = new HashSet<String>(){private static final long serialVersionUID = 1L; {addAll(Arrays.asList("newFile.txt","nestedDir"));}}; 
		Assert.assertTrue(content.equals(awaited));
		
													// Writing and reading files
		try(final Reader	is = new StringReader("test string");	
			final Writer	os = fs.open("./nestedDir/nestedFile.txt").create().charWrite()) {
			
			Utils.copyStream(is, os);
		}

		Assert.assertEquals(fs.size(),11);
		count = 0;
		try(final Reader	is = fs.charRead();		
			final Writer	os = new StringWriter()) {
			
			Utils.copyStream(is, os);
			Assert.assertEquals(os.toString(),"test string");
			count++;
		}
		Assert.assertEquals(count,1);
		
		final String	actualPath = fs.getPath();			// Test push/pop functionality
		
		try(final FileSystemInterface		clone = fs.clone()) {	// Clone, move data and test moving results
			Assert.assertEquals(fs.getPath(),clone.getPath());
			fs.push("/newDir").move(clone.open("/copyDir").mkDir()).pop();
		}
		Assert.assertEquals(fs.getPath(),actualPath);
		
		count = 0;
		try(final InputStream	is = fs.open("/copyDir/nestedDir/nestedFile.txt").read();
			final OutputStream	os = new ByteArrayOutputStream()) {
			
			Utils.copyStream(is, os);
			Assert.assertEquals(os.toString(),"test string");
			count++;
		}
		Assert.assertEquals(count,1);

		fs.rename("nestedFile.new");						// Test rename
		count = 0;
		for (String item : fs.open("../").list(".*")) {
			Assert.assertEquals(item,"nestedFile.new");
			count++;
		}
		Assert.assertEquals(count,1);
		
		if (testMetadata) {									// Test metadata if needed
			fs.setAttributes(Utils.mkMap("key1","value1"));
			Assert.assertEquals(fs.getAttributes().get("key1").toString(),"value1");
		}		

															// Test mount/unmount
		try(final FileSystemInterface	mount = new FileSystemOnFile(new File("./src/test/resources/chav1961/purelib/fsys/advanced/").toURI())) {
			fs.open("/copyDir/nestedDir").mount(mount);
			
			Assert.assertArrayEquals(fs.open("../nestedDir").list(),new String[]{"advancedfile.txt"});
			Assert.assertEquals(fs.open("/copyDir/nestedDir").unmount(),mount);
			Assert.assertArrayEquals(fs.list(),new String[]{"nestedFile.new"});
		}
		
		try{fs.open("/copyDir").delete();					// Remove directory
			Assert.fail("Mandatory exceptin was not detected (attempt to remove non-empty directory)");
		} catch (IOException exc) {
		}
		
		fs.deleteAll();
		Assert.assertFalse(fs.exists());
	}


	private void joinTest(final FileSystemInterface fs, boolean testMetadata) throws Exception {
		try(final FileSystemInterface	join = new FileSystemOnFile(new File("./src/test/resources/chav1961/purelib/fsys/advanced/").toURI())) {
			fs.open("/newDir").mkDir();
			
			Assert.assertEquals(0,fs.list().length);
			Assert.assertFalse(fs.isJoined());
			
			fs.join(join);
			Assert.assertEquals(1,fs.list().length);
			Assert.assertTrue(fs.isJoined());
			
			Assert.assertEquals(join.getPath(),fs.unjoin().getPath());
			Assert.assertEquals(0,fs.list().length);
			Assert.assertFalse(fs.isJoined());
		}		
	}
}
