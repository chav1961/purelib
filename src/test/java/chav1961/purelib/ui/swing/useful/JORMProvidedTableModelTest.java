package chav1961.purelib.ui.swing.useful;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.sql.SimpleProvider;
import chav1961.purelib.sql.SimpleProviderRecord;
import chav1961.purelib.sql.interfaces.ORMProvider;
import chav1961.purelib.testing.DatabaseTestCategory;
import chav1961.purelib.testing.TestingUtils;

public class JORMProvidedTableModelTest {
	@Category(DatabaseTestCategory.class)
	@Test
	public void basicTest() throws DebuggingException, SQLException, SyntaxException, LocalizationException, ContentException, IOException {
		TestingUtils.prepareDatabase("drop table public.test");
		Assert.assertTrue(TestingUtils.prepareDatabase("create table public.test (f1 integer primary key, f2 varchar(100))"
				,"insert into public.test (f1, f2) values (1,'line 1')"
				,"insert into public.test (f1, f2) values (2,'line 2')"
				,"insert into public.test (f1, f2) values (3,'line 3')"
				,"insert into public.test (f1, f2) values (4,'line 4')"
				,"insert into public.test (f1, f2) values (5,'line 5')"));
		
		try(final Connection	conn = TestingUtils.getTestConnection()) {
			final ContentMetadataInterface	clazzModel = ContentModelFactory.forAnnotatedClass(ProvidedModelTest.class);
			final ContentMetadataInterface	tableModel = ContentModelFactory.forDBContentDescription(conn.getMetaData(),null,"public","test");
			final String[]					fields = new String[]{"f1","f2"}, primaryKeys = new String[]{"f1"};
			final ProvidedModelTest			inst = new ProvidedModelTest();
			
			try(final SimpleProvider<ProvidedModelTest>	provider = new SimpleProvider<ProvidedModelTest>(
																	tableModel.getRoot(), 
																	clazzModel.getRoot(), 
																	ProvidedModelTest.class, 
																	fields, 
																	primaryKeys){
																		@Override public ProvidedModelTest newRecord() throws SQLException {return null;}
																		@Override public ProvidedModelTest duplicateRecord(ProvidedModelTest rec) throws SQLException {return null;}
				
						}) {
				try(final ORMProvider<ProvidedModelTest>	associated = provider.associate(conn)) {
					final JORMProvidedTableModel<ProvidedModelTest>	model = new JORMProvidedTableModel<>(PureLibSettings.PURELIB_LOCALIZER,associated,inst,false);
					
					
					Assert.assertEquals(5,model.getRowCount());
					Assert.assertEquals(2,model.getColumnCount());
					Assert.assertEquals("value",model.getColumnName(0));
					Assert.assertEquals(int.class,model.getColumnClass(0));
					Assert.assertEquals("value",model.getColumnName(1));
					Assert.assertEquals(String.class,model.getColumnClass(1));
					
					Assert.assertEquals(3,model.getValueAt(2,0));
					Assert.assertEquals("line 3",model.getValueAt(2,1));
					model.setValueAt("test",2,1);
					model.refresh();
					Assert.assertEquals("test",model.getValueAt(2,1));
				}
			}
		}
	}
}