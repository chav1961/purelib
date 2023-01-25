package chav1961.purelib.ui.swing.useful;

import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.Iterable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimerTask;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleTimerTask;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.ui.interfaces.PureLibStandardIcons;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentKeeper;

/**
 * <p>This class supports navigation on the file system in the tree style. The class is abstract, and children of the class must 
 * implements at least two methods:</p>
 * <ul>
 * <li>{@linkplain #placeFileContent(Point, Iterable<File>)} method to process DROP operation with the list of files on any tree item</li>
 * <li>{@linkplain #refreshLinkedContent(FileSystemInterface)} method to refresh linked content after change selection inside the tree</li>
 * </ul>
 * <p>Every item in the tree is an instance of {@linkplain JFileItemDescriptor} and can be cast to it safely.</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @see LoggerFacade
 * @see FileSystemInterface
 * @see FilterCallback
 * @see JFileItemDescriptor
 * @since 0.0.7
 */
public abstract class JFileTree extends JTree implements FileContentKeeper {
	private static final long 	serialVersionUID = -9727348906597529L;

	protected static final Icon	DIR_ICON = PureLibStandardIcons.DIRECTORY.getIcon();
	protected static final Icon	DIR_ICON_OPENED = PureLibStandardIcons.DIRECTORY_OPENED.getIcon();
	protected static final Icon	FILE_ICON = PureLibStandardIcons.FILE.getIcon();

	private static final long	REFRESH_DELAY = 500;

	protected final LoggerFacade			logger;
	protected final FileSystemInterface		fsi;
	protected final boolean 				showFiles;
	protected final FilterCallback[]		filter;
	protected final Pattern					filterPattern;
	
	private TimerTask						tt = null;
	private boolean							fastRefresh = false;

