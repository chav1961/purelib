package chav1961.purelib.ui.swing.useful.editors;

import java.awt.Component;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

import chav1961.purelib.ui.swing.interfaces.SwingItemEditor;

public class NumericEditor<R> implements SwingItemEditor<Number, R> {
	private static final Set<Class<?>>	SUPPORTED_EDITORS = Set.of(TableCellEditor.class, TreeCellEditor.class);
	
	public NumericEditor() {
	}


	@Override
	public boolean canServe(final Class<Number> class2Edit, final Class<R> editorType, final Object... options) {
		if (class2Edit == null) {
			throw new NullPointerException("Class to edit descriptor can't be null"); 
		}
		else if (editorType == null) {
			throw new NullPointerException("Editor type can't be null"); 
		}
		else if (class2Edit.isArray()) {
			return canServe((Class<Number>) class2Edit.getComponentType(), editorType, options);
		}
		else {
			return Number.class.isAssignableFrom(class2Edit) && SUPPORTED_EDITORS.contains(editorType); 
		}
	}

	@Override
	public R getEditor(final Class<R> editorType, final Object... options) {
		if (editorType == null) {
			throw new NullPointerException("Editor type can't be null"); 
		}
		else if (TableCellEditor.class.isAssignableFrom(editorType)) {
			return (R) new DefaultCellEditor(new JTextField()) {
				private static final long serialVersionUID = 0L;

			};
		}
		else {
			throw new UnsupportedOperationException("Required cell editor ["+editorType+"] is not supported yet");
		}
	}

}
