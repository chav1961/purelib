package chav1961.purelib.ui.swing.useful;

import java.awt.AWTEvent;
import java.awt.Component;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.ToolTipManager;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.useful.DnDManager.DnDInterface;
import chav1961.purelib.ui.swing.useful.DnDManager.DnDMode;

/**
 * <p>This class is a placeholder for different purposes. This class supports:</p>
 * <ul>
 * <li>focusing mechanism (gain and loose focus)</li>
 * <li>Drag&Drop ability (implements by {@linkplain DnDManager})</li>
 * <li>Localizable title and tool tip for it's content (implemented by {@linkplain Localizer})</li>
 * </ul> 
 * <p>This class is not thread-safe</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @since 0.0.7
 */
public class JPlaceHolder extends JLabel implements LocaleChangeListener, DnDInterface, AutoCloseable {
	private static final long serialVersionUID = 1L;

	private final Localizer		localizer;
	private final DnDManager	mgr = new DnDManager(this, this);
	private final String		innerText, innerTooltip;
	
	public JPlaceHolder(final Localizer localizer, final String innerText, final String innerTooltip) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (Utils.checkEmptyOrNullString(innerText)) {
			throw new IllegalArgumentException("Inner text can't be null or empty");
		}
		else {
			setFocusable(true);
            enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK | AWTEvent.INPUT_METHOD_EVENT_MASK);

            this.localizer = localizer;
			this.innerText = innerText;
			this.innerTooltip = innerTooltip;
			
			ToolTipManager.sharedInstance().registerComponent(this);
			mgr.selectDnDMode(DnDMode.NONE);
			fillLocalizedStrings();
		}
	}

	@Override
	public void close() throws RuntimeException {
		ToolTipManager.sharedInstance().unregisterComponent(this);
		mgr.close();
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public Class<?> getSourceContentClass(final DnDMode currentMode, final Component component, final int x, final int y) {
		return null;
	}

	@Override
	public Object getSourceContent(final DnDMode currentMode, final Component from, final int xFrom, final int yFrom, final Component to, final int xTo, final int yTo) {
		return null;
	}

	@Override
	public boolean canReceive(final DnDMode currentMode, final Component from, final int xFrom, final int yFrom, final Component to, final int xTo, final int yTo, final Class<?> contentClass) {
		return false;
	}

	@Override
	public void track(final DnDMode currentMode, final Component from, final int xFromAbsolute, final int yFromAbsolute, final Component to, final int xToAbsolute, final int yToAbsolute) {
	}

	@Override
	public void complete(final DnDMode currentMode, final Component from, final int xFrom, final int yFrom, final Component to, final int xTo, final int yTo, final Object content) {
	}

	@Override
	public String getToolTipText() {
		if (Utils.checkEmptyOrNullString(innerTooltip)) {
			return innerTooltip;
		}
		else {
			try {
				return localizer.getValue(innerTooltip);
			} catch (LocalizationException exc) {
				return innerTooltip;
			}
		}
	}

	/**
	 * <p>Get Drag&Drop manager associated with the placeholder</p>
	 * @return manager associated. Can't be null
	 */
	public DnDManager getDnDManager() {
		return mgr;
	}
	
	private void fillLocalizedStrings() {
		try {
			setText(localizer.getValue(innerText));
		} catch (LocalizationException exc) {
			setText(innerText);
		}
	}
}
