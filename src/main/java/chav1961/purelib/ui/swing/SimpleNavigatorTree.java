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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;

/**
 * <p>This class implements application navigation menu as a tree.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @lastUpdate 0.0.5
 */
public class SimpleNavigatorTree<T> extends JTree implements LocaleChangeListener, NodeMetadataOwner {
	private static final long 				serialVersionUID = -978827310276837317L;
	public static final String				JSON_NAME = "name";
	public static final String				JSON_LABEL = "label";
	public static final String				JSON_TOOLTIP = "tooltip";
	public static final String				JSON_ICON = "icon";
	public static final String				JSON_CONTENT = "content";
	
	private final ContentType				contentType;
	private final Localizer					localizer;
	private final ContentNodeMetadata		metadata;
	private final JsonNode					root;
	private final FileSystemInterface		fsi;
	private final boolean					lazyLoading;
	private final DefaultMutableTreeNode	rootNode = new DefaultMutableTreeNode();
	private final LightWeightListenerList<ActionListener>	listeners = new LightWeightListenerList<>(ActionListener.class); 
	
	public enum ContentType {
		METADATA, JSON, FSYS
	}

	public SimpleNavigatorTree(final Localizer localizer, final FileSystemInterface fsi) throws NullPointerException, IllegalArgumentException, LocalizationException, IOException {
		this(localizer, fsi, false);
	}
	
	public SimpleNavigatorTree(final Localizer localizer, final FileSystemInterface fsi, final boolean lazyLoading) throws NullPointerException, IllegalArgumentException, LocalizationException, IOException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (fsi == null) {
			throw new NullPointerException("File system root can't be null");
		}
		else {
			this.localizer = localizer;
			this.metadata = null;
			this.root = null;
			this.fsi = fsi;
			this.contentType = ContentType.FSYS;
			this.lazyLoading = lazyLoading;
			
			setName(fsi.getPath());
			setModel(new DefaultTreeModel(content2Tree(fsi,rootNode)));
			setCellRenderer(new DefaultTreeCellRenderer() {
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
					final String	item = (String)((DefaultMutableTreeNode)value).getUserObject();
					final JLabel 	label = (JLabel)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
					
					try(final FileSystemInterface	temp = fsi.clone().open(item)) {
						label.setText(temp.getName());
						label.setName(temp.getPath());
						label.setIcon(defineIcon((T)item)); 
					} catch (IOException exc) {
					}
					
					return label;
				}
			});
			
