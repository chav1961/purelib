package chav1961.purelib.basic;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ SyntaxTreeTest.class, BitCharSetTest.class, CharUtilsTest.class,
		ConsoleCommandManagerTest.class, InternalUtilsTest.class,
		LineByLineProcessorTest.class, LoggerFacadeTest.class,
		PluggableClassLoaderTest.class, SequenceIteratorTest.class,  
		UtilsTest.class , XMLBasedParserText.class,
		ClassLoaderWrapperTest.class, AssemblerTemplateRepoTest.class,
		LongIdMapTest.class, ReusableInstancesTest.class,
		PureLibSettingsTest.class, TemporaryStoreTest.class})
public class AllTests {

}
