package chav1961.purelib.ui.swing.useful;

import java.awt.AWTEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Locale;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import javax.swing.JLabel;
import javax.swing.ToolTipManager;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

/**
 * <p>This class is a placeholder for different purposes. This class supports:</p>
 * <ul>
 * <li>focusing mechanism (gain and loose focus)</li>
 * <li>Drag&amp;Drop ability (implements by {@linkplain DnDManager})</li>
 * <li>Localizable title and tool tip for it's content (implemented by {@linkplain Localizer})</li>
 * </ul> 
 * <p>This class is not thread-safe</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public class JPlaceHolder extends JLabel implements LocaleChangeListener, AutoCloseable {
	private static final long serialVersionUID = 1L;

	private final Localizer		localizer;
	private final String		innerText, innerTooltip;
	private final BiPredicate<Transferable, DataFlavor[]>	support;
	
	public JPlaceHolder(final Localizer localizer, final String innerText, final String innerTooltip, final BiPredicate<Transferable, DataFlavor[]> support) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (Utils.checkEmptyOrNullString(innerText)) {
			throw new IllegalArgumentException("Inner text can't be null or empty");
		}
		else if (support == null) {
			throw new NullPointerException("Data flavor accept predicate can't be null");
		}
		else {
			setFocusable(true);
            enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK | AWTEvent.INPUT_METHOD_EVENT_MASK);

            this.localizer = localizer;
			this.innerText = innerText;
			this.innerTooltip = innerTooltip;
			this.support = support;
			
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final MouseEvent e) {
					requestFocusInWindow();
				}
			});
			ToolTipManager.sharedInstance().registerComponent(this);
			
            new DropTarget(this, DnDConstants.ACTION_COPY, new InternalDropTargetHandler(this, support, this::acceptDrop), true);
			fillLocalizedStrings();
		}
	}

	@Override
	public void close() throws RuntimeException {
		ToolTipManager.sharedInstance().unregisterComponent(this);
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	public boolean acceptDrop(final Transferable trans) throws IOException {
		return support.test(trans, trans.getTransferDataFlavors());
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

	private void fillLocalizedStrings() {
		try {
			setText(localizer.getValue(innerText));
		} catch (LocalizationException exc) {
			setText(innerText);
		}
	}
}
