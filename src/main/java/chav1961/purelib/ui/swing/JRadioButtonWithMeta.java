package chav1961.purelib.ui.swing;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JRadioButton;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.inner.BooleanPropChangeListenerRepo;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JRadioButtonWithMeta extends JRadioButton implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface, BooleanPropChangeListenerSource {
	private static final long 			serialVersionUID = -3207016216489833670L;
	private static final Class<?>[]		VALID_CLASSES = {Boolean.class,boolean.class};
	
	private final BooleanPropChangeListenerRepo	repo = new BooleanPropChangeListenerRepo();
	private final ContentNodeMetadata	metadata;
	private final Localizer				localizer;
	private boolean						currentValue, invalid = false;
	
	public JRadioButtonWithMeta(final ContentNodeMetadata metadata, final Localizer localizer, final JComponentMonitor monitor) throws LocalizationException {
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
							if (monitor.process(MonitorEvent.Saving,metadata,JRadioButtonWithMeta.this)) {
								currentValue = isSelected();
							}
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JRadioButtonWithMeta.this);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JRadioButtonWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					currentValue = isSelected();
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JRadioButtonWithMeta.this);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JRadioButtonWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
			});
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JRadioButtonWithMeta.this)) {
						assignValueToComponent(currentValue);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					}
				} catch (ContentException exc) {
					SwingUtils.getNearestLogger(JRadioButtonWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
				}
			}, SwingUtils.ACTION_ROLLBACK);
			addActionListener((e)->{
				try{
					getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(true);
					if (getInputVerifier().verify(JRadioButtonWithMeta.this)) {
						monitor.process(MonitorEvent.Saving,metadata,JRadioButtonWithMeta.this);
					}
				} catch (ContentException exc) {
				}
			});
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JRadioButtonWithMeta.this);
					} catch (ContentException e) {
						return false;
					}
				}
			});
			
//			if (InternalUtils.checkMandatory(metadata)) {
//				InternalUtils.prepareMandatoryColor(this);
//			}
//			else {
//				InternalUtils.prepareOptionalColor(this);
//			}
			if (metadata.getFormatAssociated() != null) {
				if (metadata.getFormatAssociated().isReadOnly(false)) {
					setEnabled(false);
				}
			}
			
			setName(name);
			InternalUtils.registerAdvancedTooptip(this);
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
	public String standardValidation(final Object value) {
		if (value == null) {
			return "Null value for checkbox content";
		}
		else if (SwingUtils.inAllowedClasses(value,VALID_CLASSES)) {
			return null;
		}
		else {
			final String	str = value.toString();
			
			return "true".equals(str) || "false".equals(str) ? null : "Neither 'true' nor 'false' for radio button content!";
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
	public void setSelected(boolean b) {
		final boolean old = isSelected();
		
		super.setSelected(b);
		if (repo != null && b != old) {
			repo.fireBooleanPropChange(this, EventChangeType.SELECTED, b);
		}
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
			SwingUtils.getNearestLogger(JRadioButtonWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
		}					
	}
}