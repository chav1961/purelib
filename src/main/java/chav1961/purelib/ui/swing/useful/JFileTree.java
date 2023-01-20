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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
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
	protected final DefaultMutableTreeNode	root = new DefaultMutableTreeNode();
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
					final DefaultMutableTreeNode	last = (DefaultMutableTreeNode)event.getPath().getLastPathComponent(); 
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
					final DefaultMutableTreeNode	last = (DefaultMutableTreeNode)event.getPath().getLastPathComponent(); 
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
			root.setUserObject(new JFileItemDescriptor("/", fsi.getAbsoluteURI().toString(), true, fsi.size(), new Date(fsi.lastModified())));
			fillChildren(root, fsi);
			((DefaultTreeModel)getModel()).setRoot(root);
			setDragEnabled(true);
			setTransferHandler(new FileTransferHandler());
			FileTransferHandler.prepare4DroppingFiles(this);
			
			setCellRenderer(SwingUtils.getCellRenderer(JFileItemDescriptor.class, new FieldFormat(JFileItemDescriptor.class), TreeCellRenderer.class));
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
	public abstract void placeFileContent(final Point location, final Iterable<File> content);
	
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
			findAndSelect((DefaultMutableTreeNode)getModel().getRoot(), path);
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
	public Collection<File> getFileContent() {
		final List<File>				result = new ArrayList<File>();
		
		walk((DefaultMutableTreeNode) getModel().getRoot(),result);
		return result;
	}

	private void walk(final DefaultMutableTreeNode item, final List<File> target) {
		target.add(new File(((JFileItemDescriptor)item.getUserObject()).getPath()));
		for (int index = 0, maxIndex = item.getChildCount(); index < maxIndex; index++) {
			walk((DefaultMutableTreeNode)item.getChildAt(index), target);
		}
	}

	@Override
	public boolean hasSelectedFileContentNow() {
		return getSelectionCount() > 0;
	}

	@Override
	public Collection<File> getSelectedFileContent() {
		final List<File>	result = new ArrayList<File>();
		
		if (hasSelectedFileContentNow()) {
			result.add(new File(((JFileItemDescriptor)((DefaultMutableTreeNode)getSelectionPath().getLastPathComponent()).getUserObject()).getPath()));
		}
		return result;
	}

	private void fillChildren(final DefaultMutableTreeNode node, final FileSystemInterface fsi) throws IOException {
		final List<DefaultMutableTreeNode>	content = new ArrayList<>();
		
		fsi.list((child)->{
			if (child.isDirectory()) {
				final JFileItemDescriptor	desc = new JFileItemDescriptor(child.getName(), child.getPath(), child.isDirectory(), child.size(), new Date(child.lastModified()));
				final boolean[]				flag = new boolean[] {false};
				
				child.list((i)->{
					return (flag[0] |= i.isDirectory()) ? ContinueMode.STOP : ContinueMode.CONTINUE; 
				});

				content.add(new DefaultMutableTreeNode(desc) {
					private static final long	serialVersionUID = 1L;
					private final boolean		dirsInside = flag[0];
					
					@Override
					public boolean isLeaf() {
						return !dirsInside;
					}
				});
			}
			else if (showFiles && (filter.length == 0 || filterPattern.matcher(child.getName()).matches())) {
				final JFileItemDescriptor	desc = new JFileItemDescriptor(child.getName(), child.getPath(), child.isDirectory(), child.size(), new Date(child.lastModified()));
				
				content.add(new DefaultMutableTreeNode(desc) {
					private static final long	serialVersionUID = 1L;
					
					@Override public boolean isLeaf() {return true;}
				});
			}
			return ContinueMode.CONTINUE;
		});
		
		content.sort((c1,c2)->((Comparable<JFileItemDescriptor>)c1.getUserObject()).compareTo((JFileItemDescriptor)c2.getUserObject()));
		node.removeAllChildren();
		for(DefaultMutableTreeNode item : content) {
			node.add(item);
		}
		content.clear();
	}

	private void findAndSelect(final DefaultMutableTreeNode node, final String path) {
		for (int index = 0, maxIndex = node.getChildCount(); index < maxIndex; index++) {
			final DefaultMutableTreeNode	item = (DefaultMutableTreeNode)node.getChildAt(index);
			final JFileItemDescriptor	desc = (JFileItemDescriptor) item.getUserObject(); 
			
			if (Objects.equals(desc.getPath(),path)) {
				final TreeNode[]	tn = ((DefaultMutableTreeNode)node.getChildAt(index)).getPath();
				
				fastRefresh = true;
				setSelectionPath(new TreePath(tn));
				break;
			}
			else {
				findAndSelect(item, path);
			}
		}
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
}
