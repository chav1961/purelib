package chav1961.purelib.streams.char2char;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import chav1961.purelib.streams.char2char.intern.CreoleWriterTest;
import chav1961.purelib.streams.char2char.intern.ListManipulationStackTest;

@RunWith(Suite.class)
@SuiteClasses({ CharStreamPrinterTest.class, CreoleWriterTest.class, ListManipulationStackTest.class,
		PreprocessingReaderTest.class })
public class AllTests {

}
