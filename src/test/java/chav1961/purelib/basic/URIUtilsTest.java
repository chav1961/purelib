package chav1961.purelib.basic;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

public class URIUtilsTest {

	@Test
	public void canServeTest() throws IOException {
		Assert.assertTrue(URIUtils.canServeURI(URI.create("scheme:subscheme:/path"),URI.create("scheme:subscheme:/")));
		Assert.assertFalse(URIUtils.canServeURI(URI.create("scheme:unknown:/path"),URI.create("scheme:subscheme:/")));

		try{URIUtils.canServeURI(null,URI.create("scheme:subscheme"));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{URIUtils.canServeURI(URI.create("scheme:subscheme:/path"),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {			
		}
		try{URIUtils.canServeURI(URI.create("scheme:unknown:/path"),URI.create("scheme:/path"));
			Assert.fail("Mandatory exception was not detected (missing subscheme in the 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
		try{URIUtils.canServeURI(URI.create("scheme:unknown:/path"),URI.create("/path"));
			Assert.fail("Mandatory exception was not detected (missing scheme in the 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
	}

	@Test
	public void loadFromURI() throws IOException, NullPointerException, URISyntaxException {
		Assert.assertArrayEquals(URIUtils.loadBytesFromURI(this.getClass().getResource("uricontent.txt").toURI()),"test string".getBytes());
		Assert.assertArrayEquals(URIUtils.loadCharsFromURI(this.getClass().getResource("uricontent.txt").toURI()),"test string".toCharArray());
		Assert.assertArrayEquals(URIUtils.loadCharsFromURI(this.getClass().getResource("uricontent.txt").toURI(),"UTF-8"),"test string".toCharArray());
		
		try{URIUtils.loadBytesFromURI(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{URIUtils.loadCharsFromURI(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{URIUtils.loadCharsFromURI(this.getClass().getResource("uricontent.txt").toURI(),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{URIUtils.loadCharsFromURI(this.getClass().getResource("uricontent.txt").toURI(),"");
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void uriManagingTest() throws IOException, NullPointerException, URISyntaxException {
		Assert.assertFalse(URIUtils.containsNestedURI(URI.create("scheme:/")));
		Assert.assertFalse(URIUtils.containsNestedURI(URI.create("scheme:/path")));
		Assert.assertFalse(URIUtils.containsNestedURI(URI.create("scheme:subscheme:/path")));
		Assert.assertTrue(URIUtils.containsNestedURI(URI.create("scheme:subscheme:/path1/!/path2")));
		Assert.assertTrue(URIUtils.containsNestedURI(URI.create("scheme:subscheme:/path1/!/path2#fragment")));

		try{URIUtils.containsNestedURI(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertNull(URIUtils.extractNestedURI(URI.create("scheme:/")));
		Assert.assertNull(URIUtils.extractNestedURI(URI.create("scheme:/path")));
		Assert.assertNull(URIUtils.extractNestedURI(URI.create("scheme:subscheme:/path")));
		Assert.assertEquals(URIUtils.extractNestedURI(URI.create("scheme:subscheme:/path1/!/path2")),URI.create("subscheme:/path1/"));
		Assert.assertEquals(URIUtils.extractNestedURI(URI.create("scheme:subscheme:/path1/!/path2#fragment")),URI.create("subscheme:/path1/"));

		try{URIUtils.extractNestedURI(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
				
		Assert.assertNull(URIUtils.extractPathInNestedURI(URI.create("scheme:/")));
		Assert.assertNull(URIUtils.extractPathInNestedURI(URI.create("scheme:/path")));
		Assert.assertNull(URIUtils.extractPathInNestedURI(URI.create("scheme:subscheme:/path")));
		Assert.assertEquals(URIUtils.extractPathInNestedURI(URI.create("scheme:subscheme:/path1/!/path2")),URI.create("/path2"));
		Assert.assertEquals(URIUtils.extractPathInNestedURI(URI.create("scheme:subscheme:/path1/!/path2#fragment")),URI.create("/path2"));

		try{URIUtils.extractPathInNestedURI(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals(URI.create("scheme:/path1/path2?query=value#fragment"),URIUtils.appendRelativePath2URI(URI.create("scheme:/path1?query=value#fragment"),"path2"));
		Assert.assertEquals(URI.create("scheme:/path1/path2?query=value#fragment"),URIUtils.appendRelativePath2URI(URI.create("scheme:/path1?query=value#fragment"),"/path2"));
		Assert.assertEquals(URI.create("scheme:/path2?query=value#fragment"),URIUtils.appendRelativePath2URI(URI.create("scheme:/path1?query=value#fragment"),"../path2"));

		try{URIUtils.appendRelativePath2URI(null,"../path2");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{URIUtils.appendRelativePath2URI(URI.create("scheme:/path1?query=value#fragment"),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{URIUtils.appendRelativePath2URI(URI.create("scheme:/path1?query=value#fragment"),"");
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertEquals(URI.create("scheme:/path1#fragment"),URIUtils.removeQueryFromURI(URI.create("scheme:/path1?query=value#fragment")));
		Assert.assertEquals(URI.create("scheme:/path1"),URIUtils.removeQueryFromURI(URI.create("scheme:/path1?query=value")));
		Assert.assertEquals(URI.create("scheme:/path1"),URIUtils.removeQueryFromURI(URI.create("scheme:/path1")));

		try{URIUtils.removeQueryFromURI(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}

