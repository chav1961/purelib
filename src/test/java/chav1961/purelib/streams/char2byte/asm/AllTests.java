package chav1961.purelib.streams.char2byte.asm;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import chav1961.purelib.streams.char2byte.asm.macro.CommandTest;
import chav1961.purelib.streams.char2byte.asm.macro.ExpressionNodeTest;
import chav1961.purelib.streams.char2byte.asm.macro.MacroClassLoaderTest;
import chav1961.purelib.streams.char2byte.asm.macro.MacroCompilerTest;
import chav1961.purelib.streams.char2byte.asm.macro.MacroProcessingTest;
import chav1961.purelib.streams.char2byte.asm.macro.MacrosTest;

@RunWith(Suite.class)
@SuiteClasses({ AsmWriterTest.class, ByteCodeLineParserTest.class, ClassContainerTest.class,
		ClassDescriptionRepoTest.class, ClassLineParserTest.class, CommandTest.class, ExpressionNodeTest.class,
		InterfaceLineParserTest.class, InternalUtilsTest.class, LongIdTreeTest.class, MethodBodyTest.class,
		TryManagerRecordTest.class })
public class AllTests {

}
