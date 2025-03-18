package chav1961.purelib.cdb;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ CompilerUtilsTest.class, InternalUtilsTest.class, PseudoCompilerUtilsTest.class,
		SyntaxNodeUtilsTest.class })
public class AllTests {

}
