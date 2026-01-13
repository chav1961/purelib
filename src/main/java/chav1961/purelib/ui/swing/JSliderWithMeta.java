package chav1961.purelib.ui.swing;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JSlider;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.inner.BooleanPropChangeListenerRepo;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JSliderWithMeta extends JSlider implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface, BooleanPropChangeListenerSource {
	private static final long serialVersionUID = 7181004176248094484L;
	private static final Class<?>[]		VALID_CLASSES = {int.class, Integer.class, short.class, Short.class, byte.class, Byte.class};
	private static final Pattern		EXTRACTOR = Pattern.compile("slider\\s*\\{(.*)\\}\\s*");
	private static final String			SLIDER_WIZARD = "slider";
	private static final String			SLIDER_WIZARD_MIN_VALUE = "minValue";
	private static final String			SLIDER_WIZARD_MAX_VALUE = "maxValue";
	private static final String			SLIDER_WIZARD_PAINT_TICKS = "paintTicks";
	private static final String			SLIDER_WIZARD_PAINT_LABELS = "paintLabels";
	private static final String			SLIDER_WIZARD_DEFAULT = SLIDER_WIZARD+"{"+SLIDER_WIZARD_MIN_VALUE+"=0;"+SLIDER_WIZARD_MAX_VALUE+"=100;"+SLIDER_WIZARD_PAINT_TICKS+"=false;"+SLIDER_WIZARD_PAINT_LABELS+"=false}";
	private static final Set<String>	AVAILABLE_SLIDER_OPTIONS = Set.of(SLIDER_WIZARD_MIN_VALUE, SLIDER_WIZARD_MAX_VALUE, SLIDER_WIZARD_PAINT_TICKS, SLIDER_WIZARD_PAINT_LABELS); 

	private final BooleanPropChangeListenerRepo	repo = new BooleanPropChangeListenerRepo();
	private final ContentNodeMetadata			metadata;
	private final Integer[]						currentValue = new Integer[1], newValue = new Integer[1];
	private boolean 							isInvalid = false;
	
	public JSliderWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor) {
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
			final FieldFormat	format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());
			
			if (format.getWizardType() != null && !format.getWizardType().isEmpty()) {
				prepareSlider(format.getWizardType());
			}
			else {
				prepareSlider(SLIDER_WIZARD_DEFAULT);
			}
			
			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (newValue != currentValue && newValue != null && !newValue.equals(currentValue)) {
							monitor.process(MonitorEvent.Saving,metadata,JSliderWithMeta.this);
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JSliderWithMeta.this);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JSliderWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JSliderWithMeta.this);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JSliderWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
			});
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JSliderWithMeta.this)) {
						assignValueToComponent(newValue);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					}
				} catch (ContentException exc) {
					SwingUtils.getNearestLogger(JSliderWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
				} finally {
					JSliderWithMeta.this.requestFocus();
				}
			}, SwingUtils.ACTION_ROLLBACK);
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JSliderWithMeta.this);
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
			if (format.isReadOnly(false)) {
				setEnabled(false);
			}
			
			fillLocalizedStrings();
		}
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
	public String getRawDataFromComponent() {
		return currentValue[0].toString();
	}

	@Override
	public Object getValueFromComponent() {
		return currentValue[0];
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return newValue[0] = getValue();
	}

	@Override
	public void assignValueToComponent(final Object value) throws ContentException {
		if ((value instanceof Number) && !(value instanceof Long)) {
			setValue(newValue[0] = ((Number)value).intValue());
		}
		else if (value instanceof String) {
			setValue(newValue[0] = Integer.valueOf(((String)value).trim()));
		}
		else {
			throw new IllegalArgumentException("Value to assign can;t be null and must have one of the types "+Arrays.toString(VALID_CLASSES));
		}
	}

	@Override
	public Class<?> getValueType() {
		return Integer.class;
	}

	@Override
	public String standardValidation(Object value) {
		return null;
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}

	@Override
	public void setInvalid(final boolean invalid) {
		isInvalid = invalid;
	}

	@Override
	public boolean isInvalid() {
		return isInvalid;
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

	private void prepareSlider(final String wizardDescriptor) {
		final Matcher	m = EXTRACTOR.matcher(wizardDescriptor);
		
		if (!m.find()) {
			throw new IllegalArgumentException("Wizard descriptor ["+wizardDescriptor+"] of field format is not supported for the given control"); 
		}
		else {
			final Properties	props = new Properties(); 
			
			try(final Reader	rdr = new StringReader(m.group(1).replace(';', '\n'))) {
				boolean			anyDrawingRequired = false;
				
				props.load(rdr);

				for(Entry<Object, Object> item : props.entrySet()) {
					if (!AVAILABLE_SLIDER_OPTIONS.contains(item.getKey().toString())) {
						throw new IllegalArgumentException("Unsupported wizard option ["+item.getKey().toString()+"]"); 
					}
				}
				
				setMinimum(SQLUtils.convert(int.class, props.getProperty(SLIDER_WIZARD_MIN_VALUE, ""+getMinimum())));
				setMaximum(SQLUtils.convert(int.class, props.getProperty(SLIDER_WIZARD_MAX_VALUE, ""+getMaximum())));
				setPaintTicks(anyDrawingRequired |= SQLUtils.convert(boolean.class, props.getProperty(SLIDER_WIZARD_PAINT_TICKS, ""+getPaintTicks())));
				setPaintLabels(anyDrawingRequired |= SQLUtils.convert(boolean.class, props.getProperty(SLIDER_WIZARD_PAINT_LABELS, ""+getPaintLabels())));
				if (anyDrawingRequired) {
					setMinorTickSpacing(1);
					setMajorTickSpacing(1);
				}
			} catch (IOException | ContentException e) {
				throw new IllegalArgumentException("Error reading wizard descriptor: "+e.getLocalizedMessage(), e); 
			}
		}
	}
	
	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue[0] = newValue[0];
			assignValueToComponent(newValue);
		} catch (ContentException exc) {
			SwingUtils.getNearestLogger(JSliderWithMeta.this).message(Severity.error, exc, exc.getLocalizedMessage());
		}					
	}
	
	
	private void fillLocalizedStrings() {
	}
}
