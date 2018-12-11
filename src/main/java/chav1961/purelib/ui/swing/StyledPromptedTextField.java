package chav1961.purelib.ui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import chav1961.purelib.i18n.interfaces.Localizer;

public class StyledPromptedTextField extends JTextField {
	private static final long serialVersionUID = 1L;
	
	private final DocumentListener	documentListener;
	private final Localizer			localizer;
	private final Prompter			prompter;
	private JPrompt					prompt;

	public interface Prompter {
		boolean exists(final String text, final int caretPosition);
		Iterable<String> prompts(final String text, final int caretPosition);
	}
	
	public StyledPromptedTextField(final Localizer localizer, final Prompter prompter) throws NullPointerException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (prompter == null) {
			throw new NullPointerException("Prompter interface can't be null");
		}
		else {
			this.localizer = localizer;			
			this.prompter = prompter;			
	   		this.prompt = new JPrompt(StyledPromptedTextField.this.getDocument(),getDocumentListener());
	   		this.documentListener = new DocumentListener() {
	            @Override
	            public void insertUpdate(DocumentEvent de) {
	            	System.err.println("Insert: "+de.getOffset()+" "+de.getLength());
	            	if (prompter.exists(getText(),de.getOffset()+de.getLength())) {
	                	System.err.println("Show...");
	        			prompt.refill(prompter.prompts(getText(),de.getOffset()+de.getLength()));
	           			prompt.show(StyledPromptedTextField.this,0,StyledPromptedTextField.this.getHeight());
	            	}
	            	else {
	            		prompt.hide();
	            	}
	            }
	
				@Override
	            public void removeUpdate(DocumentEvent de) {
	            	if (prompter.exists(getText(),de.getOffset()+de.getLength())) {
	            		
	            	}
	            }
	
	            @Override
	            public void changedUpdate(DocumentEvent de) {
	            	if (prompter.exists(getText(),de.getOffset()+de.getLength())) {
	            		
	            	}
	            }
	        };
	        getDocument().addDocumentListener(documentListener);
		}
	}
	
	private DocumentListener getDocumentListener() {
		return documentListener;
	}
	
    private static class JPrompt extends JPopupMenu implements ActionListener {
		private static final long serialVersionUID = 1L;

		private final Document			document;
		private final DocumentListener	documentListener;
		
		public JPrompt(final Document doc, final DocumentListener documentListener, final String... items) {
			this.document = doc;
			this.documentListener = documentListener;
	        setLightWeightPopupEnabled(true);
	        setFocusable(false);
	        refill(Arrays.asList(items));
		}

		public void refill(Iterable<String> prompts) {
			removeAll();
	        for (String text : prompts) {
	        	final JMenuItem	item = new JMenuItem(text);
	        
	        	item.addActionListener(this);
	        	item.setActionCommand(text);
	        	add(item);
	        }
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (e.getActionCommand() != null) {
				try{document.removeDocumentListener(documentListener);
					document.insertString(document.getEndPosition().getOffset()-1,e.getActionCommand(),null);
				} catch (BadLocationException e1) {
				} finally {
					document.addDocumentListener(documentListener);
				}
			}
		}
    }
}

