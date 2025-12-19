package chav1961.purelib.ui.swing.useful;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.testing.SwingUnitTest;
import chav1961.purelib.testing.TestingUtils;
import chav1961.purelib.ui.swing.SwingUtils;

@Tag("DatabaseTestCategory")
public class JDatabaseTableWithMetaTest {
	private Connection	conn;
	
	@Before
	public void prepare() throws DebuggingException, SQLException {
		conn = TestingUtils.getTestConnection(PureLibSettings.INTERNAL_LOADER);
		TestingUtils.prepareDatabase(conn, 
				"drop table public.\"JDatabaseTableWithMetaTest_x\"", 
				"create table public.\"JDatabaseTableWithMetaTest_x\"(f1 integer primary key, f2 varchar(100) not null, f3 numeric(15,2))",
				"insert into public.\"JDatabaseTableWithMetaTest_x\"(f1,f2,f3) values(1,'assa',100)");
	}
	
	@After
	public void unprepare() throws SQLException, DebuggingException {
		TestingUtils.prepareDatabase(conn, "drop table public.JDatabaseTableWithMetaTest_x");
		conn.close();
	}
	
	@Test
	@Tag("UITestCategory")
	public void readOnlyTest() throws SQLException, ContentException, DebuggingException {
		final JFrame f = new JFrame();
		
		try(final Statement		stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			final ResultSet		rs = stmt.executeQuery("select * from public.\"JDatabaseTableWithMetaTest_x\"")) {
			final SwingUnitTest ut = new SwingUnitTest(f);
			final ContentMetadataInterface	mdi = ContentModelFactory.forDBContentDescription(conn.getMetaData(), null, "public", "JDatabaseTableWithMetaTest_x");
			final JDataBaseTableWithMeta<?,?> 	t = new JDataBaseTableWithMeta<>(mdi.getRoot(), PureLibSettings.PURELIB_LOCALIZER, PureLibSettings.CURRENT_LOGGER, true, true);
			
			f.getContentPane().add(new JScrollPane(t));
			SwingUtils.centerMainWindow(f);
			f.setVisible(true);
			t.assignResultSet(rs);
			
			ut.use(t);
			ut.await();
			ut.keys(KeyStroke.getKeyStroke("DOWN"),KeyStroke.getKeyStroke("RIGHT"));
			
			ut.await();
			t.resetResultSetAndManagers();
			f.setVisible(false);
		} finally {
			f.dispose();
		}
	}
}
