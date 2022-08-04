package chav1961.purelib.ui.swing.useful;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.Position.Bias;

import chav1961.purelib.basic.Utils;

public class PrimitiveEditorKit extends EditorKit {
	private static final long serialVersionUID = 1L;

	@FunctionalInterface
	static interface ThrowableActionListener {
		void actionPerformed(ActionEvent e) throws BadLocationException;
	}
	
	private static final KeyStroke	KS_LEFT = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
	private static final KeyStroke	KS_RIGHT = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
	private static final KeyStroke	KS_HOME = KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0);
	private static final KeyStroke	KS_END = KeyStroke.getKeyStroke(KeyEvent.VK_END, 0);
	private static final KeyStroke	KS_LEFT_SEL = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK);
	private static final KeyStroke	KS_RIGHT_SEL = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK);
	private static final KeyStroke	KS_HOME_SEL = KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.SHIFT_DOWN_MASK);
	private static final KeyStroke	KS_END_SEL = KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.SHIFT_DOWN_MASK);
	private static final KeyStroke	KS_DEL = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
	private static final KeyStroke	KS_BKSP = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
	private static final KeyStroke	KS_CUT = KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK);
	private static final KeyStroke	KS_COPY = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
	private static final KeyStroke	KS_PASTE = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK);
	private static final Action[]	ACTIONS = new Action[] {
											new PrimitiveAction("caret-backward", KS_LEFT, (e)->((PrimitiveDocument)e.getSource()).left()),
											new PrimitiveAction("caret-forward", KS_RIGHT, (e)->((PrimitiveDocument)e.getSource()).right()),
											new PrimitiveAction("caret-begin", KS_HOME, (e)->((PrimitiveDocument)e.getSource()).home()),
											new PrimitiveAction("caret-end", KS_END, (e)->((PrimitiveDocument)e.getSource()).end()),
											new PrimitiveAction("selection-backward", KS_LEFT_SEL, (e)->((PrimitiveDocument)e.getSource()).leftSel()),
											new PrimitiveAction("selection-forward", KS_RIGHT_SEL, (e)->((PrimitiveDocument)e.getSource()).rightSel()),
											new PrimitiveAction("selection-begin", KS_HOME_SEL, (e)->((PrimitiveDocument)e.getSource()).homeSel()),
											new PrimitiveAction("selection-end", KS_END_SEL, (e)->((PrimitiveDocument)e.getSource()).endSel()),
											new PrimitiveAction("delete-next", KS_DEL, (e)->((PrimitiveDocument)e.getSource()).del()),
											new PrimitiveAction("delete-previous", KS_BKSP, (e)->((PrimitiveDocument)e.getSource()).bksp()),
											new PrimitiveAction("cut-to-clipboard", KS_CUT, (e)->((PrimitiveDocument)e.getSource()).cut()),
											new PrimitiveAction("copy-to-clipboard", KS_COPY, (e)->((PrimitiveDocument)e.getSource()).copy()),
											new PrimitiveAction("paste-from-clipboard", KS_PASTE, (e)->((PrimitiveDocument)e.getSource()).paste()),
										};

	private final PrimitiveDocument	doc = new PrimitiveDocument();
	private final Caret				caret = new DefaultCaret();
	
	public PrimitiveEditorKit() {
	}
	
	@Override
	public String getContentType() {
		return "text/plain";
	}

	@Override
	public ViewFactory getViewFactory() {
		// TODO Auto-generated method stub
		return new ViewFactory() {
			
			@Override
			public View create(final Element elem) {
				// TODO Auto-generated method stub
				return new View(elem) {

					@Override
					public float getPreferredSpan(int axis) {
						return 0;
					}

					@Override
					public void paint(Graphics g, final Shape allocation) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public Shape modelToView(int pos, Shape a, Bias b) throws BadLocationException {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public int viewToModel(float x, float y, Shape a, Bias[] biasReturn) {
						// TODO Auto-generated method stub
						return 0;
					}
				};
			}
		};
	}

	@Override
	public Action[] getActions() {
		return ACTIONS;
	}

	@Override
	public Caret createCaret() {
		return caret;
	}

	@Override
	public Document createDefaultDocument() {
		return doc;
	}

	@Override
	public void read(InputStream in, Document doc, int pos) throws IOException, BadLocationException {
		read(new InputStreamReader(in), doc, pos);
	}

	@Override
	public void write(OutputStream out, Document doc, int pos, int len) throws IOException, BadLocationException {
		write(new OutputStreamWriter(out), doc, pos, len);
	}

	@Override
	public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
		doc.insertString(pos, Utils.fromResource(in), null);
	}

	@Override
	public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
		out.write(doc.getText(pos, len));
		out.flush();
	}
}