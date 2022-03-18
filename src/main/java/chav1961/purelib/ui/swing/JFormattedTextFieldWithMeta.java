package chav1961.purelib.ui.swing;

import java.awt.AWTEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.inner.BooleanPropChangeListenerRepo;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JFormattedTextFieldWithMeta extends JFormattedTextField implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface, BooleanPropChangeListenerSource {
	private static final long 	serialVersionUID = -7990739033479280548L;
	
	private static final Class<?>[]		VALID_CLASSES = {String.class};
	
	private final BooleanPropChangeListenerRepo	repo = new BooleanPropChangeListenerRepo();
	private final ContentNodeMetadata	metadata;
	private final MaskFormatter			formatter;
	private String						currentValue, newValue;
	private boolean						invalid = false;
	
	public JFormattedTextFieldWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor) throws LocalizationException, SyntaxException {
		super();
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null"); 
		}
		else if (!InternalUtils.checkClassTypes(metadata.getType(),VALID_CLASSES)) {
			throw new IllegalArgumentException("Invalid node type ["+metadata.getType().getCanonicalName()+"] for the given control. Only "+Arrays.toString(VALID_CLASSES)+" are available");
		}
		else if (metadata.getFormatAssociated() == null) {
			throw new IllegalArgumentException("Metadata doesn't contain field format!"); 
		}
		else {
			this.metadata = metadata;
			
			final String		name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final FieldFormat	format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());
			
			try{this.formatter = new MaskFormatter(format.getFormatMask());
				setFormatterFactory(new DefaultFormatterFactory(formatter));
			} catch (ParseException e) {
				throw new SyntaxException(0,0,"Illegal format mask ["+format.getFormatMask()+"]: "+e.getLocalizedMessage());
			}
			enableEvents(AWTEvent.FOCUS_EVENT_MASK|AWTEvent.COMPONENT_EVENT_MASK);
			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{
						SwingUtilities.invokeLater(()->{
							if (newValue != currentValue && newValue != null && !newValue.equals(currentValue)) {
								try{monitor.process(MonitorEvent.Saving,metadata,JFormattedTextFieldWithMeta.this);
								} catch (ContentException exc) {
									SwingUtils.getNearestLogger(JFormattedTextFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
								}
							}
						});
						monitor.process(MonitorEvent.FocusLost,metadata,JFormattedTextFieldWithMeta.this);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JFormattedTextFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JFormattedTextFieldWithMeta.this);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JFormattedTextFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
					SwingUtilities.invokeLater(()->{
						if (format.needSelectOnFocus()) {
							selectAll();
						}
					});
				}
			});
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JFormattedTextFieldWithMeta.this)) {
						assignValueToComponent(currentValue);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					}
				} catch (ContentException exc) {
					SwingUtils.getNearestLogger(JFormattedTextFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
				} finally {
					JFormattedTextFieldWithMeta.this.requestFocus();
				}
			}, SwingUtils.ACTION_ROLLBACK);
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{if (monitor.process(MonitorEvent.Validation,metadata,JFormattedTextFieldWithMeta.this)) {
							newValue = (String)getChangedValueFromComponent();
							getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(true);
							return true;
						}
						else {
							return false;
						}
					} catch (ContentException e) {
						return false;
					}
				}
			});

			if (InternalUtils.checkMandatory(metadata)) {
				InternalUtils.prepareMandatoryColor(this);
			}
			else {
				InternalUtils.prepareOptionalColor(this);
			}
			switch (format.getAlignment()) {
				case CenterAlignment: setAlignmentX(JTextField.CENTER_ALIGNMENT); break;
				case LeftAlignment	: setAlignmentX(JTextField.LEFT_ALIGNMENT); break;
				case RightAlignment	: setAlignmentX(JTextField.RIGHT_ALIGNMENT); break;
				default: break;
			}
			if (format.isReadOnly(false)) {
				setEditable(false);
			}
			if (format.getLength() != 0) {
				setColumns(format.getLength());
			}
			
			setName(name);
			InternalUtils.registerAdvancedTooptip(this);
			fillLocalizedStrings();
		}		
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}
	
	@Override
	public String getRawDataFromComponent() {
		return currentValue;
	}

	@Override
	public Object getValueFromComponent() {
		return currentValue;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return getValue();
	}

	@Override
	public void assignValueToComponent(final Object value) {
		if (value == null) {
			setValue("");
			newValue = null;
		}
		else {
			setValue(value.toString());
			newValue = value.toString();
		}
	}

	@Override
	public Class<?> getValueType() {
		return String.class;
	}

	@Override
	public String standardValidation(final Object val) {
		if (val == null) {
			if (InternalUtils.checkNullAvailable(getNodeMetadata())) {
				return null;
			}
			else {
				return InternalUtils.buildStandardValidationMessage(getNodeMetadata(), InternalUtils.VALIDATION_NULL_VALUE);
			}
		}
		else if (InternalUtils.checkMandatory(getNodeMetadata())) {
			if (val.toString().trim().isEmpty()) {
				return InternalUtils.buildStandardValidationMessage(getNodeMetadata(), InternalUtils.VALIDATION_MANDATORY);
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	@Override
	public void setInvalid(boolean invalid) {
		this.invalid = invalid; 
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}
	
	@Override
	public void addBooleanPropChangeListener(final BooleanPropChangeListener listener) {
		repo.addBooleanPropChangeListener(listener);
	}

	@Override
	public void removeBooleanPropChangeListener(final BooleanPropChangeListener listener) {
		repo.removeBooleanPropChangeListener(listener);
	}
	
	@Override
	public void setVisible(final boolean aFlag) {
		final boolean old = isVisible();
		
		super.setVisible(aFlag);
		if (repo != null && aFlag != old) {
			repo.fireBooleanPropChange(this, EventChangeType.VISIBILE, aFlag);
		}
	}
	
	@Override
	public void setEnabled(boolean b) {
		final boolean old = isEnabled();
		
		super.setEnabled(b);
		if (repo != null && b != old) {
			repo.fireBooleanPropChange(this, EventChangeType.ENABLED, b);
		}
	}
	
	@Override
	public void setEditable(boolean b) {
		final boolean old = isEditable();
		
		super.setEditable(b);
		if (repo != null && b != old) {
			repo.fireBooleanPropChange(this, EventChangeType.MODIFIABLE, b);
		}
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
		
		if (getNodeMetadata().getTooltipId() != null && !getNodeMetadata().getTooltipId().trim().isEmpty()) {
			setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
		}
	}
	
	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = newValue;
		} catch (ContentException exc) {
			SwingUtils.getNearestLogger(JFormattedTextFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
		}					
	}
}
