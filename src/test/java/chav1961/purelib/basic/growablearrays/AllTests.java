package chav1961.purelib.basic.growablearrays;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ GrowableArraysTest.class, InOutGrowableByteArrayTest.class, InOutGrowableCharArrayTest.class, ManagersTest.class, StreamsTest.class })
public class AllTests {

}
