package chav1961.purelib.sql.content;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.sql.RsMetaDataElement;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.sql.interfaces.ResultSetContentParser;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class FactoryAndParsersTest {
	@Test
	public void factoryTest() throws SQLException, IOException {
		try{ResultSetFactory.buildResultSet(null,null,ResultSet.TYPE_FORWARD_ONLY);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{ResultSetFactory.buildResultSet(null,URI.create("unknown:subscheme"),ResultSet.TYPE_FORWARD_ONLY);
			Assert.fail("Mandatory exception was not detected (illegal 2-nd argument schema)");
		} catch (IllegalArgumentException exc) {
		}
		try{ResultSetFactory.buildResultSet(null,URI.create("rsps:unknown:/?key=value"),ResultSet.TYPE_FORWARD_ONLY);
			Assert.fail("Mandatory exception was not detected (2-nd argument subschema has no appropriative parser)");
		} catch (IOException exc) {
		}
		try{ResultSetFactory.buildResultSet(null,URI.create("rsps:csv:file:./src/test/resources/chav1961/purelib/sql/content/test.csv"),ResultSet.TYPE_FORWARD_ONLY);
			Assert.fail("Mandatory exception was not detected (missing query string in 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{ResultSetFactory.buildResultSet(null,URI.create("rsps:csv:file:./src/test/resources/chav1961/purelib/sql/content/test.csv?encoding=utf8"),ResultSet.TYPE_FORWARD_ONLY);
			Assert.fail("Mandatory exception was not detected (missing fields in the query string in 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{ResultSetFactory.buildResultSet(null,URI.create("rsps:csv:file:./src/test/resources/chav1961/purelib/sql/content/test.csv?toSum=INTEGER&forLine=VARCHAR(10)&forNull=VARCHAR(10)"),666);
			Assert.fail("Mandatory exception was not detected (illegal 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
	}	 
	 
	@Test
	public void parsersTest() throws SQLException, IOException, SyntaxException {
		try(final ResultSet	rs = ResultSetFactory.buildResultSet(null,URI.create("rsps:csv:file:./src/test/resources/chav1961/purelib/sql/content/test.csv?toSum=INTEGER&forLine=VARCHAR(10)&forNull=VARCHAR(10)"),ResultSet.TYPE_FORWARD_ONLY)) {
			test(rs);
		}
		try(final ResultSet	rs = ResultSetFactory.buildResultSet(null,URI.create("rsps:csv:file:./src/test/resources/chav1961/purelib/sql/content/test.csv?toSum=INTEGER&forLine=VARCHAR(10)&forNull=VARCHAR(10)"),ResultSet.TYPE_SCROLL_SENSITIVE)) {
			test(rs);
		}
		test(new CsvContentParser(),"rsps:csv:/");
		
		try(final ResultSet	rs = ResultSetFactory.buildResultSet(null,URI.create("rsps:json:file:./src/test/resources/chav1961/purelib/sql/content/test.json?toSum=INTEGER&forLine=VARCHAR(10)&forNull=VARCHAR(10)"),ResultSet.TYPE_FORWARD_ONLY)) {
			test(rs);
		}
		try(final ResultSet	rs = ResultSetFactory.buildResultSet(null,URI.create("rsps:json:file:./src/test/resources/chav1961/purelib/sql/content/test.json?toSum=INTEGER&forLine=VARCHAR(10)&forNull=VARCHAR(10)"),ResultSet.TYPE_SCROLL_SENSITIVE)) {
			test(rs);
		}
		test(new JsonContentParser(),"rsps:json:/");
		
		try(final ResultSet	rs = ResultSetFactory.buildResultSet(null,URI.create("rsps:xml:file:./src/test/resources/chav1961/purelib/sql/content/test.xml?rowtag=row&toSum=INTEGER&forLine=VARCHAR(10)&forNull=VARCHAR(10)"),ResultSet.TYPE_FORWARD_ONLY)) {
			test(rs);
		}
		try(final ResultSet	rs = ResultSetFactory.buildResultSet(null,URI.create("rsps:xml:file:./src/test/resources/chav1961/purelib/sql/content/test.xml?rowtag=row&toSum=INTEGER&forLine=VARCHAR(10)&forNull=VARCHAR(10)"),ResultSet.TYPE_SCROLL_SENSITIVE)) {
			test(rs);
		}
		test(new XMLContentParser(),"rsps:xml:/");
	}
 
	private void test(final ResultSet rs) throws SQLException {
		int		count = 0, total = 0;
		
		while (rs.next()) {
			count++;
			total += rs.getInt("toSum");
			Assert.assertEquals("line",rs.getString("forLine"));
			Assert.assertNull(rs.getString("forNull"));
			Assert.assertTrue(rs.wasNull());
		}
		Assert.assertEquals(count, 3);
		Assert.assertEquals(total, 60);
	}
	
	private void test(final ResultSetContentParser parser, final String uri) throws SQLException, IOException, SyntaxException {
		Assert.assertTrue(parser.canServe(URI.create(uri)));
		
		try{parser.newInstance(null,ResultSet.TYPE_FORWARD_ONLY,new RsMetaDataElement[0], new SubstitutableProperties());
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{parser.newInstance(new URL("file://"),-1,new RsMetaDataElement[0], new SubstitutableProperties());
			Assert.fail("Mandatory exception was not detected (illegal 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{parser.newInstance(new URL("file://"),ResultSet.TYPE_FORWARD_ONLY,null, new SubstitutableProperties());
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{parser.newInstance(new URL("file://"),ResultSet.TYPE_FORWARD_ONLY,new RsMetaDataElement[0], new SubstitutableProperties());
			Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{parser.newInstance(new URL("file://"),ResultSet.TYPE_FORWARD_ONLY,SQLContentUtils.buildMetadataFromQueryString("f1=DATE",new Hashtable<>()),null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{parser.newInstance(new URL("file://"),ResultSet.TYPE_FORWARD_ONLY,SQLContentUtils.buildMetadataFromQueryString("f1=DATE",new Hashtable<>()),new SubstitutableProperties());
			Assert.fail("Mandatory exception was not detected (missing mandatory prop in 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
