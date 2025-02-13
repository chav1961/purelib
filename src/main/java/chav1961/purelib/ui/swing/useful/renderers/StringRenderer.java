package chav1961.purelib.ui.swing.useful.renderers;

import java.awt.Component;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;

/**
 * <p>This class provides string renderers for swing components.</p> 
 * @param <R> renderer type. Available types are {@linkplain TableCellRenderer}, {@linkplain ListCellRenderer} and {@linkplain TreeCellRenderer}
 */
public class StringRenderer<R> implements SwingItemRenderer<String, R> {
	private static final Set<Class<?>>	SUPPORTED_RENDERERDS = Set.of(TableCellRenderer.class, ListCellRenderer.class, TreeCellRenderer.class);
	
	public StringRenderer() {
	}

	@Override
	public boolean canServe(final Class<String> class2Render, final Class<R> rendererType, final Object... options) {
		if (class2Render == null) {
			throw new NullPointerException("Class to render descriptor can't be null"); 
		}
		else if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (class2Render.isArray()) {
			return canServe((Class<String>) class2Render.getComponentType(), rendererType, options);
		}
		else {
			return String.class.isAssignableFrom(class2Render) && SUPPORTED_RENDERERDS.contains(rendererType); 
		}
	}

	@Override
	public R getRenderer(final Class<R> rendererType, final FieldFormat ff, final Object... options) {
		if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (ListCellRenderer.class.isAssignableFrom(rendererType)) {
			return (R) new DefaultListCellRenderer() {
				private static final long serialVersionUID = 0L;

				@Override
				public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
					return super.getListCellRendererComponent(list, value== null ? null : value.toString(), index, isSelected, cellHasFocus);
				}
			};
		}
		else if (TableCellRenderer.class.isAssignableFrom(rendererType)) {
			return (R) new DefaultTableCellRenderer() {
				private static final long serialVersionUID = 0L;

				@Override
				public Component getTableCellRendererComponent(final JTable list, final Object value, final boolean isSelected, final boolean cellHasFocus, final int row, final int column) {
					final Component	c = super.getTableCellRendererComponent(list, value, isSelected, cellHasFocus, row, column);
					
//					if (!list.isEnabled()) {
//						c.setForeground(c.getForeground().brighter());
//						c.setBackground(getBackground().darker());
//					}
//					
					return c;
				}
			};
		}
		else {
			throw new UnsupportedOperationException("Required cell renderer ["+rendererType+"] is not supported yet");
		}
	}
}
