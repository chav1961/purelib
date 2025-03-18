package chav1961.purelib.sql;


import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.testing.TestingUtils;

public class SimpleResultSetProviderTest {
	@Tag("DatabaseTestCategory")
	@Test
	public void readOnlyTest() throws DebuggingException, SQLException, IOException, ContentException {
		TestingUtils.prepareDatabase("drop table public.test");
		Assert.assertTrue(TestingUtils.prepareDatabase("create table public.test (f1 integer primary key, f2 varchar(100))"
				,"insert into public.test(f1,f2) values(10,'line 10')"
				,"insert into public.test(f1,f2) values(20,'line 20')"
				,"insert into public.test(f1,f2) values(30,'line 30')"
				));
		
		try(final Connection	conn = TestingUtils.getTestConnection();
			final Statement		stmt = conn.createStatement()) {
			
			try(final SimpleURLClassLoader	ldr = new SimpleURLClassLoader(new URL[0]);
				final ResultSet				rs = stmt.executeQuery("select * from public.test");
				final ProviderRecord		pr = new ProviderRecord(rs,ldr)) {

				StringBuilder	sb = new StringBuilder();
				int				count = 0, sum = 0;
				
				while(pr.next()) {
					sum += pr.f1;
					sb.append(pr.f2);
					count++;
				}
				Assert.assertEquals(3,count);
				Assert.assertEquals(60,sum);
				Assert.assertEquals("line 10line 20line 30",sb.toString());
				
				try{pr.insert();
					Assert.fail("Mandatory exception was not detected (read-only result set)");
				} catch (IllegalStateException exc) {
				}
				try{pr.update();
					Assert.fail("Mandatory exception was not detected (read-only result set)");
				} catch (IllegalStateException exc) {
				}
				try{pr.delete();
					Assert.fail("Mandatory exception was not detected (read-only result set)");
				} catch (IllegalStateException exc) {
				}
				try{pr.refresh();
					Assert.fail("Mandatory exception was not detected (result set exhausted)");
				} catch (IllegalStateException exc) {
				}
				
				try{new ProviderRecord(null,ldr);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (IllegalStateException exc) {
				}
				try{new ProviderRecord(rs,null);
					Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
				} catch (IllegalStateException exc) {
				}
			}
		} finally {
			TestingUtils.prepareDatabase("drop table public.test");
		}
	}

	@Tag("DatabaseTestCategory")
	@Test
	public void mutableTest() throws DebuggingException, SQLException, IOException, ContentException {
		TestingUtils.prepareDatabase("drop table public.test");
		Assert.assertTrue(TestingUtils.prepareDatabase("create table public.test (f1 integer primary key, f2 varchar(100))"
				,"insert into public.test(f1,f2) values(10,'line 10')"
				,"insert into public.test(f1,f2) values(20,'line 20')"
				,"insert into public.test(f1,f2) values(30,'line 30')"
				));
		
		try(final Connection	conn = TestingUtils.getTestConnection();
			final Statement		stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE)) {
			
			try(final SimpleURLClassLoader	ldr = new SimpleURLClassLoader(new URL[0]);
				final ResultSet				rs = stmt.executeQuery("select * from public.test");
				final ProviderRecord		pr = new ProviderRecord(rs,ldr)) {

				Assert.assertTrue(pr.next());
				pr.delete();
				Assert.assertTrue(pr.next());
				pr.f1 = 100;
				pr.f2 = "updated";
				pr.update();
				Assert.assertTrue(pr.next());
				pr.f1 = 200;
				pr.f2 = "inserted";
				pr.insert();
				Assert.assertFalse(pr.next());

				try{pr.refresh();
					Assert.fail("Mandatory exception was not detected (result set exhausted)");
				} catch (IllegalStateException exc) {
				}
			}

			try(final SimpleURLClassLoader	ldr = new SimpleURLClassLoader(new URL[0]);
				final ResultSet				rs = stmt.executeQuery("select * from public.test");
				final ProviderRecord		pr = new ProviderRecord(rs,ldr)) {

				Assert.assertTrue(pr.next());
				Assert.assertEquals(100,pr.f1);
				Assert.assertEquals("updated",pr.f2);
				Assert.assertTrue(pr.next());
				Assert.assertEquals(30,pr.f1);
				Assert.assertEquals("line 30",pr.f2);
				Assert.assertTrue(pr.next());
				Assert.assertEquals(200,pr.f1);
				Assert.assertEquals("inserted",pr.f2);
				Assert.assertFalse(pr.next());
				
				try{pr.refresh();
					Assert.fail("Mandatory exception was not detected (result set exhausted)");
				} catch (IllegalStateException exc) {
				}
			}
		} finally {
			TestingUtils.prepareDatabase("drop table public.test");
		}
	}
}

class ProviderRecord extends SimpleResultSetProvider {
	public int		f1;
	public String	f2;

	public ProviderRecord(ResultSet rs, SimpleURLClassLoader loader) throws ContentException, SQLException {
		super(rs, loader);
	}

	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}
}
