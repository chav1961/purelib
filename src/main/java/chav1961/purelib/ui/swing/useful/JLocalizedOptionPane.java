package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.HeadlessException;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class JLocalizedOptionPane implements LocaleChangeListener {
	private static final long 	serialVersionUID = 1L;
	private static final String	LOCALIZED_PANE_YES = "JLocalizedOptionPane.yesButton";
	private static final String	LOCALIZED_PANE_NO = "JLocalizedOptionPane.noButton";
	private static final String	LOCALIZED_PANE_OK = "JLocalizedOptionPane.okButton";
	private static final String	LOCALIZED_PANE_CANCEL = "JLocalizedOptionPane.cancelButton";

	private final Localizer	localizer;
	private final Icon[]	icons;

	public JLocalizedOptionPane(final Localizer localizer) {
		this(localizer,selectIcons());
	}

	public JLocalizedOptionPane(final Localizer localizer, final Icon[] iconSet) {
		if (localizer== null) {
			throw new NullPointerException("Loclaizer can't be null");
		}
		else if (iconSet == null || iconSet.length != 4) {
			throw new IllegalArgumentException("Icon set can't be null and must contain exactry 4 elements");
		}
		else {
			this.localizer = localizer;
			this.icons = iconSet;
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
	}

	public void message(final Component parent, final Object content, final String title, final int type) throws LocalizationException {
		JOptionPane.showMessageDialog(parent,localizeObject(content,localizer),localizer.getValue(title),type,extractIcon(icons,type));
	}

	public int confirm(final Component parent, final Object content, final String title, final int type, final int options) throws LocalizationException {
		final Object[]	buttons = buildButtons(options,localizer);
		
		return JOptionPane.showOptionDialog(parent,localizeObject(content,localizer),localizer.getValue(title), options, type, extractIcon(icons,type), buttons, buttons[0]);
	}

	private static Icon[] selectIcons() {
		return new Icon[]{(Icon)UIManager.get("OptionPane.errorIcon")
						  ,(Icon)UIManager.get("OptionPane.warningIcon")
						  ,(Icon)UIManager.get("OptionPane.questionIcon")
						  ,(Icon)UIManager.get("OptionPane.informationIcon")};
	}
	
	private static Object localizeObject(final Object source, final Localizer localizer) throws LocalizationException {
		if (source == null) {
			return null;
		}
		else if (source instanceof String) {
			return localizer.getValue((String)source);
		}
		else if (source instanceof LocaleChangeListener) {
			((LocaleChangeListener)source).localeChanged(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
			return source;
		}
		else {
			return source;
		}
	}

	private static Icon extractIcon(final Icon[] icons, final int options) {
		if ((options & JOptionPane.ERROR_MESSAGE) != 0) {
			return icons[0];
		}
		else if ((options & JOptionPane.WARNING_MESSAGE) != 0) {
			return icons[1];
		}
		else if ((options & JOptionPane.QUESTION_MESSAGE) != 0) {
			return icons[2];
		}
		else if ((options & JOptionPane.INFORMATION_MESSAGE) != 0) {
			return icons[3];
		}
		else {
			return null;
		}
	}

	private static Object[] buildButtons(final int options, final Localizer localizer) throws LocalizationException {
		if ((options & JOptionPane.DEFAULT_OPTION) != 0) {
			return new String[]{localizer.getValue(LOCALIZED_PANE_OK)};
		}
		else if ((options & JOptionPane.YES_NO_OPTION) != 0) {
			return new String[]{localizer.getValue(LOCALIZED_PANE_YES),localizer.getValue(LOCALIZED_PANE_NO)};
		}
		else if ((options & JOptionPane.YES_NO_CANCEL_OPTION) != 0) {
			return new String[]{localizer.getValue(LOCALIZED_PANE_YES),localizer.getValue(LOCALIZED_PANE_NO),localizer.getValue(LOCALIZED_PANE_CANCEL)};
		}
		else if ((options & JOptionPane.OK_CANCEL_OPTION) != 0) {
			return new String[]{localizer.getValue(LOCALIZED_PANE_OK),localizer.getValue(LOCALIZED_PANE_CANCEL)};
		}
		else {
			return new String[]{localizer.getValue(LOCALIZED_PANE_OK)};
		}
	}
}
