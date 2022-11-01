package chav1961.purelib.streams.byte2byte;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ZLibStreamTest.class , PseudoRandomInputStreamTest.class, SqlStreamsTest.class, FragmentedStreamTest.class, MultipartStreamsTest.class })
public class AllTests {

}
