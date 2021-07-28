package chav1961.purelib.ui.swing.interfaces;

import javax.swing.Icon;

@FunctionalInterface
public interface IconKeeper {
	public enum IconSize {
		ICON_DEFAULT,
		ICON16x16,
		ICON64x64;
	}
	
	Icon getIcon();

	default Icon getIcon(final IconSize size) {
		return getIcon();
	}
	
	default Icon getDisabledIcon() {
		return getIcon();
	}

	default Icon getDisabledIcon(final IconSize size) {
		return getIcon();
	}
}
