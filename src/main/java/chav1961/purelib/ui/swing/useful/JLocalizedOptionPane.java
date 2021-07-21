package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class JLocalizedOptionPane implements LocaleChangeListener {
	private static final String	LOCALIZED_PANE_YES = "JLocalizedOptionPane.yesButton";
	private static final String	LOCALIZED_PANE_NO = "JLocalizedOptionPane.noButton";
	private static final String	LOCALIZED_PANE_OK = "JLocalizedOptionPane.okButton";
	private static final String	LOCALIZED_PANE_CANCEL = "JLocalizedOptionPane.cancelButton";

	private final Localizer	localizer;
	private final Icon[]	icons;
	private final boolean	ignoreLocalizationErrros;

	public JLocalizedOptionPane(final Localizer localizer) {
		this(localizer,selectIcons(),false);
	}

	public JLocalizedOptionPane(final Localizer localizer, final boolean ignoreLocalizationErrros) {
		this(localizer,selectIcons(),ignoreLocalizationErrros);
	}
	
	public JLocalizedOptionPane(final Localizer localizer, final Icon[] iconSet) {
		this(localizer,iconSet,false);
	}
	
	public JLocalizedOptionPane(final Localizer localizer, final Icon[] iconSet, final boolean ignoreLocalizationErrros) {
		if (localizer== null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (iconSet == null || iconSet.length != 4) {
			throw new IllegalArgumentException("Icon set can't be null and must contain exactly 4 elements");
		}
		else {
			this.localizer = localizer;
			this.icons = iconSet;
			this.ignoreLocalizationErrros = ignoreLocalizationErrros;
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
	}

	public void message(final Component parent, final Object content, final String title, final int type) throws LocalizationException {
		JOptionPane.showMessageDialog(parent, localizeObject(content,localizer), fromLocalizer(title), type, extractIcon(icons,type));
	}

	public int confirm(final Component parent, final Object content, final String title, final int type, final int options) throws LocalizationException {
		final Object[]	buttons = buildButtons(options,localizer);
		
		return JOptionPane.showOptionDialog(parent, localizeObject(content,localizer), fromLocalizer(title), options, type, extractIcon(icons,type), buttons, buttons[0]);
	}

	private static Icon[] selectIcons() {
		return new Icon[]{(Icon)UIManager.get("OptionPane.errorIcon")
						  ,(Icon)UIManager.get("OptionPane.warningIcon")
						  ,(Icon)UIManager.get("OptionPane.questionIcon")
						  ,(Icon)UIManager.get("OptionPane.informationIcon")};
	}
	
	private Object localizeObject(final Object source, final Localizer localizer) throws LocalizationException {
		if (source == null) {
			return null;
		}
		else if (source instanceof LocalizedFormatter) {
			return ((LocalizedFormatter)source).toString(localizer);
		}
		else if (source instanceof String) {
			return fromLocalizer((String)source);
		}
		else if (source instanceof LocaleChangeListener) {
			((LocaleChangeListener)source).localeChanged(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
			return source;
		}
		else {
			return source;
		}
	}

	private String fromLocalizer(final String source) throws LocalizationException {
		try{return localizer.getValue(source);
		} catch (LocalizationException exc) {
			if (ignoreLocalizationErrros) {
				return source;
			}
			else {
				throw exc;
			}
		}
	}
	
	private static Icon extractIcon(final Icon[] icons, final int options) {
		switch (options) {
			case JOptionPane.ERROR_MESSAGE		: return icons[0];
			case JOptionPane.WARNING_MESSAGE 	: return icons[1];
			case JOptionPane.QUESTION_MESSAGE	: return icons[2];
			case JOptionPane.INFORMATION_MESSAGE: return icons[3];
			default : return null;
		}
	}

	private static Object[] buildButtons(final int options, final Localizer localizer) throws LocalizationException {
		switch (options) {
			case JOptionPane.DEFAULT_OPTION			: return new String[]{localizer.getValue(LOCALIZED_PANE_OK)};
			case JOptionPane.YES_NO_OPTION			: return new String[]{localizer.getValue(LOCALIZED_PANE_YES),localizer.getValue(LOCALIZED_PANE_NO)};
			case JOptionPane.YES_NO_CANCEL_OPTION	: return new String[]{localizer.getValue(LOCALIZED_PANE_YES),localizer.getValue(LOCALIZED_PANE_NO),localizer.getValue(LOCALIZED_PANE_CANCEL)};
			case JOptionPane.OK_CANCEL_OPTION		: return new String[]{localizer.getValue(LOCALIZED_PANE_OK),localizer.getValue(LOCALIZED_PANE_CANCEL)};
			default : return new String[]{localizer.getValue(LOCALIZED_PANE_OK)}; 
		}
	}
}
