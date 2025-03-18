package chav1961.purelib.streams;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ CharSourcesAndTargetsTest.class, CsvSaxParserTest.class, CsvStaxParserTest.class,
		JsonSaxParserTest.class, JsonStaxParserTest.class, JsonStaxPrinterTest.class, StreamsUtilTest.class })
public class AllTests {

}
