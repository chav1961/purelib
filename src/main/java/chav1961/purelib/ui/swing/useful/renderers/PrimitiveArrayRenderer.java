package chav1961.purelib.ui.swing.useful.renderers;

import java.awt.Component;
import java.lang.reflect.Array;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
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

public class PrimitiveArrayRenderer<T,R> implements SwingItemRenderer<T, R> {
	private static final Set<Class<?>>	SUPPORTED_RENDERERDS = Set.of(TableCellRenderer.class, ListCellRenderer.class, TreeCellRenderer.class);

	public PrimitiveArrayRenderer() {
	}

	@Override
	public boolean canServe(Class<T> class2Render, Class<R> rendererType, Object... options) {
		if (class2Render == null) {
			throw new NullPointerException("Class to render descriptor can't be null"); 
		}
		else if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else {
			return class2Render.isArray() && class2Render.getComponentType().isPrimitive();
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
					final JLabel		label = (JLabel)super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);

					fillLabel(label, val);
					label.setIcon(null);
					return label;
				}
			};
		}
		else if (TableCellRenderer.class.isAssignableFrom(rendererType)) {
			return (R) new DefaultTableCellRenderer() {
				private static final long serialVersionUID = 0L;
				
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					final JLabel		label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

					fillLabel(label, value);
					label.setIcon(null);
					return label;
				}
			};
		}
		else {
			throw new UnsupportedOperationException("Required cell renderer ["+rendererType+"] is not supported yet");
		}
	}
	
	private static void fillLabel(final JLabel label, final Object val) {
		final StringBuilder	sb = new StringBuilder("<html><body>");
		
		for(int arrIndex = 0, maxArrIndex = Array.getLength(val); arrIndex < maxArrIndex; arrIndex++) {
			sb.append("<p><b>");
			switch (CompilerUtils.defineClassType(val.getClass().getComponentType())) {
				case CompilerUtils.CLASSTYPE_BYTE	:
					sb.append(Array.getByte(val, arrIndex));
					break;
				case CompilerUtils.CLASSTYPE_SHORT	:
					sb.append(Array.getShort(val, arrIndex));
					break;
				case CompilerUtils.CLASSTYPE_CHAR	:	
					sb.append(Array.getChar(val, arrIndex));
					break;
				case CompilerUtils.CLASSTYPE_INT	:	
					sb.append(Array.getInt(val, arrIndex));
					break;
				case CompilerUtils.CLASSTYPE_LONG	:	
					sb.append(Array.getLong(val, arrIndex));
					break;
				case CompilerUtils.CLASSTYPE_FLOAT	:	
					sb.append(Array.getFloat(val, arrIndex));
					break;
				case CompilerUtils.CLASSTYPE_DOUBLE	:	
					sb.append(Array.getDouble(val, arrIndex));
					break;
				case CompilerUtils.CLASSTYPE_BOOLEAN:	
					sb.append(Array.getBoolean(val, arrIndex));
					break;
				default :
			}
			sb.append("</b></p>");
			label.setText(sb.append("</body></html>").toString());
			label.setHorizontalAlignment(JLabel.RIGHT);
		}
	}
}
