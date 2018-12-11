package chav1961.purelib.ui.swing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.LabelAndField;
import chav1961.purelib.ui.UIUtils;
import chav1961.purelib.ui.interfacers.FormManager;
import chav1961.purelib.ui.interfacers.FormModel;

public class MicroTableEditor {
	private static final Set<Class<?>>	WRAPPERS = new HashSet<>();
	
	static {
		WRAPPERS.add(Byte.class);
		WRAPPERS.add(Short.class);
		WRAPPERS.add(Integer.class);
		WRAPPERS.add(Long.class);
		WRAPPERS.add(Float.class);
		WRAPPERS.add(Double.class);
		WRAPPERS.add(Character.class);
		WRAPPERS.add(Boolean.class);
		WRAPPERS.add(String.class);
	}
	
	public enum EditorRepresentation {
		EDITED_LIST, MARKED_LIST, TWO_PANEL
	}

	public interface ContentGetter<T> {
		T getContent();
	}
	
	private final Localizer				localizer;
	private final EditorRepresentation	representation;
	
	public MicroTableEditor(final Localizer localizer, final EditorRepresentation representation) throws NullPointerException {
		if (localizer == null) {
			throw new NullPointerException("Localizer reference can't be null");
		}
		else if (representation == null) {
			throw new NullPointerException("Editor representation can't be null");
		}
		else {
			this.localizer = localizer;
			this.representation = representation;
		}
	}

	public <T> JComponent build(final FormManager<?,T> formManager, final FormModel<?,T> formModel) throws LocalizationException, SyntaxException, IllegalArgumentException, NullPointerException, ContentException {
		return build(formManager,formModel,null,buildColumnList(localizer,formModel.getInstanceType()));
	}

	public <T> JComponent build(final FormManager<?,T> formManager, final FormModel<?,T> formModel, final FormModel<?,T> availableModel) throws LocalizationException, SyntaxException, IllegalArgumentException, NullPointerException, ContentException {
		return build(formManager,formModel,availableModel,buildColumnList(localizer,formModel.getInstanceType()));
	}
	
	public <T> JComponent build(final FormManager<?,T> formManager, final FormModel<?,T> formModel, final FormModel<?,T> availableModel, final String[] columns) throws LocalizationException, SyntaxException, IllegalArgumentException, NullPointerException, ContentException {
		switch (representation) {
			case EDITED_LIST	: return new MicroTableEditorEditedList(localizer,new MicroTableEditorEditableContent(localizer,formManager,formModel,columns),formModel.getOperationsSupported());
//			case MARKED_LIST	: return new MicroTableEditorMarkedList<T>(localizer,new MicroTableEditorMarkableContent<T>(columns,contentType,currentContent,availableContent));
//			case TWO_PANEL		: return new MicroTableEditorTwoPanelList<T>(localizer,contentType,new MicroTableEditorEditableContent<T>(formModel,columns,contentType,currentContent),new MicroTableEditorEditableContent<T>(formModel, columns,contentType,availableContent));
			default : throw new UnsupportedOperationException("Representation ["+representation+"] is not supported yet");
		}
	}

	private static String[] buildColumnList(final Localizer localizer, final Class<?> instanceType) throws IllegalArgumentException, NullPointerException, LocalizationException, SyntaxException, ContentException {
		if (instanceType.isPrimitive() || WRAPPERS.contains(instanceType)) {
			return new String[]{"value"};
		}
		else {
			final List<LabelAndField<Object,Object>>	list = new ArrayList<>(); 
					
			UIUtils.collectFields(localizer,instanceType,null,list,(loc,id)->{return null;},(loc,desc,tooltip,initial)->{return null;});
			
			if (list.size() == 0) {
				throw new ContentException("Content type ["+instanceType+"] to use in the MicroTableEditor is neither primitive/wrapped nor contains any fields annotated with @LocaleResource"); 
			}
			else {
				final String[]	result = new String[list.size()];
				
				for (int index = 0, maxIndex = result.length; index < maxIndex; index++) {
					result[index] = list.get(index).fieldDesc.field.getName();
				}
				return result;
			}
		}
	}
}
