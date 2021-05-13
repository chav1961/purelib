package chav1961.purelib.ui.swing.useful;

import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class JCloseButton extends JButton implements LocaleChangeListener {
	private static final long serialVersionUID = 5015631943131172836L;
	
	private static final Icon	DELETE_ICON = new ImageIcon(JCloseButton.class.getResource("delete.png"));

	private final Localizer		localizer;
	
	public JCloseButton(final Localizer localizer, final ActionListener listener) {
		super(DELETE_ICON);
		this.localizer = localizer;
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	private void fillLocalizedStrings() {
		setToolTipText("");
	}
}
