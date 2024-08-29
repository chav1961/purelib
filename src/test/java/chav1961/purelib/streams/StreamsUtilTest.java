package chav1961.purelib.streams;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.streams.char2char.CreoleWriter;

@Tag("OrdinalTestCategory")
public class StreamsUtilTest {
	@Test
	public void getStreamClass4OutputTest() throws IOException {
		Assert.assertEquals(WriterWrapper.class,StreamsUtil.getStreamClassForOutput(new StringWriter(),MimeType.MIME_PLAIN_TEXT,MimeType.MIME_PLAIN_TEXT).getClass());
		Assert.assertEquals(CreoleWriter.class,StreamsUtil.getStreamClassForOutput(new StringWriter(),MimeType.MIME_CREOLE_TEXT,MimeType.MIME_PLAIN_TEXT).getClass());
		Assert.assertEquals(WriterWrapper.class,StreamsUtil.getStreamClassForOutput(new StringWriter(),MimeType.MIME_PLAIN_TEXT,MimeType.MIME_FAVICON).getClass());
		
		try{StreamsUtil.getStreamClassForOutput(null,MimeType.MIME_PLAIN_TEXT,MimeType.MIME_PLAIN_TEXT);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{StreamsUtil.getStreamClassForOutput(new StringWriter(),null,MimeType.MIME_PLAIN_TEXT);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{StreamsUtil.getStreamClassForOutput(new StringWriter(),MimeType.MIME_PLAIN_TEXT,null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void copyJsonStaxTest() throws NullPointerException, IOException, SyntaxException {
		final String		content = Utils.fromResource(this.getClass().getResource("staxcopy.json"));
		
		try(final Writer				wr = new StringWriter()) {
			try(final Reader			rdr = new StringReader(content);
				final JsonStaxParser	parser = new JsonStaxParser(rdr);
				final JsonStaxPrinter	printer = new JsonStaxPrinter(wr)) {
				
				StreamsUtil.copyJsonStax(parser,printer);
				
				try{StreamsUtil.copyJsonStax(null,printer);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try{StreamsUtil.copyJsonStax(parser,null);
					Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
				} catch (NullPointerException exc) {
				}
			}
			Assert.assertEquals(content,wr.toString());
		}
	}
	
	@Test
	public void loadCreoleContent() throws NullPointerException, IOException, URISyntaxException {
		Assert.assertEquals("== Test==\npart\n",StreamsUtil.loadCreoleContent(this.getClass().getResource("creolecontent.cre").toURI(),MarkupOutputFormat.XML2TEXT).replace("\r", ""));
		
		try{StreamsUtil.loadCreoleContent(null,MarkupOutputFormat.XML2TEXT);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{StreamsUtil.loadCreoleContent(this.getClass().getResource("creolecontent.cre").toURI(),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}
}
