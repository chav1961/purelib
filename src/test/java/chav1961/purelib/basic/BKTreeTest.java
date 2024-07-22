package chav1961.purelib.basic;

import java.util.Arrays;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
public class BKTreeTest {
	private final String[]			content = {"test1","test234","test12","test23","test123","test2"};
	
	@Test
	public void addAndWalkTest() {
		final BKTree<char[],String>	tree = new BKTree<>(char[].class, (v1,v2)->CharUtils.calcLevenstain(v1, v2).distance);
		final Set<String>			walked = new HashSet<>();
		final Set<String>			awaited = new HashSet<>(Arrays.asList(content));
		
		Assert.assertEquals(char[].class, tree.getContentType());
		
		for (String item : content) {
			tree.add(item.toCharArray(), item);
		}
		Assert.assertTrue(tree.contains("test1".toCharArray()));
		Assert.assertFalse(tree.contains("test3".toCharArray()));
		
		tree.walk((v1,v2)->walked.add(v2));
		Assert.assertEquals(awaited, walked);
		
		try{new BKTree<char[],String>(null, (v1,v2)->CharUtils.calcLevenstain(v1, v2).distance);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new BKTree<char[],String>(char[].class, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		try{tree.add(null, "test");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{tree.add("test1".toCharArray(), "test1");
			Assert.fail("Mandatory exception was not detected (duplicate 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{tree.contains(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{tree.walk(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void findTest() {
		final BKTree<char[],String>	tree = new BKTree<>(char[].class, (v1,v2)->CharUtils.calcLevenstain(v1, v2).distance);
		final Set<String>			walked = new HashSet<>();

		for (String item : content) {
			tree.add(item.toCharArray(), item);
		}
		
		tree.walk("test2".toCharArray(),0,(v1,v2)->walked.add(v2));
		Assert.assertEquals(new HashSet<>(Arrays.asList("test2")), walked);
		
		walked.clear();
		tree.walk("test2".toCharArray(),1,(v1,v2)->walked.add(v2));
		Assert.assertEquals(new HashSet<>(Arrays.asList("test2","test12","test23","test1")), walked);
		
		try{tree.walk(null,1,(v1,v2)->walked.add(v2));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{tree.walk("test2".toCharArray(),-1,(v1,v2)->walked.add(v2));
			Assert.fail("Mandatory exception was not detected (negative 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{tree.walk("test2".toCharArray(),1,null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}	
}
