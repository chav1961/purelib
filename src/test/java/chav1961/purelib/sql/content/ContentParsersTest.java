package chav1961.purelib.sql.content;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;

public class ContentParsersTest {
	@Test
	public void csvTest() throws UnsupportedEncodingException, IOException, SyntaxException, SQLException {
		try(final InputStream		is = new ByteArrayInputStream("col1,col2,col3\nvalue1,value2,value3".getBytes("UTF-8"))) {
			final CsvContentParser	parser = new CsvContentParser(is,"UTF-8", ',');
			
			Assert.assertEquals(parser.getMetaData().getColumnCount(),3);
			Assert.assertEquals(parser.getMetaData().getColumnName(1),"col1");
			Assert.assertEquals(parser.getMetaData().getColumnName(2),"col2");
			Assert.assertEquals(parser.getMetaData().getColumnName(3),"col3");
			Assert.assertEquals(parser.getAccessContent().getRowCount(),1);
			parser.getAccessContent().setCurrentRow(1);
			Object[] x = parser.getAccessContent().getRow(1);
			Assert.assertArrayEquals(parser.getAccessContent().getRow(1),new String[]{"value1","value2","value3"});
		}

		try(final InputStream		is = new ByteArrayInputStream("\"col1\",col2,\"col3\"\nvalue1,\"value2\",value3".getBytes("UTF-8"))) {
			final CsvContentParser	parser = new CsvContentParser(is,"UTF-8", ',');
			
			Assert.assertEquals(parser.getMetaData().getColumnCount(),3);
			Assert.assertEquals(parser.getMetaData().getColumnName(1),"col1");
			Assert.assertEquals(parser.getMetaData().getColumnName(2),"col2");
			Assert.assertEquals(parser.getMetaData().getColumnName(3),"col3");
			Assert.assertEquals(parser.getAccessContent().getRowCount(),1);
			parser.getAccessContent().setCurrentRow(1);
			Object[] x = parser.getAccessContent().getRow(1);
			Assert.assertArrayEquals(parser.getAccessContent().getRow(1),new String[]{"value1","value2","value3"});
		}
		
		try{new CsvContentParser(null,"UTF-8", ',');
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new CsvContentParser(new ByteArrayInputStream(new byte[0]),null, ',');
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{new CsvContentParser(new ByteArrayInputStream(new byte[0]),"", ',');
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (UnsupportedEncodingException exc) {
		}
		try{new CsvContentParser(new ByteArrayInputStream(new byte[0]),"shaize", ',');
			Assert.fail("Mandatory exception was not detected (unknown 2-nd argument)");
		} catch (UnsupportedEncodingException exc) {
		}
	}
}
