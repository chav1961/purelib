package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import chav1961.purelib.basic.MimeType;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.swing.JToolBarWithMeta;

public class JMultiLineEditor {
	private static final String		KEY_EDITOR_TITLE = "JMultiLineEditor.title";
	private static final float		DEFAULT_SCALE = 0.75f;
	
	private final Localizer			localizer;
	private final MimeType			mimeType;
	private final String			content;
	private final boolean			readOnly;
	private final JToolBarWithMeta	toolbar;
	private final JEditorPane		pane;
	
	public JMultiLineEditor(final Localizer localizer, final MimeType mimeType, final String content) {
		this(localizer, mimeType, content, false, false, new Dimension((int)(DEFAULT_SCALE * Toolkit.getDefaultToolkit().getScreenSize().getWidth()), (int)(DEFAULT_SCALE * Toolkit.getDefaultToolkit().getScreenSize().getHeight())));
	}
	
	public JMultiLineEditor(final Localizer localizer, final MimeType mimeType, final String content, final boolean readOnly, final boolean showToolbar, final Dimension preferredSize) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (mimeType == null) {
			throw new NullPointerException("MIME type can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content string can't be null");
		}
		else if (preferredSize == null) {
			throw new NullPointerException("Preferred size can't be null");
		}
		else {
			this.localizer = localizer;
			this.mimeType = mimeType;
			this.content = content;
			this.readOnly = readOnly;
			
			if (!readOnly && showToolbar) {
				this.toolbar = null;
			}
			else {
				this.toolbar = null;
			}
			this.pane = new JEditorPane(mimeType.toString(), content);
			pane.setEditable(readOnly);
			pane.setPreferredSize(preferredSize);
		}
	}
	
	public boolean show(final JFrame parent) {
		if (readOnly || toolbar == null) {
			final JScrollPane	scroll = new JScrollPane(pane);
			final JDialogContainer<Object,Enum<?>,Component>	dialog = new JDialogContainer<Object,Enum<?>,Component>(localizer, parent, KEY_EDITOR_TITLE, scroll);

			if (dialog.showDialog()) {
				scroll.remove(pane);
				return true;
			}
			else {
				scroll.remove(pane);
				return false;
			}
		}
		else {
			final JScrollPane	scroll = new JScrollPane(pane);
			final JPanel		panel = new JPanel(new BorderLayout());
			
			panel.add(toolbar, BorderLayout.NORTH);
			panel.add(scroll, BorderLayout.CENTER);
			
			final JDialogContainer<Object,Enum<?>,Component>	dialog = new JDialogContainer<Object,Enum<?>,Component>(localizer, parent, KEY_EDITOR_TITLE, scroll);

			if (dialog.showDialog()) {
				scroll.remove(pane);
				panel.remove(toolbar);
				panel.remove(scroll);
				return true;
			}
			else {
				scroll.remove(pane);
				panel.remove(toolbar);
				panel.remove(scroll);
				return false;
			}
		}
	}
	
	public String getChangedContent() {
		if (readOnly) {
			throw new IllegalStateException("Attempt to get changed content on read-only editor");
		}
		else {
			return pane.getText();
		}
	}
}
