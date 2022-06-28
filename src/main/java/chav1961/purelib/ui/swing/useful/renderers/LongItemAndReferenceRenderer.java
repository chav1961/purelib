package chav1961.purelib.ui.swing.useful.renderers;

import java.awt.Component;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import chav1961.purelib.ui.interfaces.LongItemAndReference;
import chav1961.purelib.ui.interfaces.ReferenceAndComment;
import chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;

public class LongItemAndReferenceRenderer<T, R> implements SwingItemRenderer<LongItemAndReference<?>, R> {
	private static final Set<Class<?>>	SUPPORTED_RENDERERDS = Set.of(TableCellRenderer.class, ListCellRenderer.class, TreeCellRenderer.class);

	public LongItemAndReferenceRenderer() {
	}

	@Override
	public boolean canServe(final Class<LongItemAndReference<?>> class2Render, final Class<R> rendererType, final Object... options) {
		if (class2Render == null) {
			throw new NullPointerException("Class to render descriptor can't be null"); 
		}
		else if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (class2Render.isArray()) {
			return canServe((Class<LongItemAndReference<?>>) class2Render.getComponentType(), rendererType, options);
		}
		else {
			return LongItemAndReference.class.isAssignableFrom(class2Render) && SUPPORTED_RENDERERDS.contains(rendererType); 
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
					final LongItemAndReference<?>	sel = (LongItemAndReference<?>)value;
					final Object	pres = sel.getPresentation(); 
					final JLabel	label;
					
					if (pres != null) {
						final String	str = pres.toString();
						
						label = (JLabel) super.getListCellRendererComponent(list, str.isEmpty() ? "<unknown>" : str, index, isSelected, cellHasFocus);
					}
					else {
						label = new JLabel("<unknown>");
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

