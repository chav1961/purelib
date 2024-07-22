package chav1961.purelib.ui.interfaces;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.Utils;

@Tag("OrdinalTestCategory")
public class LRUPersistenceTest {
	@Test
	public void staticTest() throws IOException {
		final File				dir = new File(System.getProperty("java.io.tmpdir"));
		final File				f = new File(dir,".lru.persistence.props");
		final List<String>		list = new ArrayList<>();
		final List<String>		template = Arrays.asList("test1", "test2", "test3", "test4", "test5", "test6", "test7", "test8", "test9", "test10", "test11");
		final LRUPersistence	inst = LRUPersistence.of(f, "prefix"); 
		
		if (f.exists() && f.isDirectory()) {
			Utils.deleteDir(f);
		}
		try{inst.loadLRU(list);
			
			Assert.assertEquals(Arrays.asList(), list);
			list.addAll(template);
			inst.saveLRU(list);
			
			list.clear();
			inst.loadLRU(list);
			Assert.assertEquals(template, list);
		} finally {
			Utils.deleteDir(f);
		}
		
		try{LRUPersistence.of(null, "prefix");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{LRUPersistence.of(dir, "prefix");
			Assert.fail("Mandatory exception was not detected (1-st argument is not a file)");
		} catch (IllegalArgumentException exc) {
		}
		try{LRUPersistence.of(f, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{LRUPersistence.of(f, "");
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

}
