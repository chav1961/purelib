package chav1961.purelib.ui.swing;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JCheckBoxWithMeta extends JCheckBox implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 			serialVersionUID = -3207016216489833670L;
	private static final Class<?>[]		VALID_CLASSES = {Boolean.class,boolean.class};
	
	private final ContentNodeMetadata	metadata;
	private final Localizer				localizer;
	private boolean						currentValue, invalid = false;
	
	public JCheckBoxWithMeta(final ContentNodeMetadata metadata, final Localizer localizer, final JComponentMonitor monitor) throws LocalizationException {
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
			
			final String	name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString(); 

			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (currentValue != isSelected()) {
							if (monitor.process(MonitorEvent.Saving,metadata,JCheckBoxWithMeta.this)) {
								currentValue = isSelected();
							}
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JCheckBoxWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					currentValue = isSelected();
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JCheckBoxWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
			});
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JCheckBoxWithMeta.this)) {
					assignValueToComponent(currentValue);
				}
				} catch (ContentException exc) {
				}
			},"rollback-value");
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JCheckBoxWithMeta.this);
					} catch (ContentException e) {
						return false;
					}
				}
			});
			
			if (metadata.getFormatAssociated() != null) {
				if (metadata.getFormatAssociated().isReadOnly(false)) {
					setEnabled(false);
				}
			}
			
			setName(name);
			fillLocalizedStrings();
		}
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
	public String getRawDataFromComponent() {
		return Boolean.valueOf(isSelected()).toString();
	}

	@Override
	public Object getValueFromComponent() {
		return currentValue;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return isSelected();
	}

	@Override
	public void assignValueToComponent(final Object value) {
		if (value == null) {
			if (InternalUtils.checkNullAvailable(getNodeMetadata())) {
				setSelected(false);
			}
			else {
				throw new NullPointerException("Value to assign can't be null");
			}
		}
		else if (value instanceof Boolean) {
			setSelected((Boolean)value);
		}
		else if (value instanceof String) {
			final String	str = value.toString();
			
			if ("true".equals(str) || "false".equals(str)) {
				setSelected(Boolean.valueOf((String)value));
			}
			else {
				throw new IllegalArgumentException("String ["+value+"] to assign contains invald value");
			}
		}
		else {
			throw new IllegalArgumentException("Value to assign must be Boolean, not ["+value.getClass().getCanonicalName()+"]");
		}
	}

	@Override
	public Class<?> getValueType() {
		return Boolean.class;
	}

	@Override
	public String standardValidation(final Object value) {
		if (value == null) {
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
	
	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = isSelected();
		} catch (ContentException exc) {
		}					
	}
}