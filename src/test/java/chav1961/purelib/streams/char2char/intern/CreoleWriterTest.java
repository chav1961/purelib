package chav1961.purelib.streams.char2char.intern;


import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.AbstractLoggerFacade;
import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.XMLUtils;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.enumerations.XSDCollection;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.streams.char2char.intern.CreoleOutputWriter;
import chav1961.purelib.streams.interfaces.intern.CreoleFontActions;
import chav1961.purelib.streams.interfaces.intern.CreoleFontState;
import chav1961.purelib.streams.interfaces.intern.CreoleSectionActions;
import chav1961.purelib.streams.interfaces.intern.CreoleSectionState;
import chav1961.purelib.streams.interfaces.intern.CreoleTerminals;
import chav1961.purelib.testing.OrdinalTestCategory;
import chav1961.purelib.testing.TestingUtils;

@Category(OrdinalTestCategory.class)
public class CreoleWriterTest {
	private final PrintStream	ps = TestingUtils.err();
	
	@Test
	public void txtTest() throws IOException {
		testLoop(MarkupOutputFormat.XML2TEXT,"txt");
	}

//	@Test
	public void htmlTest() throws IOException {
		testLoop(MarkupOutputFormat.XML2HTML,"html");
	}

//	@Test
	public void xmlTest() throws IOException {
		testLoop(MarkupOutputFormat.XML,"xml");
		testXSD(MarkupOutputFormat.XML,XSDCollection.CreoleXML);
	}

//	@Test
	public void fopTest() throws IOException {
		testLoop(MarkupOutputFormat.XML2PDF,"fop");
		testXSD(MarkupOutputFormat.XML2PDF,XSDCollection.CreoleXMLFO);
	}

	@Test
	public void lowLevelTest() throws IOException {
		try(final CharArrayWriter			wr = new CharArrayWriter()) { // It's an official WikiCreole test case
			try(final InputStream			is = new FileInputStream("./src/test/resources/chav1961/purelib/streams/char2char/cretest.cre"); // It's an official WikiCreole test case
				final Reader				rdr = new InputStreamReader(is,"UTF-8");
				final CreoleOutputWriter	cow = new CreoleOutputWriter(){
					@Override
					public void close() throws IOException {
					}

					@Override
					public void write(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
						ps.print(new String(content,from,to-from));
					}

					@Override
					public void writeEscaped(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
						ps.print(new String(content,from,to-from));
					}
					
					@Override
					public void insertImage(final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
						ps.print("<IMAGE: "+new String(data,startLink,endLink-startLink)+">");
					}

					@Override
					public void insertLink(final boolean localRef, final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
						ps.print("<LINK: "+new String(data,startLink,endLink-startLink)+">");
					}

					@Override
					public void processSection(final FSM<CreoleTerminals, CreoleSectionState, CreoleSectionActions, Long> fsm, final CreoleTerminals terminal, final CreoleSectionState fromState, final CreoleSectionState toState, final CreoleSectionActions[] action, final Long parameter) throws FlowException {
						ps.print("<Section: "+fromState+"->"+toState+">");
					}

					@Override
					public void processFont(final FSM<CreoleTerminals, CreoleFontState, CreoleFontActions, Long> fsm, final CreoleTerminals terminal, final CreoleFontState fromState, final CreoleFontState toState, final CreoleFontActions[] action, final Long parameter) throws FlowException {
						ps.print("<Font: "+fromState+"->"+toState+">");
					}
				}; 
				final CreoleWriter			cwr = new CreoleWriter(cow)) {
	
				Utils.copyStream(rdr,cwr);
			}
		}
	}
	
	private void testXSD(final MarkupOutputFormat type, final XSDCollection coll) throws IOException {
		final String	processed;
		
		try(final CharArrayWriter	wr = new CharArrayWriter()) { // It's an official WikiCreole test case
			try(final Reader		rdr = new FileReader("./src/test/resources/chav1961/purelib/streams/char2char/cretest.cre"); // It's an official WikiCreole test case
				final CreoleWriter	cwr = new CreoleWriter(wr,type)) {
	
				Utils.copyStream(rdr,cwr);
			}
			processed = wr.toString().replace("\r","");
		}
		ps.println(processed);
		
		try(final InputStream	xsd = XMLUtils.getPurelibXSD(coll);
			final InputStream	xml = new ByteArrayInputStream(processed.getBytes("UTF-8"));) {
			
			Assert.assertTrue(XMLUtils.validateXMLByXSD(xml,xsd,PureLibSettings.SYSTEM_ERR_LOGGER));
		}
	} 
	
	private void testLoop(final MarkupOutputFormat type, final String extension) throws IOException {
//		final String	processed;
		
		try(final CharArrayWriter	wr = new CharArrayWriter()) { // It's an official WikiCreole test case
			try(final InputStream	is = new FileInputStream("./src/test/resources/chav1961/purelib/streams/char2char/cretest.cre"); // It's an official WikiCreole test case
				final Reader		rdr = new InputStreamReader(is,"UTF-8");
				final CreoleWriter	cwr = new CreoleWriter(wr,type)) {
	
				Utils.copyStream(rdr,cwr);
			}
//			processed = 
			wr.toString().replace("\r","").replace("\n","").replace("\t","");
		}

		try(final InputStream	is = new FileInputStream("./src/test/resources/chav1961/purelib/streams/char2char/cretest."+extension);
			final Reader		rdr = new InputStreamReader(is,"UTF-8");
			final StringWriter	wr = new StringWriter()) {
			
			Utils.copyStream(rdr,wr);
			wr.flush();
	//		Assert.assertEquals(processed,wr.toString().replace("\r","").replace("\n","").replace("\t",""));
		}
	}
}
