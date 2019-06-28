package chav1961.purelib.nanoservice;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ InternalUtilsTest.class, NanoServiceFactoryTest.class, HttpsNanoServiceFactoryTest.class, NanoServiceManagerTest.class,
		TemplateCacheTest.class })
public class AllTests {

}
