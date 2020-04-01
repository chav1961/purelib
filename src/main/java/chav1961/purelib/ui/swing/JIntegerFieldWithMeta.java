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
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JIntegerFieldWithMeta extends JFormattedTextField implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 	serialVersionUID = -7990739033479280548L;

	private static final Class<?>[]		VALID_CLASSES = {BigInteger.class, Byte.class, byte.class, Short.class, short.class, Integer.class, int.class, Long.class, long.class};
	
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
			final FieldFormat				format = metadata.getFormatAssociated();
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
								} catch (ContentException ex) {
								}
								prepareFieldColor(currentValue = getValue(), format);
							}
						});
						monitor.process(MonitorEvent.FocusLost,metadata,JIntegerFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					currentValue = getText();
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JIntegerFieldWithMeta.this);
					} catch (ContentException exc) {
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
					}
				} catch (ContentException exc) {
				} finally {
					JIntegerFieldWithMeta.this.requestFocus();
				}
			},"rollback-value");
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{final boolean	validated = monitor.process(MonitorEvent.Validation,metadata,JIntegerFieldWithMeta.this);
						final Object	value = getValue(); 

						if (value instanceof BigInteger) {
							InternalUtils.setFieldColor(JIntegerFieldWithMeta.this,format,((BigInteger)value).signum());
						}
						else if (value instanceof Byte) {
							InternalUtils.setFieldColor(JIntegerFieldWithMeta.this,format,(int)Math.signum(((Byte)value).longValue()));
						}
						else if (value instanceof Short) {
							InternalUtils.setFieldColor(JIntegerFieldWithMeta.this,format,(int)Math.signum(((Short)value).longValue()));
						}
						else if (value instanceof Integer) {
							InternalUtils.setFieldColor(JIntegerFieldWithMeta.this,format,(int)Math.signum(((Integer)value).longValue()));
						}
						else if (value instanceof Long) {
							InternalUtils.setFieldColor(JIntegerFieldWithMeta.this,format,(int)Math.signum(((Long)value).longValue()));
						}
						return validated;
					} catch (ContentException e) {
						return false;
					}
				}
			});

			setBackground(format.isMandatory() ? PureLibSettings.defaultColorScheme().MANDATORY_BACKGROUND : PureLibSettings.defaultColorScheme().OPTIONAL_BACKGROUND);
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
			fillLocalizedStrings();
		}		
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		final Object			value = getValue();
		
		setFormatterFactory(new DefaultFormatterFactory(InternalUtils.prepareNumberFormatter(getNodeMetadata().getFormatAssociated(),newLocale)));
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
		return getValue();
	}

	@Override
	public void assignValueToComponent(final Object value) throws ContentException {
		if (value == null) {
			throw new NullPointerException("Value to assign can't be null");
		}
		else {
			setValue(newValue = SQLUtils.convert(getValueType(),value));
			prepareFieldColor(newValue,metadata.getFormatAssociated());
		}
	}

	@Override
	public Class<?> getValueType() {
		return contentType;
	}

	@Override
	public String standardValidation(final String value) {
		if (value == null || value.isEmpty()) {
			return "Null or empty value is not valid for number";
		}
		else {
			try{getFormatter().stringToValue(value.toString());
				return null;
			} catch (ParseException exc) {
				return exc.getLocalizedMessage();
			}
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
	
	private void fillLocalizedStrings() throws LocalizationException {
		final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
		
		setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
	}
	
	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = newValue;
		} catch (ContentException exc) {
		}					
	}

	private void prepareFieldColor(final Object value, final FieldFormat format) {
		if (value instanceof BigInteger) {
			InternalUtils.setFieldColor(JIntegerFieldWithMeta.this,format,((BigInteger)value).signum());
		}
		else if (value instanceof Long) {
			InternalUtils.setFieldColor(JIntegerFieldWithMeta.this,format,(int)Math.signum(((Long)value).longValue()));
		}
	}
}
