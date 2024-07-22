package chav1961.purelib.nanoservice;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ HttpsNanoServiceFactoryTest.class, InternalUtilsTest.class, NanoServiceFactoryTest.class,
		NanoServiceHttpsTest.class, NanoServiceManagerTest.class, TemplateCacheTest.class })
public class AllTests {

}
