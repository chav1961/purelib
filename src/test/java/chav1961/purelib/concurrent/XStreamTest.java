package chav1961.purelib.concurrent;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class XStreamTest {
	private boolean			readErrorsDetected;
	
	@Test
	public void test() throws IOException {
		final Thread		t;
		
		readErrorsDetected = false;
		try(final XStream	xs = new XStream();
			final Writer	wr = xs.getWriter()) {

			t = new Thread(new Runnable(){
									@Override
									public void run() {
										try(final Reader	rdr = xs.getReader()) {
											final char[]	buffer = new char[100];
											final int		len = rdr.read(buffer);
											
											Assert.assertEquals(new String(buffer,0,len),"test string");											
										} catch (IOException e) {
											readErrorsDetected  = true;
										}
									}
								}
							);
			t.setDaemon(true);
			t.start();
			wr.write("test string");
		}
		try{t.join();} catch (InterruptedException e) {}
		if (readErrorsDetected) {
			Assert.fail("Error in the reader!");
		}
	}
}
