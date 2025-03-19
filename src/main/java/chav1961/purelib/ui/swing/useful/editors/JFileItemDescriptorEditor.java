package chav1961.purelib.ui.swing.useful.editors;

import java.awt.Component;
import java.awt.Container;
import java.net.URI;
import java.util.Date;
import java.util.EventObject;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreeCellEditor;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.concurrent.LightWeightListenerList.LightWeightListenerCallback;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.internal.PureLibLocalizer;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.SwingItemEditor;
import chav1961.purelib.ui.swing.useful.JFileItemDescriptor;

public class JFileItemDescriptorEditor<R> implements SwingItemEditor<JFileItemDescriptor, R> {
	private static final Set<Class<?>>	SUPPORTED_EDITORS = Set.of(TreeCellEditor.class);
	
	public JFileItemDescriptorEditor() {
	}


	@Override
	public boolean canServe(final Class<JFileItemDescriptor> class2Edit, final Class<R> editorType, final Object... options) {
		if (class2Edit == null) {
			throw new NullPointerException("Class to edit descriptor can't be null"); 
		}
		else if (editorType == null) {
			throw new NullPointerException("Editor type can't be null"); 
		}
		else if (class2Edit.isArray()) {
			return canServe((Class<JFileItemDescriptor>) class2Edit.getComponentType(), editorType, options);
		}
		else {
			return JFileItemDescriptor.class.isAssignableFrom(class2Edit) && SUPPORTED_EDITORS.contains(editorType); 
		}
	}

	@Override
	public R getEditor(final Class<R> editorType, final Object... options) {
		if (editorType == null) {
			throw new NullPointerException("Editor type can't be null"); 
		}
		else if (TreeCellEditor.class.isAssignableFrom(editorType)) {
			try{final Class<?>				clazz = options.length > 0 && (options[0] instanceof Class) ? (Class<?>)options[0] : Number.class;
				final FieldFormat			ff = options.length > 1 && (options[1] instanceof FieldFormat) ? (FieldFormat)options[1] : null;
				final URI					app = URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/String/field?visibility=package");
				final ContentNodeMetadata	mcnd = new MutableContentNodeMetadata("field", String.class, "./field", URI.create(PureLibLocalizer.LOCALIZER_SCHEME_STRING), "testSet1", null, null, ff, app, null); 
				final JComponentMonitor		jcm = (event, metadata, component, parameters)->true;
				final JTextField 			tf = (JTextField) SwingUtils.prepareRenderer(mcnd, PureLibSettings.PURELIB_LOCALIZER, FieldFormat.ContentType.StringContent, jcm);
				
				return editorType.cast(new NameCellEditor(tf));
			} catch (SyntaxException e) {
				throw new PreparationException(e.getLocalizedMessage(), e);
			}
		}
		else {
			throw new UnsupportedOperationException("Required cell editor ["+editorType+"] is not supported yet");
		}
	}

	private static class NameCellEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 1L;

		private JFileItemDescriptor	desc = null;
		
		public NameCellEditor(final JTextField textField) {
			super(textField);
		}
		
		@Override
		public Object getCellEditorValue() {
			final String	name = (String)super.getCellEditorValue();
			
			return new JFileItemDescriptor(name, desc.getPath().replace("/"+desc.getName(), "/"+name), desc.isDirectory(), desc.getSize(), desc.getLastModified());
		}
		
		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
			final JTextField	tf = (JTextField)super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);

			desc = (JFileItemDescriptor)((DefaultMutableTreeNode)value).getUserObject();
			tf.setText(desc.getName());
			return tf;
		}
	}
}

