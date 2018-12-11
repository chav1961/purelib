package chav1961.purelib.ui.swing;

import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.JToolTip;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

class LocalizedToolBar extends JToolBar implements LocaleChangeListener {
	private static final long 			serialVersionUID = -147983553667717676L;

	protected final Localizer			localizer;
	private final Map<String,JButton>	mappedButtons = new HashMap<>();
	
	LocalizedToolBar(final Localizer localizer) throws LocalizationException, IllegalArgumentException {
		this.localizer = localizer;
		setFloatable(false);
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
//		for (Entry<String, JButton> item : mappedButtons.entrySet()) {
//			item.getValue().setToolTipText(localizer.getValue(item.getKey()));
//		}
	}

	@Override
	public JToolTip createToolTip() {
		return new SmartToolTip(localizer,this);
	}
	
	protected void add(final JButton button, final String tooltipId) {
		super.add(button);
		mappedButtons.put(tooltipId,button);
	}
	
	protected JButton createButton(final Action action, final URL grayURL, final URL whiteURL, final String tooltipId) throws LocalizationException, IllegalArgumentException {
		return createButton(action,"",grayURL,whiteURL,tooltipId);
	}

	protected JButton createButton(final Action action, final String actionCommand, final URL grayURL, final URL whiteURL, final String tooltipId) throws LocalizationException, IllegalArgumentException {
		return createButton(action,actionCommand,grayURL,whiteURL,grayURL,whiteURL,tooltipId);
	}

	protected JButton createButton(final Action action, final URL grayURL, final URL whiteURL, final URL graySelectedURL, final URL whiteSelectedURL, final String tooltipId) throws LocalizationException, IllegalArgumentException {
		return createButton(action,"",grayURL,whiteURL,graySelectedURL,whiteSelectedURL,tooltipId);
	}
	
	protected JButton createButton(final Action action, final String actionCommand, final URL grayURL, final URL whiteURL, final URL graySelectedURL, final URL whiteSelectedURL, final String tooltipId) throws LocalizationException, IllegalArgumentException {
		final JButton	button = new StyledButton(localizer,tooltipId,whiteURL,grayURL,whiteSelectedURL,graySelectedURL);
		
		button.setFocusable(false);
		
		if (action != null) {
			button.addActionListener(action);
		}
		button.setActionCommand(actionCommand);
		return button;
	}
}