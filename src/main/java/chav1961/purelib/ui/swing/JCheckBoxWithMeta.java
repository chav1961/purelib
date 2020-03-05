package chav1961.purelib.ui.swing;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JCheckBoxWithMeta extends JCheckBox implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 			serialVersionUID = -3207016216489833670L;
	private final ContentNodeMetadata	metadata;
	private boolean						currentValue, invalid = false;
	private volatile boolean			loaded = false;
	
	public JCheckBoxWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor) throws LocalizationException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null"); 
		}
		else {
			this.metadata = metadata;
			
			final String	name = URIUtils.removeQueryFromURI(metadata.getApplicationPath()).toString(); 
			
			addComponentListener(new ComponentListener() {
				@Override 
				public void componentResized(ComponentEvent e) {
					if (!loaded) {
						callLoad(monitor);
						loaded = true;
					}
				}
				
				@Override 
				public void componentMoved(ComponentEvent e) {
					if (!loaded) {
						callLoad(monitor);
						loaded = true;
					}
				}
				
				@Override 
				public void componentHidden(ComponentEvent e) {
					if (!loaded) {
						callLoad(monitor);
						loaded = true;
					}
				}
				
				@Override
				public void componentShown(ComponentEvent e) {
					if (!loaded) {
						callLoad(monitor);
						loaded = true;
					}
				}				
			});
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
			throw new NullPointerException("Value to assign can't be null");
		}
		else if (value instanceof Boolean) {
			setSelected((Boolean)value);
		}
		else if (value instanceof String) {
			final String	message = standardValidation((String)value); 
			
			if (message == null) {
				setSelected(Boolean.valueOf((String)value));
			}
			else {
				throw new IllegalArgumentException("String ["+value+"] to assign contains invald value: "+message);
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
	public String standardValidation(final String value) {
		return "true".equals(value) || "false".equals(value) ? null : "Neither 'true' nor 'false' for checkbox content!";
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
		try{final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
			
			setToolTipText(localizer.getValue(getNodeMetadata().getLabelId()));
		} catch (IOException e) {
			throw new LocalizationException(e);
		}
	}
	
	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = isSelected();
		} catch (ContentException exc) {
		}					
	}
}