package chav1961.purelib.ui.swing.useful;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

public class JCloseableTab extends JLabel {
	private static final long serialVersionUID = -5601021193645267745L;

	public JCloseableTab() {
		super();
	}

	public JCloseableTab(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
	}

	public JCloseableTab(Icon image) {
		super(image);
	}

	public JCloseableTab(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
	}

	public JCloseableTab(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	public JCloseableTab(String text) {
		super(text);
	}
	
	public void associate(final JTabbedPane container, final Component tab) {
		if (container == null) {
		}
		setFocusable(true);
	}
}
