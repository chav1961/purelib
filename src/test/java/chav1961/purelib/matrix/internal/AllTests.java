package chav1961.purelib.matrix.internal;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ DoubleComplexMatrixTest.class, DoubleRealMatrixTest.class, FloatComplexMatrixTest.class,
		FloatRealMatrixTest.class, IntRealMatrixTest.class, LongRealMatrixTest.class })
public class AllTests {

}
