package chav1961.purelib.ui.swing.useful;


import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
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
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import chav1961.purelib.basic.AbstractLoggerFacade;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;

/**
 * <p>This class is a wrapper for {@linkplain SystemTray} class. It supports {@linkplain LoggerFacade} interface to print messages and all localization features
 * supported by {@linkplain Localizer} class. You also can assign any pop-up menu for it to use inside the system tray</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public class JSystemTray extends AbstractLoggerFacade implements LocaleChangeListener, AutoCloseable {
	private final LightWeightListenerList<ActionListener>	listeners = new LightWeightListenerList<>(ActionListener.class);
	private final Localizer		localizer;
	private final String		applicationName;
	private final Image			image;
	private final String		toolTip;
	private final JPopupMenu	popup;
	private final TrayIcon		icon;
	private final boolean		onlyEn;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to sue. Can't be null
	 * @param applicationName application name to show in the display message title. Can't be null or empty and must be valid localization key in the localizer passed 
	 * @param image icon image reference inside the system tray. Can't be null. Preferred size of the icon is 16*16px. 
	 * @throws EnvironmentException if operation system doesn't support system trays 
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException application name is null or empty
	 * @see TrayIcon#displayMessage(String, String, MessageType)
	 */
	public JSystemTray(final Localizer localizer, final String applicationName, final URI image) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (applicationName == null || applicationName.isEmpty()) {
			throw new IllegalArgumentException("Application name can't be null or empty");
		}
		else if (image == null) {
			throw new NullPointerException("Tray icon image URI can't be null");
		}
		else if (!SystemTray.isSupported()) {
			throw new EnvironmentException("System tray can't be used in current OS environment"); 
		}
		else {
			this.localizer = localizer;
			this.applicationName = applicationName;
			this.image = loadImage(image);
			this.toolTip = null;
			this.popup = null;
			this.icon = new TrayIcon(this.image);
			this.icon.addActionListener((e)->fireAction(e));
			this.onlyEn = false;
			fillLocalizedStrings();
			
			try{SystemTray.getSystemTray().add(icon);
			} catch (AWTException e) {
				throw new EnvironmentException("Tray icon placing failure: "+e.getLocalizedMessage()); 
			}
		}
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to sue. Can't be null
	 * @param applicationName application name to show in the display message title. Can't be null or empty and must be valid localization key in the localizer passed 
	 * @param image icon image reference inside the system tray. Can't be null. Preferred size of the icon is 16*16px. 
	 * @param tooltip tooltip inside system tray. Can't be null or empty and must be valid localization key in the localizer passed
	 * @throws EnvironmentException if operation system doesn't support system trays 
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException application name or tooltip is null or empty
	 * @see TrayIcon#displayMessage(String, String, MessageType)
	 */
	public JSystemTray(final Localizer localizer, final String applicationName, final URI image, final String tooltip) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (applicationName == null || applicationName.isEmpty()) {
			throw new IllegalArgumentException("Application name can't be null or empty");
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
			this.applicationName = applicationName;
			this.image = loadImage(image);
			this.toolTip = tooltip;
			this.popup = null;
			this.icon = new TrayIcon(this.image);
			this.icon.addActionListener((e)->fireAction(e));
			this.onlyEn = false;
			fillLocalizedStrings();
			
			try{SystemTray.getSystemTray().add(icon);
			} catch (AWTException e) {
				throw new EnvironmentException("Tray icon placing failure: "+e.getLocalizedMessage()); 
			}
		}
	}

	/**
	 * <p>Constructor of the class.</p>
	 * @param localizer localizer to sue. Can't be null
	 * @param applicationName application name to show in the display message title. Can't be null or empty and must be valid localization key in the localizer passed 
	 * @param image icon image reference inside the system tray. Can't be null. Preferred size of the icon is 16*16px. 
	 * @param tooltip tooltip inside system tray. Can't be null or empty and must be valid localization key in the localizer passed
	 * @param menu pop-up menu to show on right click on the icon inside the system tray. Can't be null. Note that not all menu options are supported (icons, radio button menus etc) because of operation system restrictions
	 * @param onlyEn use only english localization to show pop-up menu (due to operation system restrictions)
	 * @throws EnvironmentException if operation system doesn't support system trays 
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException application name or tooltip is null or empty
	 * @see TrayIcon#displayMessage(String, String, MessageType)
	 */
	public JSystemTray(final Localizer localizer, final String applicationName, final URI image, final String tooltip, final JPopupMenu menu, final boolean onlyEn) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (applicationName == null || applicationName.isEmpty()) {
			throw new IllegalArgumentException("Application name can't be null or empty");
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
			this.applicationName = applicationName;
			this.image = loadImage(image);
			this.toolTip = tooltip;
			this.popup = menu;
			this.icon = new TrayIcon(this.image);
			this.icon.addActionListener((e)->fireAction(e));
			this.onlyEn = onlyEn;
			fillLocalizedStrings();
			
			try{
				SystemTray.getSystemTray().add(icon);
			} catch (AWTException e) {
				throw new EnvironmentException("Tray icon placing failure: "+e.getLocalizedMessage()); 
			}
		}
	}

	@Override
	public boolean canServe(final URI resource) throws NullPointerException {
		return false;
	}

	@Override
	public LoggerFacade newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		return null;
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		SwingUtils.walkDown(popup, (mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				if (node instanceof LocaleChangeListener) {
					((LocaleChangeListener)node).localeChanged(oldLocale, newLocale);
				}
			}
			return ContinueMode.CONTINUE;
		});
	}

	@Override
	public void close() throws RuntimeException {
		if (!isInTransaction()) {
			SystemTray.getSystemTray().remove(icon);
		}
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
		return new AbstractLoggerFacade() {
			private final List<TransactionMessage>	list = new ArrayList<>();
			
			@Override public LoggerFacade newInstance(URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {return JSystemTray.this.newInstance(resource);}
			@Override public boolean canServe(URI resource) throws NullPointerException {return JSystemTray.this.canServe(resource);}
			@Override protected AbstractLoggerFacade getAbstractLoggerFacade(String mark, Class<?> root) {return JSystemTray.this.getAbstractLoggerFacade(mark, root);}
			
			@Override 
			protected void toLogger(Severity level, String text, Throwable throwable) {
				list.add(new TransactionMessage(level, text, throwable));
			}
			
			@Override
			public void rollback() {
				super.rollback();
				list.clear();
			}
			
			@Override
			public void close() {
				for (TransactionMessage item : list) {
					JSystemTray.this.toLogger(item.level, item.text, item.exception);
				}
				super.close();
			}
		};
	}

	@Override
	protected synchronized void toLogger(final Severity level, final String text, final Throwable throwable) {
		switch (level) {
			case info	: case note		:
				icon.displayMessage(getLocalizedString(applicationName), getLocalizedString(text), MessageType.INFO);
				break;
			case error	: case severe	:
				icon.displayMessage(getLocalizedString(applicationName), getLocalizedString(text), MessageType.ERROR);
				break;
			case tooltip:
				break;
			case debug	: case trace	:
				icon.displayMessage(getLocalizedString(applicationName), getLocalizedString(text), MessageType.NONE);
				break;
			case warning:
				icon.displayMessage(getLocalizedString(applicationName), getLocalizedString(text), MessageType.WARNING);
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
			
			jMenu2Menu(popup,localPopup
					,(e)->((JMenuItem)SwingUtils.findComponentByName(popup,e.getActionCommand())).doClick()
					,(e)->((JCheckBoxMenuItem)SwingUtils.findComponentByName(popup,((CheckboxMenuItem)e.getSource()).getActionCommand())).doClick()
					,onlyEn);
			icon.setPopupMenu(localPopup);
		}
		if (toolTip != null) {
			icon.setToolTip(localizer.getValue(toolTip));
		}
	}

	private void jMenu2Menu(final JPopupMenu popup, final PopupMenu localPopup, final ActionListener actionListener, final ItemListener itemListener, boolean onlyEn2) {
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
							final Menu 					menu = new Menu(translateString(localizer, meta.getLabelId(), onlyEn));
							final ListenerDescriptor	ld = new ListenerDescriptor((JComponent)node,localizer,menu,onlyEn);  
							
							if (stack.get(0) instanceof PopupMenu) {
								((PopupMenu)stack.get(0)).add(menu);
							}
							else if (stack.get(0) instanceof Menu) {
								((Menu)stack.get(0)).add(menu);
							}
							else {
								((PopupMenu)stack.get(0)).add(menu);
							}
							stack.add(0,menu);
							if (node instanceof BooleanPropChangeListenerSource) {
								((BooleanPropChangeListenerSource)node).addBooleanPropChangeListener(ld);
							}
							localizer.addLocaleChangeListener(ld);
						}
						else if ((node instanceof JCheckBoxMenuItem) || (node instanceof JRadioButtonMenuItem)) {
							final CheckboxMenuItem 		menu = new CheckboxMenuItem(translateString(localizer, meta.getLabelId(), onlyEn));
							final ListenerDescriptor	ld = new ListenerDescriptor((JComponent)node,localizer,menu,onlyEn);  

							if (stack.get(0) instanceof PopupMenu) {
								((PopupMenu)stack.get(0)).add(menu);
							}
							else if (stack.get(0) instanceof Menu) {
								((Menu)stack.get(0)).add(menu);
							}
							else {
								((PopupMenu)stack.get(0)).add(menu);
							}
							menu.addItemListener(itemListener);
							menu.setActionCommand(node.getName());
							menu.setState(((JMenuItem)node).isSelected());
							if (node instanceof BooleanPropChangeListenerSource) {
								((BooleanPropChangeListenerSource)node).addBooleanPropChangeListener(ld);
							}
							localizer.addLocaleChangeListener(ld);
						}
						else if (node instanceof JMenuItem) {
							final MenuItem 				menu = new MenuItem(translateString(localizer, meta.getLabelId(), onlyEn));
							final ListenerDescriptor	ld = new ListenerDescriptor((JComponent)node,localizer,menu,onlyEn);  

							if (stack.get(0) instanceof PopupMenu) {
								((PopupMenu)stack.get(0)).add(menu);
							}
							else if (stack.get(0) instanceof Menu) {
								((Menu)stack.get(0)).add(menu);
							}
							else {
								((PopupMenu)stack.get(0)).add(menu);
							}
							menu.addActionListener(actionListener);
							menu.setActionCommand(node.getName());
							if (node instanceof BooleanPropChangeListenerSource) {
								((BooleanPropChangeListenerSource)node).addBooleanPropChangeListener(ld);
							}
							localizer.addLocaleChangeListener(ld);
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

	private String getLocalizedString(final String source) {
		try{return localizer.getValue(source);
		} catch (LocalizationException e) {
			return source;
		}
	}
	
	private static String translateString(final Localizer localizer, final String sourceKey, final boolean onlyEn) {
		try{final String val = localizer.getValue(sourceKey);
		
			if (CharUtils.isASCIIOnly(val.toCharArray(), 0, val.length())) {
				return val; 
			}
			else {
				return localizer.getLocalValue(sourceKey, onlyEn ? Locale.forLanguageTag("en") : Locale.getDefault());
			}
		} catch (LocalizationException exc) {
			return sourceKey;
		}
	}
	
	private static class ListenerDescriptor implements BooleanPropChangeListener, LocaleChangeListener {
		private final JComponent	swingComponent;
		private final Localizer		localizer; 
		private final Object		awtComponent;
		private final boolean		onlyEn;
		
		public ListenerDescriptor(JComponent swingComponent, Localizer localizer, Object awtComponent, boolean onlyEn) {
			this.swingComponent = swingComponent;
			this.localizer = localizer;
			this.awtComponent = awtComponent;
			this.onlyEn = onlyEn;
		}

		@Override
		public void booleanPropChange(final BooleanPropChangeEvent event) {
			switch (event.getEventChangeType()) {
				case SELECTED	:
					if (awtComponent instanceof CheckboxMenuItem) {
						((CheckboxMenuItem)awtComponent).setState(event.getNewValue());
					}
					break;
				case ENABLED : case VISIBILE :
					if (awtComponent instanceof MenuItem) {
						((MenuItem)awtComponent).setEnabled(event.getNewValue());
					}
					else if (awtComponent instanceof PopupMenu) {
						((PopupMenu)awtComponent).setEnabled(event.getNewValue());
					}
					break;
				default:
					throw new UnsupportedOperationException("Event change type ["+event.getEventChangeType()+"] is ot supported yet");
			}
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			if (swingComponent instanceof NodeMetadataOwner) {
				final ContentNodeMetadata 	meta = ((NodeMetadataOwner)swingComponent).getNodeMetadata();
				final String				text = translateString(localizer, meta.getLabelId(), onlyEn);
				
				if (awtComponent instanceof MenuItem) {
					((MenuItem)awtComponent).setLabel(text);
				}
				else if (awtComponent instanceof PopupMenu) {
					((PopupMenu)awtComponent).setLabel(text);
				}
			}
		}
	}
}
