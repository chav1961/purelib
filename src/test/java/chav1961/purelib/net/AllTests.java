package chav1961.purelib.net;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ AbstractDiscoveryTest.class, LightWeightNetworkDiscoveryTest.class, URIsTest.class })
public class AllTests {

}
