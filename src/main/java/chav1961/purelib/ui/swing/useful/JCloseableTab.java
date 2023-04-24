package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.MalformedURLException;
import java.util.Locale;


import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.PureLibStandardIcons;
import chav1961.purelib.ui.swing.SwingUtils;

/**
 * <p>This class is a tab label with 'close' icon to use as tab label in the {@linkplain JTabbedPane} component. This tab label can also have a popup menu associated with it. To use 
 * this class, create JCloseableTab instance, and then call {@linkplain #associate(JTabbedPane, Component)} or {@linkplain #associate(JTabbedPane, Component, JPopupMenu)} methods
 * before place component into {@linkplain JTabbedPane}. When the 'close' icon will be clicked,  the tab will be removed from the {@linkplain JTabbedPane} automatically. 
 * If tab removed implements {@linkplain AutoCloseable} interface, the {@linkplain AutoCloseable#close()} method will be called at removing.</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @last.update 0.0.7
 */
public class JCloseableTab extends JPanel implements LocaleChangeListener {
	private static final long 	serialVersionUID = -5601021193645267745L;
	
	public static final String	LABEL_NAME = "JCloseableTab.label";
	public static final String	CROSSER_NAME = "JCloseableTab.crosser";
	
	private static final Icon	RED_ICON = PureLibStandardIcons.CLOSE.getIcon();
	private static final Icon	GRAY_ICON = PureLibStandardIcons.CLOSE.getDisabledIcon();

