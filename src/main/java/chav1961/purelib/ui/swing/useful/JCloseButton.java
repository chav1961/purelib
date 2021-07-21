package chav1961.purelib.ui.swing.useful;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.border.LineBorder;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.interfaces.PureLibStandardIcons;

public class JCloseButton extends JButton implements LocaleChangeListener {
	private static final long serialVersionUID = 5015631943131172836L;
	
	private static final Icon	DELETE_ICON = PureLibStandardIcons.REMOVE.getIcon();
	private static final String	BUTTON_TOOLTIP = "JCloseButton.tt";

	private final Localizer		localizer;
	
	public JCloseButton(final Localizer localizer, final ActionListener listener) throws NullPointerException, LocalizationException {
		super(DELETE_ICON);
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (listener == null) {
			throw new NullPointerException("Action listener can't be null");
		}
		else {
			this.localizer = localizer;
			setPreferredSize(new Dimension(DELETE_ICON.getIconWidth(),DELETE_ICON.getIconHeight()));
			setBorder(null);
			addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseClicked(MouseEvent e) {}
				@Override
				public void mouseEntered(MouseEvent e) {
					setBorder(new LineBorder(Color.DARK_GRAY));
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					setBorder(null);
				}
			});
			addActionListener(listener);
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	private void fillLocalizedStrings() throws LocalizationException{
		setToolTipText(localizer.getValue(BUTTON_TOOLTIP));
	}
}
