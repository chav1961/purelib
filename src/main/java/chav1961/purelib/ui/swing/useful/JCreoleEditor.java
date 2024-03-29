package chav1961.purelib.ui.swing.useful;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleLexema;
import chav1961.purelib.ui.HighlightItem;
import chav1961.purelib.ui.interfaces.FormModel;

/**
 * <p>This is a swing component for Creole editor with syntax highlighting and paragraph formatting. Use it as usual {@linkplain JTextPane} control.</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @see CreoleWriter 
 * @see FormModel 
 * @since 0.0.3
 * @last.update 0.0.5
 */

public class JCreoleEditor extends JTextPaneHighlighter<CreoleLexema>{
	private static final long 				serialVersionUID = 1068656384609061286L;
	
	{	SimpleAttributeSet	sas = new SimpleAttributeSet();
	
		StyleConstants.setBold(sas,false);
		StyleConstants.setItalic(sas,false);
		characterStyles.put(CreoleLexema.Plain,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setBold(sas,true);
		StyleConstants.setItalic(sas,false);
		characterStyles.put(CreoleLexema.Bold,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setBold(sas,false);
		StyleConstants.setItalic(sas,true);
		characterStyles.put(CreoleLexema.Italic,sas);

		sas = new SimpleAttributeSet();
		
		StyleConstants.setBold(sas,true);
		StyleConstants.setItalic(sas,true);
		characterStyles.put(CreoleLexema.BoldItalic,sas);
		
		sas = new SimpleAttributeSet();

		StyleConstants.setFontSize(sas,12);

		characterStyles.put(CreoleLexema.Paragraph,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setFirstLineIndent(sas,12.0f);
		StyleConstants.setLeftIndent(sas,0.0f);
		StyleConstants.setRightIndent(sas,0.0f);
		StyleConstants.setSpaceBelow(sas,0.0f);
		StyleConstants.setSpaceAbove(sas,0.0f);
		paragraphStyles.put(CreoleLexema.Paragraph,sas);

		sas = new SimpleAttributeSet();
		
		StyleConstants.setFontSize(sas,24);
		StyleConstants.setBold(sas,true);
		characterStyles.put(CreoleLexema.Header1,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setFontSize(sas,22);
		StyleConstants.setBold(sas,true);
		characterStyles.put(CreoleLexema.Header2,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setFontSize(sas,20);
		StyleConstants.setBold(sas,true);
		characterStyles.put(CreoleLexema.Header3,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setFontSize(sas,18);
		StyleConstants.setBold(sas,true);
		characterStyles.put(CreoleLexema.Header4,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setFontSize(sas,16);
		StyleConstants.setBold(sas,true);
		characterStyles.put(CreoleLexema.Header5,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setFontSize(sas,14);
		StyleConstants.setBold(sas,true);
		characterStyles.put(CreoleLexema.Header6,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setFirstLineIndent(sas,20.0f);
		StyleConstants.setLeftIndent(sas,10.0f);
		StyleConstants.setRightIndent(sas,0.0f);
		StyleConstants.setSpaceBelow(sas,0.0f);
		StyleConstants.setSpaceAbove(sas,0.0f);
		paragraphStyles.put(CreoleLexema.OrderedList1,sas);

		sas = new SimpleAttributeSet();
		
		StyleConstants.setFirstLineIndent(sas,30.0f);
		StyleConstants.setLeftIndent(sas,20.0f);
		StyleConstants.setRightIndent(sas,0.0f);
		StyleConstants.setSpaceBelow(sas,0.0f);
		StyleConstants.setSpaceAbove(sas,0.0f);
		paragraphStyles.put(CreoleLexema.OrderedList2,sas);

		sas = new SimpleAttributeSet();
		
		StyleConstants.setFirstLineIndent(sas,40.0f);
		StyleConstants.setLeftIndent(sas,30.0f);
		StyleConstants.setRightIndent(sas,0.0f);
		StyleConstants.setSpaceBelow(sas,0.0f);
		StyleConstants.setSpaceAbove(sas,0.0f);
		paragraphStyles.put(CreoleLexema.OrderedList3,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setFirstLineIndent(sas,50.0f);
		StyleConstants.setLeftIndent(sas,40.0f);
		StyleConstants.setRightIndent(sas,0.0f);
		StyleConstants.setSpaceBelow(sas,0.0f);
		StyleConstants.setSpaceAbove(sas,0.0f);
		paragraphStyles.put(CreoleLexema.OrderedList4,sas);

		sas = new SimpleAttributeSet();
		
		StyleConstants.setFirstLineIndent(sas,60.0f);
		StyleConstants.setLeftIndent(sas,50.0f);
		StyleConstants.setRightIndent(sas,0.0f);
		StyleConstants.setSpaceBelow(sas,0.0f);
		StyleConstants.setSpaceAbove(sas,0.0f);
		paragraphStyles.put(CreoleLexema.OrderedList5,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setFirstLineIndent(sas,20.0f);
		StyleConstants.setLeftIndent(sas,10.0f);
		StyleConstants.setRightIndent(sas,0.0f);
		StyleConstants.setSpaceBelow(sas,0.0f);
		StyleConstants.setSpaceAbove(sas,0.0f);
		paragraphStyles.put(CreoleLexema.UnorderedList1,sas);

		sas = new SimpleAttributeSet();
		
		StyleConstants.setFirstLineIndent(sas,30.0f);
		StyleConstants.setLeftIndent(sas,20.0f);
		StyleConstants.setRightIndent(sas,0.0f);
		StyleConstants.setSpaceBelow(sas,0.0f);
		StyleConstants.setSpaceAbove(sas,0.0f);
		paragraphStyles.put(CreoleLexema.UnorderedList2,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setFirstLineIndent(sas,40.0f);
		StyleConstants.setLeftIndent(sas,30.0f);
		StyleConstants.setRightIndent(sas,0.0f);
		StyleConstants.setSpaceBelow(sas,0.0f);
		StyleConstants.setSpaceAbove(sas,0.0f);
		paragraphStyles.put(CreoleLexema.UnorderedList3,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setFirstLineIndent(sas,50.0f);
		StyleConstants.setLeftIndent(sas,40.0f);
		StyleConstants.setRightIndent(sas,0.0f);
		StyleConstants.setSpaceBelow(sas,0.0f);
		StyleConstants.setSpaceAbove(sas,0.0f);
		paragraphStyles.put(CreoleLexema.UnorderedList4,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setFirstLineIndent(sas,60.0f);
		StyleConstants.setLeftIndent(sas,50.0f);
		StyleConstants.setRightIndent(sas,0.0f);
		StyleConstants.setSpaceBelow(sas,0.0f);
		StyleConstants.setSpaceAbove(sas,0.0f);
		paragraphStyles.put(CreoleLexema.UnorderedList5,sas);

		sas = new SimpleAttributeSet();
		
		StyleConstants.setForeground(sas, Color.GREEN);
		StyleConstants.setBold(sas, true);
		characterStyles.put(CreoleLexema.ListMark,sas);		
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setForeground(sas, Color.BLUE);
		StyleConstants.setUnderline(sas, true);
		characterStyles.put(CreoleLexema.LinkRef,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setForeground(sas, Color.ORANGE);
		StyleConstants.setUnderline(sas, true);
		characterStyles.put(CreoleLexema.ImageRef,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setForeground(sas, Color.RED);
		characterStyles.put(CreoleLexema.HorizontalLine,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setForeground(sas, Color.BLUE);
		StyleConstants.setBold(sas, true);
		StyleConstants.setBackground(sas, Color.LIGHT_GRAY);
		characterStyles.put(CreoleLexema.TableHeader,sas);

		sas = new SimpleAttributeSet();
		
		StyleConstants.setForeground(sas, Color.BLACK);
		StyleConstants.setBackground(sas, Color.LIGHT_GRAY);
		characterStyles.put(CreoleLexema.TableBody,sas); 
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setForeground(sas, Color.BLACK);
		StyleConstants.setBackground(sas, new Color(192,255,255));
		characterStyles.put(CreoleLexema.NonCreoleContent,sas);
	}
	
	public JCreoleEditor() {
		super(true);
	}

	@Override
	public boolean isHighlightingLocked() {
		return super.isHighlightingLocked();
	}
	
	/**
	 * <p>Get paragraph styles for the given Creole lexema</p>
	 * @param lex lexema to get paragraph style for
	 * @return style found. Can be null
	 * @since 0.0.5
	 */
	public AttributeSet getParagraphStyles(final CreoleLexema lex) {
		if (lex == null) {
			throw new NullPointerException("Lexema to get attributes for can't be null"); 
		}
		else {
			return paragraphStyles.get(lex);
		}
	}

	/**
	 * <p>Get character styles for the given Creole lexema</p>
	 * @param lex lexema to get character style for
	 * @return style found. Can be null
	 * @since 0.0.5
	 */
	public AttributeSet getCharacterStyles(final CreoleLexema lex) {
		if (lex == null) {
			throw new NullPointerException("Lexema to get attributes for can't be null"); 
		}
		else {
			return characterStyles.get(lex);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected HighlightItem<CreoleLexema>[] parseString(final String text) {
		try(final StringWriter	writer = new StringWriter()){
			try(final CreoleWriter	cre = new CreoleWriter(writer,MarkupOutputFormat.PARSEDCSV)) {
				cre.write((text+(text.endsWith("\n") ? "" : "\n")));
				cre.flush();
			}
			final List<HighlightItem<CreoleLexema>>	result = new ArrayList<>();
			
			for (String item : writer.toString().split("\n")) {
				final String[]	parts = item.split(",");
				
				if (parts.length == 3) {
					result.add(new HighlightItem<CreoleLexema>(Integer.valueOf(parts[1]),Integer.valueOf(parts[2]),CreoleLexema.valueOf(parts[0])));
				}
			}
			return result.toArray(new HighlightItem[result.size()]);
		} catch (IOException e) {
			return new HighlightItem[0];
		}
	}
}
