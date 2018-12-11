package chav1961.purelib.sql;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import chav1961.purelib.sql.fsys.ResultSetAndMetaDataTest;

@RunWith(Suite.class)
@SuiteClasses({ ArraysLobsAndXMLsTest.class, InternalUtilsTest.class, ResultSetAndMetaDataTest.class })
public class AllTests {

}