	/**
	 * <p>Constructor of the class.</p>
	 * @param fsi file system to show content. Can't be null
	 * @param showFiles show files in the tree. When false, only directories will be shown
	 * @param filter filter to accept for files. Directories are never filtered.
	 * @throws NullPointerException when logger or file system is null
	 * @throws IllegalArgumentException when filter callback is null or contains nulls inside
	 * @throws IOException on any I/O errors
	 */
	public JFileTree(final FileSystemInterface fsi, final boolean showFiles, final FilterCallback... filter) throws NullPointerException, IllegalArgumentException, IOException {
		this(PureLibSettings.CURRENT_LOGGER, fsi, showFiles, filter);
	}
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param logger logger to log error messages if any. Can't be null
	 * @param fsi file system to show content. Can't be null
	 * @param showFiles show files in the tree. When false, only directories will be shown
	 * @param filter filter to accept for files. Directories are never filtered.
	 * @throws NullPointerException when logger or file system is null
	 * @throws IllegalArgumentException when filter callback is null or contains nulls inside
	 * @throws IOException on any I/O errors
	 */
	public JFileTree(final LoggerFacade logger, final FileSystemInterface fsi, final boolean showFiles, final FilterCallback... filter) throws NullPointerException, IllegalArgumentException, IOException {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (fsi == null) {
			throw new NullPointerException("File system interface can't be null");
		}
		else if (filter == null || Utils.checkArrayContent4Nulls(filter) >= 0) {
			throw new IllegalArgumentException("Filter list is null or contains nulls inside");
		}
		else {
			this.logger = logger;
			this.fsi = fsi;
			this.showFiles = showFiles;
			this.filter = filter;
			this.filterPattern = Pattern.compile(buildFilterPattern(filter));
			
			getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			setRootVisible(true);
			addTreeWillExpandListener(new TreeWillExpandListener() {
				@Override public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {}
				
				@Override
				public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
					final JFileItemDescriptorNode	last = (JFileItemDescriptorNode)event.getPath().getLastPathComponent(); 
					final String					path = ((JFileItemDescriptor)last.getUserObject()).getPath();
					
					try(final FileSystemInterface	partFsi = fsi.clone().open(path)) {
						fillChildren(last, partFsi);
					} catch (IOException e) {
						logger.message(Severity.error, "error expanding tree: "+e.getLocalizedMessage(),e);
					}
				}
			});
			addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(final TreeSelectionEvent event) {
					final JFileItemDescriptorNode	last = (JFileItemDescriptorNode)event.getPath().getLastPathComponent(); 
					final String					path = ((JFileItemDescriptor)last.getUserObject()).getPath();
					
					if (tt != null) {
						tt.cancel();
						tt = null;
					}
					
					if (fastRefresh) {
						try(final FileSystemInterface	partFsi = fsi.clone().open(path)) {
							
							refreshLinkedContent(partFsi);
						} catch (IOException e) {
							logger.message(Severity.error, "error refreshing right panel: "+e.getLocalizedMessage(),e);
						} finally {
							fastRefresh = false;
						}
					}
					else {
						tt = new SimpleTimerTask(()->{
							try(final FileSystemInterface	partFsi = fsi.clone().open(path)) {
								
								refreshLinkedContent(partFsi);
							} catch (IOException e) {
								logger.message(Severity.error, "error refreshing right panel: "+e.getLocalizedMessage(),e);
							}
						});
						PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(tt, REFRESH_DELAY);
					}
				}
			});
			final JFileItemDescriptorNode	root = new JFileItemDescriptorNode(new JFileItemDescriptor("/", fsi.getAbsoluteURI().toString(), true, fsi.size(), new Date(fsi.lastModified())));

			fillChildren(root, fsi);
			((DefaultTreeModel)getModel()).setRoot(root);
			setDragEnabled(true);
			setTransferHandler(new FileTransferHandler());
			FileTransferHandler.prepare4DroppingFiles(this);
			
			setCellRenderer(SwingUtils.getCellRenderer(JFileItemDescriptor.class, new FieldFormat(JFileItemDescriptor.class), TreeCellRenderer.class));
			setCellEditor(SwingUtils.getCellEditor(JFileItemDescriptor.class, new FieldFormat(JFileItemDescriptor.class), TreeCellEditor.class));
	        addComponentListener(new ComponentListener() {
				@Override public void componentResized(ComponentEvent e) {}
				@Override public void componentMoved(ComponentEvent e) {}
				
				@Override
				public void componentShown(ComponentEvent e) {
					final ToolTipManager 	toolTipManager = ToolTipManager.sharedInstance();
			        
			        toolTipManager.registerComponent(JFileTree.this);
				}
				
				@Override
				public void componentHidden(ComponentEvent e) {
					final ToolTipManager 	toolTipManager = ToolTipManager.sharedInstance();
			        
			        toolTipManager.unregisterComponent(JFileTree.this);
				}
			});
		}
	}

	@Override
	public abstract void placeFileContent(final Point location, final Iterable<JFileItemDescriptor> content);
	
	/**
	 * <p>Refresh linked content. Typical use of this method is refresh list of files in another component, when selection in this component changes. 
	 * Calling of this method is optimized to reduce calls when user simply moves selection from item to item (for example, by arrow keys).</p> 
	 * @param content file system item that was selected now
	 */
	public abstract void refreshLinkedContent(final FileSystemInterface content);

	/**
	 * <p>Set selection in the tree by the path to file/directory
	 * @param path file/directory path to select
	 * @throws IllegalArgumentException when path to select is null or empty
	 */
	public void setSelection(final String path) throws IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(path)) {
			throw new IllegalArgumentException("Path to select can-t be null or empty"); 
		}
		else {
			findAndSelect((JFileItemDescriptorNode)getModel().getRoot(), path);
		}
	}

	/**
	 * <p>Get selected item from the tree</p>
	 * @return item selected or null if no selection available
	 */
	public JFileItemDescriptor getSelectedItem() {
		if (!isSelectionEmpty()) {
			return (JFileItemDescriptor)((JFileItemDescriptorNode)getLastSelectedPathComponent()).getUserObject();
		}
		else {
			return null;
		}
	}
	
	/**
	 * <p>Add new file item descriptor to the path</p>
	 * @param path path to add content to. Can't be null or empty and must points to directory
	 * @param desc file descriptor to add
	 * @return true if descriptor was added successfully, false otherwise (for example file name is not available for the filter list if the tree navigator)
	 * @throws IllegalArgumentException path to add element to is null, empty, not exists or is not a directory 
	 * @throws NullPointerException descriptor to add is null
	 * @see #JFileTree(FileSystemInterface, boolean, FilterCallback...)
	 * @see #JFileTree(LoggerFacade, FileSystemInterface, boolean, FilterCallback...)
	 */
	public boolean add(final String path, final JFileItemDescriptor desc) throws IllegalArgumentException, NullPointerException {
		return add(path, desc, !desc.isDirectory());
	}
	
	/**
	 * <p>Add new file item descriptor to the path</p>
	 * @param path path to add content to. Can't be null or empty and must points to directory
	 * @param desc file descriptor to add
	 * @param isLeaf mark descriptor to add as leaf
	 * @return true if descriptor was added successfully, false otherwise (for example file name is not available for the filter list if the tree navigator)
	 * @throws IllegalArgumentException path to add element to is null, empty, not exists or is not a directory 
	 * @throws NullPointerException descriptor to add is null
	 * @see #JFileTree(FileSystemInterface, boolean, FilterCallback...)
	 * @see #JFileTree(LoggerFacade, FileSystemInterface, boolean, FilterCallback...)
	 */
	public boolean add(final String path, final JFileItemDescriptor desc, final boolean isLeaf) throws IllegalArgumentException, NullPointerException {
		if (Utils.checkEmptyOrNullString(path)) {
			throw new IllegalArgumentException("Path to add element to can't be null or empty"); 
		}
		else if (desc == null) {
			throw new NullPointerException("Descriptor to add can't be null");
		}
		else {
			final TreeNode[] 	found = find((JFileItemDescriptorNode)getModel().getRoot(), path);
			
			if (found == null || found.length == 0) {
				throw new IllegalArgumentException("Path to add ["+path+"] not found in the tree"); 
			}
			else {
				final JFileItemDescriptorNode	node = (JFileItemDescriptorNode)found[found.length-1];
						
				if (!((JFileItemDescriptor)node.getUserObject()).isDirectory()) {
					throw new IllegalArgumentException("Path to add ["+path+"] is not a directory!"); 
				}
				else if (desc.isDirectory() || showFiles && (filter.length == 0 || filterPattern.matcher(desc.getName()).matches())) {
					final List<JFileItemDescriptorNode> list = new ArrayList<>();
					
					for(int index = 0; index < node.getChildCount(); index++) {
						list.add((JFileItemDescriptorNode)node.getChildAt(index));
					}
					list.add(new JFileItemDescriptorNode(desc));
					sortAndAdd(node, list);
					return true;
				}
				else {
					return false;
				}
			}
		}
	}

	/**
	 * <p>Remove subtree from tree</p>
	 * @param path path to remove from tree. Can't be null or empty
	 * @throws IllegalArgumentException path to add element to is null, empty or not exists 
	 */
	public void removeSubtree(final String path) throws IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(path)) {
			throw new IllegalArgumentException("Path to remove subtree can't be null or empty"); 
		}
		else {
			final TreeNode[] 	found = find((JFileItemDescriptorNode)getModel().getRoot(), path);
			
			if (found == null || found.length == 0) {
				throw new IllegalArgumentException("Path to remove subtree ["+path+"] not found in the tree"); 
			}
			else {
				((DefaultTreeModel)getModel()).removeNodeFromParent((DefaultMutableTreeNode)found[found.length-1]);
			}
		}
	}	
	
	@Override
	public String getToolTipText(final MouseEvent event) {
		final TreePath	path = getClosestPathForLocation(event.getX(), event.getY());
		
		if (path != null) {
			final JFileItemDescriptor	desc = (JFileItemDescriptor) ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
			final StringBuilder				sb = new StringBuilder();
			
			try {
				sb.append(URLDecoder.decode(desc.getPath(),PureLibSettings.DEFAULT_CONTENT_ENCODING));
			} catch (UnsupportedEncodingException e) {
				logger.message(Severity.warning, e.getLocalizedMessage(), e);
			}
			return sb.toString();
		}
		else {
			return super.getToolTipText(event);
		}
	}
	
	@Override
	public boolean hasFileContentNow() {
		return true;
	}

	@Override
	public Collection<JFileItemDescriptor> getFileContent() {
		final List<JFileItemDescriptor>	result = new ArrayList<>();
		
		walk((DefaultMutableTreeNode) getModel().getRoot(),result);
		return result;
	}

	private void walk(final DefaultMutableTreeNode item, final List<JFileItemDescriptor> target) {
		target.add((JFileItemDescriptor)item.getUserObject());
		for (int index = 0, maxIndex = item.getChildCount(); index < maxIndex; index++) {
			walk((DefaultMutableTreeNode)item.getChildAt(index), target);
		}
	}

	@Override
	public boolean hasSelectedFileContentNow() {
		return getSelectionCount() > 0;
	}

	@Override
	public Collection<JFileItemDescriptor> getSelectedFileContent() {
		final List<JFileItemDescriptor>	result = new ArrayList<>();
		
		if (hasSelectedFileContentNow()) {
			result.add((JFileItemDescriptor)((DefaultMutableTreeNode)getSelectionPath().getLastPathComponent()).getUserObject());
		}
		return result;
	}

	private void fillChildren(final JFileItemDescriptorNode node, final FileSystemInterface fsi) throws IOException {
		final List<JFileItemDescriptorNode>	content = new ArrayList<>();
		
		fsi.list((child)->{
			if (child.isDirectory()) {
				final JFileItemDescriptor	desc = new JFileItemDescriptor(child.getName(), child.getPath(), child.isDirectory(), child.size(), new Date(child.lastModified()));
				final boolean[]				flag = new boolean[] {false};
				
				child.list((i)->{
					return (flag[0] |= i.isDirectory()) ? ContinueMode.STOP : ContinueMode.CONTINUE; 
				});

				content.add(new JFileItemDescriptorNode(desc));
			}
			else if (showFiles && (filter.length == 0 || filterPattern.matcher(child.getName()).matches())) {
				final JFileItemDescriptor	desc = new JFileItemDescriptor(child.getName(), child.getPath(), child.isDirectory(), child.size(), new Date(child.lastModified()));
				
				content.add(new JFileItemDescriptorNode(desc));
			}
			return ContinueMode.CONTINUE;
		});

		sortAndAdd(node, content);
	}

	private void findAndSelect(final JFileItemDescriptorNode node, final String path) {
		final TreeNode[]	selectionPath = find(node, path);
		
		if (selectionPath != null && selectionPath.length > 0) {
			fastRefresh = true;
			setSelectionPath(new TreePath(selectionPath));
		}
	}	
	
	private TreeNode[] find(final JFileItemDescriptorNode node, final String path) {
		for (int index = 0, maxIndex = node.getChildCount(); index < maxIndex; index++) {
			final JFileItemDescriptorNode	item = (JFileItemDescriptorNode)node.getChildAt(index);
			final JFileItemDescriptor	desc = (JFileItemDescriptor) item.getUserObject(); 
			
			if (Objects.equals(desc.getPath(),path)) {
				return ((JFileItemDescriptorNode)node.getChildAt(index)).getPath();
			}
			else if (path.startsWith(desc.getPath())) {
				final TreeNode[] result = find(item, path);
				
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private void sortAndAdd(final JFileItemDescriptorNode node, final List<JFileItemDescriptorNode> content) {
		content.sort((c1,c2)->((Comparable<JFileItemDescriptor>)c1.getUserObject()).compareTo((JFileItemDescriptor)c2.getUserObject()));
		node.removeAllChildren();
		for(JFileItemDescriptorNode item : content) {
			node.add(item);
		}
		content.clear();
		((DefaultTreeModel)getModel()).reload(node);
	}
	
	private static String buildFilterPattern(final FilterCallback[] filters) {
		if (filters.length == 0) {
			return ".*";
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			
			for (FilterCallback item : filters) {
				for (String mask : item.getFileMask()) {
					sb.append('|').append(Utils.fileMask2Regex(mask));
				}
			}
			return sb.substring(1);
		}
	}
	
	private static class JFileItemDescriptorNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = -7778314375257616437L;

		public JFileItemDescriptorNode(final JFileItemDescriptor desc) {
			super(desc, desc.isDirectory());
		}
		
		@Override
		public boolean isLeaf() {
			return !allowsChildren;
		}
	}
}
