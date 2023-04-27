package chav1961.purelib.ui.swing.useful.interfaces;

import javax.swing.text.AttributeSet;

import chav1961.purelib.ui.swing.useful.JTextPaneHighlighter;

/**
 * <p>This interface describes any owner of character and/or paragraph attributes. Returned attributes can be used in the styled Swing documents</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @see JTextPaneHighlighter
 */
public interface StyledAttributesKeeper {
	/**
	 * <p>Get character attributes.</p>
	 * @return character attributes. Can be empty but not null. You can return {@linkplain JTextPaneHighlighter#ORDINAL_CHARACTER_STYLE} when special settings are not required
	 */
	AttributeSet getCharacterAttributes();
	
	/**
	 * <p>Get paragraph attributes.</p>
	 * @return paragraph attributes. Can be empty but not null. You can return {@linkplain JTextPaneHighlighter#ORDINAL_PARAGRAPH_STYLE} when special settings are not required
	 */
	AttributeSet getParagraphAttributes();
}
