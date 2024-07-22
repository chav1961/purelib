package chav1961.purelib.testing;


import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.DebuggingException;

public class TestingUtilsTest {
	@Tag("DatabaseTestCategory")
	@Test
	public void basicTest() throws DebuggingException, SQLException {
		try(final Connection	conn = TestingUtils.getTestConnection()) {
			
		}
	}
}
