package chav1961.purelib.ui.swing;

import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JLabel;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.interfaces.ValueChangeAdapter;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class ContentNodeMetadataPanel<T> implements NodeMetadataOwner, LocaleChangeListener, ValueChangeAdapter<T> {
	
	public ContentNodeMetadataPanel(final Localizer parent, final ContentNodeMetadata metadata) {
		
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addValueChangeListener(final ValueChangeListener<T> listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeValueChangeListener(final ValueChangeListener<T> listener) {
		// TODO Auto-generated method stub
		
	}
	
	public T getValue() {
		return null;
	}
	
	public void setValue(final T value) {
		
	}
	
	public JLabel getLabel() {
		return null;
	}
	
	public JComponent getContent() {
		return null;
	}
}
