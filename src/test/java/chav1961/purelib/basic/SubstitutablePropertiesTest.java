package chav1961.purelib.basic;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import chav1961.purelib.basic.SubstitutableProperties.Format;
import chav1961.purelib.basic.SubstitutableProperties.PropertyGroupChangeEvent;
import chav1961.purelib.basic.SubstitutableProperties.PropertyGroupChangeListener;

public class SubstitutablePropertiesTest {
	@Test
	public void basicTest() throws IOException {
		final SubstitutableProperties	props1 = new SubstitutableProperties(Utils.mkProps("key1","value1","key2","${key3}","key3","value3"));
		final SubstitutableProperties	props2 = new SubstitutableProperties();
		final SubstitutableProperties	props3 = new SubstitutableProperties();
		final SubstitutableProperties	props4 = new SubstitutableProperties();

		props2.putAll(Utils.mkProps("key1","value1","key2","${key3}","key3","value3"));
		props3.putAll(Utils.mkProps("key1","value1","key2","${key3}","key3","value3"));
		
		Assert.assertEquals(props3, props2);
		Assert.assertEquals(props3.hashCode(), props2.hashCode());
		Assert.assertEquals(props3.toString(), props2.toString());
		Assert.assertFalse(props3.equals(props4));
		
		Assert.assertTrue(props1.containsKey("key1")); 
		Assert.assertFalse(props1.containsKey("unknown"));
		Assert.assertEquals("value1",props1.getProperty("key1"));
		Assert.assertEquals("value3",props1.getProperty("key3"));
		Assert.assertEquals("value3",props1.getProperty("key3",String.class));
		Assert.assertNull(props1.getProperty("unknown"));
		Assert.assertNull(props1.getProperty("unknown",String.class));
		Assert.assertTrue(props1.theSame(props1));
		Assert.assertFalse(props1.theSame(props3));
		
		try{new SubstitutableProperties((Properties)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new SubstitutableProperties((URI)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		try{props1.getProperty(null,String.class);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{props1.getProperty("key1",(Class<?>)null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		try{props1.theSame(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void kayManagementTest() throws IOException {
		final SubstitutableProperties	props = new SubstitutableProperties(Utils.mkProps("key1","value1","key2","value2"));
		final Pattern	p = Pattern.compile(".*2");

		
		int	count = 0;
		for (String item : props.availableKeys()) {
			count++;
		}
		Assert.assertEquals(2, count);
		
		count = 0;
		for (String item : props.availableKeys(p)) {
			count++; 
		}
		Assert.assertEquals(1, count);
		
		try{props.availableKeys(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertTrue(props.containsAllKeys("key1","key2"));
		Assert.assertTrue(props.containsAllKeys(Pattern.compile("key1|key2")));		
		Assert.assertFalse(props.containsAllKeys("key1","key3"));
		Assert.assertFalse(props.containsAllKeys(Pattern.compile("key1|key3")));
		
		Assert.assertTrue(props.containsAnyKeys("key1","key3"));
		Assert.assertTrue(props.containsAnyKeys(Pattern.compile("key1|key3")));
		Assert.assertFalse(props.containsAnyKeys("key3","key4"));
		Assert.assertFalse(props.containsAnyKeys(Pattern.compile("key3|key4")));
		
		try{props.containsAllKeys((String[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{props.containsAllKeys((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{props.containsAllKeys((Pattern)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{props.containsAnyKeys((String[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{props.containsAnyKeys((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{props.containsAnyKeys((Pattern)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals(2, props.size());
		Assert.assertEquals(0, props.remove(p));
		Assert.assertEquals(2, props.size());
		Assert.assertEquals(1, props.removeAll(p));
		Assert.assertEquals(1, props.size());

		try{props.remove((Pattern)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{props.removeAll((Pattern)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}	
	
	@Test
	public void loadUnloadTest() throws IOException {
		final File	tempDir = new File(System.getProperty("java.io.tmpdir"));
		final File	tempFile = new File(tempDir, ".properties");
		final SubstitutableProperties	props = new SubstitutableProperties();
		final SubstitutableProperties	newProps = new SubstitutableProperties();
		
		props.putAll(Utils.mkProps("key1","value1","key2","value2"));
		
		if (tempFile.exists()) {
			if (tempFile.isDirectory()) {
				Utils.deleteDir(tempFile);
			}
			else {
				tempFile.delete();
			}
		}

		Assert.assertFalse(props.tryLoad(tempFile));
		Assert.assertFalse(props.tryLoad(tempDir));
		
		try{props.tryLoad(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try {
			props.store(tempFile);
			Assert.assertTrue(newProps.tryLoad(tempFile));
			Assert.assertEquals(props, newProps);
		} finally {
			tempFile.delete();
		}

		try{props.store(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{props.store(tempDir);
			Assert.fail("Mandatory exception was not detected (1-st argument points to directory)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void formatsTest() throws IOException {
		final SubstitutableProperties	props = new SubstitutableProperties();
		final SubstitutableProperties	newProps = new SubstitutableProperties();

		newProps.clear();
		newProps.putAll(Utils.mkProps("key1","value1","key2","value2"));
		props.clear();
		props.load(getClass().getResourceAsStream("properties.format"), Format.Ordinal);
		Assert.assertEquals(newProps, props);

		newProps.clear();
		newProps.putAll(Utils.mkProps("[section1].key1","value1","[section1].key2","value2","[section2].key1","value1","[section2].key2","value2"));
		props.clear();
		props.load(getClass().getResourceAsStream("windowsproperties.format"), Format.WindowsStyled);
		Assert.assertEquals(newProps, props);
		
		newProps.clear();
		newProps.putAll(Utils.mkProps("key1","value1","key2","value2"));
		props.clear();
		props.load(getClass().getResourceAsStream("xmlproperties.format"), Format.XML);
		Assert.assertEquals(newProps, props);
		
		try{props.load((InputStream)null, Format.XML);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) { 
		}
		try{props.load((Reader)null, Format.XML);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{props.load(getClass().getResourceAsStream("xmlproperties.format"), null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{props.load(new StringReader(""), Format.XML);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void listenersTest() throws IOException {
		final boolean						marks[] = new boolean[2];
		final SubstitutableProperties		props = new SubstitutableProperties();
		final PropertyGroupChangeListener	x = new PropertyGroupChangeListener() {
												@Override public void propertyChange(PropertyChangeEvent evt) {marks[0] = true;}
												@Override public void propertiesChange(PropertyGroupChangeEvent event) {marks[1] = true;}
											};
		
		props.addPropertyChangeListener(x);
		try{props.addPropertyChangeListener(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		marks[0] = false;
		props.put("key1", "value1");
		Assert.assertTrue(marks[0]);

		marks[0] = false;
		props.put("key1", "value1");
		Assert.assertFalse(marks[0]);	// value not changed

		marks[0] = false;
		props.putIfAbsent("key2", "value2");
		Assert.assertTrue(marks[0]);

		marks[0] = false;
		props.putIfAbsent("key2", "value2");
		Assert.assertFalse(marks[0]);	// value not changed

		marks[0] = false;
		props.setProperty("key3", "value3");
		Assert.assertTrue(marks[0]);

		marks[0] = false;
		props.setProperty("key3", "value3");
		Assert.assertFalse(marks[0]);	// value not changed
		
		marks[0] = false;
		props.remove("key2");
		Assert.assertTrue(marks[0]);

		marks[1] = false;
		props.putAll(Utils.mkProps("key1","value1","key2","value2"));
		Assert.assertTrue(marks[1]);
		
		props.removePropertyChangeListener(x);
		try{props.removePropertyChangeListener(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
