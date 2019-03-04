package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.ui.swing.SwingUtils;

public class JCloseableTab extends JLabel {
	private static final long 	serialVersionUID = -5601021193645267745L;
	private static final Icon	GRAY_ICON = new ImageIcon(JCloseableTab.class.getResource("grayicon.png"));
	private static final Icon	RED_ICON = new ImageIcon(JCloseableTab.class.getResource("redicon.png"));

	private final JCrosser		crosser = new JCrosser(); 
	private JTabbedPane 		container = null;
	private Component 			tab = null;
	private JMenu				popup = null;
	
	public JCloseableTab() {
		super();
		prepare();
	}

	public JCloseableTab(final Icon image, final int horizontalAlignment) {
		super(image, horizontalAlignment);
		prepare();
	}

	public JCloseableTab(final Icon image) {
		super(image);
		prepare();
	}

	public JCloseableTab(final String text, final Icon icon, final int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		prepare();
	}

	public JCloseableTab(final String text, final int horizontalAlignment) {
		super(text, horizontalAlignment);
		prepare();
	}

	public JCloseableTab(final String text) {
		super(text);
		prepare();
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

	public void associate(final JTabbedPane container, final Component tab, final JMenu popup) {
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
	
	@Override
	public void setBorder(Border border) {
		super.setBorder(border);
		new ComponentKeepedBorder(0,crosser).install(this);
	}

	private void popup() {
		if (popup != null) {
			final List<Object>	submenus = new ArrayList<>();
			
			SwingUtils.walkDown(popup,(mode,component)->{
				switch (mode) {
					case ENTER	:
						if (component instanceof JMenu) {
							final JMenu	newMenu = new JMenu();
							
							newMenu.setIcon(((JMenu)component).getIcon());
							newMenu.setText(((JMenu)component).getText());
							newMenu.setToolTipText(((JMenu)component).getToolTipText());
							if (submenus.size() == 0) {
								submenus.add(0,new JPopupMenu());								
							}
							else {
								submenus.add(0,new JPopupMenu());
							}
						}
						else if (component instanceof JMenuItem) {
							final JMenuItem	newItem = new JMenuItem();
							
							newItem.setIcon(((JMenuItem)component).getIcon());
							newItem.setText(((JMenuItem)component).getText());
							newItem.setToolTipText(((JMenuItem)component).getToolTipText());
							newItem.setActionCommand(((JMenuItem)component).getActionCommand());
							for (ActionListener listener : ((JMenuItem)component).getActionListeners()) {
								newItem.addActionListener(listener);
							}
							
							if (submenus.size() >= 1) {
								((JMenu)submenus.get(0)).add(newItem);
							}
							else {
								((JPopupMenu)submenus.get(0)).add(newItem);
							}
						}
						else if (component instanceof JSeparator) {
							if (submenus.size() >= 1) {
								((JMenu)submenus.get(0)).addSeparator();
							}
							else {
								((JPopupMenu)submenus.get(0)).addSeparator();
							}
						}
						break;
					case EXIT	:
						if (submenus.size() >= 2) {
							final JMenu	menu = (JMenu)submenus.remove(0);
							
							((JMenu)submenus.get(0)).add(menu);
						}
						else if (submenus.size() >= 1) {
							final JMenu	menu = (JMenu)submenus.remove(0);
							
							((JPopupMenu)submenus.get(0)).add(menu);
						}
						break;
				}
				return ContinueMode.CONTINUE;
			});
			((JPopupMenu)submenus.remove(0)).show(this,getWidth()/2,getHeight()/2);
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
				return;
			}
		}
		container.remove(tab);
		container = null;
		tab = null;
		popup = null;
	}
	
	private void prepare() {
		final Font	oldFont = getFont(); 
		
		setFocusable(true);
		new ComponentKeepedBorder(0,crosser).install(this);
		setFont(new Font(oldFont.getFontName(),Font.PLAIN,oldFont.getSize()));
		addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			
			@Override 
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON2) {
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
		addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {}
			@Override public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
					popup();
				}
			}
		});
	}
	
	private class JCrosser extends AbstractButton {
		private static final long serialVersionUID = 1L;
		
		private Icon					currentIcon = GRAY_ICON;

		private JCrosser() {
			setSize(20,20);
			addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				
				@Override
				public void mouseExited(MouseEvent e) {
					currentIcon = GRAY_ICON;
					repaint();
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					currentIcon = RED_ICON; 
					repaint();
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					closeTab();
				}
			});
		}
		
		@Override
		public void paintComponent(final Graphics g) {
			currentIcon.paintIcon(this,g,0,0);
		}
	}
}
