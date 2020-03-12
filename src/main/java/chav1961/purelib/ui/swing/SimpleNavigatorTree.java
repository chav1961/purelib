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
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;

public class SimpleNavigatorTree extends JTree implements LocaleChangeListener, NodeMetadataOwner {
	private static final long 			serialVersionUID = -978827310276837317L;

	private final Localizer					localizer;
	private final ContentNodeMetadata		metadata;
	private final LightWeightListenerList<ActionListener>	listeners = new LightWeightListenerList<>(ActionListener.class); 
	
	public SimpleNavigatorTree(final Localizer localizer, final ContentNodeMetadata metadata) throws LocalizationException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (!metadata.getRelativeUIPath().toString().contains(Constants.MODEL_NAVIGATION_TOP_PREFIX)) {
			throw new IllegalArgumentException("Metadata must be refered to navigation top node");
		}
		else {
			this.localizer = localizer;
			this.metadata = metadata;

			final String	name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			
			setModel(new DefaultTreeModel(metadata2Tree(metadata)));
			assignKeys(this,metadata);
			setRootVisible(false);
			setEditable(false);
			setCellRenderer(new DefaultTreeCellRenderer() {
								private static final long serialVersionUID = 1L;
				
								@Override
								public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
									try{final ContentNodeMetadata	item = (ContentNodeMetadata)((DefaultMutableTreeNode)value).getUserObject();
										final JLabel 				label = item.getIcon() != null ? new JLabel(localizer.getValue(item.getLabelId()),new ImageIcon(item.getIcon().toURL()),JLabel.LEFT) : new JLabel(localizer.getValue(item.getLabelId()),JLabel.LEFT);

										label.setName(URIUtils.removeQueryFromURI(item.getUIPath()).toString());
										if (selected || hasFocus) {
											label.setOpaque(true);
											label.setForeground(Color.WHITE);
											label.setBackground(Color.BLUE);
										}
										else {
											label.setForeground(tree.getForeground());
										}
										if (hasFocus) {
											label.setBorder(new LineBorder(Color.RED));
										}
										return label;
									} catch (LocalizationException | MalformedURLException exc) {
										return new JLabel(exc.getLocalizedMessage());
									}
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
								callAction(enterPath);
							}
							break;
						case KeyEvent.VK_CONTEXT_MENU	:
							final TreePath 		contentPath = getSelectionPath();
							
							if (contentPath != null) {
								final Rectangle				rect = getRowBounds(getRowForPath(contentPath));
								final ContentNodeMetadata	association = (ContentNodeMetadata)contentPath.getLastPathComponent();
								
								showPopupMenu(contentPath,association,rect.x+rect.width/2,rect.y+rect.height/2);
							}
							else {
								final PointerInfo 	info = MouseInfo.getPointerInfo();
								
								if (info != null) {
									final Point		p = info.getLocation();
									
									SwingUtilities.convertPointFromScreen(p,SimpleNavigatorTree.this);								
									showPopupMenu(null,metadata,p.x,p.y);
								}
								else {
									showPopupMenu(null,metadata,SimpleNavigatorTree.this.getWidth()/2,SimpleNavigatorTree.this.getHeight()/2);
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
							callAction(getPathForLocation(e.getX(), e.getY()));
						 }
					}
					else if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
						 if (getRowForLocation(e.getX(), e.getY()) != -1) {
							final TreePath 		curPath = getPathForLocation(e.getX(), e.getY());

							if (curPath != null) {
								showPopupMenu(curPath,(ContentNodeMetadata)curPath.getLastPathComponent(),e.getX(),e.getY());
							}
							else {
								showPopupMenu(null,metadata,e.getX(),e.getY());
							}
						 }
						 else {
							showPopupMenu(null,metadata,e.getX(),e.getY());
						 }
					}
				}
			});
			
			setName(name);
		}
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		final DefaultMutableTreeNode	root = (DefaultMutableTreeNode)getModel().getRoot(); 
		
		((DefaultTreeModel)this.getModel()).setRoot(root);
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}
	
	@Override
	public String getToolTipText(final MouseEvent event) {
		if (getRowForLocation(event.getX(), event.getY()) == -1) {
			return super.getToolTipText(event);
		}
		else {
			final TreePath 				curPath = getPathForLocation(event.getX(), event.getY());
			final TreeNode 				node = (TreeNode)curPath.getLastPathComponent();
			final ContentNodeMetadata	meta = (ContentNodeMetadata) ((DefaultMutableTreeNode)node).getUserObject();

			if (meta.getTooltipId() != null) {
				try{return localizer.getValue(meta.getTooltipId());
				} catch (LocalizationException e) {
					return meta.getTooltipId(); 
				}
			}
			else {
				return super.getToolTipText(event);
			}
		}
	}
	
	public void addActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener can't be null");
		}
		else {
			listeners.addListener(listener);
		}
	}

	public void removeActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener can't be null");
		}
		else {
			listeners.removeListener(listener);
		}
	}

	public boolean findAndSelect(final URI item) {
		if (findAndSelect((DefaultMutableTreeNode)getModel().getRoot(),item)) {
			return true;
		}
		else {
			setSelectionPath(null);
			return false;
		}
	}
	
	public void findAndDoubleClick(final URI item) {
		if (findAndSelect(item)) {
			callAction(getSelectionPath());
		}
	}

	public ContentNodeMetadata getSelectedNodeMetadata() {
		final TreePath 	curPath = getSelectionPath();
		
		if (curPath != null) {
			return (ContentNodeMetadata)((DefaultMutableTreeNode)curPath.getLastPathComponent()).getUserObject();
		}
		else {
			return null;
		}
	}
	
	protected JPopupMenu getPopupMenu(final TreePath path, final ContentNodeMetadata meta) {
		return null;
	}
	
	private static MutableTreeNode metadata2Tree(final ContentNodeMetadata meta) {
		final DefaultMutableTreeNode	node = new DefaultMutableTreeNode(meta,true);
		
		for (ContentNodeMetadata item : meta) {
			final String	path = item.getRelativeUIPath().toString(); 
			
			if (path.contains(Constants.MODEL_NAVIGATION_NODE_PREFIX) || path.contains(Constants.MODEL_NAVIGATION_LEAF_PREFIX)) {
				node.add(metadata2Tree(item));
			}				
		}
		return node;
	}

	private static void assignKeys(final SimpleNavigatorTree component, final ContentNodeMetadata meta) {
		for (ContentNodeMetadata item : meta) {
			if (meta.getRelativeUIPath().toString().contains(Constants.MODEL_NAVIGATION_KEYSET_PREFIX)) {
				for (ContentNodeMetadata key : item) {
					SwingUtils.assignActionKey(component,WHEN_FOCUSED,KeyStroke.getKeyStroke(key.getLabelId()),(e)->{
						component.findAndDoubleClick(URI.create(e.getActionCommand()));
					},key.getUIPath().toString());
				}
			}				
		}
	}
	
	private void callAction(final TreePath path) {
		if (path != null) {
			final TreeNode 				node = (TreeNode)path.getLastPathComponent();
			final ContentNodeMetadata	association = (ContentNodeMetadata)((DefaultMutableTreeNode)node).getUserObject();
			final String				actionCommand = association.getApplicationPath() != null ? association.getApplicationPath().toString() : ""; 
			
			processAction(new ActionEvent(this,0,actionCommand),actionCommand);
		}
		else {
			processAction(new ActionEvent(this,0,null),null);
		}
	}
	
	private void processAction(final ActionEvent event, final String actionCommand) {
		listeners.fireEvent((listener)->{
			try{listener.actionPerformed(event);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		});
	}

	private void showPopupMenu(final TreePath path, final ContentNodeMetadata meta, final int x, final int y) {
		final JPopupMenu	popup = getPopupMenu(path,meta);
		
		if (popup != null) {
			popup.setOpaque(true);  
			popup.setLightWeightPopupEnabled(true);
			popup.show(this,x,y);
		}
	}

	private boolean findAndSelect(final DefaultMutableTreeNode root, final URI item) {
		final ContentNodeMetadata	meta = (ContentNodeMetadata)root.getUserObject();
		
		if (meta.getUIPath().equals(item)) {
			setSelectionPath(new TreePath(root.getPath()));
			return true;
		}
		else {
			for (int index = 0, maxIndex = root.getChildCount(); index < maxIndex; index++) {
				if (findAndSelect((DefaultMutableTreeNode)root.getChildAt(index),item)) {
					return true;
				}
			}
			return false;
		}
	}

}
