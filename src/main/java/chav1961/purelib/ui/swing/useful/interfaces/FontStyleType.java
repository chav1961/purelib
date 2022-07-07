package chav1961.purelib.ui.swing.useful.interfaces;

import java.awt.Font;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.purelib.ui.swing.useful.interfaces.FontStyleType/chav1961/purelib/i18n/localization.xml")
public enum FontStyleType {
	@LocaleResource(value="chav1961.purelib.ui.swing.useful.interfaces.FontStyleType.plain",tooltip="chav1961.purelib.ui.swing.useful.interfaces.FontStyleType.plain.tt")
	PLAIN(Font.PLAIN),
	@LocaleResource(value="chav1961.purelib.ui.swing.useful.interfaces.FontStyleType.bold",tooltip="chav1961.purelib.ui.swing.useful.interfaces.FontStyleType.bold.tt")
	BOLD(Font.BOLD),
	@LocaleResource(value="chav1961.purelib.ui.swing.useful.interfaces.FontStyleType.italic",tooltip="chav1961.purelib.ui.swing.useful.interfaces.FontStyleType.italic.tt")
	ITALIC(Font.ITALIC),
	@LocaleResource(value="chav1961.purelib.ui.swing.useful.interfaces.FontStyleType.bolditalic",tooltip="chav1961.purelib.ui.swing.useful.interfaces.FontStyleType.bolditalic.tt")
	BOLD_ITALIC(Font.BOLD | Font.ITALIC);
	
	private final int	style;
	
	private FontStyleType(final int style) {
		this.style = style;
	}
	
	public int getFontStyle() {
		return style;
	}
}

