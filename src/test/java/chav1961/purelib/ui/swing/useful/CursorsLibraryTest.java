package chav1961.purelib.ui.swing.useful;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class CursorsLibraryTest {
	@Test
	public void test() {
		Assert.assertNotNull(CursorsAndIconsLibrary.DRAG_HAND);		
	}
}
