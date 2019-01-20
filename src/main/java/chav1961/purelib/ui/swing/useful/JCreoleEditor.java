package chav1961.purelib.ui.swing.useful;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleLexema;
import chav1961.purelib.ui.HighlightItem;
import chav1961.purelib.ui.interfacers.FormModel;

/**
 * <p>This is a swing component for Creole editor with syntax highlighting and paragraph formatting. Use it as usual {@linkplain JTextPane} control.</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @see CreoleWriter 
 * @see FormModel 
 * @since 0.0.3
 */

public class JCreoleEditor extends JTextPaneHighlighter<CreoleLexema>{
	private static final long 				serialVersionUID = 1068656384609061286L;
	private static final Set<CreoleLexema>	NEED_PREPROCESS = new HashSet<>();

	static {
		NEED_PREPROCESS.add(CreoleLexema.OrderedList1);
		NEED_PREPROCESS.add(CreoleLexema.OrderedList2);
		NEED_PREPROCESS.add(CreoleLexema.OrderedList3);
		NEED_PREPROCESS.add(CreoleLexema.OrderedList4);
		NEED_PREPROCESS.add(CreoleLexema.OrderedList5);
		NEED_PREPROCESS.add(CreoleLexema.UnorderedList1);
		NEED_PREPROCESS.add(CreoleLexema.UnorderedList2);
		NEED_PREPROCESS.add(CreoleLexema.UnorderedList3);
		NEED_PREPROCESS.add(CreoleLexema.UnorderedList4);
		NEED_PREPROCESS.add(CreoleLexema.UnorderedList5);
		NEED_PREPROCESS.add(CreoleLexema.Paragraph);
	}
	
	{	SimpleAttributeSet	sas = new SimpleAttributeSet();
	
		characterStyles.put(CreoleLexema.Plain,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setBold(sas,true);
		characterStyles.put(CreoleLexema.Bold,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setItalic(sas,true);
		characterStyles.put(CreoleLexema.Italic,sas);
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setFirstLineIndent(sas,12.0f);
		StyleConstants.setLeftIndent(sas,0.0f);
		StyleConstants.setRightIndent(sas,0.0f);
		StyleConstants.setSpaceBelow(sas,0.0f);
		StyleConstants.setSpaceAbove(sas,0.0f);
		paragraphStyles.put(CreoleLexema.Paragraph,sas);

//		sas = new SimpleAttributeSet();
//		
//		StyleConstants.setForeground(sas, Color.CYAN);
//		StyleConstants.setBackground(sas, Color.LIGHT_GRAY);
//		StyleConstants.setBold(sas, true);
//		characterStyles.put(CreoleLexema.Paragraph,sas);		
		
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
//		StyleConstants.setBackground(sas, Color.LIGHT_GRAY);
		StyleConstants.setBold(sas, true);
		characterStyles.put(CreoleLexema.ListMark,sas);		
//		characterStyles.put(CreoleLexema.OrderedList1,sas);		
//		characterStyles.put(CreoleLexema.OrderedList2,sas);
//		characterStyles.put(CreoleLexema.OrderedList3,sas);
//		characterStyles.put(CreoleLexema.OrderedList4,sas);
//		characterStyles.put(CreoleLexema.OrderedList5,sas);
//		characterStyles.put(CreoleLexema.UnorderedList1,sas);
//		characterStyles.put(CreoleLexema.UnorderedList2,sas);
//		characterStyles.put(CreoleLexema.UnorderedList3,sas);
//		characterStyles.put(CreoleLexema.UnorderedList4,sas);
//		characterStyles.put(CreoleLexema.UnorderedList5,sas);
		
		
		
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
		
		StyleConstants.setBackground(sas, Color.LIGHT_GRAY);
		characterStyles.put(CreoleLexema.TableBody,sas); 
		
		sas = new SimpleAttributeSet();
		
		StyleConstants.setForeground(sas, Color.WHITE);
		StyleConstants.setBackground(sas, Color.BLACK);
		characterStyles.put(CreoleLexema.NonCreoleContent,sas);
		
	}
	
	public JCreoleEditor() {
		super(true);
	}

	protected HighlightItem<CreoleLexema> preprocessLexema(final HighlightItem<CreoleLexema> source) {
//		if (NEED_PREPROCESS.contains(source.type)) {
//			return new HighlightItem<CreoleLexema>(source.from,source.length-1,source.type);
//		}
//		else {
			return source;
//		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected HighlightItem<CreoleLexema>[] parseString(final String text) {
		try(final StringWriter	writer = new StringWriter()){
			try(final CreoleWriter	cre = new CreoleWriter(writer,MarkupOutputFormat.PARSEDCSV)) {
				cre.write(text+(text.endsWith("\n") ? "" : "\n"));
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
