package chav1961.purelib.json;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ JsonNodeAndUtilsTest.class, JsonSaxHandlerFactoryTest.class, JsonSerializerTest.class })
public class AllTests {

}
