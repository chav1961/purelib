package chav1961.purelib.streams.char2char;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import chav1961.purelib.testing.OrdinalTestCategory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Assert;

@Category(OrdinalTestCategory.class)
public class SubstitutableWriterTest {

	@Test
	public void staticTest() {
	}

	@Test
	public void basicTest() throws IOException {
		testValue("test string value test string", "test string ${value} test string");
	}
	
	private void testValue(final String awaited, final String test) throws IOException {
		try(final Writer	wr = new StringWriter()) {
			try(final SubstitutableWriter	swr = new SubstitutableWriter(wr, (s)->s)){
				swr.write(test);
				swr.flush();
			}
			Assert.assertEquals(awaited,wr.toString());
		}
	}
}
