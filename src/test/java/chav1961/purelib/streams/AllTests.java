package chav1961.purelib.streams;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CharSourcesAndTargetsTest.class
		, JsonSaxHandlerFactoryTest.class, JsonSaxParserTest.class
		, JsonStaxParserTest.class, JsonStaxPrinterTest.class })
public class AllTests {

}
