package chav1961.purelib.basic;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ ArgParserTest.class, AsmScriptEngineTest.class, BitCharSetTest.class, BKTreeTest.class,
		CharArrayPieceTest.class, CharUtilsTest.class, ColorUtilsTest.class, ConsoleCommandManagerTest.class,
		CSSUtilsTest.class, DirectoryListenerTest.class, FSMTest.class, GettersAndSettersFactoryTest.class,
		InternalUtilsTest.class, LineByLineProcessorTest.class, LoggerFacadeTest.class, LongIdMapTest.class,
		MimeTypeTest.class, PluggableClassLoaderTest.class, PureLibSettingsTest.class, ReusableInstancesTest.class,
		ScriptEngineTest.class, SequenceIteratorTest.class, SubstitutablePropertiesTest.class, SyntaxTreeTest.class,
		TemporaryStoreTest.class, UnsafedUtilsTest.class, URIUtilsTest.class, UtilsTest.class, XMLBasedParserText.class,
		XMLUtilsTest.class })
public class AllTests {

}
