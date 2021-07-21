package chav1961.purelib.ui.swing.interfaces;

import javax.swing.Icon;

@FunctionalInterface
public interface IconKeeper {
	Icon getIcon();
	
	default Icon getDisabledIcon() {
		return getIcon();
	}
}
