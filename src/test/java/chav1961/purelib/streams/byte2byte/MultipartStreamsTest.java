package chav1961.purelib.streams.byte2byte;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.streams.MultipartEntry;

@Tag("OrdinalTestCategory")
public class MultipartStreamsTest {

	@Test
	public void inputStreamTest() throws IOException {
		try(final InputStream			is = this.getClass().getResourceAsStream("multiparttemplate.txt");
			final MultipartInputStream	mis = new MultipartInputStream(is)) {
			MultipartEntry				me;
			
			int		count = 0;
			while ((me = mis.getNextEntry()) != null) {
				
				try(final StringWriter	wr = new StringWriter()) {
//					mis.closeEntry();
					Utils.copyStream(new InputStreamReader(mis), wr);
					
					switch (count) {
						case 0 :
							Assert.assertEquals("text", me.getName());
							Assert.assertEquals("text default\r\n", wr.toString());
							break;
						case 1 :
							Assert.assertEquals("file1", me.getName());
							Assert.assertEquals("Content of a.txt.\r\n\r\n", wr.toString());
							break;
						case 2 :
							Assert.assertEquals("file2", me.getName());
							Assert.assertEquals("<!DOCTYPE html><title>Content of a.html.</title>\r\n\r\n", wr.toString());
							break;
						default :
							Assert.fail("Extra part delected");
					}
					
				}
				count++;
			}
			Assert.assertEquals(3, count);
		}

		try(final InputStream			is = this.getClass().getResourceAsStream("multiparttemplate.txt");
			final MultipartInputStream	mis = new MultipartInputStream(is)) {
			MultipartEntry				me;
			
			int		count = 0;
			while ((me = mis.getNextEntry()) != null) {
				switch (count) {
					case 0 :
						Assert.assertEquals("text", me.getName());
						mis.closeEntry();
						break;
					case 1 :
						Assert.assertEquals("file1", me.getName());
						break;
					case 2 :
						Assert.assertEquals("file2", me.getName());
						mis.closeEntry();
						break;
					default :
						Assert.fail("Extra part delected");
				}
				count++;
			}
			Assert.assertEquals(3, count);
		}
		
		try{new MultipartInputStream(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
