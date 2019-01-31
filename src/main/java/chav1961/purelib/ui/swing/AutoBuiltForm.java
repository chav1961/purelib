package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.LabelAndField;
import chav1961.purelib.ui.interfacers.Action;
import chav1961.purelib.ui.interfacers.FormManager;
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
 * @since 0.0.2 last update 0.0.3
 */


public class AutoBuiltForm<T> extends JPanel implements LocaleChangeListener, AutoCloseable {
	private static final long 				serialVersionUID = 4920624779261769348L;
	private static final int				GAP_SIZE = 5; 
	
	private final Localizer					localizer, personalLocalizer;
	private final FormManager<Object,T>		formManager;
	private final ContentMetadataInterface	mdi;
	private final LightWeightListenerList<ActionListener>	listeners = new LightWeightListenerList<>(ActionListener.class);
	private boolean							closed = false, localizerPushed = false;

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
			final JPanel					buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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
					if (node.getApplicationPath() != null){ 
						if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_ACTION)) {
							final JButton		button = new JButton();
							
							button.setName(node.getUIPath().toString()+"/action");

							button.setActionCommand(node.getApplicationPath().toString());
							button.addActionListener((e)->{
								listeners.fireEvent((l)->{
									if (l instanceof ActionListener) {
										((ActionListener)l).actionPerformed(e);
									}
								});
							});
							buttonPanel.add(button);							
						}
						if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_FIELD)) {
							final JLabel		label = new JLabel();
							final JTextField	field = new JTextField();
							
							label.setName(node.getUIPath().toString()+"/label");
							childPanel.add(label,LabelledLayout.LABEL_AREA);
							field.setName(node.getUIPath().toString()+"/field");
							childPanel.add(field,LabelledLayout.CONTENT_AREA);
						}
					}
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
			final boolean[]	result = new boolean[]{false};
			
			mdi.walkDown((mode,applicationPath,uiPath,node)->{
				if (mode == NodeEnterMode.ENTER) {
					if (node.getApplicationPath() != null){ 
						if(actionCommand.equals(node.getApplicationPath().toString())) {
							final JButton		button = (JButton) SwingUtils.findComponentByName(this,node.getUIPath().toString());
							
							result[0] = true;
							button.doClick();
							return ContinueMode.STOP;
						}
					}
				}
				return ContinueMode.CONTINUE;
			}, mdi.getRoot().getUIPath());
			return result[0];
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
				if(node.getApplicationPath() != null) {
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_ACTION)) {
						final JButton		button = (JButton) SwingUtils.findComponentByName(this,node.getUIPath().toString()+"/action");
		
						try{button.setText(localizer.getValue(node.getLabelId()));
						button.setToolTipText(localizer.getValue(node.getTooltipId()));
						} catch (LocalizationException e) {
						}
					}
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_FIELD)) {
						final JLabel		label = (JLabel) SwingUtils.findComponentByName(this,node.getUIPath().toString()+"/label");
						final JTextField	field = (JTextField) SwingUtils.findComponentByName(this,node.getUIPath().toString()+"/field");
		
						try{label.setText(localizer.getValue(node.getLabelId()));
							field.setToolTipText(localizer.getValue(node.getTooltipId()));
						} catch (LocalizationException e) {
						}
					}
				}
			}
			return ContinueMode.CONTINUE;
		}, mdi.getRoot().getUIPath());
	}
}
