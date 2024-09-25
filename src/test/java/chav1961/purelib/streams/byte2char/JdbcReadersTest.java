package chav1961.purelib.streams.byte2char;


import java.io.IOException;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.sql.content.ResultSetFactory;

@Tag("OrdinalTestCategory")
public class JdbcReadersTest {
	@Test
	@Disabled
	public void csvReaderTest() throws SQLException, IOException {
		final String	content1, content2;
		
		try(final ResultSet			rs = ResultSetFactory.buildResultSet(null,URI.create(ResultSetFactory.RESULTSET_PARSERS_SCHEMA+":csv:file:./src/test/resources/chav1961/purelib/streams/byte2char/csvtest.csv?F1=INTEGER&F2=INTEGER&F3=INTEGER"),ResultSet.TYPE_FORWARD_ONLY);
			final Jdbc2CsvReader	csvRdr = new Jdbc2CsvReader(rs,',',true)) {
			
			content1 = Utils.fromResource(csvRdr);
		}

		content2 = Utils.fromResource(JdbcReadersTest.class.getResource("csvTest.csv"));
		Assert.assertEquals(content2.replace("\r",""),content1.replace("\r",""));
		
		try{new Jdbc2CsvReader(null,',',true).close();
			Assert.fail("Mandtory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void jsonReaderTest() throws SQLException, IOException {
		final String	content1, content2;
		
		try(final ResultSet			rs = ResultSetFactory.buildResultSet(null,URI.create(ResultSetFactory.RESULTSET_PARSERS_SCHEMA+":json:file:./src/test/resources/chav1961/purelib/streams/byte2char/jsontest.json?F1=INTEGER&F2=INTEGER&F3=INTEGER"),ResultSet.TYPE_FORWARD_ONLY);
			final Jdbc2JsonReader	csvRdr = new Jdbc2JsonReader(rs)) {
			
			content1 = Utils.fromResource(csvRdr);
		}

		content2 = Utils.fromResource(JdbcReadersTest.class.getResource("jsontest.json"));
		Assert.assertEquals(content2.replace("\r",""),content1.replace("\r",""));
		
		try{new Jdbc2JsonReader(null).close();
			Assert.fail("Mandtory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
