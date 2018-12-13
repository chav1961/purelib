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
import javax.swing.JToolTip;
import javax.swing.SpringLayout;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.ConvertorInterface;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
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
	private static final long serialVersionUID = 4920624779261769348L;
	
//	private static final ItemDistributor	MULTICOLUMN_DISTRIBUTOR = null;  

	private final Localizer					localizer, personalLocalizer;
	private final FormManager<Object,T>		formManager;
	private final List<LabelAndField<JLabel,JComponent>>	fieldList = new ArrayList<>();
	private final List<ActionButton>		actionList = new ArrayList<>();
	private final List<ActionListener>		actionListeners = new ArrayList<>();
	private boolean							closed = false;

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

	public AutoBuiltForm(final Localizer localizer, final URL leftIcon, final T instance, final FormManager<Object,T> formMgr, final int columns) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		this(localizer,leftIcon,instance,formMgr,buildSimpleItemDistributor(instance,columns));
	}
	
	public AutoBuiltForm(final Localizer localizer, final URL leftIcon, final T instance, final FormManager<Object,T> formMgr, final ItemDistributor distributor) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (instance == null) {
			throw new NullPointerException("Instance can't be null");
		}
		else if (formMgr == null) {
			throw new NullPointerException("Form manager can't be null");
		}
		else if (distributor == null) {
			throw new NullPointerException("Column distributor can't be null");
		}
		else {
			final BorderLayout			totalLayout = new BorderLayout(5,5);
			final SpringLayout			layout = new SpringLayout();
			final JPanel				childPanel = new JPanel(layout);
			final Class<?>				instanceClass = instance.getClass(); 

			this.formManager = formMgr;
			if (instanceClass.isAnnotationPresent(LocaleResourceLocation.class)) {
				if (!localizer.containsLocalizerHere(instanceClass.getAnnotation(LocaleResourceLocation.class).value())) {
					try{this.personalLocalizer = LocalizerFactory.getLocalizer(URI.create(instanceClass.getAnnotation(LocaleResourceLocation.class).value()));
						this.localizer = localizer.push(this.personalLocalizer);
					} catch (IOException e) {
						throw new LocalizationException(e.getLocalizedMessage(),e);
					}
				}
				else {
					this.localizer = localizer;
					this.personalLocalizer = localizer.getLocalizerById(instanceClass.getAnnotation(LocaleResourceLocation.class).value());
				}
			}
			else {
				this.localizer = localizer;
				this.personalLocalizer = null;
			}

			UIUtils.collectFields(this.personalLocalizer != null ? this.personalLocalizer : this.localizer,instanceClass,instance,fieldList,
					(loc,id)->{return new JLabel(loc.getValue(id));},
					(loc,desc,tooltip,initialValue)->{
						final JComponent	editor = SwingUtils.prepareCellEditorComponent(loc,desc,initialValue); 
						
						editor.setToolTipText(loc.getValue(tooltip));
						return editor;
					});
			collectActions(this.personalLocalizer,instanceClass,instance,actionList);
			placeCollectedFields(childPanel,layout,fieldList,instance,formMgr,distributor);
			placeCollectedActions(childPanel,layout,actionList,instance,formMgr);

			setLayout(totalLayout);
			childPanel.validate();
			add(childPanel,BorderLayout.CENTER);
			
			if (leftIcon != null) {
				add(new JLabel(new ImageIcon(leftIcon)),BorderLayout.WEST);
			}
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
			for (LabelAndField<JLabel,JComponent> item : fieldList) {
				item.label.setText(localizer.getValue(item.labelId));
				item.field.setToolTipText(localizer.getValue(item.labelToolTipId));
			}
			for(ActionButton item : actionList) {
				item.button.setText(localizer.getValue(item.captionId));
				item.button.setToolTipText(localizer.getValue(item.tooltipId));
			}
		}
	}

	@Override
	public void close() {
		if (!closed){
			closed = true;
			synchronized(actionListeners) {
				actionListeners.clear();
			}
		}
	}
	
	public Localizer getLocalizerAssociated() {
		return personalLocalizer != null ? personalLocalizer : localizer;
	}

	public FormManager<Object,T> getFormManagerAssociated() {
		return formManager;
	}
	
	public String[] getLabelIds() throws NullPointerException {
		final List<String>	result = new ArrayList<>();
		
		for (LabelAndField item : fieldList) {
			result.add(item.labelId);
		}
		return result.toArray(new String[result.size()]);
	}

	public String[] getModifiableLabelIds() throws NullPointerException {
		final List<String>	result = new ArrayList<>();
		
		for (LabelAndField item : fieldList) {
			if (!item.fieldDesc.fieldFormat.isReadOnly()) {
				result.add(item.labelId);
			}
		}
		return result.toArray(new String[result.size()]);
	}
	
	public boolean hasActions() {
		return actionList.size() > 0;
	}

	public boolean doClick(final String actionCommand) {
		if (actionCommand == null || actionCommand.isEmpty()) {
			throw new IllegalArgumentException("Action command string can't be null or empty");
		}
		else {
			for (ActionButton item : actionList) {
				if (actionCommand.equals(item.button.getActionCommand())) {
					item.button.doClick();
					return true;
				}
			}
			return false;
		}
	}
	
	public void addActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener can't be null"); 
		}
		else {
			synchronized(actionListeners) {
				actionListeners.add(listener);
			}
		}
	}
	
	public void removeActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener can't be null"); 
		}
		else {
			synchronized(actionListeners) {
				actionListeners.remove(listener);
			}
		}
	}

	private void placeCollectedFields(final JPanel container, final SpringLayout chooseLayout, final List<LabelAndField<JLabel,JComponent>> list, final T instance, final FormManager<Object,T> formMgr, final ItemDistributor distr) throws ContentException {
		final int					columns = distr.getXY(list.size()-1,list.get(list.size()-1).fieldDesc)[1]+1; 
		final ConvertorInterface	conv = new DefaultDataConvertor();
		final int 					linesInColumn = (list.size() + columns - 1) / columns;
		final List<LabelAndField>[]	splittedList = new List[columns]; 
		
		for (int index = 0; index < list.size(); index++) {		// Split total list into parts
			final int	splittedIndex = index / linesInColumn;
			
			if (splittedList[splittedIndex] == null) {
				splittedList[splittedIndex] = new ArrayList<>();
			}
			splittedList[splittedIndex].add(list.get(index));
		}
		for (List<LabelAndField> item : splittedList) {			// Make parts size the same
			while (item.size() < linesInColumn) {
				item.add(null);
			}
		}

		JComponent		leftBound = container;
		JPanel			leftPanel = null, rightPanel = null;
		
		for (int splittedIndex = 0; splittedIndex < splittedList.length; splittedIndex++) {
			leftPanel = new JPanel(new GridLayout(linesInColumn,1));
			rightPanel = new JPanel(new GridLayout(linesInColumn,1));
	
			for (LabelAndField<JLabel,JComponent> item : splittedList[splittedIndex]) {
				if (item != null) {
					final FieldProcessor<T>	processor = new FieldProcessor<>(item,instance,conv,formMgr);
					final InputVerifier		oldVerifier = item.field.getInputVerifier();
					
					leftPanel.add(item.label);
					rightPanel.add(item.field);
					item.field.setInputVerifier(new InputVerifier() {
						@Override
						public boolean shouldYieldFocus(JComponent input) {
							return (oldVerifier == null || oldVerifier.shouldYieldFocus(input)) && super.shouldYieldFocus(input);
						}
						
						@Override
						public boolean verify(final JComponent input) {
							return (oldVerifier == null || oldVerifier.verify(input)) && processor.processFieldValueChanging((inst)->{refreshRecord(instance,list);});
						}
					});
				}
				else {
					leftPanel.add(new JLabel(" "));
					rightPanel.add(new JLabel(" "));
				}
			}
			
			container.add(leftPanel);
			container.add(rightPanel);
			
			if (leftBound == container) {
				chooseLayout.putConstraint(SpringLayout.WEST,leftPanel,0,SpringLayout.WEST,leftBound);
			}
			else {
				chooseLayout.putConstraint(SpringLayout.WEST,leftPanel,5,SpringLayout.EAST,leftBound);
			}
			chooseLayout.putConstraint(SpringLayout.WEST,rightPanel,5,SpringLayout.EAST,leftPanel);
			chooseLayout.putConstraint(SpringLayout.NORTH,leftPanel,0,SpringLayout.NORTH,rightPanel);
			chooseLayout.putConstraint(SpringLayout.SOUTH,leftPanel,0,SpringLayout.SOUTH,rightPanel);
			leftBound = rightPanel;
		}
		chooseLayout.putConstraint(SpringLayout.EAST,leftBound,0,SpringLayout.EAST,container);
	}

	private void placeCollectedActions(final JPanel container, final SpringLayout chooseLayout, final List<ActionButton> actions, final T instance, final FormManager<Object, T> formMgr) {
		final JPanel				panel = new JPanel();
		
		for (ActionButton item : actions) {
			final boolean			simulateCheck = item.simulateCheck;
			
			final ActionListener 	listener = new ActionListener() {
										@Override
										public void actionPerformed(final ActionEvent e) {
											RefreshMode	mode;

											if (simulateCheck) {
												try{switch (mode = formMgr.onRecord(FormManager.Action.CHECK,instance,null,null,null)) {
														case REJECT	:  
															return;
														case NONE	:  
															break;
														case FIELD_ONLY : case RECORD_ONLY : case TOTAL :
															refreshRecord(instance,fieldList);
															break;
														default : throw new UnsupportedOperationException("Refresh mode ["+mode+"] is not supported yet"); 
													}
												} catch (FlowException | ContentException | LocalizationException exc) {
													formMgr.getLogger().message(Severity.error,exc,exc.getLocalizedMessage());
												}
											}
											
											try{switch (mode = formMgr.onAction(instance,null,item.button.getActionCommand(),null)) {
													case DEFAULT :
														final ActionListener[]	refs;
														
														synchronized(actionListeners) {
															refs = actionListeners.toArray(new ActionListener[actionListeners.size()]);
														}
														for (ActionListener item : refs) {
															try {item.actionPerformed(e);																
															} catch (Exception exc) {
															}
														}
														break;
													case REJECT	: case NONE :
														break;
													case FIELD_ONLY : case RECORD_ONLY : case TOTAL :
														refreshRecord(instance,fieldList);
														break;
													default : throw new UnsupportedOperationException("Refresh mode ["+mode+"] is not supported yet"); 
												}
											} catch (FlowException | ContentException | LocalizationException exc) {
												formMgr.getLogger().message(Severity.error,exc,exc.getLocalizedMessage());
											}
										}
									};
			panel.add(item.button);
			item.button.addActionListener(listener);
		}
		container.add(panel);
		chooseLayout.putConstraint(SpringLayout.WEST,panel,0,SpringLayout.WEST,container);
		chooseLayout.putConstraint(SpringLayout.EAST,panel,0,SpringLayout.EAST,container);
		chooseLayout.putConstraint(SpringLayout.SOUTH,panel,0,SpringLayout.SOUTH,container);
	}
	
	private void refreshRecord(final T instance,final List<LabelAndField<JLabel,JComponent>> list) throws ContentException {
		for (LabelAndField<JLabel,JComponent> item : list) {
			SwingUtils.assignValueToComponent(item.field,item.fieldDesc.getFieldValue(instance));
		}
	}

	private static void collectActions(final Localizer localizer, final Class<?> clazz, final Object instance, final List<ActionButton> actions) throws IllegalArgumentException, NullPointerException, SyntaxException, LocalizationException, ContentException {
		if (clazz != null) {
			if (clazz.isAnnotationPresent(MultiAction.class) || clazz.isAnnotationPresent(Action.class)) {
				for (Action item : clazz.isAnnotationPresent(MultiAction.class) 
								 	? clazz.getAnnotation(MultiAction.class).value() 
								 	: new Action[] {clazz.getAnnotation(Action.class)}) {
					final JButton	button = new JButton(localizer.getValue(item.resource().value())) { private static final long serialVersionUID = 1L;
										@Override
										public JToolTip createToolTip() {
											return new SmartToolTip(localizer,this);
										}
									};
					
					button.setToolTipText(localizer.getValue(item.resource().tooltip()));
					button.setActionCommand(item.actionString());
					actions.add(new ActionButton(button,item.resource().value(),item.resource().tooltip(),item.simulateCheck()));
				}
			}
			collectActions(localizer,clazz.getSuperclass(),instance,actions);
		}
	}

	private static ItemDistributor buildSimpleItemDistributor(Object obj, final int columnCount) {
		if (columnCount <= 0 ) {
			throw new IllegalArgumentException("Column count ["+columnCount+"] must be positive");
		}
		else if (obj != null) {
			Class<?>	clazz = obj.getClass();
			int			totalCount = 0;
			
			while (clazz != null) {
				for (Field item : clazz.getDeclaredFields()) {
					if (item.isAnnotationPresent(LocaleResource.class)) {
						totalCount++;
					}
				}
				clazz = clazz.getSuperclass();
			}
			return new SimpleFieldDistributor(totalCount,columnCount);
		}
		else {
			return new SimpleFieldDistributor(0,0);
		}
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
	
	private static class SimpleFieldDistributor implements ItemDistributor {
		private final int	totalCount;
		private final int	columnCount;
		private final int	itemsInColumn;
		
		private SimpleFieldDistributor(final int totalCount, final int columnCount) {
			this.totalCount = totalCount;
			this.columnCount = columnCount;
			this.itemsInColumn = (totalCount + columnCount - 1) / columnCount; 
		}

		@Override
		public int[] getXY(final int sequential, final FieldDescriptor desc) {
			return new int[]{sequential % itemsInColumn,sequential/itemsInColumn,1,1};
		}

		@Override
		public String toString() {
			return "FieldDistributor [totalCount=" + totalCount + ", columnCount=" + columnCount + ", itemsInColumn=" + itemsInColumn + "]";
		}
	}
}
