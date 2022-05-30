package chav1961.purelib.ui.swing.useful;

import java.awt.Component;

import javax.swing.JTabbedPane;

public class JCloseableTabbedPane extends JTabbedPane implements AutoCloseable {
	private static final long serialVersionUID = 4857065198521444125L;

	public JCloseableTabbedPane() {
		
	}
	
	@Override
	public void close() throws RuntimeException {
		while (getTabCount() > 0) {
			final Component	component = getTabComponentAt(0);
			
			if (component instanceof AutoCloseable) {
				try{((AutoCloseable)component).close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			removeTabAt(0);
		}
	}
}
