package chav1961.purelib.ui.swing.useful;

import java.util.Locale;

import javax.swing.JOptionPane;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class JLocalizedOptionPane extends JOptionPane implements LocaleChangeListener {
	private static final long serialVersionUID = 1L;

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		
	}

}
