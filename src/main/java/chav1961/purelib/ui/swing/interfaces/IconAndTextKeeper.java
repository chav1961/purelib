package chav1961.purelib.ui.swing.interfaces;

public interface IconAndTextKeeper extends IconKeeper {
	String getText();
	String getToolTipText();
	
	default String getToolTipText(int x, int y) {
		return getToolTipText();
	}
}
