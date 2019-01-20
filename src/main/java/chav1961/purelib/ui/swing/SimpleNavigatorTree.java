package chav1961.purelib.ui.swing;


import java.awt.Color;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.SmartToolTip;

public class SimpleNavigatorTree extends JTree implements LocaleChangeListener {
	private static final long 			serialVersionUID = -978827310276837317L;

	private final Localizer				localizer;
	private final List<ActionListener>	listeners = new ArrayList<>();
	private final ActionListener		menuListener = new ActionListener() {
											@Override
											public void actionPerformed(ActionEvent e) {
												processAction((JMenuItem)e.getSource(),new ActionEvent(SimpleNavigatorTree.this,0,null));
											}
										};
	
	public SimpleNavigatorTree(final Localizer localizer, final JMenuBar bar) throws LocalizationException {
		super(menuBar2Tree(bar));
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;
			setEditable(false);
			setCellRenderer(new DefaultTreeCellRenderer() {
								private static final long serialVersionUID = 1L;
				
								@Override
								public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
									final JComponent	item = (JComponent) ((DefaultMutableTreeNode)value).getUserObject();
									
									if (selected || hasFocus) {
										item.setForeground(Color.WHITE);
										item.setBackground(Color.BLUE);
									}
									else {
										item.setForeground(tree.getForeground());
										item.setBackground(tree.getBackground());
									}
									
									return item;
								}
							});
			getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			ToolTipManager.sharedInstance().registerComponent(this);
			
			addKeyListener(new KeyListener() {
				@Override public void keyTyped(final KeyEvent e) {}
				@Override public void keyReleased(final KeyEvent e) {}
				
				@Override
				public void keyPressed(final KeyEvent e) {
					switch (e.getKeyCode()) {
						case KeyEvent.VK_ENTER			:
							final TreePath 			enterPath = getSelectionPath();
							
							if (enterPath != null) {
								final JMenuItem 	item = (JMenuItem)((DefaultMutableTreeNode)enterPath.getLastPathComponent()).getUserObject();

								processAction(item,new ActionEvent(item,0,null));
							}
							break;
						case KeyEvent.VK_CONTEXT_MENU	:
							final TreePath 		contentPath = getSelectionPath();
							
							if (contentPath != null) {
								final Rectangle					rect = getRowBounds(getRowForPath(contentPath));
								
								showPopupMenu(contentPath,rect.x+rect.width/2,rect.y+rect.height/2);
							}
							else {
								final PointerInfo 	info = MouseInfo.getPointerInfo();
								
								if (info != null) {
									final Point		p = info.getLocation();
									
									SwingUtilities.convertPointFromScreen(p,SimpleNavigatorTree.this);								
									showPopupMenu(contentPath,p.x,p.y);
								}
								else {
									showPopupMenu(contentPath,SimpleNavigatorTree.this.getWidth()/2,SimpleNavigatorTree.this.getHeight()/2);
								}
							}
							break;
					}
				}
			});
			
			addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseExited(MouseEvent e) {}
				@Override public void mouseEntered(MouseEvent e) {}
				
