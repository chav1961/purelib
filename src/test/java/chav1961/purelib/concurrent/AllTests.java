package chav1961.purelib.concurrent;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ JUnitExecutorTest.class, LightWeightFutureTest.class, ListenablesTest.class,
		SimpleBitmapResourceDispatcherTest.class, SimpleObjectResourceDispatcherTest.class, XByteStreamTest.class,
		XCharStreamTest.class, XStreamTest.class })
public class AllTests {

}
