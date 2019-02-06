package chav1961.purelib.sql.content;

import java.io.IOException;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

public class ResultSetFactoryTest {
	@Test
	public void buildTest() throws IOException, SQLException {
		try(final ResultSet		rs = ResultSetFactory.buildResultSet(null,URI.create(
									 ResultSetFactory.RESULTSET_PARSERS_SCHEMA+":csv:"+ResultSetFactoryTest.class.getResource("test.csv"))
									,ResultSet.TYPE_FORWARD_ONLY)) {
			
			Assert.assertEquals(3,rs.getMetaData().getColumnCount());
			Assert.assertEquals("col1",rs.getMetaData().getColumnName(1));
			Assert.assertEquals("col2",rs.getMetaData().getColumnName(2));
			Assert.assertEquals("col3",rs.getMetaData().getColumnName(3));
			
			int count = 0;
			while (rs.next()) {
				Assert.assertEquals(100,rs.getInt("col1"));
				count++;
			}
			Assert.assertEquals(1,count);
		}
	}
}
