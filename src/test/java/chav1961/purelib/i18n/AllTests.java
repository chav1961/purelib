package chav1961.purelib.i18n;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ LocaleSpecificTextWrapperTest.class, LocalizerFactoryTest.class, LocalizerTest.class,
		MutableLocalizerTest.class })
public class AllTests {

}
