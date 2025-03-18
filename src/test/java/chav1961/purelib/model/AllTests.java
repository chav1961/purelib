package chav1961.purelib.model;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ ContentFilterTest.class, ContentMetadataTest.class, ContentModelFactoryTest.class,
		ContentNodeMetadataTest.class, FieldFormatTest.class, ModelManagedMapTest.class, ModelUtilsTest.class })
public class AllTests {

}