			prepareCommon(lazyLoading);
		}
	}
	
	public SimpleNavigatorTree(final Localizer localizer, final JsonNode root) throws NullPointerException, IllegalArgumentException, LocalizationException {
		this(localizer,root,false);
	}
	
	public SimpleNavigatorTree(final Localizer localizer, final JsonNode root, final boolean lazyLoading) throws NullPointerException, IllegalArgumentException, LocalizationException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (root == null) {
			throw new NullPointerException("Root node can't be null");
		}
		else if (root.getType() != JsonNodeType.JsonObject || !root.hasName(JSON_NAME)) {
			throw new IllegalArgumentException("Root node must be json node and must have ["+JSON_NAME+"] field");
		}
		else {
			this.localizer = localizer;
			this.metadata = null;
			this.root = root;
			this.fsi = null;
			this.contentType = ContentType.JSON;
			this.lazyLoading = lazyLoading;
			
			setName(root.getChild(JSON_NAME).getStringValue());
			setModel(new DefaultTreeModel(content2Tree(root,rootNode)));
			setCellRenderer(new DefaultTreeCellRenderer() {
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
					final JLabel 		label = (JLabel)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
					
					try{final JsonNode	item = (JsonNode)((DefaultMutableTreeNode)value).getUserObject();
								
						label.setText(extractValue(item.hasName(JSON_LABEL) ? item.getChild(JSON_LABEL).getStringValue() : item.getChild(JSON_NAME).getStringValue()));
						if (item.hasName(JSON_TOOLTIP)) {
							label.setToolTipText(extractValue(item.getChild(JSON_TOOLTIP).getStringValue()));
						}
						label.setIcon(defineIcon((T)item)); 

						label.setName(item.getChild(JSON_NAME).getStringValue());
						return label;
					} catch (LocalizationException | MalformedURLException exc) {
						return label;
					}
				}
			});
			
			prepareCommon(lazyLoading);
		}
	}

	public SimpleNavigatorTree(final Localizer localizer, final ContentNodeMetadata metadata) throws NullPointerException, IllegalArgumentException, LocalizationException {
		this(localizer,metadata,false);
	}
	
	public SimpleNavigatorTree(final Localizer localizer, final ContentNodeMetadata metadata, final boolean lazyLoading) throws NullPointerException, IllegalArgumentException, LocalizationException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (!metadata.getRelativeUIPath().toString().contains(Constants.MODEL_NAVIGATION_TOP_PREFIX)) {
			throw new IllegalArgumentException("Metadata must be referred to navigation top node");
		}
		else {
			this.localizer = localizer;
			this.metadata = metadata;
			this.root = null;
			this.fsi = null;
			this.contentType = ContentType.METADATA;
			this.lazyLoading = lazyLoading;
			
			setName(URIUtils.removeQueryFromURI(metadata.getUIPath()).toString());
			setModel(new DefaultTreeModel(content2Tree(metadata,rootNode)));
			setCellRenderer(new DefaultTreeCellRenderer() {
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
					final JLabel 					label = (JLabel)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
					
					try{final ContentNodeMetadata	item = (ContentNodeMetadata)((DefaultMutableTreeNode)value).getUserObject();
								
						label.setText(localizer.getValue(item.getLabelId()));
						if (item.getTooltipId() != null) {
							label.setToolTipText(localizer.getValue(item.getTooltipId()));
						}
						label.setIcon(defineIcon((T)item)); 

						label.setName(URIUtils.removeQueryFromURI(item.getUIPath()).toString());
						return label;
					} catch (LocalizationException | MalformedURLException exc) {
						return label;
					}
				}
			});
			
			prepareCommon(lazyLoading);
			assignKeys(this,metadata);
		}
	}

	public ContentType getConentType() {
		return contentType;
	}
	
	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		final DefaultMutableTreeNode	root = (DefaultMutableTreeNode)getModel().getRoot(); 
		
		((DefaultTreeModel)this.getModel()).setRoot(root);
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		if (getConentType() == ContentType.METADATA) {
			return metadata;
		}
		else {
			throw new IllegalStateException("Navigator doesn't contain metadata, it's type is ["+getConentType()+"]");
		}
	}
	
	@Override
	public String getToolTipText(final MouseEvent event) {
		if (getRowForLocation(event.getX(), event.getY()) == -1) {
			return super.getToolTipText(event);
		}
		else {
			final TreePath 				curPath = getPathForLocation(event.getX(), event.getY());
			final TreeNode 				node = (TreeNode)curPath.getLastPathComponent();
			
			switch (getConentType()) {
				case FSYS		:
					return super.getToolTipText(event);
				case JSON		:
					final JsonNode	nodeValue = (JsonNode) ((DefaultMutableTreeNode)node).getUserObject();
					
					if (nodeValue.hasName(SimpleNavigatorTree.JSON_TOOLTIP)) {
						try{return extractValue(nodeValue.getChild(SimpleNavigatorTree.JSON_TOOLTIP).getStringValue());
						} catch (LocalizationException e) {
							return super.getToolTipText(event);
						}
					}
					else {
						return super.getToolTipText(event);
					}
				case METADATA	:
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
				default	: throw new UnsupportedOperationException("Content type ["+getConentType()+"] is not supported yet");
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

	public boolean findAndSelect(final URI item) throws NullPointerException, IllegalArgumentException {
		if (item == null) {
			throw new NullPointerException("URI to find can't be null");
		}
		else {
			try{if (findAndSelect((DefaultMutableTreeNode)getModel().getRoot(),item)) {
					return true;
				}
				else {
					setSelectionPath(null);
					return false;
				}
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}
	
	public void findAndDoubleClick(final URI item) throws NullPointerException {
		if (item == null) {
			throw new NullPointerException("URI to find can't be null");
		}
		else {
			if (findAndSelect(item)) {
				callAction(getSelectionPath());
			}
		}
	}

	protected void appendNodes(final T submenu, final DefaultMutableTreeNode node) {
	}
	
	protected JPopupMenu getPopupMenu(final TreePath path, final T meta) {
		return null;
	}
	
	protected Icon defineIcon(final T node) throws MalformedURLException {
		switch (getConentType()) {
			case FSYS		:
				return null;
			case JSON		:
				final JsonNode	jsonNode = (JsonNode)node;
				
				if (jsonNode.hasName(JSON_ICON)) {
					return new ImageIcon(URI.create(jsonNode.getChild(JSON_TOOLTIP).getStringValue()).toURL()); 
				}
				else {
					return null;
				}
			case METADATA	:
				final ContentNodeMetadata	metadata = (ContentNodeMetadata)node;

				if (metadata.getIcon() != null) {
					return new ImageIcon(metadata.getIcon().toURL()); 
				}
				else {
					return null;
				}
			default	: throw new UnsupportedOperationException("Content type ["+getConentType()+"] is not supported yet");
		}
	}
	
	private void prepareCommon(final boolean lazyLoading) {
		setRootVisible(false);
		setEditable(false);
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
							
							switch (getConentType()) {
								case FSYS		:
									final String	fsiAssociation = (String)contentPath.getLastPathComponent();
									
									showPopupMenu(contentPath,(T)fsiAssociation,rect.x+rect.width/2,rect.y+rect.height/2);
									break;
								case JSON		:
									final JsonNode	nodeAssociation = (JsonNode)contentPath.getLastPathComponent();
									
									showPopupMenu(contentPath,(T)nodeAssociation,rect.x+rect.width/2,rect.y+rect.height/2);
									break;
								case METADATA	:
									final ContentNodeMetadata	metadataAssociation = (ContentNodeMetadata)contentPath.getLastPathComponent();
									
									showPopupMenu(contentPath,(T)metadataAssociation,rect.x+rect.width/2,rect.y+rect.height/2);
									break;
								default	: throw new UnsupportedOperationException("Content type ["+getConentType()+"] is not supported yet");
							}
						}
						else {
							final PointerInfo 	info = MouseInfo.getPointerInfo();
							
							if (info != null) {
								final Point		p = info.getLocation();
								
								SwingUtilities.convertPointFromScreen(p,SimpleNavigatorTree.this);								
								switch (getConentType()) {
									case FSYS		:
										showPopupMenu(null,(T)fsi,p.x,p.y);
										break;
									case JSON		:
										showPopupMenu(null,(T)root,p.x,p.y);
										break;
									case METADATA	:
										showPopupMenu(null,(T)metadata,p.x,p.y);
										break;
									default	: throw new UnsupportedOperationException("Content type ["+getConentType()+"] is not supported yet");
								}
							}
							else {
								switch (getConentType()) {
									case FSYS		:
										showPopupMenu(null,(T)fsi,SimpleNavigatorTree.this.getWidth()/2,SimpleNavigatorTree.this.getHeight()/2);
										break;
									case JSON		:
										showPopupMenu(null,(T)root,SimpleNavigatorTree.this.getWidth()/2,SimpleNavigatorTree.this.getHeight()/2);
										break;
									case METADATA	:
										showPopupMenu(null,(T)metadata,SimpleNavigatorTree.this.getWidth()/2,SimpleNavigatorTree.this.getHeight()/2);
										break;
									default	: throw new UnsupportedOperationException("Content type ["+getConentType()+"] is not supported yet");
								}
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
							showPopupMenu(curPath,(T)((DefaultMutableTreeNode)curPath.getLastPathComponent()).getUserObject(),e.getX(),e.getY());
						}
						else {
							switch (getConentType()) {
								case FSYS		:
									showPopupMenu(null,(T)fsi,e.getX(),e.getY());
									break;
								case JSON		:
									showPopupMenu(null,(T)root,e.getX(),e.getY());
									break;
								case METADATA	:
									showPopupMenu(null,(T)metadata,e.getX(),e.getY());
									break;
								default	: throw new UnsupportedOperationException("Content type ["+getConentType()+"] is not supported yet");
							}
						}
					 }
					 else {
						switch (getConentType()) {
							case FSYS		:
								showPopupMenu(null,(T)fsi,e.getX(),e.getY());
								break;
							case JSON		:
								showPopupMenu(null,(T)root,e.getX(),e.getY());
								break;
							case METADATA	:
								showPopupMenu(null,(T)metadata,e.getX(),e.getY());
								break;
							default	: throw new UnsupportedOperationException("Content type ["+getConentType()+"] is not supported yet");
						}
					 }
				}
			}
		});
		
		if (lazyLoading) {
			addTreeWillExpandListener(new TreeWillExpandListener() {
				@Override
				public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
					// TODO Auto-generated method stub
					
				}
			});
		}
		
		InternalUtils.registerAdvancedTooptip(this);
	}
	
	private MutableTreeNode content2Tree(final ContentNodeMetadata meta, final DefaultMutableTreeNode node) {
		node.setUserObject(meta);
		
		for (ContentNodeMetadata item : meta) {
			final String	path = item.getRelativeUIPath().toString(); 
			
			if (path.contains(Constants.MODEL_NAVIGATION_NODE_PREFIX) || path.contains(Constants.MODEL_NAVIGATION_LEAF_PREFIX)) {
				final DefaultMutableTreeNode 	temp = new DefaultMutableTreeNode();
				final MutableTreeNode			child = content2Tree(item,temp);  
				
				if (path.contains(Constants.MODEL_NAVIGATION_NODE_PREFIX)) {
					appendNodes((T)item,(DefaultMutableTreeNode)child);
				}
				node.add(child);
			}				
		}
		return node;
	}

	private MutableTreeNode content2Tree(final JsonNode root, final DefaultMutableTreeNode node) {
		if (root.getType() != JsonNodeType.JsonObject || !root.hasName(JSON_NAME)) {
			throw new IllegalArgumentException("Node inside json must be json object node and must have ["+JSON_NAME+"] field");
		}
		else {
			node.setUserObject(root);
			
			if (root.hasName("content")) {
				for (JsonNode item : root.getChild("content").children()) {
					final DefaultMutableTreeNode 	temp = new DefaultMutableTreeNode();
					
					node.add(content2Tree(item,temp));
				}
			}
			return node;
		}
	}

	private MutableTreeNode content2Tree(final FileSystemInterface root, final DefaultMutableTreeNode node) throws IOException {
		node.setUserObject(root.getPath());
		
		if (root.isDirectory()) {
			for (String item : root.list()) {
				final DefaultMutableTreeNode 	temp = new DefaultMutableTreeNode();
				
				node.add(content2Tree(root.push("./"+item),temp));
				root.pop();
			}
		}
		return node;
	}
	
	private static <T> void assignKeys(final SimpleNavigatorTree<T> component, final ContentNodeMetadata meta) {
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
			final TreeNode 	node = (TreeNode)path.getLastPathComponent();
			final String	actionCommand;

			switch (getConentType()) {
				case FSYS		:
					actionCommand = collectJsonTreePath(path); 
					break;
				case JSON		:
					actionCommand = collectJsonTreePath(path); 
					break;
				case METADATA	:
					final ContentNodeMetadata	metadataAssociation = (ContentNodeMetadata)((DefaultMutableTreeNode)node).getUserObject();
					
					actionCommand = metadataAssociation.getApplicationPath() != null ? metadataAssociation.getApplicationPath().getSchemeSpecificPart() : ""; 
					break;
				default	: throw new UnsupportedOperationException("Content type ["+getConentType()+"] is not supported yet");
			}
			processAction(new ActionEvent(this,0,actionCommand),actionCommand);
		}
		else {
			processAction(new ActionEvent(this,0,null),null);
		}
	}
	
	private String collectJsonTreePath(final TreePath path) {
		final StringBuilder	sb = new StringBuilder("action:");

		for (Object item : path.getPath()) {
			sb.append('/').append(((JsonNode)((DefaultMutableTreeNode)item).getUserObject()).getChild(JSON_NAME).getStringValue());
		}
		return sb.toString();
	}

	private void processAction(final ActionEvent event, final String actionCommand) {
		listeners.fireEvent((listener)->{
			try{listener.actionPerformed(event);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		});
	}

	private void showPopupMenu(final TreePath path, final T meta, final int x, final int y) {
		final JPopupMenu	popup = getPopupMenu(path,meta);
		
		if (popup != null) {
			popup.setOpaque(true);  
			popup.setLightWeightPopupEnabled(true);
			popup.show(this,x,y);
		}
	}

	private boolean findAndSelect(final DefaultMutableTreeNode root, final URI item) throws UnsupportedEncodingException {
		switch (getConentType()) {
			case FSYS		: return findAndSelectFSys(root, item);
			case JSON		: return findAndSelectJson(root, "action:", item);
			case METADATA	: return findAndSelectMeta(root, item);
			default	: throw new UnsupportedOperationException("Content type ["+getConentType()+"] is not supported yet");
		}
	}
	
	private boolean findAndSelectMeta(final DefaultMutableTreeNode root, final URI item) {
		final ContentNodeMetadata	meta = (ContentNodeMetadata)root.getUserObject();
		
		if (meta.getUIPath().equals(item)) {
			setSelectionPath(new TreePath(root.getPath()));
			return true;
		}
		else {
			for (int index = 0, maxIndex = root.getChildCount(); index < maxIndex; index++) {
				if (findAndSelectMeta((DefaultMutableTreeNode)root.getChildAt(index),item)) {
					return true;
				}
			}
			return false;
		}
	}

	private boolean findAndSelectJson(final DefaultMutableTreeNode root, final String currentPath, final URI item) throws UnsupportedEncodingException {
		final JsonNode	meta = (JsonNode)root.getUserObject();
		final String	newURI = currentPath+'/'+URLEncoder.encode(meta.getChild(JSON_NAME).getStringValue(),"UTF-8"); 
		
		if (URI.create(newURI).equals(item)) {
			setSelectionPath(new TreePath(root.getPath()));
			return true;
		}
		else {
			for (int index = 0, maxIndex = root.getChildCount(); index < maxIndex; index++) {
				if (findAndSelectJson((DefaultMutableTreeNode)root.getChildAt(index),newURI,item)) {
					return true;
				}
			}
			return false;
		}
	}

	private boolean findAndSelectFSys(final DefaultMutableTreeNode root, final URI item) {
		// TODO Auto-generated method stub
		return false;
	}

	
	private String extractValue(final String key) throws LocalizationException {
		if (localizer.containsKey(key)) {
			return localizer.getValue(key);
		}
		else {
			return key;
		}
	}
}
