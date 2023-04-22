package chav1961.purelib.concurrent;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ JUnitExecutorTest.class, XByteStreamTest.class, XCharStreamTest.class, XStreamTest.class 
	, ListenablesTest.class, LightWeightFutureTest.class, SimpleBitmapResourceDispatcherTest.class
	, SimpleObjectResourceDispatcherTest.class })
public class AllTests {

}
