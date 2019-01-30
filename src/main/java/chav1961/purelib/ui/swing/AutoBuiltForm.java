package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.SpringLayout;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.ConvertorInterface;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.LocalizerFactory.FillLocalizedContentCallback;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.SimpleContentMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.AbstractLowLevelFormFactory.FieldDescriptor;
import chav1961.purelib.ui.FormFieldFormat;
import chav1961.purelib.ui.LabelAndField;
import chav1961.purelib.ui.UIUtils;
import chav1961.purelib.ui.interfacers.Action;
import chav1961.purelib.ui.interfacers.FormManager;
import chav1961.purelib.ui.interfacers.Format;
import chav1961.purelib.ui.interfacers.MultiAction;
import chav1961.purelib.ui.interfacers.RefreshMode;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.useful.LabelledLayout;

/**
 * <p>This class is a simplest for builder for any class. It supports the {@linkplain FormManager} interface to process user actions in the generated form.</p>
 * <p>Form was build has two columns:</p>
 * <ul>
 * <li>field label<li>      
 * <li>field to input content<li>
 * </ul>
 * <p>Fields of the class to show must be annotated with {@linkplain LocaleResource} annotations. These annotations are source for the field labels and field tooltips 
 * in the form built. Form can also contains a set of buttons to process actions on it. These buttons are not represents as fields in the class to show, but as 
 * {@linkplain Action} annotation before class description. Any user actions on the form showing fire {@linkplain FormManager#onField(Object, Object, String, Object)} or
 * {@linkplain FormManager#onAction(Object, Object, String, Object)} calls on the {@linkplain FormManager} interface. Yo simplify code, it's recommended that class to show
 * also implements {@linkplain FormManager} interface itself.</p>
 * <p>Form built doesn't contain any predefined buttons ("OK", "Cancel" and so on). You must close this form yourself</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */


public class AutoBuiltForm<T> extends JPanel implements LocaleChangeListener, AutoCloseable {
	private static final long 				serialVersionUID = 4920624779261769348L;
	private static final int				GAP_SIZE = 5; 
	
	private final Localizer					localizer, personalLocalizer;
	private final FormManager<Object,T>		formManager;
	private final ContentMetadataInterface	mdi;
	private final LightWeightListenerList<ActionListener>	listeners = new LightWeightListenerList<>(ActionListener.class);
	private boolean							closed = false, localizerPushed = false;

