package chav1961.purelib.sql.content;

import java.sql.Types;
import java.util.Hashtable;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.sql.RsMetaDataElement;

public class SQLContentUtilsTest {
	@Test
	public void staticTest() throws SyntaxException {
		final Hashtable<String,String[]>	source = new Hashtable<>(), excludes = new Hashtable<>(), includes = new Hashtable<>(); 
		
		source.put("key1",new String[]{"value1"});
		source.put("key2",new String[]{"value2"});
		source.put("key3",new String[]{"value3"});

		excludes.put("key2",new String[]{"value2"});

		includes.put("f1",new String[]{"v1"});
		includes.put("f2",new String[]{"v2"});
		
		Assert.assertTrue(new SubstitutableProperties(Utils.mkProps("key1","value1","key3","value3"))
								.theSame(SQLContentUtils.extractOptions(source, excludes)));
		
		
		try{SQLContentUtils.extractOptions(null, excludes);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{SQLContentUtils.extractOptions(source, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {			
		}
		
		Assert.assertArrayEquals(new RsMetaDataElement[]{
					new RsMetaDataElement("f1","","VARCHAR",Types.VARCHAR,1,0)
					,new RsMetaDataElement("f2","","NUMERIC",Types.NUMERIC,10,2)
				}
				, SQLContentUtils.buildMetadataFromQueryString("f1=VARCHAR(1)&f2=NUMERIC(10,2)&key1=value1",includes));
		
		try{SQLContentUtils.buildMetadataFromQueryString(null,includes);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {			
		}
		try{SQLContentUtils.buildMetadataFromQueryString("",includes);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {			
		}
		try{SQLContentUtils.buildMetadataFromQueryString("f1=VARCHAR(1)&f2=NUMERIC(10,2)&key1=value1",null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
		
		try{SQLContentUtils.buildMetadataFromQueryString("f1=VARCHAR(1)&f2=NUMERIC(10,2)&key1=value1",new Hashtable<>());
			Assert.fail("Mandatory exception was not detected (include list names doesn't intersect with query string keys)");
		} catch (IllegalArgumentException exc) {			
		}
	}
}
