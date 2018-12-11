package chav1961.purelib.etc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Assert;
import org.junit.Test;

import sun.net.www.protocol.root.Handler;

public class HandlersTest {
	@Test
	public void test() throws MalformedURLException, IOException {
		Assert.assertNotNull(new Handler(){@Override public URLConnection openConnection(final URL url) throws IOException {return super.openConnection(url);}}
			.openConnection(new URL("root:"+HandlersTest.class.getResource("HandlersTest.class")+"!../../../chav1961/purelib/etc/test.txt")));
	}
}
