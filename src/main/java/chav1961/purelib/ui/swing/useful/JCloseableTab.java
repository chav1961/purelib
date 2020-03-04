package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.SwingUtils;

public class JCloseableTab extends JPanel implements LocaleChangeListener {
	private static final long 	serialVersionUID = -5601021193645267745L;
	
	public static final String	LABEL_NAME = "JCloseableTab.label";
	public static final String	CROSSER_NAME = "JCloseableTab.crosser";
	
	private static final Icon	GRAY_ICON = new ImageIcon(JCloseableTab.class.getResource("grayicon.png"));
	private static final Icon	RED_ICON = new ImageIcon(JCloseableTab.class.getResource("redicon.png"));

	private final Localizer		localizer;
	private final JLabel		label = new JLabel();
	private final JLabel		crosser = new JLabel(GRAY_ICON);
	private String				text, tooltip;
	private JTabbedPane 		container = null;
	private Component 			tab = null;
	private JPopupMenu			popup = null;
	private boolean				closeEnable = true;

	public JCloseableTab(final Localizer localizer, final ContentNodeMetadata meta) throws LocalizationException {
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
			if (meta.getIcon() != null) {
				try{
					label.setIcon(new ImageIcon(meta.getIcon().toURL()));
				} catch (MalformedURLException e) {
					PureLibSettings.CURRENT_LOGGER.message(Severity.warning,e,"Icon loading failure: "+e.getLocalizedMessage());
				}
			}
			prepare(meta.getUIPath().toString());
		}
	}
	
	
	public JCloseableTab(final Localizer localizer) throws LocalizationException {
		super(new BorderLayout(2,2));
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.localizer = localizer;
			prepare("");
		}
	}

	public JCloseableTab(final Localizer localizer, final Icon image, final int horizontalAlignment) throws LocalizationException {
		super(new BorderLayout(2,2));
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.localizer = localizer;
			label.setIcon(image);
			label.setHorizontalAlignment(horizontalAlignment);
			prepare("");
		}
	}

	public JCloseableTab(final Localizer localizer, final Icon image) throws LocalizationException {
		super(new BorderLayout(2,2));
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.localizer = localizer;
			label.setIcon(image);
			prepare("");
		}
	}

	public JCloseableTab(final Localizer localizer, final String text, final Icon icon, final int horizontalAlignment) throws LocalizationException {
		super(new BorderLayout(2,2));
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.text = text;
			label.setIcon(icon);
			label.setHorizontalAlignment(horizontalAlignment);
			prepare("");
		}
	}

	public JCloseableTab(final Localizer localizer, final String text, final int horizontalAlignment) throws LocalizationException {
		super(new BorderLayout(2,2));
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.text = text;
			label.setHorizontalAlignment(horizontalAlignment);
			prepare("");
		}
	}

	public JCloseableTab(final Localizer localizer, final String text) throws LocalizationException {
		super(new BorderLayout(2,2));
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.text = text;
			prepare("");
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings(oldLocale,newLocale);
	}

	public void setText(final String text) {
		this.text = text;
		try{if (localizer != null) {
				fillLocalizedStrings(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
			}
		} catch (LocalizationException e) {
		}
	}

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
			SwingUtils.assignActionListeners(popup,tab);
		}
	}
	
	public void setCloseEnable(final boolean enable) {
		closeEnable = enable;
	}

	public boolean isCloseEnable() {
		return closeEnable;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintChildren(g);
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

	private void closeTab() {
		if (tab instanceof AutoCloseable) {
			try{((AutoCloseable)tab).close();
			} catch (Exception exc) {
				PureLibSettings.CURRENT_LOGGER.message(Severity.error,exc,"Exception on close tab window: "+exc.getLocalizedMessage());
			}
		}
		container.remove(tab);
		container = null;
		tab = null;
		popup = null;
	}
	
	private void prepare(final String path) throws LocalizationException, IllegalArgumentException {
		final Font	oldFont = getFont(); 
		
		label.setFocusable(true);
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
				selectTab();
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
		if (text == null) {
			label.setText("");
		}
		else {
			label.setText(localizer.getValue(text));
		}
		if (tooltip == null) {
			label.setToolTipText("");
		}
		else {
			label.setToolTipText(localizer.getValue(tooltip));
		}
	}
}
