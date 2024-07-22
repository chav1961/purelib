package chav1961.purelib.streams.byte2byte;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ FragmentedStreamTest.class, MultipartStreamsTest.class, PseudoRandomInputStreamTest.class,
		SqlStreamsTest.class, ZLibStreamTest.class })
public class AllTests {

}
