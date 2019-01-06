package chav1961.purelib.basic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {
	@Test
	public void copyStreamTest() throws IOException {
		try(final ByteArrayInputStream	bais = new ByteArrayInputStream("test string".getBytes());
			final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
		
			Assert.assertEquals(Utils.copyStream(bais,baos),"test string".length());
			Assert.assertEquals(baos.toString(),"test string");
			
			try{Utils.copyStream(null,baos);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {				
			}
			try{Utils.copyStream(bais,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {				
			}
		}

		try(final Reader		rdr = new StringReader("test string");
			final StringWriter	wr = new StringWriter()) {
		
			Assert.assertEquals(Utils.copyStream(rdr,wr),"test string".length());
			Assert.assertEquals(wr.toString(),"test string");
			
			try{Utils.copyStream(null,wr);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {				
			}
			try{Utils.copyStream(rdr,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {				
			}
		}		
	}

	@Test
	public void mkMapSetAndPropertiesTest() throws IOException {
		final Map<String,Object>	etalon = new HashMap<String,Object>(){private static final long serialVersionUID = 1L; {put("key1","value1"); put("key2","value2");}};
		
		Assert.assertEquals(Utils.mkMap("key1","value1","key2","value2"),etalon);
		
		try{Utils.mkMap((Object[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {				
		}
		try{Utils.mkMap("key1");
			Assert.fail("Mandatory exception was not detected (odd amount of parameters)");
		} catch (IllegalArgumentException exc) {				
		}
		try{Utils.mkMap(null,"value1");
			Assert.fail("Mandatory exception was not detected (key is null)");
		} catch (IllegalArgumentException exc) {				
		}
		
		final Set<String>		etalonSet = new HashSet<String>(){private static final long serialVersionUID = 1L; {add("key1"); add("key2");}};

		Assert.assertEquals(Utils.mkSet(String.class,"key1","key2"),etalonSet);

		try{Utils.mkSet(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {				
		}
		try{Utils.mkSet(String.class,(String[])null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {				
		}

		final Properties		etalonProps = new Properties(){private static final long serialVersionUID = 1L; {setProperty("key1","value1"); setProperty("key2","value2");}};

		Assert.assertEquals(Utils.mkProps("key1","value1","key2","value2"),etalonProps);

		try{Utils.mkProps((String[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {				
		}
		try{Utils.mkProps("key1");
			Assert.fail("Mandatory exception was not detected (odd amount of parameters)");
		} catch (IllegalArgumentException exc) {				
		}
		try{Utils.mkProps(null,"value1");
			Assert.fail("Mandatory exception was not detected (key is null)");
		} catch (IllegalArgumentException exc) {				
		}
	}

	@Test
	public void fromResourceTest() throws IOException {
		Assert.assertEquals(Utils.fromResource(new StringReader("test string")),"test string");
		Assert.assertEquals(Utils.fromResource(this.getClass().getResource("resourcefile.txt")),"test string");
		
		try{Utils.fromResource((Reader)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{Utils.fromResource((URL)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{Utils.fromResource(this.getClass().getResource("resourcefile.txt"),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
	}

	@Test
	public void wrappingTest() throws IOException {
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new boolean[]{true,false})),new boolean[]{true,false});
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new byte[]{1,2,3})),new byte[]{1,2,3});
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new short[]{1,2,3})),new short[]{1,2,3});
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new int[]{1,2,3})),new int[]{1,2,3});
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new long[]{1,2,3})),new long[]{1,2,3});
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new float[]{1,2,3})),new float[]{1,2,3},0.0001f);
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new double[]{1,2,3})),new double[]{1,2,3},0.0001);
		Assert.assertArrayEquals(Utils.unwrapArray(Utils.wrapArray(new char[]{'1','2','3'})),new char[]{'1','2','3'});
	}

	@Test
	public void canServeTest() throws IOException {
		Assert.assertTrue(Utils.canServeURI(URI.create("scheme:subscheme:/path"),URI.create("scheme:subscheme:/")));
		Assert.assertFalse(Utils.canServeURI(URI.create("scheme:unknown:/path"),URI.create("scheme:subscheme:/")));

		try{Utils.canServeURI(null,URI.create("scheme:subscheme"));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{Utils.canServeURI(URI.create("scheme:subscheme:/path"),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {			
		}
		try{Utils.canServeURI(URI.create("scheme:unknown:/path"),URI.create("scheme:/path"));
			Assert.fail("Mandatory exception was not detected (missing subscheme in the 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
		try{Utils.canServeURI(URI.create("scheme:unknown:/path"),URI.create("/path"));
			Assert.fail("Mandatory exception was not detected (missing scheme in the 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
	}

	@Test
	public void loadFromURI() throws IOException, NullPointerException, URISyntaxException {
		Assert.assertArrayEquals(Utils.loadBytesFromURI(this.getClass().getResource("uricontent.txt").toURI()),"test string".getBytes());
		Assert.assertArrayEquals(Utils.loadCharsFromURI(this.getClass().getResource("uricontent.txt").toURI()),"test string".toCharArray());
		Assert.assertArrayEquals(Utils.loadCharsFromURI(this.getClass().getResource("uricontent.txt").toURI(),"UTF-8"),"test string".toCharArray());
		
		try{Utils.loadBytesFromURI(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{Utils.loadCharsFromURI(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{Utils.loadCharsFromURI(this.getClass().getResource("uricontent.txt").toURI(),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{Utils.loadCharsFromURI(this.getClass().getResource("uricontent.txt").toURI(),"");
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void nestedURITest() throws IOException, NullPointerException, URISyntaxException {
		Assert.assertFalse(Utils.containsNestedURI(URI.create("scheme:/")));
		Assert.assertFalse(Utils.containsNestedURI(URI.create("scheme:/path")));
		Assert.assertFalse(Utils.containsNestedURI(URI.create("scheme:subscheme:/path")));
		Assert.assertTrue(Utils.containsNestedURI(URI.create("scheme:subscheme:/path1/!/path2")));
		Assert.assertTrue(Utils.containsNestedURI(URI.create("scheme:subscheme:/path1/!/path2#fragment")));

		try{Utils.containsNestedURI(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertNull(Utils.extractNestedURI(URI.create("scheme:/")));
		Assert.assertNull(Utils.extractNestedURI(URI.create("scheme:/path")));
		Assert.assertNull(Utils.extractNestedURI(URI.create("scheme:subscheme:/path")));
		Assert.assertEquals(Utils.extractNestedURI(URI.create("scheme:subscheme:/path1/!/path2")),URI.create("subscheme:/path1/"));
		Assert.assertEquals(Utils.extractNestedURI(URI.create("scheme:subscheme:/path1/!/path2#fragment")),URI.create("subscheme:/path1/"));

		try{Utils.extractNestedURI(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
				
		Assert.assertNull(Utils.extractPathInNestedURI(URI.create("scheme:/")));
		Assert.assertNull(Utils.extractPathInNestedURI(URI.create("scheme:/path")));
		Assert.assertNull(Utils.extractPathInNestedURI(URI.create("scheme:subscheme:/path")));
		Assert.assertEquals(Utils.extractPathInNestedURI(URI.create("scheme:subscheme:/path1/!/path2")),URI.create("/path2"));
		Assert.assertEquals(Utils.extractPathInNestedURI(URI.create("scheme:subscheme:/path1/!/path2#fragment")),URI.create("/path2"));

		try{Utils.extractPathInNestedURI(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Test
	public void fileMask2RegexTest() throws IOException, NullPointerException, URISyntaxException {
		Assert.assertEquals("q1\\_",Utils.fileMask2Regex("q1_"));
		Assert.assertEquals("q1\\_\\ q2",Utils.fileMask2Regex("q1_ q2"));
		Assert.assertEquals(".*",Utils.fileMask2Regex("*"));
		Assert.assertEquals("..*",Utils.fileMask2Regex("?*"));
		Assert.assertEquals(".*\\.txt",Utils.fileMask2Regex("*.txt"));
		
		try{Utils.fileMask2Regex(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{Utils.fileMask2Regex("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
