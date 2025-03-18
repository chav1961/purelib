package chav1961.purelib.streams.char2byte.asm;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ AsmWriterTest.class, ByteCodeLineParserTest.class, ClassContainerTest.class,
		ClassDescriptionRepoTest.class, ClassLineParserTest.class, CompilerUtilsTest.class,
		InterfaceLineParserTest.class, InternalUtilsTest.class, LongIdTreeTest.class, MethodBodyTest.class,
		StackAndVarRepoNewTest.class, StackAndVarRepoTest.class, TryManagerRecordTest.class })
public class AllTests {

}
