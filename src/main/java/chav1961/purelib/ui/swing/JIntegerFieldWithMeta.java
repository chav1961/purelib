package chav1961.purelib.ui.swing;

import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.InternationalFormatter;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.inner.BooleanPropChangeListenerRepo;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JIntegerFieldWithMeta extends JFormattedTextField implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface, BooleanPropChangeListenerSource {
	private static final long 	serialVersionUID = -7990739033479280548L;

	private static final Class<?>[]		VALID_CLASSES = {BigInteger.class, Byte.class, byte.class, Short.class, short.class, Integer.class, int.class, Long.class, long.class};
	
	private final BooleanPropChangeListenerRepo	repo = new BooleanPropChangeListenerRepo();
	private final ContentNodeMetadata	metadata;
	private final Class<?>				contentType;
	private Object						currentValue, newValue;
	private boolean						invalid = false;
	
	public JIntegerFieldWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor) throws LocalizationException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null"); 
		}
		else if (!InternalUtils.checkClassTypes(metadata.getType(),VALID_CLASSES)) {
			throw new IllegalArgumentException("Invalid node type ["+metadata.getType().getCanonicalName()+"] for the given control. Only "+Arrays.toString(VALID_CLASSES)+" are available");
		}
		else {
			this.metadata = metadata;
			this.contentType = metadata.getType();
			
			final String					name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final Localizer					localizer = LocalizerFactory.getLocalizer(metadata.getLocalizerAssociated());
			final FieldFormat				format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());
			final InternationalFormatter	formatter = InternalUtils.prepareNumberFormatter(format,localizer.currentLocale().getLocale());
			final int						columns;
			
			if (format.getFormatMask() != null) {
				columns = format.getLength(); 
			}
			else {
				columns = format.getLength() == 0 ? 15 : format.getLength() + 1;
			}
			setFormatterFactory(new DefaultFormatterFactory(formatter));
			if (columns > 0) {
				setColumns(columns);
			}			
			
			setHorizontalAlignment(JTextField.RIGHT);
			setFont(new Font(getFont().getFontName(),Font.BOLD,getFont().getSize()));
			setAutoscrolls(false);
			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{SwingUtilities.invokeLater(()->{
							if (!getValue().equals(currentValue)) {
								try{monitor.process(MonitorEvent.Saving,metadata,JIntegerFieldWithMeta.this);
								} catch (ContentException exc) {
									SwingUtils.getNearestLogger(JIntegerFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
								}
								currentValue = getValue();
								InternalUtils.setFieldColor(JIntegerFieldWithMeta.this, format, getSignum(currentValue));
							}
						});
						monitor.process(MonitorEvent.FocusLost,metadata,JIntegerFieldWithMeta.this);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JIntegerFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					currentValue = getText();
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JIntegerFieldWithMeta.this);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JIntegerFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
					SwingUtilities.invokeLater(()->{
						if (getDocument().getLength() > 0) {
							setCaretPosition(getDocument().getLength());
						}
						if (format.needSelectOnFocus()) {
							selectAll();
						}
					});
				}
			});
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JIntegerFieldWithMeta.this)) {
						assignValueToComponent(currentValue);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					}
				} catch (ContentException exc) {
					SwingUtils.getNearestLogger(JIntegerFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
				} finally {
					JIntegerFieldWithMeta.this.requestFocus();
				}
			}, SwingUtils.ACTION_ROLLBACK);
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{final boolean	validated = monitor.process(MonitorEvent.Validation, metadata, JIntegerFieldWithMeta.this);
						
						if (validated) {
							final Object	value = getValue2Validate(getText());
							final int		signum = getSignum(value);
							
							InternalUtils.setFieldColor(JIntegerFieldWithMeta.this, format, signum);
							getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(true);
						}
						return validated;
					} catch (ContentException e) {
						return false;
					}
				}
			});

			if (InternalUtils.isContentMandatory(metadata)) {
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
		final FieldFormat		format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());
		final Object			value = getValue();
		
		setFormatterFactory(new DefaultFormatterFactory(InternalUtils.prepareNumberFormatter(format,newLocale)));
		setValue(value);
		fillLocalizedStrings();
	}
	
	@Override
	public String getRawDataFromComponent() {
		return currentValue == null ? null : currentValue.toString();
	}

	@Override
	public Object getValueFromComponent() {
		return currentValue;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return getValue2Validate(getText());
	}

	@Override
	public void assignValueToComponent(final Object value) throws ContentException {
		if (value == null) {
			throw new NullPointerException("Value to assign can't be null");
		}
		else {
			final FieldFormat	format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());
			
			setValue(newValue = SQLUtils.convert(getValueType(),value));
			InternalUtils.setFieldColor(this, format, getSignum(newValue));
		}
	}

	@Override
	public Class<?> getValueType() {
		return contentType;
	}

	@Override
	public String standardValidation(final Object val) {
		if (SwingUtils.inAllowedClasses(val, VALID_CLASSES)) {
			final Object	number;
			
			if (val instanceof String) {
				final String	value = val.toString();
				
				if (Utils.checkEmptyOrNullString(value)) {
					return InternalUtils.buildStandardValidationMessage(getNodeMetadata(), InternalUtils.VALIDATION_MANDATORY);			
				}
				else {
					try{
						number = getValue2Validate(value);
					} catch (SyntaxException e) {
						return InternalUtils.buildStandardValidationMessage(getNodeMetadata(), InternalUtils.VALIDATION_ILLEGAL_VALUE);			
					}
				}
			}
			else {
				number = val;
			}

			final int	signum = getSignum(number);
			
			if (metadata.getFormatAssociated() != null && metadata.getFormatAssociated().hasSignRestriction()) {
				if (!metadata.getFormatAssociated().isHighlighted(signum)) {
					return InternalUtils.buildStandardValidationMessage(getNodeMetadata(), InternalUtils.VALIDATION_ILLEGAL_VALUE_SIGN, number);			
				}
				else {
					return null;
				}
			}
			else {
				return null;
			}
		}
		else {
			return InternalUtils.buildStandardValidationMessage(getNodeMetadata(), InternalUtils.VALIDATION_ILLEGAL_TYPE);			
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
	public boolean isEnabled() {
		if (getParent() != null) {
			return super.isEnabled() && getParent().isEnabled();
		}
		else {
			return super.isEnabled();
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
			SwingUtils.getNearestLogger(JIntegerFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
		}					
	}

	private int getSignum(final Object value) {
		if (value instanceof BigInteger) {
			return ((BigInteger)value).signum();
		}
		else if (value instanceof Byte) {
			return (int)Math.signum(((Byte)value).longValue());
		}
		else if (value instanceof Short) {
			return (int)Math.signum(((Short)value).longValue());
		}
		else if (value instanceof Integer) {
			return (int)Math.signum(((Integer)value).longValue());
		}
		else if (value instanceof Long) {
			return (int)Math.signum(((Long)value).longValue());
		}
		else {
			throw new UnsupportedOperationException("Value class ["+value.getClass().getCanonicalName()+"] is not supported yet");
		}
	}
	
	private Object getValue2Validate(final String value) throws SyntaxException {
		try{
			return getFormatter().stringToValue(value);
		} catch (ParseException exc) {
			throw new SyntaxException(0, exc.getErrorOffset(), exc.getLocalizedMessage());
		}
	}
}
