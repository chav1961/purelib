package chav1961.purelib.sql.content;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ FactoryAndParsersTest.class, FakeResultSetMetaDataTest.class, SQLContentUtilsTest.class })
public class AllTests {

}
