package chav1961.purelib.ui;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ AbstractInMemoryFormModelTest.class, ColorPairTest.class, ConstraintCheckerFactoryTest.class,
		FormMonitorTest.class })
public class AllTests {

}
