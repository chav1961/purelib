package chav1961.purelib.ui.swing.useful;


import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleTimerTask;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.ui.HighlightItem;

/**
 * <p>This is a simple syntax highlighter for the Swing applications. Use it instead of {@linkplain JTextPane} component where you wish.</p>
 * <p>This class is not thread-safe</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @see CreoleWriter 
 * @since 0.0.3
 * @param <LexemaType> any marker for the text pieces. It's strongly recommended to use enumerations for it
 */
public abstract class JTextPaneHighlighter<LexemaType> extends JTextPane {
	private static final long 					serialVersionUID = -1205048630967887904L;
	private static final SimpleAttributeSet		ORDINAL_CHARACTER_STYLE = new SimpleAttributeSet();
	private static final SimpleAttributeSet		ORDINAL_PARAGRAPH_STYLE = new SimpleAttributeSet();
	private static final long					REFRESH_DELAY_MILLISECONDS = 100;

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
										@Override public void removeUpdate(final DocumentEvent e) {highlight();}
										@Override public void insertUpdate(final DocumentEvent e) {highlight();}
										@Override public void changedUpdate(final DocumentEvent e) {highlight();}
									}; 
	private final Comparator<HighlightItem<LexemaType>>	sorter = new Comparator<HighlightItem<LexemaType>>() {	// Order to exclude attributes overlapping 
										@Override
										public int compare(HighlightItem<LexemaType> o1, HighlightItem<LexemaType> o2) {
											if (o1.from == o2.from) {
												return o2.length - o1.length;
											}
											else {
												return o1.from - o2.from;
											}
										}
									};
	private final boolean			useNestedLexemas;
	
	private volatile TimerTask		tt = null;	// Timer is used to reduce parser calls because JTextPane always fills it's content line-by-line!

	/**
	 * <p>Constructor of the class</p>
	 */
	protected JTextPaneHighlighter() {
		this(false);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param useNestedLexemas allow different content attributes to overlap
	 */
	protected JTextPaneHighlighter(final boolean useNestedLexemas) {
		this.useNestedLexemas = useNestedLexemas;
		setDocument(doc);
		doc.addDocumentListener(listener);
	}
	
	/**
	 * <p>The most important method to implement. Get current content of the control, parse it and return array of parsed text pieces with their's marks.
	 * if you typed true on the {@linkplain #JTextPaneHighlighter(boolean)} constructor, ranges of the parsed pieces can overlaps. All non-marked text pieces
	 * will have default text attributes on the screen. When calculate offsets and lengths of the pieces, pay attention, that character <b>'\r'</b> in the 
	 * string parsing must be counted as missing (has zero length).</p> 
	 * @param program program to parse
	 * @return array of pieces and it's marks in the program 
	 */
	protected abstract HighlightItem<LexemaType>[] parseString(final String program);

	/**
	 * <p>Get default paragraph style for non-marked content</p> 
	 * @return ordinal paragraph style (alignment, margins etc)
	 */
	protected SimpleAttributeSet getOrdinalCharacterStyle() {
		return ORDINAL_CHARACTER_STYLE;
	}

	/**
	 * <p>Get default character style for non-marked content.</p>
	 * @return ordinal character style (color, font, text style etc)
	 */
	protected SimpleAttributeSet getOrdinalParagraphStyle() {
		return ORDINAL_PARAGRAPH_STYLE;
	}
	
	/**
	 * <p>Preprocess current lexema. Yoy can make advanced processing in this method. 
	 * @param source souce lexema
	 * @return preprocessed lexema
	 */
	protected HighlightItem<LexemaType> preprocessLexema(final HighlightItem<LexemaType> source, final String text) {
		return source;
	}

	private void callHighlighter() {
		SwingUtilities.invokeLater(()->{
			try{doc.removeDocumentListener(listener);
				doc.setCharacterAttributes(0,doc.getLength(),getOrdinalCharacterStyle(),false);
				doc.setParagraphAttributes(0,doc.getLength(),getOrdinalParagraphStyle(),false);
				
				final String	text = getText().replace("\r","");
				
				if (!text.trim().isEmpty()) {
					final HighlightItem<LexemaType>[]	lexList = parseString(text);
	
					Arrays.sort(lexList, sorter);
					for (HighlightItem<LexemaType> currentItem : lexList) {
						final HighlightItem<LexemaType>	item = preprocessLexema(currentItem,text);
						
						if (item.length > 0) {
							if (characterStyles.containsKey(item.type)) {
								doc.setCharacterAttributes(item.from,item.length,characterStyles.get(item.type),useNestedLexemas);
							}
							if (paragraphStyles.containsKey(item.type)) {
								doc.setParagraphAttributes(item.from,item.length,paragraphStyles.get(item.type),useNestedLexemas);
							}
						}
					}
				}
			} finally {
				doc.addDocumentListener(listener);
			}
		});
	}
	
	private void highlight() {
		synchronized(this) {
			if (tt != null) {
				tt.cancel();
			}
			tt = new SimpleTimerTask(()->callHighlighter());
			PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(tt, REFRESH_DELAY_MILLISECONDS);
		}
	}
}
