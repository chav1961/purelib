package chav1961.purelib.ui.swing.useful.editors;

import java.net.URI;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
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
			try{final Class<?>				clazz = options.length > 0 && (options[0] instanceof Class) ? (Class)options[0] : Number.class;
				final FieldFormat			ff = options.length > 1 && (options[1] instanceof FieldFormat) ? (FieldFormat)options[1] : null;
				final URI					app = URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/Number/field?visibility=package");
				final ContentNodeMetadata	mcnd = new MutableContentNodeMetadata("field", clazz, "./field", URI.create(PureLibLocalizer.LOCALIZER_SCHEME_STRING), "testSet1", null, null, ff, app, null); 
				final JComponentMonitor		jcm = (event, metadata, component, parameters)->true;
				final JTextField 			tf = (JTextField) SwingUtils.prepareRenderer(mcnd, PureLibSettings.PURELIB_LOCALIZER, FieldFormat.ContentType.NumericContent, jcm);
				
				return (R) new DefaultCellEditor(tf);
			} catch (SyntaxException e) {
				throw new PreparationException(e.getLocalizedMessage(), e);
			}
		}
		else {
			throw new UnsupportedOperationException("Required cell editor ["+editorType+"] is not supported yet");
		}
	}
}
