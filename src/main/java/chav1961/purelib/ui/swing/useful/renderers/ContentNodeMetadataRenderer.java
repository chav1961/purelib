package chav1961.purelib.ui.swing.useful.renderers;

import java.awt.Color;
import java.awt.Component;
import java.net.MalformedURLException;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.ReferenceAndComment;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;

public class ContentNodeMetadataRenderer<T, R> implements SwingItemRenderer<ContentNodeMetadata, R> {
	private static final Set<Class<?>>	SUPPORTED_RENDERERDS = Set.of(TableCellRenderer.class, ListCellRenderer.class, TreeCellRenderer.class);

	public ContentNodeMetadataRenderer() {
	}
	
	@Override
	public boolean canServe(Class<ContentNodeMetadata> class2Render, Class<R> rendererType, Object... options) {
		if (class2Render == null) {
			throw new NullPointerException("Class to render descriptor can't be null"); 
		}
		else if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (class2Render.isArray()) {
			return canServe((Class<ContentNodeMetadata>) class2Render.getComponentType(), rendererType, options);
		}
		else {
			return ContentNodeMetadata.class.isAssignableFrom(class2Render) && SUPPORTED_RENDERERDS.contains(rendererType); 
		}
	}

	@Override
	public R getRenderer(final Class<R> rendererType, final FieldFormat ff, final Object... options) {
		if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (TreeCellRenderer.class.isAssignableFrom(rendererType)) {
			return (R) new DefaultTreeCellRenderer() {
				private static final long serialVersionUID = 0L;
				
				@Override
			    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
					final ContentNodeMetadata	md = (ContentNodeMetadata)((DefaultMutableTreeNode)value).getUserObject();
					final JLabel				label = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
					final LocalizerOwner		localizerOwner = (LocalizerOwner)SwingUtils.getNearestOwner(tree, LocalizerOwner.class);

					if (localizerOwner != null) {
						final Localizer		l = localizerOwner.getLocalizer();
						
						if (l.containsKey(md.getLabelId())) {
							label.setText(l.getValue(md.getLabelId()));
						}
						else {
							label.setText(md.getLabelId());
						}
					}
					if (md.getIcon() != null) {
						try{
							label.setIcon(new ImageIcon(md.getIcon().toURL()));
						} catch (MalformedURLException e) {
							label.setIcon(null);
						}
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
