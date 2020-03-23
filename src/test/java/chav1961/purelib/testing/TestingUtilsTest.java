package chav1961.purelib.testing;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.DebuggingException;

public class TestingUtilsTest {
	@Category(DatabaseTestCategory.class)
	@Test
	public void basicTest() throws DebuggingException, SQLException {
		try(final Connection	conn = TestingUtils.getTestConnection()) {
			
		}
	}
}
