package chav1961.purelib.ui.swing.useful.renderers;

import java.awt.Component;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.ui.interfaces.PureLibStandardIcons;
import chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;
import chav1961.purelib.ui.swing.useful.JFileItemDescriptor;

public class JFileItemDescriptorRenderer<R> implements SwingItemRenderer<JFileItemDescriptor, R> {
	private static final Set<Class<?>>	SUPPORTED_RENDERERDS = Set.of(/*TableCellRenderer.class, ListCellRenderer.class,*/ TreeCellRenderer.class);
	protected static final Icon			DIR_ICON = PureLibStandardIcons.DIRECTORY.getIcon();
	protected static final Icon			DIR_ICON_OPENED = PureLibStandardIcons.DIRECTORY_OPENED.getIcon();
	protected static final Icon			FILE_ICON = PureLibStandardIcons.FILE.getIcon();
	
	public JFileItemDescriptorRenderer() {
	}

	@Override
	public boolean canServe(Class<JFileItemDescriptor> class2Render, Class<R> rendererType, Object... options) {
		if (class2Render == null) {
			throw new NullPointerException("Class to render descriptor can't be null"); 
		}
		else if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (class2Render.isArray()) {
			return canServe((Class<JFileItemDescriptor>) class2Render.getComponentType(), rendererType, options);
		}
		else {
			return JFileItemDescriptor.class.isAssignableFrom(class2Render) && SUPPORTED_RENDERERDS.contains(rendererType); 
		}
	}

	@Override
	public R getRenderer(Class<R> rendererType, final FieldFormat ff, Object... options) {
		if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
//		else if (ListCellRenderer.class.isAssignableFrom(rendererType)) {
//			return (R) new DefaultListCellRenderer() {
//				private static final long serialVersionUID = 0L;
//
//				@Override
//				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//					return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//				}
//			};
//		}
		else if (TreeCellRenderer.class.isAssignableFrom(rendererType)) {
			return (R) new DefaultTreeCellRenderer() {
				private static final long serialVersionUID = 1L;
	
				@Override
				public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
					final JLabel				label = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
					final JFileItemDescriptor	desc = (JFileItemDescriptor) ((DefaultMutableTreeNode)value).getUserObject();
					
					try {final String	path = URLDecoder.decode(desc.getName(), PureLibSettings.DEFAULT_CONTENT_ENCODING);
					
						label.setText(path.endsWith("/") ? path : path.substring(path.lastIndexOf('/')+1));
						label.setIcon(desc.isDirectory() ? (expanded ? DIR_ICON_OPENED : DIR_ICON) : FILE_ICON);
					} catch (UnsupportedEncodingException e) {
						label.setText("I/O error: "+e.getLocalizedMessage());
					}
					return label;
				}
			};
		}
		else {
			throw new UnsupportedOperationException("Required cell renderer ["+rendererType+"] is not supported yet");
		}
	}
}
