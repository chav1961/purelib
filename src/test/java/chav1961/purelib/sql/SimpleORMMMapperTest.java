package chav1961.purelib.sql;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.sql.content.ResultSetFactory;
import chav1961.purelib.sql.interfaces.ORMMapper;
import chav1961.purelib.ui.interfaces.Format;

public class SimpleORMMMapperTest {
	private static final String			FIELD_LIST = "field1=VARCHAR(100)&field2=INTEGER&field3=INTEGER&field4=CHAR(1)&field5=INTEGER&field6=INTEGER&field7=REAL&field8=REAL&field9=BIT";

	@Test
	public void resultSetTest() throws URISyntaxException, IOException, SQLException, ContentException, LocalizationException {
		final URI						res = URI.create(ResultSetFactory.RESULTSET_PARSERS_SCHEMA+":csv:"+getClass().getResource("SimpleORMMMapperContent.csv").toURI()+'?'+FIELD_LIST);
		final ResultSet					rs = ResultSetFactory.buildResultSet(null,res,ResultSet.TYPE_FORWARD_ONLY);
		final ContentMetadataInterface	mdiRs = ContentModelFactory.forQueryContentDescription(rs.getMetaData());
		final PseudoRecord				pr = new PseudoRecord();
		final ContentMetadataInterface	mdiPr = ContentModelFactory.forAnnotatedClass(PseudoRecord.class);
		final ORMMapper					provider = new SimpleORMMapper(mdiPr.getRoot(),mdiRs.getRoot());
		
		Assert.assertTrue(rs.next());
		provider.fromRecord(pr, rs);
		
		Assert.assertEquals("test string",pr.field1);
		Assert.assertEquals(100,pr.field2);
		Assert.assertEquals(200,pr.field3);
		Assert.assertEquals('a',pr.field4);
		Assert.assertEquals(300,pr.field5);
		Assert.assertEquals(400,pr.field6);
		Assert.assertEquals(500.5f,pr.field7,0.001f);
		Assert.assertEquals(600.5,pr.field8,0.001);
		Assert.assertTrue(pr.field9);
		
		try{provider.fromRecord(null,rs);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{provider.fromRecord(new Object(),rs);
			Assert.fail("Mandatory exception was not detected (illegal type of 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{provider.fromRecord(pr,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		try{new SimpleORMMapper(null,mdiRs.getRoot());
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new SimpleORMMapper(mdiPr.getRoot(),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}
}
