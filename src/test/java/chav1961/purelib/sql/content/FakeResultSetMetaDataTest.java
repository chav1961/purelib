package chav1961.purelib.sql.content;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.sql.RsMetaDataElement;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class FakeResultSetMetaDataTest {
	@Test
	public void test() throws SQLException {
		final ResultSetMetaData	rsmd = new FakeResultSetMetaData(new RsMetaDataElement[]{
											new RsMetaDataElement("F1", "", "VARCHAR", Types.VARCHAR, 100, 0),
											new RsMetaDataElement("F2", "", "VARCHAR", Types.VARCHAR, 200, 0)
										},true);
		
		Assert.assertEquals(2,rsmd.getColumnCount());
		Assert.assertEquals("F1",rsmd.getColumnName(1));
		Assert.assertEquals("F2",rsmd.getColumnName(2));
		Assert.assertEquals("table",rsmd.getTableName(1));
		Assert.assertEquals("schema",rsmd.getSchemaName(1));
		Assert.assertNull(rsmd.getCatalogName(1));
	}
}
