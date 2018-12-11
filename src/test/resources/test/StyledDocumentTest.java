package test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class StyledDocumentTest extends JFrame {
	private static final long 		serialVersionUID = 1L;
	
	private final StyledDocument	doc = new DefaultStyledDocument();
	private final JTextPane			area = new JTextPane(); 
	
	public StyledDocumentTest() throws BadLocationException {
		super("Test");
		
		area.setDocument(doc);

		final Style 				style = doc.addStyle("StyleName", null);
		
		StyleConstants.setForeground(style,Color.RED);
		StyleConstants.setBackground(style,Color.YELLOW);
		StyleConstants.setFontSize(style,20);
		
		doc.insertString(0,"test",style);
		
		getContentPane().add(area,BorderLayout.CENTER);
		setSize(new Dimension(300, 150));
	}
	
	public static void main(String[] args) throws BadLocationException {
		new StyledDocumentTest().setVisible(true);
	}
}
