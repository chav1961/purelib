package chav1961.purelib.streams.char2char;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.AbstractLoggerFacade;
import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.enumerations.XSDCollection;
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleTerminals;

public class CreoleWriterTest {
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

	@Test
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
					void internalWrite(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
						System.err.print(new String(content,from,to-from));
					}

					@Override
					void internalWriteEscaped(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
						System.err.print(new String(content,from,to-from));
					}
					
					@Override
					void insertImage(final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
						System.err.print("<IMAGE: "+new String(data,startLink,endLink-startLink)+">");
					}

					@Override
					void insertLink(final boolean localRef, final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
						System.err.print("<LINK: "+new String(data,startLink,endLink-startLink)+">");
					}

					@Override
					protected void processSection(final FSM<CreoleTerminals, SectionState, SectionActions, Long> fsm, final CreoleTerminals terminal, final SectionState fromState, final SectionState toState, final SectionActions[] action, final Long parameter) throws FlowException {
						System.err.print("<Section: "+fromState+"->"+toState+">");
					}

					@Override
					protected void processFont(final FSM<CreoleTerminals, FontState, FontActions, Long> fsm, final CreoleTerminals terminal, final FontState fromState, final FontState toState, final FontActions[] action, final Long parameter) throws FlowException {
						System.err.print("<Font: "+fromState+"->"+toState+">");
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
		
		
		try(final InputStream	xsd = Utils.getPurelibXSD(coll);
			final InputStream	xml = new ByteArrayInputStream(processed.getBytes("UTF-8"));
			final LoggerFacade	logger = new AbstractLoggerFacade() {
										@Override protected void toLogger(Severity level, String text, Throwable throwable) {System.err.println(text+": "+throwable);}
										@Override protected AbstractLoggerFacade getAbstractLoggerFacade(String mark, Class<?> root) {return this;}
									}) {
			
			Assert.assertTrue(Utils.validateXMLByXSD(xml,xsd,logger));
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