	private final Localizer		localizer;
	private final JLabel		label = new JLabel();
	private final JLabel		crosser = new JLabel(GRAY_ICON);
	private final boolean		closeButtonVisible;
	private String				text, tooltip;
	private JTabbedPane 		container = null;
	private Component 			tab = null;
	private JPopupMenu			popup = null;
	private boolean				closeEnable = true;

	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use for tab text. Can't be null
	 * @param meta label model. Can't be null 
	 */
	public JCloseableTab(final Localizer localizer, final ContentNodeMetadata meta) {
		this(localizer, meta, true);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use for tab text. Can't be null
	 * @param meta label model. Can't be null 
	 * @param closeButtonVisible is close button visible 
	 * @since 0.0.6
	 */
	public JCloseableTab(final Localizer localizer, final ContentNodeMetadata meta, final boolean closeButtonVisible) {
		super(new BorderLayout(2,2));
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (meta == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.text = meta.getLabelId();
			this.tooltip = meta.getTooltipId();
			this.closeButtonVisible = closeButtonVisible;
			if (meta.getIcon() != null) {
				try{
					label.setIcon(new ImageIcon(meta.getIcon().toURL()));
				} catch (MalformedURLException e) {
					SwingUtils.getNearestLogger(this).message(Severity.warning,e,"Icon loading failure: "+e.getLocalizedMessage());
				}
			}
			prepare(meta.getUIPath().toString());
		}
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use for tab text. Can't be null
	 */
	public JCloseableTab(final Localizer localizer) {
		this(localizer, true);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use for tab text. Can't be null
	 * @param closeButtonVisible is close button visible 
	 * @since 0.0.6
	 */
	public JCloseableTab(final Localizer localizer, final boolean closeButtonVisible) {
		super(new BorderLayout(2,2));
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.closeButtonVisible = closeButtonVisible;
			prepare("");
		}
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use for tab text. Can't be null
	 * @param image image at the left part of the label. Can't be null
	 * @param horizontalAlignment label alignment. See {@linkplain JLabel} constants
	 * @see JLabel
	 */
	public JCloseableTab(final Localizer localizer, final Icon image, final int horizontalAlignment) throws LocalizationException {
		this(localizer, image, horizontalAlignment, true);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use for tab text. Can't be null
	 * @param image image at the left part of the label. Can't be null
	 * @param horizontalAlignment label alignment. See {@linkplain JLabel} constants
	 * @param closeButtonVisible is close button visible 
	 * @see JLabel
	 * @since 0.0.6
	 */
	public JCloseableTab(final Localizer localizer, final Icon image, final int horizontalAlignment, final boolean closeButtonVisible) throws LocalizationException {
		super(new BorderLayout(2,2));
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.localizer = localizer;
			label.setIcon(image);
			label.setHorizontalAlignment(horizontalAlignment);
			this.closeButtonVisible = closeButtonVisible;
			prepare("");
		}
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use for tab text. Can't be null
	 * @param image image at the left part of the label. Can't be null
	 * @see JLabel
	 */
	public JCloseableTab(final Localizer localizer, final Icon image) {
		this(localizer, image, true);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use for tab text. Can't be null
	 * @param image image at the left part of the label. Can't be null
	 * @param closeButtonVisible is close button visible 
	 * @see JLabel
	 * @since 0.0.6
	 */
	public JCloseableTab(final Localizer localizer, final Icon image, final boolean closeButtonVisible) {
		super(new BorderLayout(2,2));
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.closeButtonVisible = closeButtonVisible;
			label.setIcon(image);
			prepare("");
		}
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use for tab text. Can't be null
	 * @param text text in the label
	 * @param image image at the left part of the label. Can't be null
	 * @param horizontalAlignment label alignment. See {@linkplain JLabel} constants
	 * @see JLabel
	 */
	public JCloseableTab(final Localizer localizer, final String text, final Icon image, final int horizontalAlignment) {
		this(localizer, text, image, horizontalAlignment, true);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use for tab text. Can't be null
	 * @param text text in the label
	 * @param image image at the left part of the label. Can't be null
	 * @param horizontalAlignment label alignment. See {@linkplain JLabel} constants
	 * @param closeButtonVisible is close button visible 
	 * @see JLabel
	 * @since 0.0.6
	 */
	public JCloseableTab(final Localizer localizer, final String text, final Icon image, final int horizontalAlignment, final boolean closeButtonVisible) {
		super(new BorderLayout(2,2));
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.text = text;
			label.setIcon(image);
			label.setHorizontalAlignment(horizontalAlignment);
			this.closeButtonVisible = closeButtonVisible;
			prepare("");
		}
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use for tab text. Can't be null
	 * @param text text in the label
	 * @param horizontalAlignment label alignment. See {@linkplain JLabel} constants
	 * @see JLabel
	 */
	public JCloseableTab(final Localizer localizer, final String text, final int horizontalAlignment) {
		this(localizer, text, horizontalAlignment, true);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use for tab text. Can't be null
	 * @param text text in the label
	 * @param horizontalAlignment label alignment. See {@linkplain JLabel} constants
	 * @param closeButtonVisible is close button visible 
	 * @see JLabel
	 * @since 0.0.6
	 */
	public JCloseableTab(final Localizer localizer, final String text, final int horizontalAlignment, final boolean closeButtonVisible) {
		super(new BorderLayout(2,2));
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.text = text;
			this.closeButtonVisible = closeButtonVisible;
			label.setHorizontalAlignment(horizontalAlignment);
			prepare("");
		}
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use for tab text. Can't be null
	 * @param text text in the label
	 * @see JLabel
	 */
	public JCloseableTab(final Localizer localizer, final String text) {
		this(localizer, text, true);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use for tab text. Can't be null
	 * @param text text in the label
	 * @param closeButtonVisible is close button visible 
	 * @see JLabel
	 * @since 0.0.6
	 */
	public JCloseableTab(final Localizer localizer, final String text, final boolean closeButtonVisible) {
		super(new BorderLayout(2,2));
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.text = text;
			this.closeButtonVisible = closeButtonVisible;
			prepare("");
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings(oldLocale,newLocale);
	}

	/**
	 * <p>Change text in the label</p>
	 * @param text new text
	 */
	public void setText(final String text) {
		this.text = text;
		if (localizer != null) {
			fillLocalizedStrings(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
		}
	}

	/**
	 * <p>Change icon in the label.</p>
	 * @param icon new icon
	 */
	public void setIcon(final Icon icon) {
		label.setIcon(icon);
	}
	
	@Override
	public void setToolTipText(final String text) {
		this.tooltip = text;
		try{fillLocalizedStrings(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
		} catch (LocalizationException e) {
			super.setToolTipText(text);
		}
	}

	/**
	 * <p>Associate label with tab component and {@linkplain JTabbedPane} container</p>
	 * @param container container where tab will be placed. Can't be null
	 * @param tab tab component will be placed into the container. Can't be null
	 */
	public void associate(final JTabbedPane container, final Component tab) {
		if (container == null) {
			throw new NullPointerException("Tab container can't be null");
		}
		else if (tab == null) {
			throw new NullPointerException("Tab component can't be null");
		}
		else {
			this.container = container;
			this.tab = tab;
			this.popup = null;
		}
	}

	/**
	 * <p>Associate label and popup menu with tab component and {@linkplain JTabbedPane} container</p>
	 * @param container container where tab will be placed. Can't be null
	 * @param tab tab component will be placed into the container. Can't be null
	 * @param popup popup menu associated. Can't be null.
	 */
	public void associate(final JTabbedPane container, final Component tab, final JPopupMenu popup) {
		if (container == null) {
			throw new NullPointerException("Tab container can't be null");
		}
		else if (tab == null) {
			throw new NullPointerException("Tab component can't be null");
		}
		else if (popup == null) {
			throw new NullPointerException("Popup menu can't be null");
		}
		else {
			this.container = container;
			this.tab = tab;
			this.popup = popup;
		}
	}

	/**
	 * <p>Enable/disable 'close' icon in the tab</p>
	 * @param enable enable (true) or disable (false) icon
	 */
	public void setCloseEnable(final boolean enable) {
		closeEnable = enable;
	}

	/**
	 * <p>Is 'close' icon enabled</p>
	 * @return true if yes
	 */
	public boolean isCloseEnable() {
		return closeEnable;
	}

	/**
	 * <p>Close current tab programmatically</p>
	 * @return true if tab was successfully closed
	 */
	public boolean closeTab() {
		boolean result = true;
		
		if (tab instanceof AutoCloseable) {
			try{((AutoCloseable)tab).close();
			} catch (Exception exc) {
				SwingUtils.getNearestLogger(this).message(Severity.error,exc,"Exception on close tab window: "+exc.getLocalizedMessage());
				result = false;
			}
		}
		container.remove(tab);
		container = null;
		tab = null;
		popup = null;
		
		return result;
	}
	
	
	/**
	 * <p>Place component into {@linkplain JTabbedPane} and associate {@linkplain JCloseableTab} label with it</p> 
	 * @param container container to place component into
	 * @param labelId tab id (see {@linkplain JTabbedPane#addTab(String, Component)}). Can't be null or empty
	 * @param tab component to place into container. Can't be null
	 * @param label tab label. Can't be null
	 * @see JTabbedPane#addTab(String, Component)
	 * @since 0.0.6
	 */
	public static void placeComponentIntoTab(final JTabbedPane container, final String labelId, final Component tab, final JCloseableTab label) {
		if (container == null) {
			throw new NullPointerException("Container can't be null");
		}
		else if (labelId == null || labelId.isEmpty()) {
			throw new IllegalArgumentException("Label id can't be null");
		}
		else if (tab == null) {
			throw new NullPointerException("Tab component to add can't be null");
		}
		else if (label == null) {
			throw new NullPointerException("Label can't be null");
		}
		else {
			label.associate(container, tab);
			container.addTab("",tab);
			container.setTabComponentAt(container.getTabCount()-1,label);
			container.setSelectedIndex(container.getTabCount()-1);
		}
	}

	/**
	 * <p>Place component into {@linkplain JTabbedPane} and associate {@linkplain JCloseableTab} label and popup menu with it</p> 
	 * @param container container to place component into
	 * @param labelId tab id (see {@linkplain JTabbedPane#addTab(String, Component)}). Can't be null or empty
	 * @param tab component to place into container. Can't be null
	 * @param label tab label. Can't be null
	 * @param menu popup menu associated with label. Can't be null
	 * @since 0.0.6
	 */
	public static void placeComponentIntoTab(final JTabbedPane container, final String labelId, final Component tab, final JCloseableTab label, final JPopupMenu menu) {
		if (container == null) {
			throw new NullPointerException("Container can't be null");
		}
		else if (labelId == null || labelId.isEmpty()) {
			throw new IllegalArgumentException("Label id can't be null");
		}
		else if (tab == null) {
			throw new NullPointerException("Tab component to add can't be null");
		}
		else if (label == null) {
			throw new NullPointerException("Label can't be null");
		}
		else if (menu == null) {
			throw new NullPointerException("Popup menu can't be null");
		}
		else {
			label.associate(container, tab, menu);
			container.addTab("",tab);
			container.setTabComponentAt(container.getTabCount()-1,label);
			container.setSelectedIndex(container.getTabCount()-1);
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintChildren(g);
	}

	/**
	 * <p>Process click on left icon (if exists). Default is select tab</p>
	 */
	protected void onClickIcon() {
		selectTab();
	}
	
	private void popup() {
		if (popup != null) {
			popup.show(this,getWidth()/2,getHeight()/2);
		}
	}
	
	private void selectTab() {
		if (container != null && tab != null) {
			container.setSelectedComponent(tab);
		}
	}

	private void clickIcon() {
		if (container != null && tab != null) {
			onClickIcon();
		}
	}
	
	
	private void prepare(final String path) throws LocalizationException, IllegalArgumentException {
		final Font	oldFont = getFont(); 
		
		label.setFocusable(true);
		crosser.setVisible(closeButtonVisible);
		crosser.setFocusable(true);
		setOpaque(false);
		label.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			
			@Override 
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					popup();
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				setFont(new Font(oldFont.getFontName(),Font.PLAIN,oldFont.getSize()));
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				setFont(new Font(oldFont.getFontName(),Font.BOLD,oldFont.getSize()));
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (label.getIcon() != null) {
					final Icon	icon = label.getIcon();
					
					if (e.getX() <= icon.getIconWidth() && e.getY() <= icon.getIconHeight()) {
						clickIcon();
					}
					else {
						selectTab();
					}
				}
				else {
					selectTab();
				}
			}
		});
		label.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {}
			@Override public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
					popup();
				}
			}
		});
		crosser.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			
			@Override
			public void mouseExited(MouseEvent e) {
				crosser.setIcon(GRAY_ICON);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				crosser.setIcon(isCloseEnable() ? RED_ICON : GRAY_ICON); 
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (isCloseEnable()) {
					closeTab();
				}
			}
		});
		add(label,BorderLayout.CENTER);
		add(crosser,BorderLayout.EAST);
		if (path != null && !path.isEmpty()) {
			label.setName(path+"/"+LABEL_NAME);
			crosser.setName(path.toString()+"/"+CROSSER_NAME);
		}
		else {
			label.setName(LABEL_NAME);
			crosser.setName(CROSSER_NAME);
		}
		
		fillLocalizedStrings(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
	}

	private void fillLocalizedStrings(final Locale oldLocale, final Locale newLocale) throws LocalizationException, IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(text)) {
			label.setText("");
		}
		else if (localizer.containsKey(text)) {
			label.setText(localizer.getValue(text));
		}
		else {
			label.setText(text);
		}
		if (Utils.checkEmptyOrNullString(tooltip)) {
			label.setToolTipText(null);
		}
		else if (localizer.containsKey(tooltip)) {
			label.setToolTipText(localizer.getValue(tooltip));
		}
		else {
			label.setToolTipText(tooltip);
		}
	}
}
