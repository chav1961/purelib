package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.sql.content.ResultSetFactory;
import chav1961.purelib.streams.char2char.SimpleReportWriter.WriterContent;

public class SimpleReportWriterTest {
	private static final String			PARTS = ">>firstPage=zd,lastPage=kd,crossPage={p1,p2,p3}\n"
												+ ">>zd\n"
												+ "--- zd ---\n"
												+ ">>p1\n"
												+ "--- p1 ---\n"
												+ ">>p2\n"
												+ "--- p2 ---\n"
												+ ">>p3\n"
												+ "--- p3 ---\n"
												+ ">>ds\n"
												+ "--- ds &field1:10; ---\n"
												+ ">>tot:after field1\n"
												+ "--- tot &sum(field2):10>; ---\n"
												+ ">>kd\n"
												+ "--- kd ---\n"
												;
	
	@Test
	public void basicTest() throws IOException, SQLException, ContentException {
		try(final Reader				rdr = new StringReader(PARTS);
			final Writer				wr = new StringWriter();
			final SimpleReportWriter	swr = new SimpleReportWriter(wr,WriterContent.CSV_CONTENT,rdr,(c,f,t)->null);
			final ResultSet				rs = ResultSetFactory.buildResultSet(null,URI.create("rsps:csv:root://"+this.getClass().getCanonicalName()+"/chav1961/purelib/streams/char2char/reportTest.csv?field1=VARCHAR(10)&field2=INTEGER&field3=INTEGER&field4=INTEGER&field5=INTEGER"),ResultSet.TYPE_FORWARD_ONLY)) {

			swr.write(rs);
			swr.flush();
			
			Assert.assertEquals("--- zd ---\n" + 
			"--- ds a ---\n" + 
			"--- ds a ---\n" + 
			"--- tot          6 ---\n" + 
			"--- ds b ---\n" + 
			"--- ds b ---\n" + 
			"--- tot         22 ---\n" + 
			"--- kd ---\n",wr.toString().replace("\r", ""));
		}
	}

}