	@FunctionalInterface
	/**
	 * <p>This interface describes simple cell distributor for the forms built</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public interface ItemDistributor {
		/**
		 * <p>Get cell distribution parameters for the given element</p>
		 * @param sequential sequential number of the element in the class to show 
		 * @param desc field descriptor for the given element
		 * @return <p>integer array with cell description:</p>
		 * <ul>
		 * <li>cell row (index 0) for the left top of the element</li>
		 * <li>cell column (index 1) for the left top of the element</li>
		 * <li>cell width to occupate (index 2)</li>
		 * <li>cell height to occupate (index 3)</li>
		 * </ul>
		 * <p>All content is measured in the 'units', not pixels or any other</p>
		 */
		int[] getXY(int sequential, FieldDescriptor desc);
	}
	
	public AutoBuiltForm(final Localizer localizer, final T instance, final FormManager<Object,T> formMgr) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		this(localizer, null, instance, formMgr, 1);
	}

	public AutoBuiltForm(final Localizer localizer, final T instance, final FormManager<Object,T> formMgr, final int columns) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		this(localizer, null, instance, formMgr, columns);
	}

	public AutoBuiltForm(final Localizer localizer, final URL leftIcon, final T instance, final FormManager<Object,T> formMgr) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		this(localizer, leftIcon, instance, formMgr, 1);
	}

	public AutoBuiltForm(final Localizer localizer, final URL leftIcon, final T instance, final FormManager<Object,T> formMgr, final int numberOfBars) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (instance == null) {
			throw new NullPointerException("Instance can't be null");
		}
		else if (formMgr == null) {
			throw new NullPointerException("Form manager can't be null");
		}
		else if (numberOfBars < 1) {
			throw new IllegalArgumentException("Bars count must be positive");
		}
		else {
			final BorderLayout				totalLayout = new BorderLayout(GAP_SIZE, GAP_SIZE);
			final JPanel					childPanel = new JPanel(new LabelledLayout(numberOfBars, GAP_SIZE, GAP_SIZE, LabelledLayout.VERTICAL_FILLING));
			final Class<?>					instanceClass = instance.getClass();
			
			this.mdi = ContentModelFactory.forAnnotatedClass(instanceClass);
			this.formManager = formMgr;
			if (mdi.getRoot().getLocalizerAssociated() != null) {
				if (!localizer.containsLocalizerHere(mdi.getRoot().getLocalizerAssociated().toString())) {
					try{this.personalLocalizer = LocalizerFactory.getLocalizer(mdi.getRoot().getLocalizerAssociated());
						this.localizer = localizer.push(this.personalLocalizer);
						this.localizerPushed = true;
					} catch (IOException e) {
						throw new LocalizationException(e.getLocalizedMessage(),e);
					}
				}
				else {
					this.localizer = localizer;
					this.personalLocalizer = localizer.getLocalizerById(mdi.getRoot().getLocalizerAssociated().toString());
				}
			}
			else {
				this.localizer = localizer;
				this.personalLocalizer = null;
			}

			mdi.walkDown((mode,applicationPath,uiPath,node)->{
				if (mode == NodeEnterMode.ENTER) {
					final JLabel		label = new JLabel();
					final JTextField	field = new JTextField();
					
					label.setName(node.getUIPath().toString()+"/label");
					label.add(childPanel,LabelledLayout.LABEL_AREA);
					field.setName(node.getUIPath().toString()+"/field");
					label.add(childPanel,LabelledLayout.CONTENT_AREA);
				}
				return ContinueMode.CONTINUE;
			}, mdi.getRoot().getUIPath());

			setLayout(totalLayout);
			childPanel.validate();
			add(childPanel,BorderLayout.CENTER);
			
			if (leftIcon != null) {
				add(new JLabel(new ImageIcon(leftIcon)),BorderLayout.WEST);
			}
			fillLocalizedStrings();
		}
	}


	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException, NullPointerException {
		if (oldLocale == null) {
			throw new NullPointerException("Old locale can't be null");
		}
		else if (newLocale == null) {
			throw new NullPointerException("New locale can't be null");
		}
		else {
			fillLocalizedStrings();
		}
	}

	@Override
	public void close() {
		if (!closed){
			closed = true;
		}
	}
	
	public Localizer getLocalizerAssociated() {
		return personalLocalizer != null ? personalLocalizer : localizer;
	}

	public FormManager<Object,T> getFormManagerAssociated() {
		return formManager;
	}
	
	public boolean doClick(final String actionCommand) {
		if (actionCommand == null || actionCommand.isEmpty()) {
			throw new IllegalArgumentException("Action command string can't be null or empty");
		}
		else {
//			for (ActionButton item : actionList) {
//				if (actionCommand.equals(item.button.getActionCommand())) {
//					item.button.doClick();
//					return true;
//				}
//			}
			return false;
		}
	}
	
	public void addActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener can't be null"); 
		}
		else {
			listeners.addListener(listener);
		}
	}
	
	public void removeActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener can't be null"); 
		}
		else {
			listeners.removeListener(listener);
		}
	}

	private void fillLocalizedStrings() {
		mdi.walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER) {
				final JLabel		label = (JLabel) SwingUtils.findComponentByName(this,node.getUIPath().toString()+"/label");
				final JTextField	field = (JTextField) SwingUtils.findComponentByName(this,node.getUIPath().toString()+"/field");

				try{label.setText(localizer.getValue(node.getLabelId()));
					field.setToolTipText(localizer.getValue(node.getTooltipId()));
				} catch (LocalizationException e) {
				}
			}
			return ContinueMode.CONTINUE;
		}, mdi.getRoot().getUIPath());
	}
	
	
	private static class ActionButton {
		final JButton			button;
		final String			captionId;
		final String			tooltipId;
		final boolean 			simulateCheck;
		
		public ActionButton(JButton button, String captionId, String tooltipId, boolean simulateCheck) {
			this.button = button;
			this.captionId = captionId;
			this.tooltipId = tooltipId;
			this.simulateCheck = simulateCheck;
		}

		@Override
		public String toString() {
			return "ActionButton [button=" + button + ", captionId=" + captionId + ", tooltipId=" + tooltipId + ", simulateCheck=" + simulateCheck + "]";
		}
	}
	
	private static class DefaultDataConvertor implements ConvertorInterface {
		@Override
		public <T> T convertTo(final Class<T> awaited, final Object source) throws NullPointerException, ContentException {
			if (source == null) {
				return null;
			}
			else if (awaited.isAssignableFrom(source.getClass())) {
				return (T)source;
			}
			else if (awaited.isPrimitive()) {
				return (T) convertTo(Utils.primitive2Wrapper(awaited),source);
			}
			else {
				try{return (T) awaited.getMethod("valueOf",String.class).invoke(null,source.toString());
				} catch (InvocationTargetException e) {
					throw new ContentException(e.getTargetException().getClass().getSimpleName()+": "+e.getTargetException().getLocalizedMessage(),e);
				} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
					throw new ContentException(e.getClass().getSimpleName()+": "+e.getLocalizedMessage(),e);
				}
			}
		}
	}
	
	@FunctionalInterface
	private interface RefreshRecordCallback<T> {
		void refreshRecord(T instance) throws ContentException;
	}
	
	private static class FieldProcessor<T> {
		private final LabelAndField			labelAndField;
		private final T						instance;
		private final ConvertorInterface	conv;
		private final FormManager<Object,T>	formMgr;
		private Object						oldValue;
		
		FieldProcessor(final LabelAndField labelAndField, final T instance, final ConvertorInterface conv, final FormManager<Object,T> formMgr) throws ContentException {
			this.labelAndField = labelAndField;
			this.instance = instance;
			this.conv = conv;
			this.formMgr = formMgr;
			this.oldValue = labelAndField.fieldDesc.getFieldValue(instance);
		}
		
		boolean processFieldValueChanging(final RefreshRecordCallback<T> callback){
			final RefreshMode	mode;
			try{if (labelAndField.field instanceof JComponentInterface) {
					final Object	convertedValue = ((JComponentInterface)labelAndField.field).getChangedValueFromComponent();
				
					labelAndField.fieldDesc.setFieldValue(instance,conv.convertTo(labelAndField.fieldDesc.fieldType,convertedValue));
					switch (mode = formMgr.onField(instance,null,labelAndField.fieldDesc.field.getName(),oldValue)) {
						case REJECT	:
							labelAndField.fieldDesc.setFieldValue(instance,oldValue);
							return false;
						case NONE : 
							return true;
						case FIELD_ONLY : 
							oldValue = labelAndField.fieldDesc.getFieldValue(instance);
							return true;
						case RECORD_ONLY : case TOTAL :
							callback.refreshRecord(instance);
							return true;
						default : throw new UnsupportedOperationException("Refresh mode ["+mode+"] is not supported yet"); 
					}
				}
				else {
					throw new UnsupportedOperationException("Field value not implements JComponentInterface: "+labelAndField);
				}
			} catch (FlowException | ContentException | LocalizationException exc) {
				formMgr.getLogger().message(Severity.error,exc,exc.getLocalizedMessage());
				try{labelAndField.fieldDesc.setFieldValue(instance,oldValue);
				} catch (ContentException exc1) {
				}
				return false;
			}
		}
	}
	
}