				@Override 
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
						 if (getRowForLocation(e.getX(), e.getY()) != -1) {
							final TreePath 		curPath = getPathForLocation(e.getX(), e.getY());
							final TreeNode 		node = (TreeNode)curPath.getLastPathComponent();
							final JMenuItem		item = (JMenuItem) ((DefaultMutableTreeNode)node).getUserObject();
							
							processAction(item,new ActionEvent(item,0,null));
						 }
					}
					else if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
						 if (getRowForLocation(e.getX(), e.getY()) != -1) {
							final TreePath 		curPath = getPathForLocation(e.getX(), e.getY());
							
							showPopupMenu(curPath,e.getX(),e.getY());
						 }
						 else {
							showPopupMenu(null,e.getX(),e.getY());
						 }
					}
				}
			});
		}
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		final DefaultMutableTreeNode	root = (DefaultMutableTreeNode)getModel().getRoot(); 
		
		walkAndChange(root,oldLocale,newLocale);
		((DefaultTreeModel)this.getModel()).setRoot(root);
	}
	
	@Override
	public String getToolTipText(final MouseEvent event) {
		if (getRowForLocation(event.getX(), event.getY()) == -1) {
			return null;
		}
		else {
			final TreePath 	curPath = getPathForLocation(event.getX(), event.getY());
			final TreeNode 	node = (TreeNode)curPath.getLastPathComponent();
			final JMenuItem	comp = (JMenuItem) ((DefaultMutableTreeNode)node).getUserObject();
			
			return comp.getToolTipText();					
		}
	}
	
	@Override
	public JToolTip createToolTip() {
		return new SmartToolTip(localizer,this);
	}
	
	public JPopupMenu getPopupMenu(final TreePath path) {
		return null;
	}

	public void addActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener can't be null");
		}
		else {
			synchronized (listeners) {
				listeners.add(listener);
			}
		}
	}

	public boolean findAndSelect(final String action) {
		return findAndSelect((DefaultMutableTreeNode)getModel().getRoot(),action);
	}
	
	public void removeActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener can't be null");
		}
		else {
			synchronized (listeners) {
				listeners.remove(listener);
			}
		}
	}
	
	private boolean findAndSelect(final DefaultMutableTreeNode node, final String action) {
		final Object	cargo = node.getUserObject();
		
		if ((cargo instanceof JMenuItem) && ((JMenuItem)cargo).getActionCommand().equals(action)) {
			final TreePath	path = new TreePath(node.getPath());
			
			scrollPathToVisible(path);
			setSelectionRow(getRowForPath(path));
			return true;
		}
		else {
			for (int index = 0; index < node.getChildCount(); index++) {
				if (findAndSelect((DefaultMutableTreeNode)node.getChildAt(index),action)) {
					return true;
				}
			}
			return false;
		}
	}

	private static MutableTreeNode menuBar2Tree(final JMenuBar bar) throws LocalizationException {
		final DefaultMutableTreeNode	root = new DefaultMutableTreeNode(new JMenuItem("root"),true);
		
		for (int index = 0; index < bar.getMenuCount(); index++) {
			root.add(menuBar2Tree((JMenuItem)bar.getComponent(index)));
		}
		return root;
	}

	private static MutableTreeNode menuBar2Tree(final JMenuItem menu) throws LocalizationException {
//		if (menu instanceof JLocalizedMenu) {
//			final JLocalizedMenuItem		item = new JLocalizedMenuItem(((JLocalizedMenu)menu).localizer,((JLocalizedMenu)menu).textId,((JLocalizedMenu)menu).tooltipId);
//			final DefaultMutableTreeNode	node = new DefaultMutableTreeNode(item,true);
//
//			for (int index = 0; index < ((JMenu)menu).getMenuComponentCount(); index++) {
//				node.add(menuBar2Tree(((JMenu)menu).getItem(index)));
//			}
//			return node;
//		}
//		else {
			return new DefaultMutableTreeNode(menu,false);
//		}
	}
	
	private void processAction(final JMenuItem item, final ActionEvent event) {
		final ActionListener[]	list;
		
		synchronized (listeners) {
			listeners.toArray(list = new ActionListener[listeners.size()]);
		}
		for (ActionListener listener : list) {
			try{listener.actionPerformed(new ActionEvent(this,0,item.getActionCommand()));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private void walkAndChange(final DefaultMutableTreeNode node, final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		final JMenuItem	item = (JMenuItem) node.getUserObject();
		
		if (item instanceof LocaleChangeListener) {
			((LocaleChangeListener)item).localeChanged(oldLocale, newLocale);
		}
		for (int index = 0; index < node.getChildCount(); index++) {
			walkAndChange((DefaultMutableTreeNode)node.getChildAt(index),oldLocale,newLocale);
		}
	}

	private void showPopupMenu(final TreePath path, final int x, final int y) {
		final JPopupMenu	popup = getPopupMenu(path);
		
		if (popup != null) {
			popup.setOpaque(true);  
			popup.setLightWeightPopupEnabled(true);
			for (int index = 0; index < popup.getComponentCount(); index++) {
				reassignListeners((JMenuItem)popup.getComponent(index));
			}
			popup.show(this,x,y);
		}
	}

	private void reassignListeners(final JMenuItem component) {
		if (component instanceof JMenu) {
			for (int index = 0; index < component.getComponentCount(); index++) {
				reassignListeners((JMenuItem)component.getComponent(index));
			}
		}
		else {
			component.removeActionListener(menuListener);
			component.addActionListener(menuListener);
		}
	}
}
