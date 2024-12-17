package chav1961.purelib.ui.swing.useful.renderers;

import java.awt.Component;
import javax.swing.Icon;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;

public class IconRenderer<R> implements SwingItemRenderer<Icon, R> {
	private static final Set<Class<?>>	SUPPORTED_RENDERERDS = Set.of(TableCellRenderer.class, ListCellRenderer.class, TreeCellRenderer.class);

	public IconRenderer() {
	}

	@Override
	public boolean canServe(Class<Icon> class2Render, Class<R> rendererType, Object... options) {
		if (class2Render == null) {
			throw new NullPointerException("Class to render descriptor can't be null"); 
		}
		else if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (class2Render.isArray()) {
			return canServe((Class<Icon>) class2Render.getComponentType(), rendererType, options);
		}
		else {
			return Icon.class.isAssignableFrom(class2Render) && SUPPORTED_RENDERERDS.contains(rendererType); 
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
				public Component getListCellRendererComponent(final JList<?> list, final Object val, final int index, final boolean isSelected, final boolean cellHasFocus) {
					final JLabel	label = (JLabel)super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
					
					label.setIcon((Icon)val);
					label.setText("");
					label.setToolTipText("");
					return label;
				}
			};
		}
		else if (TableCellRenderer.class.isAssignableFrom(rendererType)) {
			return (R) new DefaultTableCellRenderer() {
				private static final long serialVersionUID = 0L;
				
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					final JLabel	label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					
					label.setIcon((Icon)value);
					label.setText("");
					label.setToolTipText("");
					return label;
				}
			};
		}
		else {
			throw new UnsupportedOperationException("Required cell renderer ["+rendererType+"] is not supported yet");
		}
	}
}
