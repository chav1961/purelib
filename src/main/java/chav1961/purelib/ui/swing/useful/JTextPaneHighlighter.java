package chav1961.purelib.ui.swing.useful;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import chav1961.purelib.ui.HighlightItem;

public abstract class JTextPaneHighlighter<LexemaType> extends JTextPane {
	private static final long 					serialVersionUID = -1205048630967887904L;
	private static final SimpleAttributeSet		ORDINAL_STYLE = new SimpleAttributeSet();
	
	protected final Map<LexemaType,AttributeSet>	styles = new HashMap<>();
	
	private final StyleContext		content = new StyleContext();
	private final StyledDocument	doc = new DefaultStyledDocument(content);
	private final DocumentListener	listener = new DocumentListener() {
										@Override public void removeUpdate(final DocumentEvent e) {highlight(doc,getText());}
										@Override public void insertUpdate(final DocumentEvent e) {highlight(doc,getText());}
										@Override public void changedUpdate(final DocumentEvent e) {highlight(doc,getText());}
									}; 
	
	protected JTextPaneHighlighter() {
		setDocument(doc);
		doc.addDocumentListener(listener);
	}
	
	protected abstract HighlightItem<LexemaType>[] parseString(final String program);

	private void highlight(final StyledDocument doc, final String text) {
		SwingUtilities.invokeLater(()->{
			int	lastEnd = 0;
			
			doc.removeDocumentListener(listener);
			for (HighlightItem<LexemaType> item : parseString(text)) {
				if (item.from - lastEnd > 1) {
					doc.setCharacterAttributes(lastEnd,item.from - lastEnd,ORDINAL_STYLE,true);
				}
				doc.setCharacterAttributes(item.from,item.length,styles.get(item.type),true);
				lastEnd = item.from + item.length;
			}
			doc.addDocumentListener(listener);
		});
	}
}
