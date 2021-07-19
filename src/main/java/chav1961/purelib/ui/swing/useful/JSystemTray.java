package chav1961.purelib.ui.swing.useful;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuComponent;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import chav1961.purelib.basic.AbstractLoggerFacade;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.SwingUtils;

public class JSystemTray extends AbstractLoggerFacade implements LocaleChangeListener, AutoCloseable {
	private final LightWeightListenerList<ActionListener>	listeners = new LightWeightListenerList<>(ActionListener.class);
	private final Localizer		localizer;
	private final Image			image;
	private final String		toolTip;
	private final JPopupMenu	popup;
	private final TrayIcon		icon;
	
	public JSystemTray(final Localizer localizer, final URI image) throws EnvironmentException, NullPointerException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (image == null) {
			throw new NullPointerException("Tray icon image URI can't be null");
		}
		else if (!SystemTray.isSupported()) {
			throw new EnvironmentException("System tray can't be used in current OS environment"); 
		}
		else {
			this.localizer = localizer;
			this.image = loadImage(image);
			this.toolTip = null;
			this.popup = null;
			this.icon = new TrayIcon(this.image);
			this.icon.addActionListener((e)->fireAction(e));
			fillLocalizedStrings();
			
			try{SystemTray.getSystemTray().add(icon);
			} catch (AWTException e) {
				throw new EnvironmentException("Tray icon placing failure: "+e.getLocalizedMessage()); 
			}
		}
	}

	public JSystemTray(final Localizer localizer, final URI image, final String tooltip) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (image == null) {
			throw new NullPointerException("Tray icon image URI can't be null");
		}
		else if (tooltip == null || tooltip.isEmpty()) {
			throw new IllegalArgumentException("Tooltip string can't be null or empty");
		}
		else if (!localizer.containsKey(tooltip)) {
			throw new IllegalArgumentException("Tooltip string id ["+tooltip+"] is unknown in the localizer ["+localizer.getLocalizerId()+"]");
		}
		else if (!SystemTray.isSupported()) {
			throw new EnvironmentException("System tray can't be used in current OS environment"); 
		}
		else {
			this.localizer = localizer;
			this.image = loadImage(image);
			this.toolTip = tooltip;
			this.popup = null;
			this.icon = new TrayIcon(this.image);
			this.icon.addActionListener((e)->fireAction(e));
			fillLocalizedStrings();
			
			try{SystemTray.getSystemTray().add(icon);
			} catch (AWTException e) {
				throw new EnvironmentException("Tray icon placing failure: "+e.getLocalizedMessage()); 
			}
		}
	}
	
	public JSystemTray(final Localizer localizer, final URI image, final String tooltip, final JPopupMenu menu) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (image == null) {
			throw new NullPointerException("Tray icon image URI can't be null");
		}
		else if (tooltip == null || tooltip.isEmpty()) {
			throw new IllegalArgumentException("Tooltip string can't be null or empty");
		}
		else if (menu == null) {
			throw new NullPointerException("Popup menu can't be null or empty");
		}
		else if (!localizer.containsKey(tooltip)) {
			throw new IllegalArgumentException("Tooltip string id ["+tooltip+"] is unknown in the localizer ["+localizer.getLocalizerId()+"]");
		}
		else if (!SystemTray.isSupported()) {
			throw new EnvironmentException("System tray can't be used in current OS environment"); 
		}
		else {
			this.localizer = localizer;
			this.image = loadImage(image);
			this.toolTip = tooltip;
			this.popup = menu;
			this.icon = new TrayIcon(this.image);
			this.icon.addActionListener((e)->fireAction(e));
			fillLocalizedStrings();
			
			try{SystemTray.getSystemTray().add(icon);
			} catch (AWTException e) {
				throw new EnvironmentException("Tray icon placing failure: "+e.getLocalizedMessage()); 
			}
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public void close() throws RuntimeException {
		SystemTray.getSystemTray().remove(icon);
	}

	public void addActionListener(final ActionListener l) {
		if (l == null) {
			throw new NullPointerException("Action listener to add can't be null");
		}
		else {
			listeners.addListener(l);
		}
	}
	
	public void removeActionListener(final ActionListener l) {
		if (l == null) {
			throw new NullPointerException("Action listener to remove can't be null");
		}
		else {
			listeners.removeListener(l);
		}
	}

	@Override
	protected AbstractLoggerFacade getAbstractLoggerFacade(final String mark, final Class<?> root) {
		return this;
	}

	@Override
	protected void toLogger(final Severity level, final String text, final Throwable throwable) {
		switch (level) {
			case info	:
				icon.displayMessage("???", text, MessageType.INFO);
				break;
			case error	: case severe	:
				icon.displayMessage("???", text, MessageType.ERROR);
				break;
			case tooltip:
				break;
			case debug	: case trace	:
				icon.displayMessage("???", text, MessageType.NONE);
				break;
			case warning:
				icon.displayMessage("???", text, MessageType.WARNING);
				break;
			default	:
				throw new UnsupportedOperationException("Severity level ["+level+"] is not supported yet");
		}
	}
	
	
	private Image loadImage(final URI image) throws EnvironmentException {
		try{return ImageIO.read(image.toURL());
		} catch (IOException e) {
			throw new EnvironmentException("I/O error loading tray image ["+image+"]: "+e.getLocalizedMessage(),e); 
		} 
	}

	private void fireAction(final ActionEvent e) {
		listeners.fireEvent((item)->item.actionPerformed(e));
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		if (popup != null) {
			final PopupMenu	localPopup = new PopupMenu();
			
			jMenu2Menu(popup,localPopup,(e)->{((JMenuItem)SwingUtils.findComponentByName(popup,e.getActionCommand())).doClick();});
			icon.setPopupMenu(localPopup);
		}
		if (toolTip != null) {
			icon.setToolTip(localizer.getValue(toolTip));
		}
	}

	private void jMenu2Menu(final JPopupMenu popup, final PopupMenu localPopup, final ActionListener listener) {
		final List<MenuComponent>	stack = new ArrayList<>();
		
		SwingUtils.walkDown(popup,(mode,node)->{
			final ContentNodeMetadata 	meta = node instanceof NodeMetadataOwner ? ((NodeMetadataOwner)node).getNodeMetadata() : null;
			
			try {
				switch (mode) {
					case ENTER	:
						if (node instanceof JPopupMenu) {
							stack.add(0,localPopup);
						}
						else if (node instanceof JMenu) {
							final Menu 	menu = new Menu(localizer.getValue(meta.getLabelId()));
							
							if (stack.get(0) instanceof Menu) {
								((Menu)stack.get(0)).add(menu);
							}
							else {
								((PopupMenu)stack.get(0)).add(menu);
							}
							stack.add(0,menu);
						}
						else if (node instanceof JMenuItem) {
							final MenuItem 	menu = new MenuItem(localizer.getValue(meta.getLabelId()));
							
							if (stack.get(0) instanceof Menu) {
								((Menu)stack.get(0)).add(menu);
							}
							else {
								((PopupMenu)stack.get(0)).add(menu);
							}
							menu.addActionListener(listener);
							menu.setActionCommand(node.getName());
						}
						else if (node instanceof JSeparator) {
							if (stack.get(0) instanceof Menu) {
								((Menu)stack.get(0)).addSeparator();
							}
							else {
								((PopupMenu)stack.get(0)).addSeparator();
							}
						}
						break;
					case EXIT	:
						if ((node instanceof JPopupMenu) || (node instanceof JMenu)) {
							stack.remove(0);
						}
						break;
					default	:
						throw new UnsupportedOperationException("Node enter mode ["+mode+"] is not supported yet");
				}
				return ContinueMode.CONTINUE;
			} catch (HeadlessException | LocalizationException e) {
				return ContinueMode.STOP;
			}
		});
	}
}
