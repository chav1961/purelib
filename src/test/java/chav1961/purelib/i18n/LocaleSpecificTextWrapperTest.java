package chav1961.purelib.i18n;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class LocaleSpecificTextWrapperTest {
	private static final String		FOR_TEXT = "text";
	private static final String		FOR_TOOLTIP = "tooltip";

	@Test
	public void staticTest() {
		final JComponent								c1 = new JComponent() {private static final long serialVersionUID = 1L;};
		final LocaleSpecificTextWrapper<JComponent>		wc1 = LocaleSpecificTextWrapper.wrap(c1);

		testWrapper(wc1,c1,()->{return true;},()->{return FOR_TOOLTIP.equals(c1.getToolTipText());});

		final JTextComponent							c2 = new JTextField();
		final LocaleSpecificTextWrapper<JTextComponent>	wc2 = LocaleSpecificTextWrapper.wrap(c2);

		testWrapper(wc2,c2,()->{return FOR_TEXT.equals(c2.getText());},()->{return FOR_TOOLTIP.equals(c2.getToolTipText());});
		
		final AbstractButton							c3 = new JButton();
		final LocaleSpecificTextWrapper<AbstractButton>	wc3 = LocaleSpecificTextWrapper.wrap(c3);
		
		testWrapper(wc3,c3,()->{return FOR_TEXT.equals(c3.getText());},()->{return FOR_TOOLTIP.equals(c3.getToolTipText());});

		final JLabel									c4 = new JLabel();
		final LocaleSpecificTextWrapper<JLabel>			wc4 = LocaleSpecificTextWrapper.wrap(c4);
		
		testWrapper(wc4,c4,()->{return FOR_TEXT.equals(c4.getText());},()->{return FOR_TOOLTIP.equals(c4.getToolTipText());});
		
		try{LocaleSpecificTextWrapper.wrap((JComponent)null);
			Assert.fail("Mandatory exception was not detetced (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{LocaleSpecificTextWrapper.wrap((JTextComponent)null);
			Assert.fail("Mandatory exception was not detetced (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{LocaleSpecificTextWrapper.wrap((AbstractButton)null);
			Assert.fail("Mandatory exception was not detetced (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{LocaleSpecificTextWrapper.wrap((JLabel)null);
			Assert.fail("Mandatory exception was not detetced (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@FunctionalInterface
	private interface AssertInterface {
		boolean check();
	}
	
	private <T extends JComponent> void testWrapper(final LocaleSpecificTextWrapper<T> wrapper, final T component, final AssertInterface checkText, final AssertInterface checkTooltip) {
		Assert.assertEquals(wrapper.getComponent(),component);
		
		wrapper.setLocaleSpecificText(FOR_TEXT);
		Assert.assertTrue(checkText.check());
		try{wrapper.setLocaleSpecificText(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		wrapper.setLocaleSpecificToolTipText(FOR_TOOLTIP);
		Assert.assertTrue(checkTooltip.check());
		try{wrapper.setLocaleSpecificToolTipText(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
