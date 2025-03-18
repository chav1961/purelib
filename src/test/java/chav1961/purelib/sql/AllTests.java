package chav1961.purelib.sql;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ ArraysLobsAndXMLsTest.class, ResultSetTests.class, RsMetaDataElementTest.class,
		SimpleResultSetProviderTest.class, SQLUtilsTest.class })
public class AllTests {

}
