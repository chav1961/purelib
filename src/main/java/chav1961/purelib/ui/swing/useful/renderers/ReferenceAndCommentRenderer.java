package chav1961.purelib.ui.swing.useful.renderers;

import java.awt.Component;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import chav1961.purelib.ui.interfaces.ReferenceAndComment;
import chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;

public class ReferenceAndCommentRenderer<T, R> implements SwingItemRenderer<ReferenceAndComment, R> {
	private static final Set<Class<?>>	SUPPORTED_RENDERERDS = Set.of(TableCellRenderer.class, ListCellRenderer.class, TreeCellRenderer.class);

	public ReferenceAndCommentRenderer() {
	}

	@Override
	public boolean canServe(final Class<ReferenceAndComment> class2Render, final Class<R> rendererType, final Object... options) {
		if (class2Render == null) {
			throw new NullPointerException("Class to render descriptor can't be null"); 
		}
		else if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (class2Render.isArray()) {
			return canServe((Class<ReferenceAndComment>) class2Render.getComponentType(), rendererType, options);
		}
		else {
			return ReferenceAndComment.class.isAssignableFrom(class2Render) && SUPPORTED_RENDERERDS.contains(rendererType); 
		}
	}

	@Override
	public R getRenderer(final Class<R> rendererType, final Object... options) {
		if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (ListCellRenderer.class.isAssignableFrom(rendererType)) {
			return (R) new DefaultListCellRenderer() {
				private static final long serialVersionUID = 0L;
				
				@Override
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					final ReferenceAndComment	sel = (ReferenceAndComment)value;
					final JLabel				label;
					
					if (sel.getReference() != null) {
						label = (JLabel) super.getListCellRendererComponent(list, sel.getReference().toString(), index, isSelected, cellHasFocus);
					}
					else {
						label = new JLabel();
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
