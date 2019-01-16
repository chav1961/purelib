package chav1961.purelib.ui.swing.useful;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import chav1961.purelib.streams.char2char.CreoleWriter.CreoleLexema;
import chav1961.purelib.ui.HighlightItem;

public abstract class JTextPaneHighlighter<LexemaType> extends JTextPane {
	private static final long 					serialVersionUID = -1205048630967887904L;
	private static final SimpleAttributeSet		ORDINAL_CHARACTER_STYLE = new SimpleAttributeSet();
	private static final SimpleAttributeSet		ORDINAL_PARAGRAPH_STYLE = new SimpleAttributeSet();
	
	static {
		StyleConstants.setBold(ORDINAL_CHARACTER_STYLE,false);
		StyleConstants.setItalic(ORDINAL_CHARACTER_STYLE,false);
		StyleConstants.setUnderline(ORDINAL_CHARACTER_STYLE,false);
		StyleConstants.setStrikeThrough(ORDINAL_CHARACTER_STYLE,false);
		StyleConstants.setSubscript(ORDINAL_CHARACTER_STYLE,false);
		StyleConstants.setSuperscript(ORDINAL_CHARACTER_STYLE,false);
		StyleConstants.setForeground(ORDINAL_CHARACTER_STYLE,Color.BLACK);
		StyleConstants.setBackground(ORDINAL_CHARACTER_STYLE,Color.WHITE);
		StyleConstants.setFontSize(ORDINAL_CHARACTER_STYLE,12);
		
		StyleConstants.setFirstLineIndent(ORDINAL_PARAGRAPH_STYLE,0.0f);
		StyleConstants.setLeftIndent(ORDINAL_PARAGRAPH_STYLE,0.0f);
		StyleConstants.setRightIndent(ORDINAL_PARAGRAPH_STYLE,0.0f);
		StyleConstants.setSpaceAbove(ORDINAL_PARAGRAPH_STYLE,0.0f);
		StyleConstants.setSpaceBelow(ORDINAL_PARAGRAPH_STYLE,0.0f);
	}
	
	protected final Map<LexemaType,AttributeSet>	characterStyles = new HashMap<>();
	protected final Map<LexemaType,AttributeSet>	paragraphStyles = new HashMap<>();
	
	private final StyleContext		content = new StyleContext();
	private final StyledDocument	doc = new DefaultStyledDocument(content);
	private final DocumentListener	listener = new DocumentListener() {
										@Override public void removeUpdate(final DocumentEvent e) {highlight(doc,getText());}
										@Override public void insertUpdate(final DocumentEvent e) {highlight(doc,getText());}
										@Override public void changedUpdate(final DocumentEvent e) {highlight(doc,getText());}
									}; 
	private final boolean			useNestedLexemas; 
	
	protected JTextPaneHighlighter() {
		this(false);
	}
	
	protected JTextPaneHighlighter(final boolean useNestedLexemas) {
		this.useNestedLexemas = useNestedLexemas;
		setDocument(doc);
		doc.addDocumentListener(listener);
	}
	
	protected abstract HighlightItem<LexemaType>[] parseString(final String program);

	protected SimpleAttributeSet getOrdinalCharacterStyle() {
		return ORDINAL_CHARACTER_STYLE;
	}

	protected SimpleAttributeSet getOrdinalParagraphStyle() {
		return ORDINAL_PARAGRAPH_STYLE;
	}
	
	protected HighlightItem<LexemaType> preprocessLexema(final HighlightItem<LexemaType> source) {
		return source;
	}
	
	private void highlight(final StyledDocument doc, final String text) {
		SwingUtilities.invokeLater(()->{
			try{doc.removeDocumentListener(listener);
				doc.setCharacterAttributes(0,doc.getLength(),getOrdinalCharacterStyle(),false);
				doc.setParagraphAttributes(0,doc.getLength(),getOrdinalParagraphStyle(),false);
				
				System.err.println("***");
				for (HighlightItem<LexemaType> currentItem : parseString(text)) {
					final HighlightItem<LexemaType>	item = preprocessLexema(currentItem); 
					System.err.println(item);
					if (item.length > 0) {
						if (characterStyles.containsKey(item.type)) {
							doc.setCharacterAttributes(item.from,item.length,characterStyles.get(item.type),useNestedLexemas);
						}
						if (paragraphStyles.containsKey(item.type)) {
							doc.setParagraphAttributes(item.from,item.length,paragraphStyles.get(item.type),useNestedLexemas);
						}
					}
				}
				System.err.println("///");
			} finally {
				doc.addDocumentListener(listener);
			}
		});
	}
}
