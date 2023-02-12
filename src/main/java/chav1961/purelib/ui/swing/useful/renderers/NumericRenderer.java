package chav1961.purelib.ui.swing.useful.renderers;

import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;

public class NumericRenderer<R> implements SwingItemRenderer<Number, R> {
	private static final Set<Class<?>>	SUPPORTED_RENDERERDS = Set.of(TableCellRenderer.class, ListCellRenderer.class, TreeCellRenderer.class);
	
	public NumericRenderer() {
	}

	@Override
	public boolean canServe(Class<Number> class2Render, Class<R> rendererType, Object... options) {
		if (class2Render == null) {
			throw new NullPointerException("Class to render descriptor can't be null"); 
		}
		else if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (class2Render.isArray()) {
			return canServe((Class<Number>) class2Render.getComponentType(), rendererType, options);
		}
		else if (class2Render.isPrimitive()) {
			return canServe((Class<Number>) CompilerUtils.toWrappedClass(class2Render), rendererType, options);
		}
		else {
			return (Number.class.isAssignableFrom(class2Render) || BigInteger.class.isAssignableFrom(class2Render) || BigDecimal.class.isAssignableFrom(class2Render)) && SUPPORTED_RENDERERDS.contains(rendererType); 
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
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				}
			};
		}
		else if (TableCellRenderer.class.isAssignableFrom(rendererType)) {
			return (R) new DefaultTableCellRenderer() {
				private static final long serialVersionUID = 0L;

				@Override
				public Component getTableCellRendererComponent(JTable list, Object value, boolean isSelected, boolean cellHasFocus, int row, int column) {
					final JLabel	label = (JLabel)super.getTableCellRendererComponent(list, value, isSelected, cellHasFocus, row, column);
					final Font		labelFont = label.getFont();
					
					label.setFont(new Font(labelFont.getName(), labelFont.getStyle() | Font.BOLD, labelFont.getSize()));
					label.setHorizontalAlignment(JLabel.RIGHT);
					return label;
				}
			};
		}
		else {
			throw new UnsupportedOperationException("Required cell renderer ["+rendererType+"] is not supported yet");
		}
	}


}
