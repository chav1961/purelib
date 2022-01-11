package chav1961.purelib.sql.model;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.testing.DatabaseTestCategory;
import chav1961.purelib.testing.TestingUtils;

@Category(DatabaseTestCategory.class)
public class SQLResultSetTest {
	private static SimpleURLClassLoader	loader;
	private static Connection			conn;
	
	@BeforeClass
	public static void prepare() throws DebuggingException {
		loader = new SimpleURLClassLoader(new URL[0]);
		conn = TestingUtils.getTestConnection(loader);
	}
	
	@AfterClass
	public static void unprepare() throws IOException, SQLException {
		conn.close();
		loader.close();
	}

	@Before
	public void prepareStep() throws DebuggingException {
		TestingUtils.prepareDatabase(conn, "create table x(y integer)", "insert into x(y) values(100)");
	}	

	@After
	public void unprepareStep() throws DebuggingException {
		TestingUtils.prepareDatabase(conn, "drop table x");
	}
	
	@Test
	public void test() {
	}
}
