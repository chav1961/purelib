package chav1961.purelib.ui.swing;

import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;
import chav1961.purelib.ui.swing.useful.ComponentKeepedBorder;

public class JDateFieldWithMeta extends JFormattedTextField implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 	serialVersionUID = -7990739033479280548L;
	
	public static final String 			CHOOSER_NAME = "chooser";
	
	private static final Class<?>[]		VALID_CLASSES = {Date.class};
	private static final int			DEFAULT_COLUMNS = 10;
	
	private final ContentNodeMetadata	metadata;
	private final Localizer 			localizer;
	private final BasicArrowButton		callSelect = new BasicArrowButton(BasicArrowButton.SOUTH);
	private	Popup 						window;
	private Date						currentValue = new Date(0), newValue = currentValue;
	private boolean						invalid = false;
	
	public JDateFieldWithMeta(final ContentNodeMetadata metadata, final Localizer localizer, final JComponentMonitor monitor) throws LocalizationException, SyntaxException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null"); 
		}
		else if (!InternalUtils.checkClassTypes(metadata.getType(),VALID_CLASSES)) {
			throw new IllegalArgumentException("Invalid node type ["+metadata.getType().getCanonicalName()+"] for the given control. Only "+Arrays.toString(VALID_CLASSES)+" are available");
		}
		else {
			this.metadata = metadata;
			this.localizer = localizer;

			final String		name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final FieldFormat	format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());
			
			setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(prepareDateFormat(format,localizer.currentLocale().getLocale()))));
			
			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					closeDropDown();
					try{if (currentValue != null && !getValue().equals(currentValue) || currentValue == null) {
							monitor.process(MonitorEvent.Saving,metadata,JDateFieldWithMeta.this);
							currentValue = (Date) getValue();
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JDateFieldWithMeta.this);
					} catch (ContentException exc) {
					}
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					currentValue = (Date) getValue();
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JDateFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
					SwingUtilities.invokeLater(()->{
						if (format.needSelectOnFocus()) {
							selectAll();
						}
					});
				}
			});
			addActionListener((e)->{
				try{monitor.process(MonitorEvent.Action,metadata,JDateFieldWithMeta.this,e.getActionCommand());
				} catch (ContentException exc) {
				}
			});
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				closeDropDown();
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JDateFieldWithMeta.this)) {
						assignValueToComponent(currentValue);
					}
				} catch (ContentException exc) {
				} finally {
					JDateFieldWithMeta.this.requestFocus();
				}
			},"rollback-value");
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_DROPDOWN,(e)->{
				callSelect.doClick();
			},"show-dropdown");
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JDateFieldWithMeta.this);
					} catch (ContentException e) {
						return false;
					}
				}
			});

			if (format != null) {
				setBackground(format.isMandatory() ? PureLibSettings.defaultColorScheme().MANDATORY_BACKGROUND : PureLibSettings.defaultColorScheme().OPTIONAL_BACKGROUND);
				switch (format.getAlignment()) {
					case CenterAlignment: setAlignmentX(JTextField.CENTER_ALIGNMENT); break;
					case LeftAlignment	: setAlignmentX(JTextField.LEFT_ALIGNMENT); break;
					case RightAlignment	: setAlignmentX(JTextField.RIGHT_ALIGNMENT); break;
					default: break;
				}
				if (format.isReadOnly(false)) {
					setEditable(false);
					callSelect.setEnabled(false);
				}
				setColumns(format.getLength() == 0 ? DEFAULT_COLUMNS : format.getLength());
			}
			else {
				setBackground(PureLibSettings.defaultColorScheme().OPTIONAL_BACKGROUND);
				setAlignmentX(JTextField.RIGHT_ALIGNMENT);
				setColumns(DEFAULT_COLUMNS);
			}
			callSelect.addActionListener((e)->{selectDate();});
			new ComponentKeepedBorder(0,callSelect).install(this);
			
			setName(name);
			callSelect.setName(name+'/'+CHOOSER_NAME);
			fillLocalizedStrings();
			InternalUtils.registerAdvancedTooptip(this);
			setValue(this.currentValue);
		}		
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		final FieldFormat	format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());
		final Object		value = getValue();

		setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(prepareDateFormat(format,newLocale))));
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
	public void assignValueToComponent(final Object value) {
		if (value == null) {
			throw new NullPointerException("Value to assign can't be null");
		}
		else if (value instanceof String) {
			final String val = value.toString();
		
			if (val.isEmpty()) {
				throw new IllegalArgumentException("Empty value is not applicable for the date");
			}
			else {
				try{setValue(getFormatter().stringToValue(val.trim()));
				} catch (ParseException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}
		else {
			setValue(value);
		}
	}

	@Override
	public Class<?> getValueType() {
		return Date.class;
	}

	@Override
	public String standardValidation(final Object val) {
		closeDropDown();
		if (val == null) {
			if (InternalUtils.checkNullAvailable(getNodeMetadata())) {
				return null;
			}
			else {
				return InternalUtils.buildStandardValidationMessage(getNodeMetadata(), InternalUtils.VALIDATION_NULL_VALUE);
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
	
	private void fillLocalizedStrings() throws LocalizationException {
		if (getNodeMetadata().getTooltipId() != null && !getNodeMetadata().getTooltipId().trim().isEmpty()) {
			setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
		}
	}

	private void closeDropDown() {
		if (window != null) {
			window.hide();
			window = null;
		}
	}
	
	private void selectDate() {
		requestFocusInWindow();
		closeDropDown();
		
		try{final JComponentMonitor		monitor = new SimpleComponentMonitor<Date>(currentValue) {
												@Override
												public void change(final Date value) {
													super.change(value);
												}
										};
			final JDateSelectionDialog	dsd = new JDateSelectionDialog(getNodeMetadata(),localizer,monitor) {
											private static final long serialVersionUID = 1L;
											@Override
											protected void storeNewValue(final Object value) {
												super.storeNewValue(value);
												if (value instanceof Date) {
													newValue = (Date)value;
												}
												else if (value instanceof Calendar) {
													newValue = new Date(((Calendar)value).getTimeInMillis());
												}
												setValue(newValue);
											}
										};
			final Point					callSelectLocation = callSelect.getLocationOnScreen();
			
			window = PopupFactory.getSharedInstance().getPopup(this,dsd,callSelectLocation.x+callSelect.getWidth()-dsd.getPreferredSize().width,callSelectLocation.y+callSelect.getHeight());
			window.show();
			SwingUtilities.invokeLater(()->dsd.assignValueToComponent(getValueFromComponent()));
		} catch (LocalizationException  e) {
			closeDropDown();
			e.printStackTrace();
		}
	}

	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = newValue;
		} catch (ContentException exc) {
		}					
	}
	
	private static DateFormat prepareDateFormat(final FieldFormat format, final Locale locale) {
		if (format == null || format.getFormatMask() == null) {
			return DateFormat.getDateInstance(DateFormat.MEDIUM,locale);
		}
		else {
			return new SimpleDateFormat(format.getFormatMask(),locale);
		}
	}
}
