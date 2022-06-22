package chav1961.purelib.ui.interfaces;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import chav1961.purelib.ui.inner.InternalConstants;
import chav1961.purelib.ui.swing.interfaces.IconKeeper;

public enum PureLibStandardIcons implements IconKeeper {
	DIRECTORY(InternalConstants.ICON_DIRECTORY),
	DIRECTORY_OPENED(InternalConstants.ICON_DIRECTORY_OPENED),
	FILE(InternalConstants.ICON_FILE),
	LARGE_DIRECTORY(InternalConstants.ICON_LARGE_DIRECTORY),
	LARGE_DIRECTORY_OPENED(InternalConstants.ICON_LARGE_DIRECTORY_OPENED),
	LARGE_FILE(InternalConstants.ICON_LARGE_FILE),
	SUCCESS(InternalConstants.ICON_SUCCESS),
	FAIL(InternalConstants.ICON_FAIL),
	ACCEPT(InternalConstants.ICON_SUCCESS),
	CANCEL(InternalConstants.ICON_FAIL),
	CLOSE(InternalConstants.ICON_CLOSE),
	NEW_DIR(InternalConstants.ICON_NEW_DIR),
	REMOVE(InternalConstants.ICON_CLOSE),
	LEVEL_UP(InternalConstants.ICON_LEVEL_UP);
	
	private final Icon	icon;
	private final Icon	disabledIcon;
	
	private PureLibStandardIcons(final ImageIcon icon) {
		this.icon = icon;
		this.disabledIcon = new ImageIcon(GrayFilter.createDisabledImage(icon.getImage()));
	}
	
	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public Icon getDisabledIcon() {
		return disabledIcon;
	}
}