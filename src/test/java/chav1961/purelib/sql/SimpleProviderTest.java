package chav1961.purelib.sql;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.Assert;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.sql.interfaces.ORMProvider;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class SimpleProviderTest {
	private Connection	conn;
	
	@Before
	public void prepare() {
		conn = null;
	}

	@After
	public void unprepare() {
		conn = null;
	}
	
	@Test
	public void basicTest() throws SyntaxException, LocalizationException, ContentException, IOException, SQLException {
//		final ContentMetadataInterface				clazzModel = ContentModelFactory.forAnnotatedClass(SimpleProviderRecord.class);
//		final ContentMetadataInterface				tableModel = ContentModelFactory.forDBContentDescription(conn.getMetaData(),null,null,"testtable");
//		final String[]								fields = new String[]{}, primaryKeys = new String[]{};
//		
//		try(final SimpleProvider<SimpleProviderRecord,SimpleProviderRecord>	provider = new SimpleProvider<SimpleProviderRecord,SimpleProviderRecord>(
//																tableModel.getRoot(), 
//																clazzModel.getRoot(), 
//																SimpleProviderRecord.class, 
//																fields, 
//																primaryKeys)) {
//			try(final ORMProvider<SimpleProviderRecord,SimpleProviderRecord>	associated = provider.associate(conn)) {
//			}
//			
//			try(final ORMProvider<SimpleProviderRecord,SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord,SimpleProviderRecord>(null,clazzModel.getRoot(),SimpleProviderRecord.class,fields,primaryKeys)) {
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (NullPointerException exc) {
//			}
//			
//			try(final ORMProvider<SimpleProviderRecord,SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord,SimpleProviderRecord>(tableModel.getRoot(),null,SimpleProviderRecord.class,fields,primaryKeys)) {
//				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
//			} catch (NullPointerException exc) {
//			}
//			
//			try(final ORMProvider<SimpleProviderRecord,SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord,SimpleProviderRecord>(tableModel.getRoot(),clazzModel.getRoot(),null,fields,primaryKeys)) {
//				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
//			} catch (NullPointerException exc) {
//			}
//			
//			try(final ORMProvider<SimpleProviderRecord,SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord,SimpleProviderRecord>(tableModel.getRoot(),clazzModel.getRoot(),SimpleProviderRecord.class,null,primaryKeys)) {
//				Assert.fail("Mandatory exception was not detected (null 4-st argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//			try(final ORMProvider<SimpleProviderRecord,SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord,SimpleProviderRecord>(tableModel.getRoot(),clazzModel.getRoot(),SimpleProviderRecord.class,new String[0],primaryKeys)){
//				Assert.fail("Mandatory exception was not detected (null 4-st argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//			try(final ORMProvider<SimpleProviderRecord,SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord,SimpleProviderRecord>(tableModel.getRoot(),clazzModel.getRoot(),SimpleProviderRecord.class,new String[]{null},primaryKeys)) {
//				Assert.fail("Mandatory exception was not detected (null 4-st argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//			
//			try(final ORMProvider<SimpleProviderRecord,SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord,SimpleProviderRecord>(tableModel.getRoot(),clazzModel.getRoot(),SimpleProviderRecord.class,fields,null)) {
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//			try(final ORMProvider<SimpleProviderRecord,SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord,SimpleProviderRecord>(tableModel.getRoot(),clazzModel.getRoot(),SimpleProviderRecord.class,fields,new String[0])) {
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//			try(final ORMProvider<SimpleProviderRecord,SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord,SimpleProviderRecord>(tableModel.getRoot(),clazzModel.getRoot(),SimpleProviderRecord.class,fields,new String[]{null})) {
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//		}
	}
}


