package chav1961.purelib.sql;


import java.sql.Types;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
public class RsMetaDataElementTest {
	@Test
	public void basicTest() {
		final RsMetaDataElement	mde1 = new RsMetaDataElement("f1","comment","VARCHAR",Types.VARCHAR,100,0),
								mde2 = new RsMetaDataElement("f1","another comment","VARCHAR",Types.VARCHAR,100,0),
								mde3 = new RsMetaDataElement("f3","comment","NUMERIC",Types.NUMERIC,10,2);
		
		Assert.assertEquals("f1",mde1.getName());
		Assert.assertEquals("comment",mde1.getDescription());
		Assert.assertEquals("VARCHAR",mde1.getTypeName());
		Assert.assertEquals(Types.VARCHAR,mde1.getType());
		Assert.assertEquals(100,mde1.getLength());
		Assert.assertEquals(0,mde1.getFrac());
		
		Assert.assertEquals(mde1,mde2);

		Assert.assertEquals("f3",mde3.getName());
		Assert.assertEquals("comment",mde3.getDescription());
		Assert.assertEquals("NUMERIC",mde3.getTypeName());
		Assert.assertEquals(Types.NUMERIC,mde3.getType());
		Assert.assertEquals(10,mde3.getLength());
		Assert.assertEquals(2,mde3.getFrac());

		Assert.assertFalse(mde1.equals(mde3));
		
		try{new RsMetaDataElement(null,"comment","VARCHAR",Types.VARCHAR,100,0);
			Assert.fail("mandatory exception was not detected(null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new RsMetaDataElement("","comment","VARCHAR",Types.VARCHAR,100,0);
			Assert.fail("mandatory exception was not detected(empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{new RsMetaDataElement("f1","comment",null,Types.VARCHAR,100,0);
			Assert.fail("mandatory exception was not detected(null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new RsMetaDataElement("f1","comment","",Types.VARCHAR,100,0);
			Assert.fail("mandatory exception was not detected(empty 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new RsMetaDataElement("f1","comment","VARCHAR2",Types.VARCHAR,100,0);
			Assert.fail("mandatory exception was not detected(unsupported 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{new RsMetaDataElement("f1","comment","VARCHAR",SQLUtils.UNKNOWN_TYPE,100,0);
			Assert.fail("mandatory exception was not detected(unknown 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{new RsMetaDataElement("f1","comment","VARCHAR",Types.VARCHAR,-1,0);
			Assert.fail("mandatory exception was not detected(negative 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new RsMetaDataElement("f1","comment","VARCHAR",Types.VARCHAR,0,-1);
			Assert.fail("mandatory exception was not detected(negative 6-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new RsMetaDataElement("f1","comment","VARCHAR",Types.VARCHAR,1,1);
			Assert.fail("mandatory exception was not detected(6-th argument >= 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
