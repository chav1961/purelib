package chav1961.purelib.ui.swing;

import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;
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
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JNumericFieldWithMeta extends JFormattedTextField implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 			serialVersionUID = -7990739033479280548L;

	private static final Class<?>[]		VALID_CLASSES = {BigDecimal.class, Float.class, float.class, Double.class, double.class};
	
	private final ContentNodeMetadata	metadata;
	private boolean						invalid = false;
	private Object						currentValue, newValue;
	
	public JNumericFieldWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor) throws LocalizationException {
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
			
			final String					name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final Localizer					localizer = LocalizerFactory.getLocalizer(metadata.getLocalizerAssociated());
			final FieldFormat				format = metadata.getFormatAssociated();
			final InternationalFormatter	formatter = InternalUtils.prepareNumberFormatter(format,localizer.currentLocale().getLocale());
			final int						columns;
			
			if (metadata.getFormatAssociated().getFormatMask() != null) {
				columns = format.getLength();
			}
			else {
				final int 			len = format.getLength() == 0 ? 15 : format.getLength();
				columns = len + 1;
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
								try{monitor.process(MonitorEvent.Saving,metadata,JNumericFieldWithMeta.this);
								} catch (ContentException e1) {
									e1.printStackTrace();
								}
								prepareFieldColor(currentValue = getValue(), format);
							}
						});
						monitor.process(MonitorEvent.FocusLost,metadata,JNumericFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					currentValue = getValue();
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JNumericFieldWithMeta.this);
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
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JNumericFieldWithMeta.this)) {
						assignValueToComponent(currentValue);
					}
				} catch (ContentException exc) {
				} finally {
					JNumericFieldWithMeta.this.requestFocus();
				}
			},"rollback-value");
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{final boolean	validated = monitor.process(MonitorEvent.Validation,metadata,JNumericFieldWithMeta.this);
						final Object	value = getValue(); 

						return validated;
					} catch (ContentException e) {
						return false;
					}
				}
			});

			if (format.isMandatory()) {
				setBackground(PureLibSettings.defaultColorScheme().MANDATORY_BACKGROUND);
				setSelectionColor(PureLibSettings.defaultColorScheme().MANDATORY_SELECTION_BACKGROUND);
				setSelectedTextColor(PureLibSettings.defaultColorScheme().MANDATORY_SELECTION_FOREGROUND);
			}
			else {
				setBackground(PureLibSettings.defaultColorScheme().OPTIONAL_BACKGROUND);
				setSelectionColor(PureLibSettings.defaultColorScheme().OPTIONAL_SELECTION_BACKGROUND);
				setSelectedTextColor(PureLibSettings.defaultColorScheme().OPTIONAL_SELECTION_FOREGROUND);
			}
			switch (format.getAlignment()) {
				case CenterAlignment: setHorizontalAlignment(JTextField.CENTER); break;
				case LeftAlignment	: setHorizontalAlignment(JTextField.LEFT); break;
				case RightAlignment	: setHorizontalAlignment(JTextField.RIGHT); break;
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
		return getNodeMetadata().getType();
	}

	@Override
	public String standardValidation(final String value) {
		if (value == null || value.isEmpty()) {
			return "Value is mandatory and must be filled!";
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
	public void setInvalid(final boolean invalid) {
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
		if (value instanceof BigDecimal) {
			InternalUtils.setFieldColor(JNumericFieldWithMeta.this,format,((BigDecimal)value).signum());
		}
		else if (value instanceof Float) {
			InternalUtils.setFieldColor(JNumericFieldWithMeta.this,format,(int)Math.signum(((Float)value).floatValue()));
		}
		else if (value instanceof Double) {
			InternalUtils.setFieldColor(JNumericFieldWithMeta.this,format,(int)Math.signum(((Double)value).doubleValue()));
		}
		else if (value instanceof Long) {
			InternalUtils.setFieldColor(JNumericFieldWithMeta.this,format,(int)Math.signum(((Long)value).longValue()));
		}
	}
}
