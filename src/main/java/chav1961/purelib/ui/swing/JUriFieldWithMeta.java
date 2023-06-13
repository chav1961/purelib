package chav1961.purelib.ui.swing;


import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.net.URI;
import java.text.Format;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.InternationalFormatter;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
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
import chav1961.purelib.ui.inner.InternalConstants;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.inner.BooleanPropChangeListenerRepo;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;
import chav1961.purelib.ui.swing.useful.ComponentKeepedBorder;

public class JUriFieldWithMeta extends JFormattedTextField implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface, BooleanPropChangeListenerSource {
	private static final long serialVersionUID = -2602314083682177026L;
	private static final Class<?>[]		VALID_CLASSES = {String.class, URI.class};
	public static final String 			GOTO_NAME = "goto";

	private final BooleanPropChangeListenerRepo	repo = new BooleanPropChangeListenerRepo();
	private final JButton				gotoButton = new JButton(InternalConstants.ICON_GOTO_LINK);
	private final ContentNodeMetadata	metadata;
	private final JComponentMonitor		monitor;
	private final ComponentKeepedBorder	border = new ComponentKeepedBorder(0, gotoButton); 
	private boolean						invalid = false;
	private Object						currentValue, newValue;
	
	public JUriFieldWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor) throws LocalizationException, SyntaxException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (monitor == null) {
			throw new NullPointerException("Component monitor can't be null"); 
		}
		else if (!InternalUtils.checkClassTypes(metadata.getType(),VALID_CLASSES)) {
			throw new IllegalArgumentException("Invalid node type ["+metadata.getType().getCanonicalName()+"] for the given control. Only "+Arrays.toString(VALID_CLASSES)+" are available");
		}
		else {
			this.metadata = metadata;
			this.monitor = monitor;
			
			setFormatter(new AbstractFormatter() {
				private static final long serialVersionUID = -5667040127793507258L;

				@Override
				public Object stringToValue(final String text) throws ParseException {
					if (Utils.checkEmptyOrNullString(text)) {
						throw new ParseException("", 0);
					}
					else {
						try {
							return URI.create(text);
						} catch (IllegalArgumentException exc) {
							throw new ParseException(text, 0); 
						}
					}
				}

				@Override
				public String valueToString(final Object value) throws ParseException {
					return value != null ? value.toString() : "";
				}
			});
			
			final String					name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final Localizer					localizer = LocalizerFactory.getLocalizer(metadata.getLocalizerAssociated());
			final FieldFormat				format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());
			final InternationalFormatter	formatter = InternalUtils.prepareNumberFormatter(format, localizer.currentLocale().getLocale());
			final int						columns;
			
			if (format.getFormatMask() != null) {
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
							if (currentValue != null && !currentValue.equals(getValue()) || currentValue == null) {
								try{monitor.process(MonitorEvent.Saving,metadata,JUriFieldWithMeta.this);
								} catch (ContentException exc) {
									SwingUtils.getNearestLogger(JUriFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
								}
								currentValue = getValue();
							}
						});
						monitor.process(MonitorEvent.FocusLost,metadata,JUriFieldWithMeta.this);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JUriFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					currentValue = getValue();
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JUriFieldWithMeta.this);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JUriFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
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
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JUriFieldWithMeta.this)) {
						assignValueToComponent(currentValue);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					}
				} catch (ContentException exc) {
					SwingUtils.getNearestLogger(JUriFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
				} finally {
					JUriFieldWithMeta.this.requestFocus();
				}
			}, SwingUtils.ACTION_ROLLBACK);
			if (!Utils.checkEmptyOrNullString(metadata.getHelpId())) {
				SwingUtils.assignActionKey(this, WHEN_FOCUSED, SwingUtils.KS_HELP, (e)->{
					try {
						SwingUtils.showCreoleHelpWindow(JUriFieldWithMeta.this, LocalizerFactory.getLocalizer(metadata.getLocalizerAssociated()), metadata.getHelpId());
					} catch (IOException exc) {
						SwingUtils.getNearestLogger(JUriFieldWithMeta.this).message(Severity.error, exc, exc.getLocalizedMessage());
					}
				},SwingUtils.ACTION_HELP);
			}
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{final boolean	validated = monitor.process(MonitorEvent.Validation, metadata, JUriFieldWithMeta.this);

						if (validated) {
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
				case CenterAlignment: setHorizontalAlignment(JTextField.CENTER); break;
				case LeftAlignment	: setHorizontalAlignment(JTextField.LEFT); break;
				case RightAlignment	: setHorizontalAlignment(JTextField.RIGHT); break;
				default: break;
			}
			if (format.isReadOnly(false)) {
				setEditable(false);
			}
			
			gotoButton.setName(name+'/'+GOTO_NAME);
			gotoButton.setFocusable(false);
			gotoButton.addActionListener((e)->gotoLink(getText()));
			gotoButton.setEnabled(Desktop.isDesktopSupported());
			border.install(this);
			setName(name);
			InternalUtils.registerAdvancedTooptip(this);
			fillLocalizedStrings();
		}
	}

	@Override
	public String standardValidation(final Object val) {
		if (val instanceof String) {
			try{URI.create(val.toString());
				return null;
			} catch (IllegalArgumentException exc) {
				return InternalUtils.buildStandardValidationMessage(getNodeMetadata(), InternalUtils.VALIDATION_ILLEGAL_VALUE, exc.getLocalizedMessage());
			}
		}
		else if (val instanceof URI) {
			return null;
		}
		else {
			return InternalUtils.buildStandardValidationMessage(getNodeMetadata(), InternalUtils.VALIDATION_ILLEGAL_TYPE, URI.class.getCanonicalName());
		}
	}
	
	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
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
			setText(value.toString());
			try{
				newValue = URI.create(value.toString());
			} catch (IllegalArgumentException exc) {
				throw new ContentException(exc.getLocalizedMessage(), exc); 
			}
		}
	}

	@Override
	public Class<?> getValueType() {
		return URI.class;
	}

	@Override
	public void setInvalid(final boolean invalid) {
		this.invalid = invalid;
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	protected void gotoLink(final String uri) {
		try{final URI	ref = URI.create(uri.trim());
		
			if (ref.isAbsolute() && monitor.process(MonitorEvent.Validation, getNodeMetadata(), this)) {
				Desktop.getDesktop().browse(ref);
			}
		} catch (IllegalArgumentException | ContentException | IOException e) {
			SwingUtils.getNearestLogger(this).message(Severity.error, e.getLocalizedMessage());
		}
	}

	private void fillLocalizedStrings() {
	}

	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = newValue;
		} catch (ContentException exc) {
			SwingUtils.getNearestLogger(JUriFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
		}					
	}

	private Object getValue2Validate(final String value) throws SyntaxException {
		try{if (!Utils.checkEmptyOrNullString(value)) {
				return URI.create(value);
			}
			else {
				return null;
			}
		} catch (IllegalArgumentException exc) {
			throw new SyntaxException(0, 0, exc.getLocalizedMessage());
		}
	}
}
