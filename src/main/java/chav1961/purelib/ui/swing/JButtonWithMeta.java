package chav1961.purelib.ui.swing;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.MalformedURLException;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.KeyStroke;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.inner.InternalConstants;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JButtonWithMeta extends JButton implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 			serialVersionUID = -3207016216489833670L;
	private final ContentNodeMetadata	metadata;
	private final Localizer				localizer;
	
	public JButtonWithMeta(final ContentNodeMetadata metadata, final Localizer localizer, final JComponentMonitor monitor) throws LocalizationException, ContentException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null"); 
		}
		else {
			this.metadata = metadata;
			this.localizer = localizer;
			
			final String	name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString(); 
			
			if (metadata.getIcon() != null) {
				try{setIcon(new ImageIcon(metadata.getIcon().toURL()));
				} catch (MalformedURLException e) {
					throw new ContentException(e.getLocalizedMessage()); 
				}
			}
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{monitor.process(MonitorEvent.FocusLost,metadata,JButtonWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					try{monitor.process(MonitorEvent.FocusGained,metadata,JButtonWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
			});
			addActionListener((e)->{
				try{monitor.process(MonitorEvent.Action, metadata, JButtonWithMeta.this, JButtonWithMeta.this, e.getActionCommand());
				} catch (ContentException exc) {
				}					
			});
			
			setName(name);
			setActionCommand(URIUtils.removeQueryFromURI(metadata.getApplicationPath()).toString());
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
		return null;
	}

	@Override
	public Object getValueFromComponent() {
		return null;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return isSelected();
	}

	@Override
	public void assignValueToComponent(final Object value) {
	}

	@Override
	public Class<?> getValueType() {
		return Boolean.class;
	}

	@Override
	public String standardValidation(final Object value) {
		return null;
	}

	@Override
	public void setInvalid(boolean invalid) {
	}

	@Override
	public boolean isInvalid() {
		return false;
	}
	
	public void markOK(final Boolean value) {
		if (value == null) {
			setIcon(null);
		}
		else if (value) {
			setIcon(InternalConstants.ICON_SUCCESS);
		}
		else {
			setIcon(InternalConstants.ICON_FAIL);
		}
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		if (getIcon() == null) {
			setText(localizer.getValue(getNodeMetadata().getLabelId()));
		}
		String	keyPrefix = "";

		if (metadata.getOwner() != null) {
			for (ContentNodeMetadata item : metadata.getOwner().byApplicationPath(metadata.getApplicationPath())) {
				if (item.getRelativeUIPath().toString().startsWith("./keyset.key")) {
					keyPrefix = item.getLabelId()+": ";
					break;
				}
			}
		}
		setToolTipText(keyPrefix+localizer.getValue(getNodeMetadata().getLabelId()));
	}
}