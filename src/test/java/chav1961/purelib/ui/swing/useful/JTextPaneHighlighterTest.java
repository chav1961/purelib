package chav1961.purelib.ui.swing.useful;


import java.util.concurrent.Semaphore;

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.ui.HighlightItem;

@Tag("OrdinalTestCategory")
public class JTextPaneHighlighterTest {
	enum TestEnum {
		ORDINAL, STYLE, PARAGRAPH
	}
	
	@Test
	public void test() throws InterruptedException {
		final SimpleAttributeSet 				sasStyle = new SimpleAttributeSet();
		final SimpleAttributeSet 				sasParagraph = new SimpleAttributeSet();
		final boolean[]							flags = new boolean[1];
		final JTextPaneHighlighter<TestEnum>	h = new JTextPaneHighlighter<TestEnum>(true) {
													{characterStyles.put(TestEnum.STYLE,sasStyle);
													 paragraphStyles.put(TestEnum.PARAGRAPH,sasParagraph);
													}
													
													@Override
													protected HighlightItem<TestEnum>[] parseString(final String program) {
														Assert.assertEquals("012",program);
														flags[0] = true;
														return new HighlightItem[] {
																new HighlightItem<>(0,1,TestEnum.ORDINAL),
																new HighlightItem<>(1,1,TestEnum.STYLE),
																new HighlightItem<>(2,1,TestEnum.PARAGRAPH)
														};
													}
												};
		
		StyleConstants.setBold(sasStyle,true);
		StyleConstants.setSpaceBelow(sasParagraph,1.0f);
												
		flags[0] = false;
		h.setText("012");
		Thread.sleep(1000);
		Assert.assertTrue(flags[0]);
		
		final StyledDocument	sd = h.getStyledDocument();
		final AttributeSet 		a1 = sd.getCharacterElement(1).getAttributes();
		final AttributeSet 		a2 = sd.getParagraphElement(2).getAttributes();

		Assert.assertEquals(sasStyle,a1);
		Assert.assertEquals(sasParagraph,a2);

		flags[0] = false;
		h.setText("");
		Thread.sleep(1000);
		Assert.assertFalse(flags[0]);
	}
}
