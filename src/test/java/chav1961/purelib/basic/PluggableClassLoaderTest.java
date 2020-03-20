package chav1961.purelib.basic;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.fsys.FileSystemOnFileSystem;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class PluggableClassLoaderTest {
	@Before
	public void prepare() {
		new File("./src/test/resources/chav1961/purelib/basic/plugindirectory").listFiles(new FileFilter(){
				@Override
				public boolean accept(File pathname) {
					pathname.delete();
					return false;
				}
			}
		);
	}
	
	@After
	public void unprepare() {
		new File("./src/test/resources/chav1961/purelib/basic/plugindirectory").listFiles(new FileFilter(){
				@Override
				public boolean accept(File pathname) {
					pathname.delete();
					return false;
				}
			}
		);
	}
	
	@Test
	public void lifeCycleTest() throws IOException, ClassNotFoundException, EnvironmentException, InvocationTargetException, IllegalAccessException, IllegalArgumentException, NoSuchMethodException, SecurityException {
//		try(final PluggableClassLoader	pcl = new PluggableClassLoader(this.getClass().getClassLoader())) {
//			
//			int	count = 0;
//			for (String item : pcl.installed()) {
//				if (item != null) {
//					count++;
//				}
//			}
//			Assert.assertEquals(count,0);
//			
//			try(final FileSystemInterface	toAdd = new FileSystemOnFileSystem(URI.create("fsys:jar:./src/test/resources/chav1961/purelib/basic/test.jar"))) {
//				pcl.install("testplugin","test plugin description",toAdd.open("test.jar"));
//
//				count = 0;
//				for (String item : pcl.installed()) {
//					if (item != null) {
//						count++;
//					}
//				}
//				Assert.assertEquals(count,1);
//				Assert.assertTrue(pcl.wasInstalled("testplugin"));
//				Assert.assertFalse(pcl.wasInstalled("unknown"));
//				
//				Class<?>	cl = pcl.loadClass("chav1961.purelib.basic.PluggablePluginChild",true);
//				cl.getMethod("main",String[].class).invoke(null,(Object)new String[0]);
//			
//				pcl.uninstall("testplugin");
//				
//				try{cl = pcl.loadClass("chav1961.purelib.basic.PluggablePluginChild");
//					Assert.fail("Mandatory exception was not detected (plugin cclass is missing)");
//				} catch (ClassNotFoundException exc) {
//				}
//			}
//		}
	}
}
