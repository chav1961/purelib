package chav1961.purelib.ui.swing;

import java.awt.event.ActionEvent;
import java.util.Locale;

import javax.swing.AbstractAction;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.UIItemState;

public class ContentNodeMetadataAction extends AbstractAction implements NodeMetadataOwner, LocaleChangeListener {
	private static final long serialVersionUID = -2063052938210397591L;

	private final ContentNodeMetadata	metadata;
	private final UIItemState 			state;
	
	public ContentNodeMetadataAction(final ContentNodeMetadata metadata, final UIItemState state) {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (state == null) {
			throw new NullPointerException("IUI item state can't be null");
		}
		else {
			this.metadata = metadata;
			this.state = state;
		}
	}
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		// TODO Auto-generated method stub
		
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
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		super.firePropertyChange(propertyName, oldValue, newValue);
    }
	
	private void fillLocalizedStrings() {
	}
}
