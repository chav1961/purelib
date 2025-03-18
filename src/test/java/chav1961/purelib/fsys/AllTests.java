package chav1961.purelib.fsys;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ FileSystemClassLoaderTest.class, FileSystemFactoryTest.class, FileSystemTest.class,
		FileSystemURLConnectionTest.class })
public class AllTests {

}
